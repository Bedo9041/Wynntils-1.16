/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.modules.utilities.managers;

import com.wynntils.Reference;
import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.data.ActionBarData;
import com.wynntils.core.framework.instances.data.CharacterData;
import com.wynntils.core.framework.instances.data.SpellData;
import com.wynntils.modules.core.managers.PacketQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import static com.wynntils.core.framework.instances.data.SpellData.SPELL_LEFT;
import static com.wynntils.core.framework.instances.data.SpellData.SPELL_RIGHT;

public class QuickCastManager {

    private static final CPacketAnimation leftClick = new CPacketAnimation(EnumHand.MAIN_HAND);
    private static final CPlayerTryUseItemPacket rightClick = new CPlayerTryUseItemPacket(EnumHand.MAIN_HAND);
    private static final CPacketPlayerDigging releaseClick = new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN);

    private static final int[] spellUnlock = { 1, 11, 21, 31 };

    private static void queueSpell(int spellNumber, boolean a, boolean b, boolean c) {
        if (!canCastSpell(spellNumber)) return;

        int level = PlayerInfo.get(CharacterData.class).getLevel();
        boolean isLowLevel = level <= 11;
        Class<?> packetClass = isLowLevel ? STitlePacket.class : SChatPacket.class;
        PacketQueue.queueComplexPacket(a == SPELL_LEFT ? leftClick : rightClick, packetClass, e -> checkKey(e, 0, a, isLowLevel));
        PacketQueue.queueComplexPacket(b == SPELL_LEFT ? leftClick : rightClick, packetClass, e -> checkKey(e, 1, b, isLowLevel));
        PacketQueue.queueComplexPacket(c == SPELL_LEFT ? leftClick : rightClick, packetClass, e -> checkKey(e, 2, c, isLowLevel));
    }

    public static void castFirstSpell() {
        if (PlayerInfo.get(CharacterData.class).getCurrentClass() == ClassType.ARCHER) {
            queueSpell(1, SPELL_LEFT, SPELL_RIGHT, SPELL_LEFT);
            return;
        }

        queueSpell(1, SPELL_RIGHT, SPELL_LEFT, SPELL_RIGHT);
    }

    public static void castSecondSpell() {
        if (PlayerInfo.get(CharacterData.class).getCurrentClass() == ClassType.ARCHER) {
            queueSpell(2, SPELL_LEFT, SPELL_LEFT, SPELL_LEFT);
            return;
        }

        queueSpell(2, SPELL_RIGHT, SPELL_RIGHT, SPELL_RIGHT);
    }

    public static void castThirdSpell() {
        if (PlayerInfo.get(CharacterData.class).getCurrentClass() == ClassType.ARCHER) {
            queueSpell(3, SPELL_LEFT, SPELL_RIGHT, SPELL_RIGHT);
            return;
        }

        queueSpell(3, SPELL_RIGHT, SPELL_LEFT, SPELL_LEFT);
    }

    public static void castFourthSpell() {
        if (PlayerInfo.get(CharacterData.class).getCurrentClass() == ClassType.ARCHER) {
            queueSpell(4, SPELL_LEFT, SPELL_LEFT, SPELL_RIGHT);
            return;
        }

        queueSpell(4, SPELL_RIGHT, SPELL_RIGHT, SPELL_LEFT);
    }

    private static boolean canCastSpell(int spell) {
        if (!Reference.onWorld || !PlayerInfo.get(CharacterData.class).isLoaded()) {
            return false;
        }

        if (PlayerInfo.get(CharacterData.class).getLevel() < spellUnlock[spell - 1]) {
            Minecraft.getInstance().player.sendMessage(new StringTextComponent(
                    TextFormatting.GRAY + "You have not yet unlocked this spell! You need to be level " + spellUnlock[spell - 1]
            ));
            return false;
        }

        return true;
    }

    private static boolean checkKey(IPacket<?> input, int pos, boolean clickType, boolean isLowLevel) {
        boolean[] spell;

        SpellData data = PlayerInfo.get(SpellData.class);
        if (isLowLevel) {
            STitlePacket title = (STitlePacket) input;
            if (title.getType() != STitlePacket.Type.SUBTITLE) return false;

            spell = data.parseSpellFromTitle(title.getMessage().getFormattedText());
        } else {
            SChatPacket title = (SChatPacket) input;
            if (title.getType() != ChatType.GAME_INFO) return false;

            PlayerInfo.get(ActionBarData.class).updateActionBar(title.getChatComponent().getString());

            spell = data.getLastSpell();
        }

        return pos < spell.length && spell[pos] == clickType;
    }

}
