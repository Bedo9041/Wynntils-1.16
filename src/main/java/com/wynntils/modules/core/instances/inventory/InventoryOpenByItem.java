/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances.inventory;

import com.wynntils.ModCore;
import com.wynntils.modules.core.interfaces.IInventoryOpenAction;
import com.wynntils.modules.core.managers.PacketQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.EnumHand;

public class InventoryOpenByItem implements IInventoryOpenAction {

    private static final CPlayerTryUseItemPacket rightClick = new CPlayerTryUseItemPacket(EnumHand.MAIN_HAND);
    public static final IPacket<?> ignoredPacket = rightClick;

    int inputSlot;

    public InventoryOpenByItem(int inputSlot) {
        this.inputSlot = inputSlot;
    }

    @Override
    public void onOpen(FakeInventory inv, Runnable onDrop) {
        Minecraft mc = ModCore.mc();

        PacketQueue.queueComplexPacket(rightClick, SOpenWindowPacket.class).setSender((conn, pack) -> {
            if (mc.player.inventory.selected != inputSlot) {
                conn.send(new CHeldItemChangePacket(inputSlot));
            }

            conn.send(pack);
            if (mc.player.inventory.selected != inputSlot) {
                conn.send(new CHeldItemChangePacket(mc.player.inventory.selected));
            }
        }).onDrop(onDrop);
    }

}
