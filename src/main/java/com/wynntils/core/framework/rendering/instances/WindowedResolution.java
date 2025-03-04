/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.rendering.instances;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public class WindowedResolution {

    private final int width;
    private final int height;

    private float scaleFactor = 0f;

    public WindowedResolution(int minWidth, int minHeight) {
        width = Display.getWidth();
        height = Display.getHeight();

        if (width < minWidth || height < minHeight) {
            scaleFactor = 1f;
            return;
        }

        int minecraftScale = new MainWindow(Minecraft.getInstance()).getScaleFactor();
        while (minWidth * scaleFactor < width / 2f && minHeight * scaleFactor < height/2f)  {
            scaleFactor += 0.1f;
        }

        // remove the minecraft scale factor
        scaleFactor /= minecraftScale / 2f;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

}
