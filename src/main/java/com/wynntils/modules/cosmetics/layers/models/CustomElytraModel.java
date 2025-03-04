/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.cosmetics.layers.models;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.core.utils.reflections.ReflectionFields;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomElytraModel extends ModelBase {

    private ModelRenderer rightWing = new ModelRenderer(this);
    private ModelRenderer leftWing = new ModelRenderer(this);

    public CustomElytraModel() {
        rightWing.mirror = true;
    }

    public void update(int maxFrames) {
        double percentage = ((System.currentTimeMillis() % 2000) / 2000d);
        int currentFrame = (int) (maxFrames * percentage);

        leftWing.cubeList.clear();
        leftWing.setTextureOffset(22, 32 * currentFrame);
        leftWing.setTextureSize(64, 32 * maxFrames);
        leftWing.addBox(-10.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
        ReflectionFields.ModelRenderer_compiled.setValue(leftWing, false);

        rightWing.cubeList.clear();
        rightWing.setTextureOffset(22, 32 * currentFrame);
        rightWing.setTextureSize(64, 32 * maxFrames);
        rightWing.addBox(0.0F, 0.0F, 0.0F, 10, 20, 2, 1.0F);
        ReflectionFields.ModelRenderer_compiled.setValue(rightWing, false);
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableCull();

        if (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isChild()) {
            GlStateManager._pushMatrix();
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
            GlStateManager.translate(0.0F, 1.5F, -0.1F);
            this.leftWing.render(scale);
            this.rightWing.render(scale);
            GlStateManager._popMatrix();
        }
        else {
            this.leftWing.render(scale);
            this.rightWing.render(scale);
        }
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        float f = 0.2617994F;
        float f1 = -0.2617994F;
        float f2 = 0.0F;
        float f3 = 0.0F;

        if (entityIn instanceof EntityLivingBase && ((EntityLivingBase)entityIn).isElytraFlying()) {
            float f4 = 1.0F;

            if (entityIn.motionY < 0.0D) {
                Vector3d vec3d = (new Vector3d(entityIn.motionX, entityIn.motionY, entityIn.motionZ)).normalize();
                f4 = 1.0F - (float)Math.pow(-vec3d.y, 1.5D);
            }

            f = f4 * 0.34906584F + (1.0F - f4) * f;
            f1 = f4 * -((float)Math.PI / 2F) + (1.0F - f4) * f1;
        }
        else if (entityIn.isSneaking()) {
            f = ((float)Math.PI * 2F / 9F);
            f1 = -((float)Math.PI / 4F);
            f2 = 3F;
            f3 = 0.08726646F;
        } else if (entityIn.isSprinting()) {
            f = ((float)Math.PI * 2F / 9F);
            f1 = -((float)Math.PI / 4F);
            f2 = 1F;
            f3 = 0.08726646F;
        }

        this.leftWing.rotationPointX = 5.0F;
        this.leftWing.rotationPointY = f2;

        if (entityIn instanceof AbstractClientPlayer) {
            AbstractClientPlayer abstractclientplayer = (AbstractClientPlayer)entityIn;
            abstractclientplayer.rotateElytraX = (float)((double)abstractclientplayer.rotateElytraX + (double)(f - abstractclientplayer.rotateElytraX) * 0.1D);
            abstractclientplayer.rotateElytraY = (float)((double)abstractclientplayer.rotateElytraY + (double)(f3 - abstractclientplayer.rotateElytraY) * 0.1D);
            abstractclientplayer.rotateElytraZ = (float)((double)abstractclientplayer.rotateElytraZ + (double)(f1 - abstractclientplayer.rotateElytraZ) * 0.1D);
            this.leftWing.rotateAngleX = abstractclientplayer.rotateElytraX;
            this.leftWing.rotateAngleY = abstractclientplayer.rotateElytraY;
            this.leftWing.rotateAngleZ = abstractclientplayer.rotateElytraZ;
        }
        else
        {
            this.leftWing.rotateAngleX = f;
            this.leftWing.rotateAngleZ = f1;
            this.leftWing.rotateAngleY = f3;
        }

        this.rightWing.rotationPointX = -this.leftWing.rotationPointX;
        this.rightWing.rotateAngleY = -this.leftWing.rotateAngleY;
        this.rightWing.rotationPointY = this.leftWing.rotationPointY;
        this.rightWing.rotateAngleX = this.leftWing.rotateAngleX;
        this.rightWing.rotateAngleZ = -this.leftWing.rotateAngleZ;
    }
}
