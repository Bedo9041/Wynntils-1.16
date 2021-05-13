/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.InventoryData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class LoreChangerOverlay implements Listener {

    @SubscribeEvent
    public void onChest(GuiOverlapEvent.ChestOverlap.DrawScreen.Post e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

    @SubscribeEvent
    public void onInventory(GuiOverlapEvent.InventoryOverlap.DrawScreen e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

    @SubscribeEvent
    public void onHorse(GuiOverlapEvent.HorseOverlap.DrawScreen e) {
        if (e.getGui().getSlotUnderMouse() == null || !e.getGui().getSlotUnderMouse().hasItem()) return;

        replaceLore(e.getGui().getSlotUnderMouse().getItem());
    }

    private static void replaceLore(ItemStack stack) {
        // Soul Point Timer
        if ((stack.getItem() == Items.NETHER_STAR || stack.getItem() == Item.byBlock(Blocks.SNOW_LAYER)) && stack.getDisplayName().getString().contains("Soul Point")) {
            List<String> lore = ItemUtils.getLore(stack);
            if (lore != null && !lore.isEmpty()) {
                if (lore.get(lore.size() - 1).contains("Time until next soul point: ")) {
                    lore.remove(lore.size() - 1);
                    lore.remove(lore.size() - 1);
                }
                lore.add("");
                int secondsUntilSoulPoint = PlayerInfo.get(InventoryData.class).getTicksToNextSoulPoint() / 20;
                int minutesUntilSoulPoint = secondsUntilSoulPoint / 60;
                secondsUntilSoulPoint %= 60;
                lore.add(TextFormatting.AQUA + "Time until next soul point: " + TextFormatting.WHITE + minutesUntilSoulPoint + ":" + String.format("%02d", secondsUntilSoulPoint));
                ItemUtils.replaceLore(stack, lore);
                return;
            }
        }

        // Wynnic Translator
        if (stack.hasTagCompound() && !stack.getTag().getBoolean("showWynnic") && Keyboard.isDown(GLFW.GLFW_KEY_LSHIFT)) {
            String fullLore = ItemUtils.getStringLore(stack);
            if (StringUtils.hasWynnic(fullLore) || StringUtils.hasGavellian(fullLore)) {
                ListNBT loreList = ItemUtils.getLoreTag(stack);
                if (loreList != null) {
                    stack.getTag().put("originalLore", loreList.copy());
                    boolean capital = true;
                    for (int index = 0; index < loreList.size(); index++) {
                        String lore = loreList.getString(index);
                        if (StringUtils.hasWynnic(lore) || StringUtils.hasGavellian(lore)) {
                            StringBuilder translated = new StringBuilder();
                            boolean colorCode = false;
                            StringBuilder number = new StringBuilder();
                            for (char character : lore.toCharArray()) {
                                if (StringUtils.isWynnicNumber(character)) {
                                    number.append(character);
                                } else {
                                    if (!number.toString().isEmpty()) {
                                        translated.append(StringUtils.translateNumberFromWynnic(number.toString()));
                                        number = new StringBuilder();
                                    }

                                    String translatedCharacter;
                                    if (StringUtils.isWynnic(character)) {
                                        translatedCharacter = StringUtils.translateCharacterFromWynnic(character);
                                        if (capital && translatedCharacter.matches("[a-z]")) {
                                            translatedCharacter = String.valueOf(Character.toUpperCase(translatedCharacter.charAt(0)));
                                        }
                                    } else if (StringUtils.isGavellian(character)) {
                                        translatedCharacter = StringUtils.translateCharacterFromGavellian(character);
                                        if (capital) {
                                            translatedCharacter = String.valueOf(Character.toUpperCase(translatedCharacter.charAt(0)));
                                        }
                                    } else {
                                        translatedCharacter = String.valueOf(character);
                                    }

                                    translated.append(translatedCharacter);

                                    if (".?!".contains(translatedCharacter)) {
                                        capital = true;
                                    } else if (translatedCharacter.equals("§")) {
                                        colorCode = true;
                                    } else if (!translatedCharacter.equals(" ") && !colorCode) {
                                        capital = false;
                                    } else if (colorCode) {
                                        colorCode = false;
                                    }
                                }
                            }
                            if (!number.toString().isEmpty()) {
                                translated.append(StringUtils.translateNumberFromWynnic(number.toString()));
                                number = new StringBuilder();
                            }

                            loreList.set(index, StringNBT.valueOf(translated.toString()));
                        }
                    }
                }
            }
            stack.getTag().putBoolean("showWynnic", true);
        }

        if (stack.hasTagCompound() && stack.getTag().getBoolean("showWynnic") && !Keyboard.isDown(GLFW.GLFW_KEY_LSHIFT)) {
            CompoundNBT tag = stack.getTag();
            if (tag.contains("originalLore")) {
                CompoundNBT displayTag = tag.getCompound("display");
                if (displayTag != null) {
                    displayTag.put("Lore", tag.get("originalLore"));
                }
                tag.removeTag("originalLore");
            }
            stack.getTag().putBoolean("showWynnic", false);
        }
    }
}
