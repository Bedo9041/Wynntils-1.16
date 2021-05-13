/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.modules.utilities.managers.ServerListManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ServerUptimeOverlay implements Listener {

    @SubscribeEvent
    public void onChest(GuiOverlapEvent.ChestOverlap.DrawScreen.Post e) {
        if (!Reference.onLobby) return;
        if (e.getGui().getSlotUnderMouse() == null || e.getGui().getSlotUnderMouse().getItem().isEmpty()) return;

        ItemStack stack = e.getGui().getSlotUnderMouse().getItem();
        if (!ItemUtils.getStringLore(stack).contains("Click to join") || stack.getItem() == Items.CLOCK) return;
        CompoundNBT nbt = stack.getTag();
        if (nbt.contains("wynntils")) return;

        String world = "WC" + stack.getCount();

        List<String> newLore = ItemUtils.getLore(stack);
        newLore.add(TextFormatting.DARK_GREEN + "Uptime: " + TextFormatting.GREEN + ServerListManager.getUptime(world));

        CompoundNBT compound = nbt.getCompound("display");
        ListNBT list = new ListNBT();

        newLore.forEach(c -> list.add(StringNBT.valueOf(c)));

        compound.put("Lore", list);
        nbt.putBoolean("wynntils", true);
    }

}
