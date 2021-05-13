/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.instances;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.PacketEvent;
import com.wynntils.core.events.custom.SpellEvent;
import com.wynntils.core.events.custom.WynnClassChangeEvent;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Location;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraftforge.eventbus.api.Event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class TotemTracker {
    private static final Pattern SHAMAN_TOTEM_TIMER = Pattern.compile("^§c([0-9][0-9]?)s$");
    private static final Pattern MOB_TOTEM_NAME = Pattern.compile("^§f§l(.*)'s§6§l Mob Totem$");
    private static final Pattern MOB_TOTEM_TIMER = Pattern.compile("^§c§l([0-9]+):([0-9]+)$");

    public enum TotemState { NONE, SUMMONED, LANDING, PREPARING, ACTIVE}
    private TotemState totemState = TotemState.NONE;

    private int totemId = -1;
    private double totemX, totemY, totemZ;
    private int totemTime = -1;

    private long totemCastTimestamp = 0;
    private long totemCreatedTimestamp = Long.MAX_VALUE;
    private long totemPreparedTimestamp = 0;

    private int potentialId = -1;
    private double potentialX, potentialY, potentialZ;

    private int heldWeaponSlot = -1;

    Map<Integer, MobTotem> mobTotemUnstarted = new HashMap<>();
    Map<Integer, MobTotem> mobTotemStarted = new HashMap<>();

    private int bufferedId = -1;
    private double bufferedX = -1;
    private double bufferedY = -1;
    private double bufferedZ = -1;

    private static boolean isClose(double a, double b)
    {
        double diff = Math.abs(a - b);
        return  (diff < 3);
    }

    private void postEvent(Event event) {
        ModCore.mc().submit(() -> FrameworkManager.getEventBus().post(event));
    }

    private Entity getBufferedEntity(int entityId) {
        Entity entity = ModCore.mc().level.getEntity(entityId);
        if (entity != null) return entity;

        if (entityId == bufferedId) {
            return new ArmorStandEntity(ModCore.mc().level, bufferedX, bufferedY, bufferedZ);
        }

        return null;
    }

    private void updateTotemPosition(double x, double y, double z) {
        totemX = x;
        totemY = y;
        totemZ = z;
    }

    private void checkTotemSummoned() {
        // Check if we have both creation and spell cast at roughly the same time
        if (Math.abs(totemCreatedTimestamp - totemCastTimestamp) < 500) {
            // If we have an active totem already, first remove that one
            removeTotem(true);
            totemId = potentialId;
            totemTime = -1;

            updateTotemPosition(potentialX, potentialY, potentialZ);
            totemState = TotemState.SUMMONED;
            postEvent(new SpellEvent.TotemSummoned());
        }
    }

    private void removeTotem(boolean forcefullyRemoved) {
        if (totemState != TotemState.NONE) {
            totemState = TotemState.NONE;
            heldWeaponSlot = -1;
            totemId = -1;
            totemTime = -1;
            totemX = 0;
            totemY = 0;
            totemZ = 0;
            postEvent(new SpellEvent.TotemRemoved(forcefullyRemoved));
        }
    }

    private void removeAllMobTotems() {
        for (MobTotem mobTotem : mobTotemStarted.values()) {
            postEvent(new SpellEvent.MobTotemRemoved(mobTotem));
        }
        mobTotemUnstarted.clear();
        mobTotemStarted.clear();
    }

    public void onTotemSpawn(PacketEvent<SSpawnObjectPacket> e) {
        if (!Reference.onWorld) return;

        if (e.getPacket().getType() == EntityType.ARMOR_STAND) {
            bufferedId = e.getPacket().getId();
            bufferedX = e.getPacket().getX();
            bufferedY = e.getPacket().getY();
            bufferedZ = e.getPacket().getZ();

            if (e.getPacket().getId() == totemId && totemState == TotemState.SUMMONED) {
                // Totems respawn with the same entityID when landing.
                // Update with more precise coordinates
                updateTotemPosition(e.getPacket().getX(), e.getPacket().getY(), e.getPacket().getZ());
                totemState = TotemState.LANDING;
                return;
            }

            // Is it created close to us? Then it's a potential new totem
            if (isClose(e.getPacket().getX(), Minecraft.getInstance().player.getX()) &&
                    isClose(e.getPacket().getY(), Minecraft.getInstance().player.getY() + 1.0) &&
                    isClose(e.getPacket().getZ(), Minecraft.getInstance().player.getZ())) {
                potentialId = e.getPacket().getId();
                potentialX = e.getPacket().getX();
                potentialY = e.getPacket().getY();
                potentialZ = e.getPacket().getZ();
                totemCreatedTimestamp = System.currentTimeMillis();
                checkTotemSummoned();
            }
        }
    }

    public void onTotemSpellCast(SpellEvent.Cast e) {
        if (e.getSpell().equals("Totem") || e.getSpell().equals("Sky Emblem")) {
            totemCastTimestamp = System.currentTimeMillis();
            heldWeaponSlot =  Minecraft.getInstance().player.inventory.selected;
            checkTotemSummoned();
        } else if (e.getSpell().equals("Uproot") || e.getSpell().equals("Gale Funnel")) {
            totemCastTimestamp = System.currentTimeMillis();
        }
    }

    public void onTotemTeleport(PacketEvent<SEntityTeleportPacket> e) {
        if (!Reference.onWorld) return;

        int thisId = e.getPacket().getId();
        if (thisId == totemId) {
            if (totemState == TotemState.SUMMONED || totemState == TotemState.LANDING) {
                // Now the totem has gotten it's final coordinates
                updateTotemPosition(e.getPacket().getX(), e.getPacket().getY(), e.getPacket().getZ());
                totemState = TotemState.PREPARING;
                totemPreparedTimestamp = System.currentTimeMillis();
            }
            if (totemState == TotemState.ACTIVE) {
                // Uproot; update our location
                updateTotemPosition(e.getPacket().getX(), e.getPacket().getY(), e.getPacket().getZ());
            }
        }
    }

    public void onTotemRename(PacketEvent<SEntityMetadataPacket> e) {
        if (!Reference.onWorld) return;

        String name = Utils.getNameFromMetadata(e.getPacket().getUnpackedData());
        if (name == null || name.isEmpty()) return;

        Entity entity = getBufferedEntity(e.getPacket().getId());
        if (!(entity instanceof ArmorStandEntity)) return;

        if (totemState == TotemState.PREPARING || totemState == TotemState.ACTIVE) {
            Matcher m = SHAMAN_TOTEM_TIMER.matcher(name);
            if (m.find()) {
                // We got a armor stand with a timer nametag
                if (totemState == TotemState.PREPARING ) {
                    // Widen search range until found
                    double acceptableDistance = 3.0 + (System.currentTimeMillis() - totemPreparedTimestamp)/1000d;
                    double distanceXZ = Math.abs(entity.getX() - totemX) + Math.abs(entity.getZ() - totemZ);
                    if (distanceXZ < acceptableDistance && entity.getY() <= (totemY + 2.0 + (acceptableDistance/3.0)) && entity.getY() >= ((totemY + 2.0))) {
                        // Update totem location if it was too far away
                        totemX = entity.getX();
                        totemY = entity.getY() - 2.0;
                        totemZ = entity.getZ();
                    }
                }

                double distanceXZ = Math.abs(entity.getX() - totemX) + Math.abs(entity.getZ() - totemZ);
                if (distanceXZ < 1.0 && entity.getY() <= (totemY + 3.0) && entity.getY() >= ((totemY + 2.0))) {
                    // ... and it's close to our totem; regard this as our timer
                    int time = Integer.parseInt(m.group(1));

                    if (totemTime == -1) {
                        totemTime = time;
                        totemState = TotemState.ACTIVE;
                        postEvent(new SpellEvent.TotemActivated(totemTime, new Location(totemX, totemY, totemZ)));
                    } else if (time != totemTime) {
                        if (time > totemTime) {
                            // Timer restarted using uproot
                            postEvent(new SpellEvent.TotemRenewed(time, new Location(totemX, totemY, totemZ)));
                        }
                        totemTime = time;
                    }
                }
                return;
            }
        }

        Matcher m2 = MOB_TOTEM_NAME.matcher(name);
        if (m2.find()) {
            int mobTotemId = e.getPacket().getId();

            MobTotem mobTotem = new MobTotem(mobTotemId,
                    new Location(entity.getX(), entity.getY() - 4.5, entity.getZ()), m2.group(1));

            mobTotemUnstarted.put(mobTotemId, mobTotem);
            return;
        }

        for (MobTotem mobTotem : mobTotemUnstarted.values()) {
            if (entity.getX() == mobTotem.getLocation().getX() && entity.getZ() == mobTotem.getLocation().getZ()
                    && entity.getY() == mobTotem.getLocation().getY() + 4.7) {
                Matcher m3 = MOB_TOTEM_TIMER.matcher(name);
                if (m3.find()) {
                    int minutes = Integer.parseInt(m3.group(1));
                    int seconds = Integer.parseInt(m3.group(2));

                    mobTotemStarted.put(mobTotem.totemId, mobTotem);
                    mobTotemUnstarted.remove(mobTotem.totemId);

                    postEvent(new SpellEvent.MobTotemActivated(mobTotem, minutes * 60 + seconds + 1));
                    return;
                }
            }
        }

    }

    public void onTotemDestroy(PacketEvent<SDestroyEntitiesPacket> e) {
        if (!Reference.onWorld) return;

        IntStream entityIDs = Arrays.stream(e.getPacket().getIds());
        if (entityIDs.filter(id -> id == totemId).findFirst().isPresent()) {
            if (totemState == TotemState.ACTIVE && totemTime == 0) {
                removeTotem(false);
            }
        }

        for (int id : e.getPacket().getIds()) {
            mobTotemUnstarted.remove(id);
            MobTotem mobTotem = mobTotemStarted.get(id);
            if (mobTotem == null) continue;
            mobTotemStarted.remove(id);

            postEvent(new SpellEvent.MobTotemRemoved(mobTotem));
        }
    }

    public void onTotemClassChange(WynnClassChangeEvent e) {
        removeTotem(true);
        removeAllMobTotems();
    }

    public void onWeaponChange(PacketEvent<CHeldItemChangePacket> e) {
        if (!Reference.onWorld) return;

        if (e.getPacket().getSlot() != heldWeaponSlot) {
            removeTotem(true);
        }
    }

    public static class MobTotem {
        private final int totemId;
        private final Location location;
        private final String owner;

        public MobTotem(int totemId, Location location, String owner) {
            this.totemId = totemId;
            this.location = location;
            this.owner = owner;
        }

        public int getTotemId() {
            return totemId;
        }

        public Location getLocation() {
            return location;
        }

        public String getOwner() {
            return owner;
        }

        @Override
        public String toString() {
            return "Mob Totem (" + owner + ") at " + location;
        }
    }

}
