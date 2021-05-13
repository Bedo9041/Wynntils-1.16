/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.packet;

import com.wynntils.core.utils.Utils;
import com.wynntils.modules.core.managers.PingManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class PacketResponse {

    IPacket input;
    Class responseType;

    Function<IPacket, Boolean> verification = null;
    Runnable onDrop = null;
    BiConsumer<ClientPlayNetHandler, IPacket> sender = null;
    boolean skipping = false;

    long lastSent = -1;
    int tries = 0;
    int maxTries = 3;

    public PacketResponse(IPacket input, Class responseType) {
        this.input = input;
        this.responseType = responseType;
    }

    public PacketResponse(IPacket input) {
        this.input = input;

        this.responseType = null;
    }

    public IPacket getInput() {
        return input;
    }

    public Class getResponseType() {
        return responseType;
    }

    public PacketResponse setVerification(Function<IPacket, Boolean> verification) {
        this.verification = verification;
        return this;
    }

    public boolean shouldSend() {
        return lastSent == -1 || System.currentTimeMillis() - lastSent > PingManager.getLastPing() + 150;
    }

    // TODO make this verification faster cuz at the current state it's slowing the packet a lot
    public boolean isResponseValid(IPacket packetType) {
        if (skipping) {
            return true;
        }
        if (responseType == null || tries >= maxTries) {
            if (this.onDrop != null) this.onDrop.run();
            return true;  // this avoids packet spamming
        }
        if (!packetType.getClass().isAssignableFrom(responseType)) return false;

        return verification == null || verification.apply(packetType);
    }

    public void send() {
        if (skipping || !shouldSend()) return;

        Utils.runAsync(() -> {
            ClientPlayNetHandler conn = Minecraft.getInstance().getConnection();
            if (this.sender != null) {
                this.sender.accept(conn, input);
            } else {
                conn.send(input);
            }
            lastSent = System.currentTimeMillis();
            tries++;
        });
    }

    /**
     * Called when the packet is dropped because the verification hasn't passed
     */
    public PacketResponse onDrop(Runnable onDrop) {
        this.onDrop = onDrop;
        return this;
    }

    /**
     * Call to not send this packet. Does not call onDrop.
     */
    public void skip() {
        skipping = true;
    }

    /**
     * Called to send packet with the current connection and the packet that was queued.
     * Default implementation is `(conn, pack) -> conn.send(pack)`.
     *
     * Can be used to change the packet sent depending on the outcome of previous packets.
     */
    public PacketResponse setSender(BiConsumer<ClientPlayNetHandler, IPacket> sender) {
        this.sender = sender;
        return this;
    }

    /**
     * Set the maximum number of times to try to send this packet
     */
    public PacketResponse withMaxTries(int maxTries) {
        this.maxTries = maxTries;
        return this;
    }

}
