/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.core.events;

import com.wynntils.ModCore;
import com.wynntils.Reference;
import com.wynntils.core.events.custom.*;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.entities.EntityManager;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.enums.DamageType;
import com.wynntils.core.framework.enums.professions.GatheringMaterial;
import com.wynntils.core.framework.enums.professions.ProfessionType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.ActionBarData;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.interfaces.Listener;
import com.wynntils.core.utils.ItemUtils;
import com.wynntils.core.utils.Utils;
import com.wynntils.core.utils.objects.Location;
import com.wynntils.core.utils.reflections.ReflectionFields;
import com.wynntils.modules.core.instances.GatheringBake;
import com.wynntils.modules.core.instances.MainMenuButtons;
import com.wynntils.modules.core.instances.TotemTracker;
import com.wynntils.modules.core.managers.*;
import com.wynntils.modules.core.managers.GuildAndFriendManager.As;
import com.wynntils.modules.core.overlays.inventories.ChestReplacer;
import com.wynntils.modules.core.overlays.inventories.HorseReplacer;
import com.wynntils.modules.core.overlays.inventories.IngameMenuReplacer;
import com.wynntils.modules.core.overlays.inventories.InventoryReplacer;
import com.wynntils.modules.utilities.UtilitiesModule;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.HorseInventoryScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.ChatVisibility;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.core.framework.instances.PlayerInfo.get;

public class ClientEvents implements Listener {
    private final TotemTracker totemTracker = new TotemTracker();

    /**
     * This replace these GUIS into a "provided" format to make it more modular
     *
     * InventoryScreen -> InventoryReplacer
     * ChestScreen -> ChestReplacer
     * GuiScreenHorseInventory -> HorseReplacer
     * IngameMenuScreen -> IngameMenuReplacer
     *
     * Since forge doesn't provides any way to intercept these guis, like events, we need to replace them
     * this may cause conflicts with other mods that does the same thing
     *
     * @see InventoryReplacer
     * @see ChestReplacer
     * @see HorseReplacer
     * @see IngameMenuReplacer
     *
     * All of these "class replacers" emits a bunch of events that you can use to edit the selected GUI
     *
     * @param e GuiOpenEvent
     */
    @SubscribeEvent
    public void onGuiOpened(GuiOpenEvent e) {
        if (e.getGui() instanceof InventoryScreen) {
            if (e.getGui() instanceof InventoryReplacer) return;

            e.setGui(new InventoryReplacer(ModCore.mc().player));
            return;
        }
        if (e.getGui() instanceof ChestScreen) {
            if (e.getGui() instanceof ChestReplacer) return;

            e.setGui(new ChestReplacer(ModCore.mc().player.inventory, ReflectionFields.ChestScreen_lowerChestInventory.getValue(e.getGui())));
            return;
        }
        if (e.getGui() instanceof HorseInventoryScreen) {
            if (e.getGui() instanceof HorseReplacer) return;

            e.setGui(new HorseReplacer(ModCore.mc().player.inventory, ReflectionFields.GuiScreenHorseInventory_horseInventory.getValue(e.getGui()), (AbstractHorseEntity) ReflectionFields.GuiScreenHorseInventory_horseEntity.getValue(e.getGui())));
        }
        if (e.getGui() instanceof IngameMenuScreen) {
            if (e.getGui() instanceof IngameMenuReplacer) return;

            e.setGui(new IngameMenuReplacer());
        }
    }

    public static final Pattern GATHERING_STATUS = Pattern.compile("\\[\\+([0-9]*) [ⒸⒷⒿⓀ] (.*?) XP\\] \\[([0-9]*)%\\]");
    public static final Pattern GATHERING_RESOURCE = Pattern.compile("\\[\\+([0-9]+) (.+)\\]");
    public static final Pattern MOB_DAMAGE = DamageType.compileDamagePattern();

    // bake status
    private GatheringBake bakeStatus = null;

    @SubscribeEvent
    public void workAroundWynncraftNPEBug(PacketEvent<STeamsPacket> e) {
        if (e.getPacket().getAction() == 1) {
            ScorePlayerTeam scoreplayerteam;

            Scoreboard scoreboard = Minecraft.getInstance().level.getScoreboard();
            scoreplayerteam = scoreboard.getTeam(e.getPacket().getName());
            if (scoreplayerteam == null) {
                // This would cause an NPE so cancel it
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void cancelSomeVelocity(PacketEvent<SEntityVelocityPacket> e) {
        // I'm not sure what this does, but the code has been here a long time,
        // just moving it here. /magicus, 2021
        SEntityVelocityPacket velocity = e.getPacket();
        if (Minecraft.getInstance().level != null) {
            Entity entity = Minecraft.getInstance().level.getEntity(velocity.getId());
            Entity vehicle = Minecraft.getInstance().player.getLowestRidingEntity();
            if ((entity == vehicle) && (vehicle != Minecraft.getInstance().player) && (vehicle.canPassengerSteer())) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void cancelSomeMovements(PacketEvent<SMoveVehiclePacket> e) {
        // I'm not sure what this does, but the code has been here a long time,
        // just moving it here. /magicus, 2021
        SMoveVehiclePacket moveVehicle = e.getPacket();
        Entity vehicle = Minecraft.getInstance().player.getLowestRidingEntity();
        if ((vehicle == Minecraft.getInstance().player) || (!vehicle.canPassengerSteer()) || (vehicle.getDistance(moveVehicle.getX(), moveVehicle.getY(), moveVehicle.getZ()) <= 25D)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void findLabels(PacketEvent<SEntityMetadataPacket> e) {
        // makes this method always be called in main thread
        if (!Minecraft.getInstance().isCallingFromMinecraftThread()) {
            Minecraft.getInstance().submit(() -> findLabels(e));
            return;
        }

        if (e.getPacket().getDataManagerEntries() == null || e.getPacket().getDataManagerEntries().isEmpty()) return;
        Entity i = Minecraft.getInstance().level.getEntity(e.getPacket().getId());
        if (i == null) return;

        if (i instanceof EntityItemFrame) {
            ItemStack item = Utils.getItemFromMetadata(e.getPacket().getDataManagerEntries());
            if (item.hasCustomHoverName()) {
                FrameworkManager.getEventBus().post(new LocationEvent.LabelFoundEvent(item.getDisplayName(), new Location(i), i));
            }
        } else if (i instanceof LivingEntity) {
            boolean visible = Utils.isNameVisibleFromMetadata(e.getPacket().getDataManagerEntries());
            if (!visible) return;

            String value = Utils.getNameFromMetadata(e.getPacket().getDataManagerEntries());
            if (value == null || value.isEmpty()) return;

            FrameworkManager.getEventBus().post(new LocationEvent.EntityLabelFoundEvent(value, new Location(i), (LivingEntity) i));
        } else if (i instanceof ArmorStandEntity) {
            boolean visible = Utils.isNameVisibleFromMetadata(e.getPacket().getDataManagerEntries());
            if (!visible) return;

            String value = Utils.getNameFromMetadata(e.getPacket().getDataManagerEntries());
            if (value == null || value.isEmpty()) return;

            FrameworkManager.getEventBus().post(new LocationEvent.LabelFoundEvent(value, new Location(i), i));
        }
    }

    @SubscribeEvent
    public void checkGatherLabelFound(LocationEvent.LabelFoundEvent event) {
        String value = event.getLabel();
        Location loc = event.getLocation();
        Entity i = event.getEntity();

        if (value.contains("Combat") || value.contains("Guild")) return;
        value = TextFormatting.stripFormatting(value);

        Matcher m = GATHERING_STATUS.matcher(value);
        if (m.matches()) { // first, gathering status
            if (bakeStatus == null || bakeStatus.isInvalid()) bakeStatus = new GatheringBake();

            bakeStatus.setXpAmount(Double.parseDouble(m.group(1)));
            bakeStatus.setType(ProfessionType.valueOf(m.group(2).toUpperCase()));
            bakeStatus.setXpPercentage(Double.parseDouble(m.group(3)));
        } else if ((m = GATHERING_RESOURCE.matcher(value)).matches()) { // second, gathering resource
            if (bakeStatus == null || bakeStatus.isInvalid()) bakeStatus = new GatheringBake();

            String resourceType = m.group(2).contains(" ") ? m.group(2).split(" ")[0] : m.group(2);

            bakeStatus.setMaterialAmount(Integer.parseInt(m.group(1)));
            bakeStatus.setMaterial(GatheringMaterial.valueOf(resourceType.toUpperCase()));
        } else {
            return;
        }

        if (bakeStatus == null || !bakeStatus.isReady()) return;

        // this tries to find a valid barrier that is the center of the three
        // below the center block there's a barrier, which is what we are looking for
        // it ALWAYS have 4 blocks at it sides that we use to detect it
        if (bakeStatus.getType() == ProfessionType.WOODCUTTING) {
            Iterable<BlockPos> positions = BlockPos.betweenClosed(
                    i.getPosition().subtract(new Vector3i(-5, -3, -5)),
                    i.getPosition().subtract(new Vector3i(+5, +3, +5)));

            for (BlockPos position : positions) {
                if (i.world.isAirBlock(position)) continue;

                BlockState b = i.world.getBlockState(position);
                if (b.getMaterial() == Material.AIR || b.getMaterial() != Material.BARRIER) continue;

                // checks if the barrier have blocks around itself
                BlockPos north = position.north();
                if (i.world.isAirBlock(position.north())
                        || i.world.isAirBlock(position.south())
                        || i.world.isAirBlock(position.east())
                        || i.world.isAirBlock(position.west())) continue;

                loc = new Location(position);
                break;
            }
        }

        FrameworkManager.getEventBus().post(
                new GameEvent.ResourceGather(bakeStatus.getType(), bakeStatus.getMaterial(),
                        bakeStatus.getMaterialAmount(), bakeStatus.getXpAmount(), bakeStatus.getXpPercentage(), loc)
        );

        bakeStatus = null;
    }

    @SubscribeEvent
    public void checkDamageLabelFound(LocationEvent.LabelFoundEvent event) {
        String value = TextFormatting.stripFormatting(event.getLabel());
        Entity i = event.getEntity();
        Map<DamageType, Integer> damageList = new HashMap<>();

        if (value.contains("Combat") || value.contains("Guild")) return;

        Matcher m = MOB_DAMAGE.matcher(value);
        while (m.find()) {
            damageList.put(DamageType.fromSymbol(m.group(2)), Integer.valueOf(m.group(1)));
        }

        if (damageList.isEmpty()) return;

        FrameworkManager.getEventBus().post(new GameEvent.DamageEntity(damageList, i));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void updateActionBar(PacketEvent<SChatPacket> e) {
        if (!Reference.onServer || e.getPacket().getType() != ChatType.GAME_INFO) return;

        PlayerInfo.get(ActionBarData.class).updateActionBar(e.getPacket().getChatComponent().getString());
        if (UtilitiesModule.getModule().getActionBarOverlay().active) e.setCanceled(true); // only disable when the wynntils action bar is enabled
    }

    @SubscribeEvent
    public void updateChatVisibility(PacketEvent<CClientSettingsPacket> e) {
        if (e.getPacket().getChatVisibility() != ChatVisibility.HIDDEN) return;

        ReflectionFields.CClientSettingsPacket_chatVisibility.setValue(e.getPacket(), ChatVisibility.FULL);
    }

    /**
     * Process the packet queue if the queue is not empty
     */
    @SubscribeEvent
    public void processPacketQueue(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !PacketQueue.hasQueuedPacket()) return;

        PingManager.calculatePing();
        PacketQueue.proccessQueue();
    }

    /**
     * Renders and process fake entities
     */
    @SubscribeEvent
    public void processFakeEntities(RenderWorldLastEvent e) {
        EntityManager.renderEntities(e.getPartialTicks(), e.getContext());
    }

    @SubscribeEvent
    public void processFakeEntitiesTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        EntityManager.tickEntities();
    }

    Screen lastScreen = null;

    /**
     *  Register the new Main Menu buttons
     */
    @SubscribeEvent
    public void addMainMenuButtons(GuiScreenEvent.InitGuiEvent.Post e) {
        Screen gui = e.getGui();

        if (gui instanceof GuiMainMenu) {
            boolean resize = lastScreen != null && lastScreen instanceof GuiMainMenu;
            MainMenuButtons.addButtons((GuiMainMenu) gui, e.getButtonList(), resize);
        }

        lastScreen = gui;
    }

    /**
     *  Handles the main menu new buttons actions
     */
    @SubscribeEvent
    public void mainMenuActionPerformed(GuiScreenEvent.ActionPerformedEvent.Post e) {
        Screen gui = e.getGui();
        if (gui != gui.mc.screen || !(gui instanceof GuiMainMenu)) return;

        MainMenuButtons.actionPerformed((GuiMainMenu) gui, e.getButton(), e.getButtonList());
    }

    @SubscribeEvent
    public void joinGuild(WynnSocialEvent.Guild.Join e) {
        GuildAndFriendManager.changePlayer(e.getMember(), true, As.GUILD, true);
    }

    @SubscribeEvent
    public void leaveGuild(WynnSocialEvent.Guild.Leave e) {
        GuildAndFriendManager.changePlayer(e.getMember(), false, As.GUILD, true);
    }

    /**
     * Detects the user class based on the class selection GUI
     * This detection happens when the user click on an item that contains the class name pattern, inside the class selection GUI
     *
     * @param e Represents the click event
     */
    @SubscribeEvent
    public void changeClass(GuiOverlapEvent.ChestOverlap.HandleMouseClick e) {
        if (!e.getGui().getLowerInv().getName().contains("Select a Class")) return;

        if (e.getMouseButton() != 0
            || e.getSlotIn() == null
            || !e.getSlotIn().hasItem()
            || !e.getSlotIn().getItem().hasCustomHoverName()
            || !e.getSlotIn().getItem().getDisplayName().contains("[>] Select")) return;


        get(CharacterData.class).setClassId(e.getSlotId());

        String classLore = ItemUtils.getLore(e.getSlotIn().getItem()).get(1);
        String className = classLore.substring(classLore.indexOf(TextFormatting.WHITE.toString()) + 2);

        ClassType selectedClass = ClassType.fromName(className);
        boolean selectedClassIsReskinned = ClassType.isReskinned(className);

        get(CharacterData.class).updatePlayerClass(selectedClass, selectedClassIsReskinned);
    }

    @SubscribeEvent
    public void addFriend(WynnSocialEvent.FriendList.Add e) {
        Collection<String> newFriends = e.getMembers();
        if (e.isSingular) {
            // Single friend added
            for (String name : newFriends) {
                GuildAndFriendManager.changePlayer(name, true, As.FRIEND, true);
            }
            return;
        }

        // Friends list updated
        for (String name : newFriends) {
            GuildAndFriendManager.changePlayer(name, true, As.FRIEND, false);
        }

        GuildAndFriendManager.tryResolveNames();
    }

    @SubscribeEvent
    public void removeFriend(WynnSocialEvent.FriendList.Remove e) {
        Collection<String> removedFriends = e.getMembers();
        if (e.isSingular) {
            // Single friend removed
            for (String name : removedFriends) {
                GuildAndFriendManager.changePlayer(name, false, As.FRIEND, true);
            }
            return;
        }

        // Friends list updated; Socket managed in addFriend
        for (String name : removedFriends) {
            GuildAndFriendManager.changePlayer(name, false, As.FRIEND, false);
        }

        GuildAndFriendManager.tryResolveNames();
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        PlayerEntityManager.onWorldLoad(e.getWorld());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        PlayerEntityManager.onWorldUnload();
    }

    @SubscribeEvent
    public void entityJoin(EntityJoinWorldEvent e) {
        if (!(e.getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) e.getEntity();
        if (player.getGameProfile() == null) return;

        String name = player.getGameProfile().getName();
        if (name.contains("\u0001") || name.contains("§")) return; // avoid player npcs

        UserManager.loadUser(e.getEntity().getUniqueID());
    }

    @SubscribeEvent
    public void onTotemSpawn(PacketEvent<SSpawnObjectPacket> e) {
        totemTracker.onTotemSpawn(e);
    }

    @SubscribeEvent
    public void onTotemSpellCast(SpellEvent.Cast e) {
        totemTracker.onTotemSpellCast(e);
    }

    @SubscribeEvent
    public void onTotemTeleport(PacketEvent<SEntityTeleportPacket> e) {
        totemTracker.onTotemTeleport(e);
    }

    @SubscribeEvent
    public void onTotemRename(PacketEvent<SEntityMetadataPacket> e) {
        totemTracker.onTotemRename(e);
    }

    @SubscribeEvent
    public void onTotemDestroy(PacketEvent<SDestroyEntitiesPacket> e) {
        totemTracker.onTotemDestroy(e);
    }

    @SubscribeEvent
    public void onTotemClassChange(WynnClassChangeEvent e) {
        totemTracker.onTotemClassChange(e);
    }

    @SubscribeEvent
    public void onWeaponChange(PacketEvent<CHeldItemChangePacket> e) {
        totemTracker.onWeaponChange(e);
    }

}
