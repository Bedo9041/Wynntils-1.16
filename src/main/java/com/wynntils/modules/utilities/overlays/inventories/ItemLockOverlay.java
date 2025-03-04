/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemLockOverlay implements Listener {

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onInventoryGui(GuiOverlapEvent.InventoryOverlap.HoveredToolTip.Pre e) {
        if (!Reference.onWorld) return;

        for (Slot s : e.getGui().inventorySlots.inventorySlots) {
            if (s.slotNumber <= 4) continue;

            renderItemLock(s, e.getGui().getGuiLeft(), e.getGui().getGuiTop());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChestGui(GuiOverlapEvent.ChestOverlap.HoveredToolTip.Pre e) {
        if (!Reference.onWorld) return;

        for (Slot s : e.getGui().inventorySlots.inventorySlots) {
            if (s.slotNumber < e.getGui().getLowerInv().getContainerSize()) continue;

            renderItemLock(s, e.getGui().getGuiLeft(), e.getGui().getGuiTop());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onHorseGui(GuiOverlapEvent.HorseOverlap.HoveredToolTip.Pre e) {
        if (!Reference.onWorld) return;

        for (Slot s : e.getGui().inventorySlots.inventorySlots) {
            if (s.slotNumber < e.getGui().getUpperInv().getContainerSize()) continue; // it's upper in horse!

            renderItemLock(s, e.getGui().getGuiLeft(), e.getGui().getGuiTop());
        }
    }

    private static void renderItemLock(Slot s, int guiLeft, int guiTop) {
        if (!UtilitiesConfig.INSTANCE.locked_slots.containsKey(PlayerInfo.get(CharacterData.class).getClassId())) return;
        if (!UtilitiesConfig.INSTANCE.locked_slots.get(PlayerInfo.get(CharacterData.class).getClassId()).contains(s.getSlotIndex())) return;

        ScreenRenderer.beginGL(0, 0);

        // HeyZeer0: this will make the lock appear over the item
        GlStateManager.translate(0, 0, 260);

        ScreenRenderer r = new ScreenRenderer();
        RenderHelper.disableStandardItemLighting();
        ScreenRenderer.scale(0.5f);
        r.drawRect(Textures.UIs.hud_overlays, (int)((guiLeft + s.xPos) / 0.5) + 25, (int)((guiTop + s.yPos) / 0.5) - 8, 0, 0, 16, 16);
        ScreenRenderer.endGL();
    }

}
