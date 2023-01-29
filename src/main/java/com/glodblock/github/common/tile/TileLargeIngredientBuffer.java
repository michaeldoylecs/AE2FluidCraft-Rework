package com.glodblock.github.common.tile;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.tile.inventory.AppEngInternalInventory;

import com.glodblock.github.inventory.AEFluidInventory;

public class TileLargeIngredientBuffer extends TileIngredientBuffer {

    public TileLargeIngredientBuffer() {
        this.invFluids = new AEFluidInventory(this, 7, 64000);
        this.invItems = new AppEngInternalInventory(this, 27);
    }

    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                26 };
    }

    @Override
    public AEFluidInventory getInternalFluid() {
        return this.invFluids;
    }
}
