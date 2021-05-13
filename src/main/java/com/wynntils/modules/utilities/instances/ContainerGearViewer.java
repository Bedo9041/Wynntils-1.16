/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.modules.utilities.instances;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class ContainerGearViewer extends Container {

    private final IInventory inventory;

    public ContainerGearViewer(IInventory inventory, PlayerEntity player) {
        this.inventory = inventory;
        inventory.openInventory(player);

        // add armor slots
        for (int i = 0; i < 4; i++) {
            int index = 3 - i;
            this.addSlotToContainer(new Slot(inventory, i, 8, 8 + i * 18) {
                    @Nullable
                    @SideOnly(Side.CLIENT)
                    public String getSlotTexture() {
                        return ItemArmor.EMPTY_SLOT_NAMES[index];
                    }
            });
        }

        // add weapon slot
        this.addSlotToContainer(new Slot(inventory, 4, 77, 62) {
            @Nullable
            @SideOnly(Side.CLIENT)
            public String getSlotTexture() {
                return "minecraft:items/empty_armor_slot_shield";
            }
        });
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.inventory.closeInventory(playerIn);
    }

}
