/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.map.overlays.objects;

import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.map.overlays.enums.MapButtonType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.SoundEvents;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.mojang.blaze3d.platform.GlStateManager._popMatrix;
import static com.mojang.blaze3d.platform.GlStateManager._pushMatrix;

public class MapButton {

    int startX, startY;
    int endX, endY;

    MapButtonType type;
    List<String> hoverLore;
    BiConsumer<MapButton, Integer> onClick;
    Function<Void, Boolean> isEnabled;

    ScreenRenderer renderer = new ScreenRenderer();

    public MapButton(int posX, int posY, MapButtonType type, List<String> hoverLore, Function<Void, Boolean> isEnabled, BiConsumer<MapButton, Integer> onClick) {
        int halfWidth = type.getWidth() / 2;
        int halfHeight = type.getHeight() / 2;

        this.startX = posX - halfWidth;
        this.startY = posY - halfHeight;
        this.endX = posX + halfWidth;
        this.endY = posY + halfHeight;

        this.type = type;
        this.hoverLore = hoverLore;
        this.isEnabled = isEnabled;
        this.onClick = onClick;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        _pushMatrix();
        {
            if (isEnabled.apply(null)) {
                color(1f, 1f, 1f, 1f);
            } else {
                color(.3f, .3f, .3f, 1f);
            }

            // icon itself
            renderer.drawRect(Textures.Map.map_buttons, startX, startY, endX, endY,
                    type.getStartX(), type.getStartY(), type.getEndX(), type.getEndY());
        }
        _popMatrix();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1f, 1f)
        );

        onClick.accept(this, mouseButton);
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public List<String> getHoverLore() {
        return hoverLore;
    }

    public MapButtonType getType() {
        return type;
    }

}
