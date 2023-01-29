package com.glodblock.github.inventory.slot;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.container.slot.SlotRestrictedInput;

public class FCSlotRestrictedInput extends SlotRestrictedInput {

    private ItemStack valid;

    public FCSlotRestrictedInput(ItemStack valid, IInventory i, int slotIndex, int x, int y, InventoryPlayer p) {
        super(PlacableItemType.INSCRIBER_PLATE, i, slotIndex, x, y, p);
        this.valid = valid;
    }

    @Override
    public boolean isItemValid(final ItemStack i) {
        if (i.getItem().getClass().isAssignableFrom(valid.getItem().getClass())) {
            return true;
        }
        return false;
    }
}
