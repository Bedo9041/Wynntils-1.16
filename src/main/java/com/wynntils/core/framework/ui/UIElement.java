/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.core.framework.ui;

import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.utils.objects.Position;

import java.util.Objects;

public abstract class UIElement extends ScreenRenderer {
    private int id;
    private static int topID = Integer.MIN_VALUE;
    public Position position = new Position();
    public boolean visible = true;

    public UIElement(float anchorX, float anchorY, int offsetX, int offsetY) {
        this.id = topID++;
        position.anchorX = anchorX;
        position.anchorY = anchorY;
        position.offsetX = offsetX;
        position.offsetY = offsetY;
    }

    public abstract void render(int mouseX, int mouseY);
    public abstract void tick(long ticks);

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UIElement && ((UIElement) obj).getId() == this.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, position, visible);
    }
}
