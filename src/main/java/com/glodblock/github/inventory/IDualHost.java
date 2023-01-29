package com.glodblock.github.inventory;

import net.minecraftforge.fluids.IFluidHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.tile.inventory.AppEngInternalAEInventory;

import com.glodblock.github.util.DualityFluidInterface;

public interface IDualHost extends IFluidHandler, IAEFluidInventory {

    DualityFluidInterface getDualityFluid();

    AppEngInternalAEInventory getConfig();

    void setConfig(int id, IAEFluidStack fluid);

    void setFluidInv(int id, IAEFluidStack fluid);
}
