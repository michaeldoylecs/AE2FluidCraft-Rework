package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEFluidStack;
import appeng.tile.inventory.AppEngInternalAEInventory;
import com.glodblock.github.util.DualityFluidInterface;
import net.minecraftforge.fluids.IFluidHandler;

public interface IDualHost extends IFluidHandler, IAEFluidInventory {

    DualityFluidInterface getDualityFluid();

    AppEngInternalAEInventory getConfig();

    void setConfig(int id, IAEFluidStack fluid);

    void setFluidInv(int id, IAEFluidStack fluid);
}
