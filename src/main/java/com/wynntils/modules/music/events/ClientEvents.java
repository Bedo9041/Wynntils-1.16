/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.music.events;

import com.wynntils.Reference;
import com.wynntils.core.events.custom.*;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.helpers.Delay;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.modules.core.enums.ToggleSetting;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.music.configs.MusicConfig;
import com.wynntils.modules.music.managers.AreaTrackManager;
import com.wynntils.modules.music.managers.BossTrackManager;
import com.wynntils.modules.music.managers.SoundTrackManager;
import com.wynntils.modules.utilities.overlays.hud.WarTimerOverlay;
import com.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class ClientEvents implements Listener {

    // player ticking
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (!MusicConfig.INSTANCE.enabled || e.phase == TickEvent.Phase.START) return;

        SoundTrackManager.getPlayer().update();
    }

    @SubscribeEvent
    public void serverLeft(WynncraftServerEvent.Leave e) {
        SoundTrackManager.getPlayer().stop();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void worldLeft(WynnWorldEvent.Leave e) {
        SoundTrackManager.getPlayer().stop();
    }

    // character selection
    @SubscribeEvent
    public void characterChange(WynnClassChangeEvent e) {
        if (e.getNewClass() == ClassType.NONE && Reference.onWorld) return; // character selection

        // Toggle wynncraft music off if wynntils music replacer is enabled
        if (MusicConfig.INSTANCE.replaceJukebox && MusicConfig.INSTANCE.enabled && Reference.onWorld) {
            new Delay(() -> ToggleSetting.MUSIC.set(false), 20);
        }

        SoundTrackManager.getPlayer().stop();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void openCharacterSelection(GuiOverlapEvent.ChestOverlap.InitGui e) {
        if (!MusicConfig.INSTANCE.classSelectionMusic || !e.getGui().getLowerInv().getName().contains("Select a Class")) return;

        SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("characterSelector"), true, MusicConfig.INSTANCE.characterSelectorQuiet);
    }

    // special tracks
    @SubscribeEvent
    public void dungeonTracks(PacketEvent<STitlePacket> e) {
        if (!MusicConfig.INSTANCE.replaceJukebox || e.getPacket().getType() != STitlePacket.Type.TITLE) return;

        String title = TextFormatting.stripFormatting(e.getPacket().getMessage().getFormattedText());
        String songName = WebManager.getMusicLocations().getDungeonTrack(title);
        if (songName == null) return;

        SoundTrackManager.findTrack(songName, true);
    }

    @SubscribeEvent
    public void warTrack(WarStageEvent e) {
        if (!MusicConfig.INSTANCE.replaceJukebox || e.getNewStage() != WarTimerOverlay.WarStage.WAITING_FOR_MOB_TIMER) return;

        SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("wars"), true);
    }

    // area tracks
    @SubscribeEvent
    public void areaTracks(SchedulerEvent.RegionUpdate e) {
        if (!MusicConfig.INSTANCE.replaceJukebox) return;

        Minecraft.getInstance().submit(BossTrackManager::update);

        if (BossTrackManager.isAlive()) return;
        AreaTrackManager.update(new Location(Minecraft.getInstance().player));
    }

    // mythic found sfx
    @SubscribeEvent
    public void onMythicFound(PacketEvent<SWindowItemsPacket> e) {
        if (!MusicConfig.SoundEffects.INSTANCE.mythicFound) return;
        if (Minecraft.getInstance().screen == null) return;
        if (!(Minecraft.getInstance().screen instanceof ChestReplacer)) return;

        ChestReplacer chest = (ChestReplacer) Minecraft.getInstance().screen;
        if (!chest.getLowerInv().getName().contains("Loot Chest") &&
                !chest.getLowerInv().getName().contains("Daily Rewards") &&
                !chest.getLowerInv().getName().contains("Objective Rewards")) return;

        int size = Math.min(chest.getLowerInv().getContainerSize(), e.getPacket().getItemStacks().size());
        for (int i = 0; i < size; i++) {
            ItemStack stack = e.getPacket().getItemStacks().get(i);
            if (stack.isEmpty() || !stack.hasCustomHoverName()) continue;
            if (!stack.getDisplayName().getString().contains(TextFormatting.DARK_PURPLE.toString())) continue;
            if (!stack.getDisplayName().getString().contains("Unidentified")) continue;

            SoundTrackManager.findTrack(WebManager.getMusicLocations().getEntryTrack("mythicFound"),
                    true, false, false, false, true, false);
            break;
        }
    }

}
