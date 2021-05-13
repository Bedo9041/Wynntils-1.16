/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.widget.button.Button;

import java.io.IOException;
import java.util.List;

public class IngameMenuReplacer extends IngameMenuScreen {

    @Override
    public void init() {
        super.init();

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.InitGui(this, buttonList));
    }

    @Override
    public void actionPerformed(Button btn) throws IOException {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.ActionPerformed(this, btn))) {
            return;
        }
        super.actionPerformed(btn);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.DrawScreen(this, mouseX, mouseY, partialTicks));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        FrameworkManager.getEventBus().post(new GuiOverlapEvent.IngameMenuOverlap.MouseClicked(this, mouseX, mouseY, mouseButton));
    }

    @Override
    public void drawHoveringText(String text, int x, int y) {
        super.drawHoveringText(text, x, y);
    }

    public List<Button> getButtonList() {
        return buttonList;
    }

}
