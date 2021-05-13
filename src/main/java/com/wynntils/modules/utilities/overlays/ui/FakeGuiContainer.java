/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.overlays.ui;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;

public abstract class FakeContainerScreen extends ContainerScreen {

    public FakeContainerScreen(Container container) {
        super(container);
    }

}
