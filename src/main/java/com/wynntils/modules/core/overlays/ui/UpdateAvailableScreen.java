/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.ui;

import com.wynntils.Reference;
import com.wynntils.core.utils.ServerUtils;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.core.config.CoreDBConfig;
import com.wynntils.modules.core.enums.UpdateStream;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;

public class UpdateAvailableScreen extends Screen {

    private ServerData server;
    private String text;

    public UpdateAvailableScreen(ServerData server) {
        this.server = server;
        if (WebManager.getUpdate().getLatestUpdate().startsWith("B")) {
            text = TextFormatting.YELLOW + "Build " + WebManager.getUpdate().getLatestUpdate().replace("B", "") + TextFormatting.WHITE + " is available.";
        } else {
            text = "A new update is available " + TextFormatting.YELLOW + "v" + WebManager.getUpdate().getLatestUpdate();
        }
    }

    @Override
    public void init() {
        this.buttonList.add(new Button(0, this.width / 2 - 100, this.height / 4 + 84, 200, 20, "View changelog"));
        this.buttonList.add(new Button(1, this.width / 2 - 100, this.height / 4 + 108, 98, 20, "Update now"));
        this.buttonList.add(new Button(2, this.width / 2 + 2, this.height / 4 + 108, 98, 20, "Update at exit"));
        this.buttonList.add(new Button(3, this.width / 2 - 100, this.height / 4 + 132, 98, 20, "Ignore update"));
        this.buttonList.add(new Button(4, this.width / 2 + 2, this.height / 4 + 132, 98, 20, "Cancel"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int yOffset = Math.min(this.height / 2, this.height / 4 + 80 - mc.font.FONT_HEIGHT * 2);
        drawCenteredString(mc.font, text, this.width/2, yOffset - mc.font.FONT_HEIGHT - 2, 0xFFFFFFFF);
        drawCenteredString(mc.font, "Update now or when leaving Minecraft?", this.width/2, yOffset, 0xFFFFFFFF);
        drawCenteredString(mc.font, "(Updating now will exit Minecraft after downloading update)", this.width/2, yOffset + mc.font.FONT_HEIGHT + 2, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void actionPerformed(Button button) {
        if (button.id == 1 || button.id == 2) {
            // Update
            CoreDBConfig.INSTANCE.showChangelogs = true;
            CoreDBConfig.INSTANCE.lastVersion = Reference.VERSION;
            CoreDBConfig.INSTANCE.saveSettings(CoreModule.getModule());
            mc.setScreen(new UpdatingScreen(button.id == 1));
        } else if (button.id == 3) {
            // Ignore
            WebManager.skipJoinUpdate();
            ServerUtils.connect(null, server);
        } else if (button.id == 4) {
            // Cancel
            mc.setScreen(null);
        } else if (button.id == 0) {
            // View changelog
            boolean major = CoreDBConfig.INSTANCE.updateStream == UpdateStream.STABLE;
            ChangelogUI.loadChangelogAndShow(this, major, true);
        }
    }

}
