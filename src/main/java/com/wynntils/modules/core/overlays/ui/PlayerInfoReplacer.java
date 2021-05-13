/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.core.overlays.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.wynntils.core.events.custom.GuiOverlapEvent;
import com.wynntils.core.framework.FrameworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.client.gui.overlay.PlayerTabOverlayGui;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

import javax.annotation.Nullable;

public class PlayerInfoReplacer extends PlayerTabOverlayGui {

    public PlayerInfoReplacer(Minecraft mcIn, IngameGui guiIngameIn) {
        super(mcIn, guiIngameIn);
    }

    @Override
    public void render(MatrixStack matrix, int width, Scoreboard scoreboardIn, @Nullable ScoreObjective scoreObjectiveIn) {
        if (FrameworkManager.getEventBus().post(new GuiOverlapEvent.PlayerInfoOverlap.RenderList(this))) return;

        super.render(matrix, width, scoreboardIn, scoreObjectiveIn);
    }

}
