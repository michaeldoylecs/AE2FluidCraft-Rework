package com.glodblock.github.common.storage;

import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEFluidStack;

public interface IStorageFluidCell extends ICellWorkbenchItem {

    long getBytes(ItemStack cellItem);

    int getBytesPerType(ItemStack cellItem);

    boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition);

    boolean storableInStorageCell();

    boolean isStorageCell(ItemStack i);

    double getIdleDrain();

    int getTotalTypes(ItemStack cellItem);
}
