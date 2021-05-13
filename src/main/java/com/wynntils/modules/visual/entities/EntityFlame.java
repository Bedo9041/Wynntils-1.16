/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.visual.entities;

import com.wynntils.core.framework.entities.instances.FakeEntity;
import com.wynntils.core.framework.rendering.textures.Textures;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.visual.configs.VisualConfig;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.blaze3d.platform.GlStateManager.*;

public class EntityFlame extends FakeEntity {

    public static AtomicInteger flames = new AtomicInteger();

    float lifespan;
    float scale;

    public EntityFlame(Location currentLocation, Random r) {
        super(currentLocation);

        lifespan = VisualConfig.Flames.INSTANCE.maxLiving * 0.1f;
        scale = VisualConfig.Flames.INSTANCE.maxScale * r.nextFloat();

        flames.incrementAndGet();
    }

    @Override
    public void tick(Random r, ClientPlayerEntity player) {
        if (livingTicks < lifespan) return;

        remove();
    }

    @Override
    public void render(float partialTicks, WorldRenderer context, RenderManager render) {
        float percentage = ((livingTicks + partialTicks) / lifespan);

        float alpha = (1f - percentage);
        boolean thirdPerson = render.options.thirdPersonView == 2;

        { // setting up rotation
            translate(0, 10 * percentage * (10 * percentage), 0);
            _depthMask(false);
            _enableBlend();
            enableAlpha();
            //disableTexture2D();
            color(1f, 1f, 1f, alpha);

            rotate(-render.playerViewY, 0f, 1f, 0f); // rotates yaw
            rotate((float) (thirdPerson ? -1 : 1) * render.playerViewX, 1.0F, 0.0F, 0.0F); // rotates pitch

            scale(scale, scale, scale);
        }

        Textures.Particles.flame.bind();

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder buffer = tes.getBuilder();
        { // initial cube
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

            buffer.vertex(-.5,  3, 0).tex(0, 1).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex( .5,  3, 0).tex(1, 1).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex( .5, -.0, 0).tex(1, 0).color(1f, 1f, 1f, alpha).endVertex();
            buffer.vertex(-.5, -.0, 0).tex(0, 0).color(1f, 1f, 1f, alpha).endVertex();

            tes.end();
        }

        { // reset to default
            _disableBlend();
            enableTexture2D();
            _depthMask(true);
            color(1f, 1f, 1f, 1f);
        }
    }

    @Override
    public void remove() {
        super.remove();

        flames.decrementAndGet();
    }

}
