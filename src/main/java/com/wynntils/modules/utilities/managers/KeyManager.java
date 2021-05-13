/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.framework.enums.wynntils.WynntilsConflictContext;
import com.wynntils.core.framework.instances.KeyHolder;
import com.wynntils.core.framework.settings.ui.SettingsUI;
import com.wynntils.modules.core.CoreModule;
import com.wynntils.modules.map.overlays.MiniMapOverlay;
import com.wynntils.modules.utilities.UtilitiesModule;
import com.wynntils.modules.utilities.events.ClientEvents;
import com.wynntils.modules.utilities.overlays.hud.StopWatchOverlay;
import com.wynntils.modules.utilities.overlays.ui.GearViewerUI;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyManager {

    private static float lastGamma = 1f;

    private static KeyHolder lockInventoryKey;
    private static KeyHolder favoriteTradeKey;
    private static KeyHolder checkForUpdatesKey;
    private static KeyHolder zoomInKey;
    private static KeyHolder zoomOutKey;
    private static KeyHolder stopwatchKey;
    private static KeyHolder itemScreenshotKey;
    private static KeyHolder showLevelOverlayKey;

    public static void registerKeys() {
        UtilitiesModule.getModule().registerKeyBinding("Gammabright", GLFW.GLFW_KEY_G, "Wynntils", KeyConflictContext.IN_GAME, true, () -> {
            if (ModCore.mc().options.gammaSetting < 1000) {
                lastGamma = ModCore.mc().options.gammaSetting;
                ModCore.mc().options.gammaSetting = 1000;
                return;
            }

            ModCore.mc().options.gammaSetting = lastGamma;
        });

        checkForUpdatesKey = CoreModule.getModule().registerKeyBinding("Check for Updates", GLFW.GLFW_KEY_L, "Wynntils", true, WebManager::checkForUpdates);

        CoreModule.getModule().registerKeyBinding("Open Settings", GLFW.GLFW_KEY_P, "Wynntils", KeyConflictContext.IN_GAME, true, () -> ModCore.mc().setScreen(SettingsUI.getInstance(ModCore.mc().screen)));

        lockInventoryKey = UtilitiesModule.getModule().registerKeyBinding("Lock Slot", GLFW.GLFW_KEY_H, "Wynntils", KeyConflictContext.GUI, true, () -> {});
        favoriteTradeKey = UtilitiesModule.getModule().registerKeyBinding("Favorite Trade", GLFW.GLFW_KEY_F, "Wynntils", KeyConflictContext.GUI, true, () -> {});

        UtilitiesModule.getModule().registerKeyBinding("Toggle AFK Protection", GLFW.GLFW_KEY_N, "Wynntils", KeyConflictContext.IN_GAME, true, ClientEvents::toggleAfkProtection);

        zoomInKey = CoreModule.getModule().registerKeyBinding("Zoom In", GLFW.GLFW_KEY_EQUALS, "Wynntils", KeyConflictContext.IN_GAME, false, () -> MiniMapOverlay.zoomBy(+1));

        zoomOutKey = CoreModule.getModule().registerKeyBinding("Zoom Out", GLFW.GLFW_KEY_MINUS, "Wynntils", KeyConflictContext.IN_GAME, false, () -> MiniMapOverlay.zoomBy(-1));

        CoreModule.getModule().registerKeyBinding("Cast First Spell", GLFW.GLFW_KEY_Z, "Wynntils", KeyConflictContext.IN_GAME, true, QuickCastManager::castFirstSpell);
        CoreModule.getModule().registerKeyBinding("Cast Second Spell", GLFW.GLFW_KEY_X, "Wynntils", KeyConflictContext.IN_GAME, true, QuickCastManager::castSecondSpell);
        CoreModule.getModule().registerKeyBinding("Cast Third Spell", GLFW.GLFW_KEY_C, "Wynntils", KeyConflictContext.IN_GAME, true, QuickCastManager::castThirdSpell);
        CoreModule.getModule().registerKeyBinding("Cast Fourth Spell", GLFW.GLFW_KEY_V, "Wynntils", KeyConflictContext.IN_GAME, true, QuickCastManager::castFourthSpell);

        CoreModule.getModule().registerKeyBinding("Mount Horse", GLFW.GLFW_KEY_Y, "Wynntils", KeyConflictContext.IN_GAME, true, MountHorseManager::mountHorseAndShowMessage);

        CoreModule.getModule().registerKeyBinding("Mob Totem Menu", GLFW.GLFW_KEY_J, "Wynntils", KeyConflictContext.IN_GAME, true, () -> {
            if (!Reference.onWorld) return;

            Minecraft.getInstance().player.chat("/totem");
        });

        CoreModule.getModule().registerKeyBinding("Open Ingredient Pouch", GLFW.GLFW_KEY_O, "Wynntils", KeyConflictContext.IN_GAME, true, () -> {
            if (!Reference.onWorld) return;

            ClientPlayerEntity player = Minecraft.getInstance().player;
            player.connection.send(new CClickWindowPacket(
                    player.inventoryContainer.windowId,
                    13, 0, ClickType.PICKUP, player.inventory.getItem(13),
                    player.inventoryContainer.getNextTransactionID(player.inventory)
            ));
        });

        stopwatchKey = CoreModule.getModule().registerKeyBinding("Start/Stop Stopwatch", GLFW.GLFW_KEY_NUMPAD5, "Wynntils", KeyConflictContext.IN_GAME, true, StopWatchOverlay::start);

        itemScreenshotKey = CoreModule.getModule().registerKeyBinding("Screenshot Current Item", GLFW.GLFW_KEY_F4, "Wynntils", KeyConflictContext.GUI, true, () -> {});

        // -98 for middle click
        CoreModule.getModule().registerKeyBinding("View Player's Gear", -98, "Wynntils", KeyConflictContext.IN_GAME, true, GearViewerUI::openGearViewer);

        showLevelOverlayKey = UtilitiesModule.getModule().registerKeyBinding("Show Item Level Overlay", GLFW.GLFW_KEY_LCONTROL, "Wynntils", WynntilsConflictContext.AMBIENT, true, () -> {});
    }

    public static KeyHolder getFavoriteTradeKey() {
        return favoriteTradeKey;
    }

    public static KeyHolder getLockInventoryKey() {
        return lockInventoryKey;
    }

    public static KeyHolder getCheckForUpdatesKey() {
        return checkForUpdatesKey;
    }

    public static KeyHolder getZoomInKey() {
        return zoomInKey;
    }

    public static KeyHolder getZoomOutKey() {
        return zoomOutKey;
    }

    public static KeyHolder getStopwatchKey() {
        return stopwatchKey;
    }

    public static KeyHolder getItemScreenshotKey() {
        return itemScreenshotKey;
    }

    public static KeyHolder getShowLevelOverlayKey() {
        return showLevelOverlayKey;
    }

}
