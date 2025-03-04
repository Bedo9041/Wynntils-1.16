/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class ChestReplacer extends ChestScreen {

    IInventory lowerInv;
    IInventory upperInv;

    public ChestReplacer(IInventory upperInv, IInventory lowerInv) {
        super(upperInv, lowerInv);

        this.lowerInv = lowerInv;
        this.upperInv = upperInv;
    }

    public IInventory getLowerInv() {
        return lowerInv;
    }

    public IInventory getUpperInv() {
        return upperInv;
    }

    @Override
    public void init() {
        super.init();
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.InitGui(this, this.buttonList));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawScreen.Pre(this, mouseX, mouseY, partialTicks))) {
            return;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawScreen.Post(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.MouseClickMove(this, mouseX, mouseY, clickedMouseButton, timeSinceLastClick)))
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HandleMouseInput(this)))
            super.handleMouseInput();
    }

    @Override
    public void drawContainerScreenForegroundLayer(int mouseX, int mouseY) {
        super.drawContainerScreenForegroundLayer(mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawContainerScreenForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void drawContainerScreenBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawContainerScreenBackgroundLayer(partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.DrawContainerScreenBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.KeyTyped(this, typedChar, keyCode)))
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void renderHoveredToolTip(int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderHoveredToolTip(x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.MouseClicked(this, mouseX, mouseY, mouseButton))) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.ChestOverlap.GuiClosed(this));
        super.onGuiClosed();
    }

    public List<Button> getButtonList() {
        return this.buttonList;
    }
}
