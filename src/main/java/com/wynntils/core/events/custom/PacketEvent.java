/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.events.custom;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraftforge.eventbus.api.GenericEvent;

/**
 * Triggered when a packet is sent to or from the client
 * is cancellable (you can avoid it to reach the client processor or the server)
 */
public class PacketEvent<T extends IPacket<?>> extends GenericEvent<T> {

    T packet;
    ClientPlayNetHandler playClient;
    ChannelHandler handler;
    ChannelHandlerContext ctx;

    public PacketEvent(T packet, ClientPlayNetHandler playClient, ChannelHandler handler, ChannelHandlerContext ctx) {
        super((Class<T>) packet.getClass());
        this.packet = packet;
        this.playClient = playClient;
        this.handler = handler;
        this.ctx = ctx;
    }

    public T getPacket() {
        return packet;
    }

    public ClientPlayNetHandler getPlayClient() {
        return playClient;
    }

    public ChannelHandler getHandler() {
        return handler;
    }

    public ChannelHandlerContext getContext() {
        return ctx;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    /**
     * Triggered when a packet is being sent by the client to the server.
     * Cancelling means it won't be sent to the server.
     */
    public static class Outgoing<T extends IPacket<?>> extends PacketEvent<T> {

        public Outgoing(T packet, ClientPlayNetHandler playClient, ChannelOutboundHandler handler, ChannelHandlerContext ctx) {
            super(packet, playClient, handler, ctx);
        }

        /**
         * Sends a packet immediately, before the packet from this event.
         * Will fire another PacketEvent.Outgoing event
         *
         * @param packet The packet to send
         */
        public void sendImmediately(IPacket<?> packet) {
            try {
                ((ChannelOutboundHandler) handler).write(ctx, packet, ctx.newPromise());
            } catch (Exception e) {
                // Shouldn't actually throw
            }
        }

        /**
         * Change the packet being sent to a different one
         *
         * @param to The packet to send to the server instead
         */
        public void transform(IPacket<?> to) {
            setCanceled(true);
            sendImmediately(to);
        }

        @Override
        public ChannelOutboundHandler getHandler() {
            return (ChannelOutboundHandler) handler;
        }

    }

    /**
     * Triggered when the client receives a packet from the server.
     *
     * Cancelling means that it won't be processed by the client (as if it wasn't sent at all)
     */
    public static class Incoming<T extends IPacket<?>> extends PacketEvent<T> {

        public Incoming(T packet, ClientPlayNetHandler playClient, ChannelInboundHandler adapter, ChannelHandlerContext ctx) {
            super(packet, playClient, adapter, ctx);
        }

        /**
         * Emulates an incoming packet as if it was sent before the packet from this event.
         * Will fire another IPacket.Incoming event
         *
         * @param packet The packet to emulate
         */
        public void emulateRead(IPacket<?> packet) {
            try {
                ((ChannelInboundHandler) handler).channelRead(ctx, packet);
            } catch (Exception e) {
                // Shouldn't actually throw
            }
        }

        /**
         * Change the packet being read to a different one
         *
         * @param to The packet to be handled by Minecraft instead
         */
        public void transform(IPacket<?> to) {
            setCanceled(true);
            emulateRead(to);
        }

        @Override
        public ChannelInboundHandler getHandler() {
            return (ChannelInboundHandler) handler;
        }

    }

}
