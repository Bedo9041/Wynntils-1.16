/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.framework.settings.ui.SettingsUI;
import com.wynntils.modules.core.overlays.inventories.IngameMenuReplacer;
import com.wynntils.modules.questbook.enums.QuestBookPages;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class MenuButtonsOverlay implements Listener {

    @SubscribeEvent
    public void init(GuiOverlapEvent.IngameMenuOverlap.InitGui e) {
        if (!Reference.onServer) return;

        int numButtonRows = 0;
        if (Reference.onWorld && UtilitiesConfig.INSTANCE.addClassHubButtons) {
            numButtonRows++;
        }
        if (UtilitiesConfig.INSTANCE.addOptionsProfileButtons) {
            numButtonRows++;
        }
        if (numButtonRows == 0) return;

        List<Button> buttonList = e.getButtonList();
        IngameMenuReplacer gui = e.getGui();
        removeDefaultButtons(buttonList);

        int yOffset = 72;
        moveButtons(buttonList, gui);

        if (Reference.onWorld && UtilitiesConfig.INSTANCE.addClassHubButtons) {
            addButtonPair(buttonList, gui, yOffset, 753, "Class selection",
                    754, "Back to Hub");
            yOffset = 48;
        }

        if (UtilitiesConfig.INSTANCE.addOptionsProfileButtons) {
            buttonList.add(new Button(756, gui.width / 2 + 2, gui.height / 4 + yOffset + -16, 98, 20, "Wynntils Menu"));
        }
    }

    private static void addButtonPair(List<Button> buttonList, IngameMenuReplacer gui, int yOffset, int buttonId1, String buttonText1, int buttonId2, String buttonText2) {
        buttonList.add(new Button(buttonId1, gui.width / 2 - 100, gui.height / 4 + yOffset + -16, 98, 20, buttonText1));
        buttonList.add(new Button(buttonId2, gui.width / 2 + 2, gui.height / 4 + yOffset + -16, 98, 20, buttonText2));
    }

    /**
     * Moves the www.wynncraft.com button to the right of the territory map button and when not showing the class selection and hub buttons moves the territory map and return to game buttons down and when not on beta moves the return to game button down
     */
    private static void moveButtons(List<Button> buttonList, IngameMenuReplacer gui) {
        for (Button button : buttonList) {
            if (button.id == 7) {
                button.y = gui.height / 4 + 48 - 16;
                button.width = 98;
                button.x = gui.width / 2 + 2;
            }
            if (!Reference.onWorld || !UtilitiesConfig.INSTANCE.addClassHubButtons) {
                if (button.id == 4 || button.id == 5) {
                    button.y += 24;
                }
            }
        }
    }

    /**
     * On Beta removes the Statistics button and the www.wynncraft.com button if the menu button is enabled and when not on beta removes the "Advancements", "Statistics" and "Open to LAN" buttons.
     * Also makes "Options..." and "Mod Options..." grey and "Disconnect" red.
     */
    private static void removeDefaultButtons(List<Button> buttonList) {
        buttonList.removeIf(b -> {
            if (UtilitiesConfig.INSTANCE.addOptionsProfileButtons && b.id == 7) return true;
            if (b.id == 6) return true;
            if (b.id == 1) {
                b.displayString = TextFormatting.RED + b.displayString;
            } else if (b.id == 12 || b.id == 0) {
                b.displayString = TextFormatting.GRAY + b.displayString;
            }
            return false;
        });
    }

    @SubscribeEvent
    public void actionPerformed(GuiOverlapEvent.IngameMenuOverlap.ActionPerformed e) {
        int id = e.getButton().id;
        switch (id) {
            case 753:
                Minecraft.getInstance().player.chat("/class");
                break;
            case 754:
                Minecraft.getInstance().player.chat("/hub");
                break;
            case 755:
                Minecraft.getInstance().setScreen(SettingsUI.getInstance(Minecraft.getInstance().screen));
                break;
            case 756:
                QuestBookPages.MAIN.getPage().open(true);
                break;
            default:
                return;
        }
        e.setCanceled(true);
    }

}
