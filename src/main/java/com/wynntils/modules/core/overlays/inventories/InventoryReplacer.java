/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class InventoryReplacer extends InventoryScreen {

    PlayerEntity player;

    public InventoryReplacer(PlayerEntity player) {
        super(player);

        this.player = player;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    @Override
    public void drawContainerScreenForegroundLayer(int mouseX, int mouseY) {
        super.drawContainerScreenForegroundLayer(mouseX, mouseY);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawContainerScreenForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void drawContainerScreenBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawContainerScreenBackgroundLayer(partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.DrawContainerScreenBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.KeyTyped(this, typedChar, keyCode)))
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void actionPerformed(Button guiButton) throws IOException {
        if (guiButton.id == 10) {
            QuestBookPages.MAIN.getPage().open(true);
            return;
        }

        super.actionPerformed(guiButton);
    }

    @Override
    public void renderHoveredToolTip(int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderHoveredToolTip(x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        GlStateManager.translate(0, 0, -300d);
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void onGuiClosed() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.InventoryOverlap.GuiClosed(this));
        super.onGuiClosed();
    }

    public List<Button> getButtonList() {
        return buttonList;
    }
}
