/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.hud;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.Reference;
import com.wynntils.core.framework.overlays.Overlay;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.modules.utilities.configs.OverlayConfig;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;

public class LowHealthVignetteOverlay extends Overlay {

    public LowHealthVignetteOverlay() {
        super("Drowning Vignette", 20, 20, true, 0, 0, 0, 0, null);
    }

    private static float animation = 10f;
    private static float currentHealth;
    private static float threshold;
    private static float value;
    private static final float INTENSITY = .3f;

    @Override
    public void render(RenderGameOverlayEvent.Post e) {
        if (!Reference.onWorld || !OverlayConfig.Health.INSTANCE.healthVignette || e.getType() != RenderGameOverlayEvent.ElementType.ALL || currentHealth > threshold) {
            return;
        }

        GlStateManager._pushMatrix();
        {
            ScreenRenderer.transformationOrigin(0, 0);
            GlStateManager.color(1, 0, 0, value);
            GlStateManager.disableDepth();
            GlStateManager._enableBlend();
            GlStateManager._depthMask(false);
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableAlpha();

            Textures.Masks.vignette.bind();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            {
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
                bufferbuilder.vertex(0.0D, ScreenRenderer.screen.getGuiScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
                bufferbuilder.vertex(ScreenRenderer.screen.getGuiScaledWidth(), ScreenRenderer.screen.getGuiScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
                bufferbuilder.vertex(ScreenRenderer.screen.getGuiScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
                bufferbuilder.vertex(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
            }
            tessellator.end();

            GlStateManager._depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        GlStateManager._popMatrix();
    }

    @Override
    public void tick(TickEvent.ClientTickEvent event, long ticks) {
        currentHealth = ScreenRenderer.mc.player.getHealth() / ScreenRenderer.mc.player.getMaxHealth();
        threshold = (float) OverlayConfig.Health.INSTANCE.lowHealthThreshold / 100;
        if (currentHealth > threshold) return;

        switch (OverlayConfig.Health.INSTANCE.healthVignetteEffect) {
            case Pulse:
                animation = (animation + .4f)%40;
                value = threshold - currentHealth * INTENSITY + .01f * Math.abs(20 - animation);
                break;
            case Growing:
                value = threshold - currentHealth * INTENSITY;
                break;
            case Static:
                value = INTENSITY;
                break;
        }
    }

}
