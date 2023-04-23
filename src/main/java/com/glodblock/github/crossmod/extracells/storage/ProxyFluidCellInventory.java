package com.glodblock.github.crossmod.extracells.storage;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.exceptions.AppEngException;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.storage.FluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class ProxyFluidCellInventory extends FluidCellInventory {
    private static final String EC_CONVERTED = "ec_converted";
    public ProxyFluidCellInventory(ItemStack o, ISaveProvider container) throws AppEngException {
        super(o, container);
    }

    @Override
    protected void loadCellFluids() {
        // If we've already converted the tag to AE2FC, just go to super.
        // Else, proceed with conversion
        if (!this.tagCompound.hasKey(EC_CONVERTED)) {
            if (this.cellFluids == null) {
                this.cellFluids = AEApi.instance().storage().createFluidList();
            }
            this.cellFluids.resetStatus(); // clears totals and stuff.
            // Load using Extra Cell's NBT format
            for (int i = 0; i < this.cellType.getTotalTypes(this.cellItem); i++) {
                FluidStack fs = FluidStack.loadFluidStackFromNBT(tagCompound.getCompoundTag("Fluid#" + i));
                final AEFluidStack aet = AEFluidStack.create(fs);
                if (aet != null) {
                    if (aet.getStackSize() > 0) {
                        this.cellFluids.add(aet);
                    }
                }
            }
            this.tagCompound.setBoolean(EC_CONVERTED, true);
        } else {
            super.loadCellFluids();
        }

    }
}
