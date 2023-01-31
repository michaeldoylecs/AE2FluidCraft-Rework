package com.glodblock.github.inventory;

import net.minecraft.item.ItemStack;

import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;

public class WirelessFluidPatternTerminalPatterns extends AppEngInternalInventory {

    private final ItemStack is;

    public WirelessFluidPatternTerminalPatterns(final ItemStack is) {
        super(null, 2);
        this.is = is;
        this.readFromNBT(Platform.openNbtData(is), "pattern");
    }

    @Override
    public void markDirty() {
        this.writeToNBT(Platform.openNbtData(is), "pattern");
    }
}
