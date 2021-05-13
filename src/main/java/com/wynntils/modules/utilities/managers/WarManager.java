/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.modules.utilities.configs.UtilitiesConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;

public class WarManager {

    /**
     * This filters the spawn of useless entities on wars
     * 78 == Armor Stands
     *
     * @param e the packet spawn event
     * @return if the mob should be filtered out
     */
    public static boolean filterMob(PacketEvent<SSpawnObjectPacket> e) {
        if (!UtilitiesConfig.Wars.INSTANCE.allowEntityFilter || !Reference.onWars) return false;

        return e.getPacket().getType() == 78;
    }

    /**
     * This blocks the user from clicking into workstations while warring
     * Works by blocking clicks at ArmorStands, which are responsible for the hitbox
     *
     * @param e the packet use entity event
     * @return if the click should be allowed
     */
    public static boolean allowClick(PacketEvent<CUseEntityPacket> e) {
        if (!UtilitiesConfig.Wars.INSTANCE.blockWorkstations || !Reference.onWars) return false;

        Entity in = e.getPacket().getEntityFromWorld(Minecraft.getInstance().level);
        return in instanceof ArmorStandEntity || in instanceof EntitySlime;
    }

}
