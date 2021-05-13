/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.overlays.inventories;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.enums.Powder;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.Utils;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WynnDataOverlay implements Listener {

    @SubscribeEvent
    public void init(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (!Reference.onWorld || !Utils.isCharacterInfoPage(e.getGui())) return;

        e.getButtonList().add(
                new Button(12,
                        (e.getGui().width - e.getGui().getXSize()) / 2 - 20,
                        (e.getGui().height - e.getGui().getYSize()) / 2 + 40,
                        18, 18,
                        "➦"
                )
        );
    }

    @SubscribeEvent
    public void drawScreen(GuiOverlapEvent.ChestOverlap.DrawScreen.Post e) {
        e.getButtonList().forEach(gb -> {
            if (gb.id == 12 && gb.isMouseOver()) {
                e.getGui().drawHoveringText(Arrays.asList("Left click: Open Build on WynnData", "Shift + Right click on item: Open Item on WynnData"), e.getMouseX(), e.getMouseY());
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void clickOnChest(GuiOverlapEvent.ChestOverlap.HandleMouseClick e) {
        if (!Utils.isCharacterInfoPage(e.getGui()) || e.getMouseButton() != 1) return;

        if (!(Keyboard.isDown(GLFW.GLFW_KEY_LSHIFT) || Keyboard.isDown(GLFW.GLFW_KEY_RSHIFT))) return;

        Slot slot = e.getGui().getSlotUnderMouse();
        if (slot == null || slot.inventory == null || !slot.hasItem()) return;

        ItemStack stack = slot.getItem();
        Utils.openUrl("https://www.wynndata.tk/i/" + Utils.encodeItemNameForUrl(stack));
        e.setCanceled(true);
    }

    private void getItemNameFromInventory(Map<String, String> urlData, String typeName, NonNullList<ItemStack> inventory, int slot) {
        ItemStack stack = inventory.get(slot);
        if (!stack.isEmpty() && WebManager.getItems().containsKey(Utils.getRawItemName(stack))) {
            urlData.put(typeName, Utils.encodeItemNameForUrl(stack));
        }
        ItemUtils.getLore(stack).forEach(line -> {
            if (line.contains("Powder Slots [")) {
                List<Powder> powders = Powder.findPowders(line);
                StringBuilder sb = new StringBuilder();
                powders.forEach(powder -> {
                    sb.append(powder.getLetterRepresentation());
                    sb.append("1");
                });
                urlData.put(typeName + "_powders", sb.toString());
                // Make sure user starts at the update powders screen
                urlData.put("action", "powders");
            }
        });
    }

    private int locateWeaponSlot() {
        for (int i = 0; i < 9; i++) {
            String lore = ItemUtils.getStringLore(Minecraft.getInstance().player.inventory.items.get(i));
            // Assume that only weapons have class requirements
            if (lore.contains("Class Req: " + PlayerInfo.get(CharacterData.class).getCurrentClass().getDisplayName())) {
                return i;
            }
        }
        return -1;
    }

    @SubscribeEvent
    public void mouseClicked(GuiOverlapEvent.ChestOverlap.MouseClicked e) {
        e.getButtonList().forEach(gb -> {
            if (gb.id != 12 || !gb.isMouseOver() || e.getMouseButton() != 0) return;

            Minecraft.getInstance().getSoundManager().play(SimpleSound.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1f));

            Map<String, String> urlData = new HashMap<>();

            NonNullList<ItemStack> armorInventory = Minecraft.getInstance().player.inventory.armorInventory;
            getItemNameFromInventory(urlData, "helmet", armorInventory, 3);
            getItemNameFromInventory(urlData, "chestplate", armorInventory, 2);
            getItemNameFromInventory(urlData, "leggings", armorInventory, 1);
            getItemNameFromInventory(urlData, "boots", armorInventory, 0);

            NonNullList<ItemStack> items = Minecraft.getInstance().player.inventory.items;
            getItemNameFromInventory(urlData, "ring1", items, 9);
            getItemNameFromInventory(urlData, "ring2", items, 10);
            getItemNameFromInventory(urlData, "bracelet", items, 11);
            getItemNameFromInventory(urlData, "necklace", items, 12);

            int weaponSlot = locateWeaponSlot();
            if (weaponSlot != -1) {
                getItemNameFromInventory(urlData, "weapon", items, weaponSlot);
            }
            StringBuilder urlBuilder = new StringBuilder("https://www.wynndata.tk/builder?");
            for (Map.Entry<String, String> itemName : urlData.entrySet()) {
                urlBuilder
                        .append(itemName.getKey())
                        .append('=')
                        .append(itemName.getValue())
                        .append('&');
            }
            urlBuilder.replace(urlBuilder.length() - 1, urlBuilder.length(), "");

            Utils.openUrl(urlBuilder.toString());
        });
    }

}
