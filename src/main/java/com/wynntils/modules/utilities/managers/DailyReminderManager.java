/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.utils.Utils;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;

public class DailyReminderManager {

    public static void checkDailyReminder(PlayerEntity p) {
        if (!UtilitiesConfig.INSTANCE.dailyReminder || !Reference.onWorld) return;

        if (System.currentTimeMillis() > UtilitiesConfig.Data.INSTANCE.dailyReminder) {
            StringTextComponent text = new StringTextComponent("");
            text.getStyle().withColor(TextFormatting.GRAY);

            StringTextComponent openingBracket = new StringTextComponent("[");
            openingBracket.getStyle().withColor(TextFormatting.DARK_GRAY);
            text.append(openingBracket);

            text.append("!");

            StringTextComponent closingBracket = new StringTextComponent("] ");
            closingBracket.getStyle().withColor(TextFormatting.DARK_GRAY);
            text.append(closingBracket);

            StringTextComponent dailyRewards = new StringTextComponent("Daily rewards ");
            dailyRewards.getStyle().withColor(TextFormatting.WHITE);
            text.append(dailyRewards);

            text.append("are available to claim!");

            p.sendMessage(text);
            ModCore.mc().getSoundManager().play(SimpleSound.getMasterRecord(SoundEvents.BLOCK_NOTE_PLING, 1.0F));

            UtilitiesConfig.Data.INSTANCE.dailyReminder = System.currentTimeMillis() + 1800000;
            UtilitiesConfig.Data.INSTANCE.saveSettings(UtilitiesModule.getModule());
        }
    }

    public static void openedDaily() {
        if (!UtilitiesConfig.INSTANCE.dailyReminder || !Reference.onWorld) return;

        long now = System.currentTimeMillis();
        UtilitiesConfig.Data.INSTANCE.lastOpenedDailyReward = now;
        UtilitiesConfig.Data.INSTANCE.dailyReminder = now + 86400000;
        UtilitiesConfig.Data.INSTANCE.saveSettings(UtilitiesModule.getModule());
    }

    public static void openedDailyInventory(GuiScreenEvent.InitGuiEvent.Post e) {
        if (!UtilitiesConfig.INSTANCE.dailyReminder || !Reference.onWorld) return;

        if (Utils.isCharacterInfoPage(e.getGui())) {
            if (!((ContainerScreen) e.getGui()).inventorySlots.getSlot(22).hasItem()) {
                UtilitiesConfig.Data.INSTANCE.dailyReminder = System.currentTimeMillis() + 86400000;
                UtilitiesConfig.Data.INSTANCE.saveSettings(UtilitiesModule.getModule());
            }
        }
    }

}
