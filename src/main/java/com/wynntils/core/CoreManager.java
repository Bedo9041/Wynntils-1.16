/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core;

import com.wynntils.core.events.ClientEvents;
import com.wynntils.core.framework.enums.wynntils.WynntilsConflictContext;
import com.wynntils.core.framework.instances.PlayerInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;

public class CoreManager {

    /**
     * Called before all modules are registered
     */
    public static void preModules() {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());

        Minecraft.getInstance().options.keyUp.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        Minecraft.getInstance().options.keyDown.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        Minecraft.getInstance().options.keyRight.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
        Minecraft.getInstance().options.keyLeft.setKeyConflictContext(WynntilsConflictContext.ALLOW_MOVEMENTS);
    }

    /**
     * Called after all modules are registered
     */
    public static void afterModules() {
        PlayerInfo.setup();
    }

}
