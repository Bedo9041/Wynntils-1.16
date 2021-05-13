/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.enums.SpellType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.StringUtils;
import com.wynntils.core.utils.helpers.RainbowText;
import com.wynntils.core.utils.reference.EmeraldSymbols;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import com.wynntils.modules.utilities.enums.IdentificationType;
import com.wynntils.modules.utilities.instances.IdentificationResult;
import com.wynntils.webapi.WebManager;
import com.wynntils.webapi.profiles.item.IdentificationOrderer;
import com.wynntils.webapi.profiles.item.ItemGuessProfile;
import com.wynntils.webapi.profiles.item.ItemProfile;
import com.wynntils.webapi.profiles.item.enums.IdentificationModifier;
import com.wynntils.webapi.profiles.item.enums.ItemTier;
import com.wynntils.webapi.profiles.item.enums.MajorIdentification;
import com.wynntils.webapi.profiles.item.objects.IdentificationContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static net.minecraft.util.text.TextFormatting.*;

public class ItemIdentificationOverlay implements Listener {

    private final static Pattern ITEM_QUALITY = Pattern.compile("(?<Quality>Normal|Unique|Rare|Legendary|Fabled|Mythic|Set) Item(?: \\[(?<Rolls>\\d+)])?(?: \\[[0-9,]+" + EmeraldSymbols.E + "])?");
    public final static Pattern ID_PATTERN = Pattern.compile("(^\\+?(?<Value>-?\\d+)(?: to \\+?(?<UpperValue>-?\\d+))?(?<Suffix>%|/\\ds| tier)?(?<Stars>\\*{0,3}) (?<ID>[a-zA-Z 0-9]+))");
    private final static Pattern MARKET_PRICE = Pattern.compile(" - (?<Quantity>\\d x )?(?<Value>(?:,?\\d{1,3})+)" + EmeraldSymbols.E);

    public static final DecimalFormat decimalFormat = new DecimalFormat("#,###,###,###");

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

    public static void replaceLore(ItemStack stack)  {
        if (!UtilitiesConfig.Identifications.INSTANCE.enabled || !stack.hasCustomHoverName() || !stack.hasTagCompound()) return;
        CompoundNBT nbt = stack.getTag();
        if (nbt.contains("wynntilsIgnore")) return;

        String itemName = StringUtils.normalizeBadString(stripFormatting(stack.getDisplayName().getString()));

        // Check if unidentified item.
        if (itemName.contains("Unidentified") && UtilitiesConfig.Identifications.INSTANCE.showItemGuesses) {
            // Add possible identifications
            nbt.putBoolean("wynntilsIgnore", true);
            addItemGuesses(stack);
            return;
        }

        // Check if item is a valid item if not ignore it
        if (!nbt.contains("wynntils") && WebManager.getItems().get(itemName) == null) {
            nbt.putBoolean("wynntilsIgnore", true);
            return;
        }

        CompoundNBT wynntils = generateData(stack);
        ItemProfile item = WebManager.getItems().get(wynntils.getString("originName"));

        // Block if the item is not the real item
        if (!wynntils.contains("isPerfect") && !stack.getDisplayName().getString().startsWith(item.getTier().getTextColor())) {
            nbt.putBoolean("wynntilsIgnore", true);
            nbt.removeTag("wynntils");
            return;
        }

        // Perfect name
        if (wynntils.contains("isPerfect")) {
            stack.setStackDisplayName(RainbowText.makeRainbow("Perfect " + wynntils.getString("originName"), true));
        }

        // Update only if should update, this is decided on generateDate
        if (!wynntils.getBoolean("shouldUpdate")) return;
        wynntils.putBoolean("shouldUpdate", false);

        // Objects
        IdentificationType idType = IdentificationType.valueOf(wynntils.getString("currentType"));
        List<String> newLore = new ArrayList<>();

        // Generating id lores
        Map<String, String> idLore = new HashMap<>();

        double relativeTotal = 0;
        int idAmount = 0;
        boolean hasNewId = false;

        if (wynntils.contains("ids")) {
            CompoundNBT ids = wynntils.getCompound("ids");
            for (String idName : ids.getKeySet()) {
                if (idName.contains("*")) continue; // star data, ignore

                IdentificationContainer id = item.getStatuses().get(idName);
                IdentificationModifier type = id != null ? id.getType() : IdentificationContainer.getTypeFromName(idName);
                if (type == null) continue; // not a valid id

                int currentValue = ids.getInteger(idName);
                boolean isInverted = IdentificationOrderer.INSTANCE.isInverted(idName);

                // id color
                String longName = IdentificationContainer.getAsLongName(idName);
                SpellType spell = SpellType.fromName(longName);
                if (spell != null) {
                    ClassType requiredClass = item.getClassNeeded();
                    if (requiredClass != null) {
                        longName = spell.forOtherClass(requiredClass).getName() + " Spell Cost";
                    } else {
                        longName = spell.forOtherClass(PlayerInfo.get(CharacterData.class).getCurrentClass()).getGenericAndSpecificName() + " Cost";
                    }
                }

                String lore;
                if (isInverted)
                    lore = (currentValue < 0 ? GREEN.toString() : currentValue > 0 ? RED + "+" : GRAY.toString())
                            + currentValue + type.getInGame();
                else
                    lore = (currentValue < 0 ? RED.toString() : currentValue > 0 ? GREEN + "+" : GRAY.toString())
                            + currentValue + type.getInGame();

                if (UtilitiesConfig.Identifications.INSTANCE.addStars && ids.contains(idName + "*")) {
                    lore += DARK_GREEN + "***".substring(0, ids.getInteger(idName + "*"));
                }
                lore += " " + GRAY + longName;

                if (id == null) { // id not in api
                    idLore.put(idName, lore + GOLD + " NEW");
                    hasNewId = true;
                    continue;
                }

                if (id.hasConstantValue()) {
                    if (id.getBaseValue() != currentValue) {
                        idLore.put(idName, lore + GOLD + " NEW");
                        hasNewId = true;
                        continue;
                    }
                    idLore.put(idName, lore);
                    continue;
                }

                IdentificationResult result = idType.identify(id, currentValue, isInverted);
                idLore.put(idName, lore + " " + result.getLore());

                if (result.getAmount() > 1d || result.getAmount() < 0d) {
                    hasNewId = true;
                    continue;
                }

                relativeTotal += result.getAmount();
                idAmount++;
            }
        }

        // Copying some parts of the old lore (stops on ids, powder or quality)
        boolean ignoreNext = false;
        for (String oldLore : ItemUtils.getLore(stack)) {
            if (ignoreNext) {
                ignoreNext = false;
                continue;
            }

            String rawLore = stripFormatting(oldLore);
            // market stuff
            if (rawLore.contains("Price:")) {
                ignoreNext = true;

                CompoundNBT market = wynntils.getCompound("marketInfo");

                newLore.add(GOLD + "Price:");
                String mLore = GOLD + " - " + GRAY;
                if (market.contains("quantity")) {
                    mLore += market.getInteger("quantity") + " x ";
                }

                int[] money = calculateMoneyAmount(market.getInteger("price"));
                String price = "";
                if (money[3] != 0) price += money[3] + "stx ";
                if (money[2] != 0) price += money[2] + EmeraldSymbols.LE + " ";
                if (money[1] != 0) price += money[1] + EmeraldSymbols.BLOCKS + " ";
                if (money[0] != 0) price += money[0] + EmeraldSymbols.EMERALDS + " ";

                price = price.trim();

                mLore += "" + WHITE + decimalFormat.format(market.getInteger("price")) + EmeraldSymbols.EMERALDS;
                mLore += DARK_GRAY + " (" + price + ")";

                newLore.add(mLore);
                continue;
            }

            // Stop on id if the item has ids
            if (idLore.size() > 0) {
                if (rawLore.startsWith("+") || rawLore.startsWith("-")) break;

                newLore.add(oldLore);
                continue;
            }

            // Stop on powders if the item has powders
            if (wynntils.contains("powderSlots") && oldLore.contains("] Powder Slots")) {
                break;
            }

            // Stop on quality if there's no other
            Matcher m = ITEM_QUALITY.matcher(rawLore);
            if (m.matches()) break;

            newLore.add(oldLore);
        }

        // Add id lores
        if (idLore.size() > 0) {
            newLore.addAll(IdentificationOrderer.INSTANCE.order(idLore,
                    UtilitiesConfig.Identifications.INSTANCE.addSpacing));

            newLore.add(" ");
        }

        // Major ids
        if (item.getMajorIds() != null && item.getMajorIds().size() > 0) {
            for (MajorIdentification majorId : item.getMajorIds()) {
                if (majorId == null) continue;
                Stream.of(StringUtils.wrapTextBySize(majorId.asLore(), 150)).forEach(c -> newLore.add(DARK_AQUA + c));
            }
            newLore.add(" ");
        }

        // Powder lore
        if (wynntils.contains("powderSlots")) newLore.add(wynntils.getString("powderSlots"));

        // Set Bonus
        if (wynntils.contains("setBonus")) {
            if (wynntils.contains("powderSlots")) newLore.add(" ");

            newLore.add(GREEN + "Set Bonus:");
            CompoundNBT ids = wynntils.getCompound("setBonus");

            Map<String, String> bonusOrder = new HashMap<>();
            for (String idName : ids.getKeySet()) {
                bonusOrder.put(idName, ids.getString(idName));
            }

            newLore.addAll(IdentificationOrderer.INSTANCE.order(bonusOrder, UtilitiesConfig.Identifications.INSTANCE.addSetBonusSpacing));
            newLore.add(" ");
        }

        // Quality lore
        String quality = item.getTier().asLore();
        int rollAmount = (wynntils.contains("rerollAmount") ? wynntils.getInteger("rerollAmount") : 0);
        if (rollAmount != 0) quality += " [" + rollAmount + "]";

        // adds reroll price if the item
        if (UtilitiesConfig.Identifications.INSTANCE.showRerollPrice && !item.isIdentified()) {
            quality += GREEN + " ["
                    + decimalFormat.format(item.getTier().getRerollPrice(item.getRequirements().getLevel(), rollAmount))
                    + EmeraldSymbols.E + "]";
        }

        newLore.add(quality);
        if (item.getRestriction() != null) newLore.add(RED + "Untradable Item");

        // Merchant & dungeon purchase offers
        if (wynntils.contains("purchaseInfo")) {
            newLore.add(" ");
            newLore.add(GOLD + "Price:");

            ListNBT purchaseInfo = wynntils.getTagList("purchaseInfo", 8 /* means StringNBT */);
            for (INBT nbtBase : purchaseInfo) {
                newLore.add(((StringNBT) nbtBase).getString());
            }
        }

        // Item lore
        if (item.getLore() != null && !item.getLore().isEmpty()) {
            if (wynntils.contains("purchaseInfo")) newLore.add(" ");

            newLore.addAll(Minecraft.getInstance().font.listFormattedStringToWidth(DARK_GRAY + item.getLore(), 150));
        }

        // Special displayname
        String specialDisplay = "";
        if (hasNewId) {
            specialDisplay = GOLD + " NEW";
        } else if (idAmount > 0 && relativeTotal > 0) {
            specialDisplay = " " + idType.getTitle(relativeTotal/(double)idAmount);
        }

        // check for item perfection
        if (relativeTotal/idAmount >= 1d && idType == IdentificationType.PERCENTAGES && !hasNewId && UtilitiesConfig.Identifications.INSTANCE.rainbowPerfect) {
            wynntils.putBoolean("isPerfect", true);
        }

        stack.setStackDisplayName(item.getTier().getTextColor() + item.getDisplayName() + specialDisplay);

        // Applying lore
        CompoundNBT compound = nbt.getCompound("display");
        ListNBT list = new ListNBT();

        newLore.forEach(c -> list.add(StringNBT.valueOf(c)));

        compound.put("Lore", list);

        nbt.put("wynntils", wynntils);
        nbt.put("display", compound);
    }

    private static void addItemGuesses(ItemStack stack) {
        String name = StringUtils.normalizeBadString(stack.getDisplayName().getString());
        String itemType = stripFormatting(name).split(" ", 3)[1];
        String levelRange = null;

        List<String> lore = ItemUtils.getLore(stack);

        for (String aLore : lore) {
            if (aLore.contains("Lv. Range")) {
                levelRange = stripFormatting(aLore).replace("- Lv. Range: ", "");
                break;
            }
        }

        if (itemType == null || levelRange == null) return;

        ItemGuessProfile igp = WebManager.getItemGuesses().get(levelRange);
        if (igp == null) return;

        Map<String, String> rarityMap = igp.getItems().get(itemType);
        if (rarityMap == null) return;

        ItemTier tier = ItemTier.fromTextColoredString(name);
        String items = rarityMap.get(tier.asCapitalizedName());

        if (items == null) return;
        String itemNamesAndCosts = "";
        String[] possiblitiesNames = items.split(", ");
        for (String possibleItem : possiblitiesNames) {
            ItemProfile itemProfile = WebManager.getItems().get(possibleItem);
            String itemDescription;
            if (UtilitiesConfig.Identifications.INSTANCE.showGuessesPrice && itemProfile != null) {
                int level = itemProfile.getRequirements().getLevel();
                int itemCost = tier.getItemIdentificationCost(level);
                itemDescription = tier.getTextColor() + possibleItem + GRAY + " [" + GREEN + itemCost + " "
                        + EmeraldSymbols.E_STRING + GRAY + "]";
            } else {
                itemDescription = tier.getTextColor() + possibleItem;
            }
            if (!itemNamesAndCosts.isEmpty()) {
                itemNamesAndCosts += GRAY + ", ";
            }
            itemNamesAndCosts += itemDescription;
        }

        ItemUtils.getLoreTag(stack).add(StringNBT.valueOf(GREEN + "- " + GRAY + "Possibilities: " + itemNamesAndCosts));
    }

    private static CompoundNBT generateData(ItemStack stack) {
        IdentificationType idType;
        if (Keyboard.isDown(GLFW.GLFW_KEY_LSHIFT)) idType = IdentificationType.MIN_MAX;
        else if (Keyboard.isDown(GLFW.GLFW_KEY_LCONTROL)) idType = IdentificationType.UPGRADE_CHANCES;
        else idType = IdentificationType.PERCENTAGES;

        if (stack.hasTagCompound() && stack.getTag().contains("wynntils")) {
            CompoundNBT compound = stack.getTag().getCompound("wynntils");

            // check for updates
            if (!compound.getString("currentType").equals(idType.toString())) {
                compound.putBoolean("shouldUpdate", true);
                compound.putString("currentType", idType.toString());

                stack.getTag().put("wynntils", compound);
            }

            return compound;
        }

        CompoundNBT mainTag = new CompoundNBT();

        {  // main data
            mainTag.putString("originName", StringUtils.normalizeBadString(stripFormatting(stack.getDisplayName().getString())));  // this replace allow market items to be scanned
            mainTag.putString("currentType", idType.toString());
            mainTag.putBoolean("shouldUpdate", true);
        }

        CompoundNBT idTag = new CompoundNBT();
        CompoundNBT setBonus = new CompoundNBT();
        ListNBT purchaseInfo = new ListNBT();
        {  // lore data
            boolean isBonus = false;
            for (String loreLine : ItemUtils.getLore(stack)) {
                String lColor = stripFormatting(loreLine);

                if (lColor.isEmpty()) continue;

                // set bonus detection
                if (lColor.contains("Set Bonus:")) {
                    isBonus = true;
                    continue;
                }

                // ids and set bonus
                { Matcher idMatcher = ID_PATTERN.matcher(lColor);
                    if (idMatcher.find()) {
                        String idName = idMatcher.group("ID");
                        boolean isRaw = idMatcher.group("Suffix") == null;
                        int stars = idMatcher.group("Stars").length();

                        SpellType spell = SpellType.fromName(idName);
                        if (spell != null) {
                            idName = spell.getGenericName() + " Cost";
                        }

                        String shortIdName = toShortIdName(idName, isRaw);
                        if (stars != 0) {
                            idTag.putInt(shortIdName + "*", stars);
                        }

                        if (isBonus) {
                            setBonus.putString(shortIdName, loreLine);
                            continue;
                        }
                        idTag.putInt(shortIdName, Integer.parseInt(idMatcher.group("Value")));
                        continue;
                    }
                }

                // rerolls
                { Matcher rerollMatcher = ITEM_QUALITY.matcher(lColor);
                    if (rerollMatcher.find()) {
                        if (rerollMatcher.group("Rolls") == null) continue;

                        mainTag.putInt("rerollAmount", Integer.parseInt(rerollMatcher.group("Rolls")));
                        continue;
                    }
                }

                // powders
                if (lColor.contains("] Powder Slots")) mainTag.putString("powderSlots", loreLine);

                // dungeon and merchant prices
                if (lColor.startsWith(" - ✔") || lColor.startsWith(" - ✖")) {
                    purchaseInfo.add(StringNBT.valueOf(loreLine));
                    continue;
                }

                // market
                { Matcher market = MARKET_PRICE.matcher(lColor);
                    if (!market.find()) continue;

                    CompoundNBT marketTag = new CompoundNBT();

                    if (market.group("Quantity") != null)
                        marketTag.putInt("quantity", Integer.parseInt(
                                market.group("Quantity").replace(",", "").replace(" x ", "")
                        ));

                    marketTag.putInt("price", Integer.parseInt(market.group("Value").replace(",", "")));

                    mainTag.put("marketInfo", marketTag);
                }

            }

            if (idTag.getSize() > 0) mainTag.put("ids", idTag);
            if (setBonus.getSize() > 0) mainTag.put("setBonus", setBonus);
            if (purchaseInfo.size() > 0) mainTag.put("purchaseInfo", purchaseInfo);
        }

        // update compound
        CompoundNBT stackCompound = stack.getTag();
        stackCompound.put("wynntils", mainTag);

        stack.setTag(stackCompound);

        return mainTag;
    }

    public static String toShortIdName(String longIdName, boolean raw) {
        String[] splitName = longIdName.split(" ");
        StringBuilder result = new StringBuilder(raw ? "raw" : "");
        for (String r : splitName) {
            if (r.startsWith("[")) continue;  // ignore ids
            result.append(Character.toUpperCase(r.charAt(0))).append(r.substring(1).toLowerCase(Locale.ROOT));
        }

        if (result.length() == 0) return "";
        result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        return result.toString();
    }

    /**
     * Calculates the amount of emeralds, emerald blocks and liquid emeralds in the player inventory
     *
     * @param money the amount of money to process
     * @return an array with the values in the respective order of emeralds[0], emerald blocks[1], liquid emeralds[2], stx[3]
     */
    private static int[] calculateMoneyAmount(int money) {
        return new int[] { money % 64, (money / 64) % 64, (money / 4096) % 64, money / (64 * 4096) };
    }

}
