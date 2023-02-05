package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import appeng.tile.inventory.BiggerAppEngInventory;
import appeng.util.Platform;

import com.glodblock.github.inventory.gui.GuiType;

public class ItemBiggerAppEngInventory extends BiggerAppEngInventory {

    private final ItemStack is;
    private final String name;

    public ItemBiggerAppEngInventory(ItemStack is, String name, int size) {
        super(null, size);
        this.name = name;
        this.is = is;
        this.readFromNBT(Platform.openNbtData(is), name);
    }

    @Override
    public void markDirty() {
        boolean isCraft = Platform.openNbtData(is).getBoolean("craftingMode");
        boolean isCraftingTerm = Platform.openNbtData(is).getString("mode_main")
                .equals(GuiType.WIRELESS_CRAFTING_TERMINAL.toString());
        if (!isCraftingTerm && isCraft) {
            for (int x = 0; x < this.getSizeInventory(); x++) {
                final ItemStack is = this.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
        this.writeToNBT(Platform.openNbtData(is), this.name);
    }

}
