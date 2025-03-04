/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core.framework.entities;

import com.wynntils.core.framework.entities.instances.FakeEntity;
import com.wynntils.core.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.mojang.blaze3d.platform.GlStateManager.*;

public class EntityManager {

    private static final Set<FakeEntity> entityList = new HashSet<>();
    private static final Set<FakeEntity> toSpawn = new HashSet<>();

    /**
     * Spawns and register a fake entity into the world
     * This method is THREAD SAFE.
     *
     * @param entity the entity you want to register
     */
    public static void spawnEntity(FakeEntity entity) {
        toSpawn.add(entity);
    }

    /**
     * Removes every single FakeEntity from the world.
     * This method is THREAD SAFE.
     */
    public static void clearEntities() {
        entityList.forEach(FakeEntity::remove);
    }

    /**
     * Called on RenderWorldLastEvent, proccess the rendering queue
     */
    public static void tickEntities() {
        if (entityList.isEmpty() && toSpawn.isEmpty()) return;

        Minecraft.getInstance().getProfiler().push("fakeEntities");
        {
            // adds all new entities to the set
            Iterator<FakeEntity> it = toSpawn.iterator();
            while (it.hasNext()) {
                entityList.add(it.next());
                it.remove();
            }

            RenderManager renderManager = Minecraft.getInstance().getRenderManager();
            if (renderManager == null || renderManager.options == null) return;

            ClientPlayerEntity player = Minecraft.getInstance().player;
            // ticks each entity
            it = entityList.iterator();
            while (it.hasNext()) {
                FakeEntity next = it.next();

                // remove marked entities
                if (next.toRemove()) {
                    it.remove();
                    continue;
                }

                Minecraft.getInstance().getProfiler().push(next.getName());
                { // render
                    next.livingTicks += 1;
                    next.tick(Utils.getRandom(), player);
                }
                Minecraft.getInstance().getProfiler().pop();
            }
        }
        Minecraft.getInstance().getProfiler().pop();
    }

    /**
     * Called on RenderWorldLastEvent, proccess the rendering queue
     *
     * @param partialTicks the world partial ticks
     * @param context the rendering context
     */
    public static void renderEntities(float partialTicks, WorldRenderer context) {
        if (entityList.isEmpty() && toSpawn.isEmpty()) return;

        Minecraft.getInstance().getProfiler().push("fakeEntities");
        {
            // adds all new entities to the set
            Iterator<FakeEntity> it = toSpawn.iterator();
            while (it.hasNext()) {
                entityList.add(it.next());
                it.remove();
            }

            RenderManager renderManager = Minecraft.getInstance().getRenderManager();
            if (renderManager == null || renderManager.options == null) return;

            ClientPlayerEntity player = Minecraft.getInstance().player;
            // ticks each entity
            it = entityList.iterator();
            while (it.hasNext()) {
                FakeEntity next = it.next();

                Minecraft.getInstance().getProfiler().push(next.getName());
                {
                    _pushMatrix();
                    {
                        next.preRender(partialTicks, context, renderManager);
                        // translates to the correctly entity position
                        // subtracting the viewer position offset
                        _translated(
                                next.currentLocation.x - renderManager.viewerPosX,
                                next.currentLocation.y - renderManager.viewerPosY,
                                next.currentLocation.z - renderManager.viewerPosZ
                        );
                        next.render(partialTicks, context, renderManager);
                    }
                    _popMatrix();
                }
                Minecraft.getInstance().getProfiler().pop();
            }
        }
        Minecraft.getInstance().getProfiler().pop();
    }

}
