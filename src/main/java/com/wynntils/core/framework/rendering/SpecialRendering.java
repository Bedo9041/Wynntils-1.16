/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.rendering;

import com.mojang.blaze3d.platform.GlStateManager;
import com.wynntils.core.framework.rendering.colors.CommonColors;
import com.wynntils.core.framework.rendering.colors.CustomColor;
import com.wynntils.core.framework.rendering.math.MatrixMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.util.Random;

public class SpecialRendering {

    private static final Vector3f[] godRaysOffset = new Vector3f[] {
            new Vector3f(1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, 0, 1),
            new Vector3f(1, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, 0, 1),
    };

    public static void renderGodRays(int x, int y, int z, double size, int rays, CustomColor color) {
        float time = Minecraft.getSystemTime() / 50f;
        Random rand = new Random(142L);

        boolean isRainbow = color == CommonColors.RAINBOW;
        GlStateManager._pushMatrix();
        {
            { // gl setting
                GlStateManager._translate(x, y, z);
                GlStateManager._blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
                GlStateManager._enableBlend();
                GlStateManager._disableAlpha();
                GlStateManager._disableCull();
                GlStateManager._disableTexture2D();
                GlStateManager._shadeModel(GL11.GL_SMOOTH);
                GlStateManager._depthMask(false);
            }

            Matrix4f matrix = new Matrix4f();
            matrix.setIdentity();

            Vector4f a = new Vector4f();
            Vector4f b = new Vector4f();
            Vector4f c = new Vector4f();

            Vector3f rotationAxis = new Vector3f(0, 1, 0);

            for (int i = 0; i < rays; i++) {
                float pos = 1 / (float) (i + 1);

                for (Vector3f vec : godRaysOffset) {
                    MatrixMath.rotate((float) Math.PI * 2 * rand.nextFloat() + time / 360, vec, matrix, matrix);
                }

                float r = (1F + rand.nextFloat() * 2.5F) * 2;

                MatrixMath.rotate(time / 180f, rotationAxis, matrix, matrix);

                a.set(0F, 0.126f * r, 0.5f * r, 1);
                b.set(0F, -0.126f * r, 0.5f * r, 1);
                c.set(0F, 0, 0.6f * r, 1);

                matrix.transform(a);
                matrix.transform(b);
                matrix.transform(c);

                float red, green, blue;
                if (isRainbow) {
                    int rgb = Color.HSBtoRGB(i / 16f, 1, 1);

                    red = ((rgb & 0x00ff0000) >> 16) / 255.0f;
                    green = ((rgb & 0x0000ff00) >> 8) / 255.0f;
                    blue = ((rgb & 0x000000ff)) / 255.0f;
                } else {
                    red = color.r;
                    green = color.g;
                    blue = color.b;
                }

                Tessellator tess = Tessellator.getInstance();
                BufferBuilder builder = tess.getBuilder();
                {
                    builder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    builder.vertex(size, size, size).color(red, green, blue, 0.9F).endVertex();
                    builder.vertex(a.x * size, a.y * size, a.z * size).color(red, green, blue, 0.01F).endVertex();
                    builder.vertex(c.x * size, c.y * size, c.z * size).color(red, green, blue, 0.01F).endVertex();
                    builder.vertex(b.x * size, b.y * size, b.z * size).color(red, green, blue, 0.01F).endVertex();
                }
                tess.end();
            }

            { // gl resetting
                GlStateManager._enableTexture2D();
                GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GlStateManager._enableBlend();
                GlStateManager._depthMask(true);
                GlStateManager._color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager._enableAlpha();
            }

        }
        GlStateManager._popMatrix();

    }

}
