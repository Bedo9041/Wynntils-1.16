/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;
import java.util.List;

public class HorseReplacer extends GuiScreenHorseInventory  {

    IInventory lowerInv, upperInv;

    public HorseReplacer(IInventory playerInv, IInventory horseInv, AbstractHorseEntity horse) {
        super(playerInv, horseInv, horse);

        this.lowerInv = playerInv; this.upperInv = horseInv;
    }

    public IInventory getUpperInv() {
        return upperInv;
    }

    public IInventory getLowerInv() {
        return lowerInv;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HandleMouseClick(this, slotIn, slotId, mouseButton, type)))
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }

    @Override
    public void drawContainerScreenForegroundLayer(int mouseX, int mouseY) {
        super.drawContainerScreenForegroundLayer(mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawContainerScreenForegroundLayer(this, mouseX, mouseY));
    }

    @Override
    protected void drawContainerScreenBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        super.drawContainerScreenBackgroundLayer(partialTicks, mouseX, mouseY);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.DrawContainerScreenBackgroundLayer(this, mouseX, mouseY));
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.KeyTyped(this, typedChar, keyCode)))
            super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void renderHoveredToolTip(int x, int y) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Pre(this, x, y))) return;

        super.renderHoveredToolTip(x, y);
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.HoveredToolTip.Post(this, x, y));
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void onGuiClosed() {
        FrameworkManager.getEventBus().post(new GuiOverlapEvent.HorseOverlap.GuiClosed(this));
        super.onGuiClosed();
    }

    public List<Button> getButtonList() {
        return buttonList;
    }

}
