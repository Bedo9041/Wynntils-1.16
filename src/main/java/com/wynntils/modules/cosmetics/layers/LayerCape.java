/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.cosmetics.layers;

import com.wynntils.ModCore;
import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.core.instances.account.WynntilsUser;
import com.wynntils.modules.core.managers.UserManager;
import com.wynntils.modules.cosmetics.configs.CosmeticsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import static com.mojang.blaze3d.platform.GlStateManager.*;

public class LayerCape implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer playerRenderer;
    private final ModelRenderer bipedCape;

    public LayerCape(RenderPlayer playerRendererIn) {
        this.playerRenderer = playerRendererIn;
        this.bipedCape = new ModelRenderer(playerRendererIn.getMainModel());
    }

    public void renderModel(AbstractClientPlayer player, ModelBase model, float scale, int maxFrames) {
        double percentage = ((System.currentTimeMillis() % 2000) / 2000d);
        int currentFrame = (int) (maxFrames * percentage) + 1;

        bipedCape.cubeList.clear();
        bipedCape.setTextureOffset(0, 32 * currentFrame);
        bipedCape.setTextureSize(64, 32 * maxFrames);
        bipedCape.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1);

        if (player.isSneaking()) bipedCape.rotationPointY = 3.0F;
        else bipedCape.rotationPointY = 0.0F;

        ReflectionFields.ModelRenderer_compiled.setValue(bipedCape, false);
        bipedCape.render(scale);
    }

    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!CosmeticsConfig.INSTANCE.forceCapes
                && !Minecraft.getInstance().options.getModelParts().toString().contains("CAPE")
                && player.getUniqueID() == ModCore.mc().player.getUniqueID()) return;

        WynntilsUser info = UserManager.getUser(player.getUniqueID());
        if (info == null || !info.getCosmetics().hasCape()) return;

        // loading cape
        ResourceLocation rl = info.getCosmetics().getLocation();

        // rendering verifications
        if (rl == null || !player.hasPlayerInfo() || player.isInvisible()) return;
        if (player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) return;

        // texture
        color(1.0F, 1.0F, 1.0F, 1.0F);
        playerRenderer.bind(rl);

        // rendering
        { _pushMatrix();
            this.playerRenderer.getMainModel().bipedBody.postRender(scale);
            translate(0.0F, 0.0F, 0.125F);
            if (player.isSneaking()) {
                translate(0.0F, 0.0F, -0.1F);
            }

            double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * (double) partialTicks - (player.prevPosX + (player.getX() - player.prevPosX) * (double) partialTicks);
            double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * (double) partialTicks - (player.prevPosY + (player.getY() - player.prevPosY) * (double) partialTicks);
            double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * (double) partialTicks - (player.prevPosZ + (player.getZ() - player.prevPosZ) * (double) partialTicks);
            float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
            double d3 = MathHelper.sin(f * 0.017453292F);
            double d4 = -MathHelper.cos(f * 0.017453292F);
            float f1 = (float) d1 * 10.0F;
            f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
            float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
            float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

            if (f2 < 0.0F) {
                f2 = 0.0F;
            }

            // Clamping f2 and f3 ...
            f2 = MathHelper.clamp(f2, 0.0F, 180.0F);
            f3 = MathHelper.clamp(f3, -50.0F, 50.0F);

            float f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
            f1 = f1 + MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

            rotate((6.0F + f2 / 2.0F + f1), 1.0F, 0.0F, 0.0F);
            rotate((f3 / 2.0F), 0.0F, 0.0F, 1.0F);
            rotate(180.0F, 0.0F, 1.0F, 0.0F);

            enableAlpha();
            _enableBlend();

            // Find out size of cape
            int frameCount = info.getCosmetics().getImage().getHeight() / (info.getCosmetics().getImage().getWidth() / 2);
            renderModel(player, playerRenderer.getMainModel(), 0.0625f, frameCount);

            _disableBlend();
            disableAlpha();
        } _popMatrix();
    }

    public boolean shouldCombineTextures() {
        return false;
    }

}
