/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.utils.reflections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.inventory.ChestScreen;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public enum ReflectionFields {

    ChestScreen_lowerChestInventory(ChestScreen.class, "lowerChestInventory", "field_147015_w"),
    Entity_CUSTOM_NAME(Entity.class, "CUSTOM_NAME", "field_184242_az"),
    Entity_CUSTOM_NAME_VISIBLE(Entity.class, "CUSTOM_NAME_VISIBLE", "field_184233_aA"),
    EntityItemFrame_ITEM(EntityItemFrame.class, "ITEM", "field_184525_c"),
    Event_phase(Event.class, "phase"),
    GuiScreen_buttonList(Screen.class, "buttonList", "field_146292_n"),
    GuiScreenHorseInventory_horseEntity(GuiScreenHorseInventory.class, "horseEntity", "field_147034_x"),
    GuiScreenHorseInventory_horseInventory(GuiScreenHorseInventory.class, "horseInventory", "field_147029_w"),
    GuiIngame_persistantChatGUI(GuiIngame.class, "persistantChatGUI", "field_73840_e"),
    GuiIngame_remainingHighlightTicks(GuiIngame.class, "remainingHighlightTicks", "field_92017_k"),
    GuiIngame_highlightingItemStack(GuiIngame.class, "highlightingItemStack", "field_92016_l"),
    GuiIngame_displayedSubTitle(GuiIngame.class, "displayedSubTitle", "field_175200_y"),
    GuiIngame_overlayPlayerList(GuiIngame.class, "overlayPlayerList", "field_175196_v"),
    NewChatGui_defaultInputFieldText(NewChatGui.class, "defaultInputFieldText", "field_146409_v"),
    GuiPlayerTabOverlay_ENTRY_ORDERING(GuiPlayerTabOverlay.class, "ENTRY_ORDERING", "field_175252_a"),
    Minecraft_resourcePackRepository(Minecraft.class, "resourcePackRepository", "field_110448_aq"),
    CClientSettingsPacket_chatVisibility(CClientSettingsPacket.class, "chatVisibility", "field_149529_c"),
    ModelRenderer_compiled(ModelRenderer.class, "compiled", "field_78812_q"),
    Minecraft_renderItem(Minecraft.class, "renderItem", "field_175621_X"),
    RenderItem_itemModelMesher(ItemRenderer.class, "itemModelMesher", "field_175059_m");

    static {
        GuiPlayerTabOverlay_ENTRY_ORDERING.removeFinal();
    }

    final Field field;

    ReflectionFields(Class<?> holdingClass, String... values) {
        this.field = ReflectionHelper.findField(holdingClass, values);
    }

    public <T> T getValue(Object parent) {
        try {
            return (T) field.get(parent);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setValue(Object parent, Object value) {
        try {
            field.set(parent, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static Field modifiersField = null;

    private void removeFinal() {
        if (modifiersField == null) {
            try {
                modifiersField = Field.class.getDeclaredField("modifiers");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                return;
            }
            modifiersField.setAccessible(true);
        }

        try {
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
