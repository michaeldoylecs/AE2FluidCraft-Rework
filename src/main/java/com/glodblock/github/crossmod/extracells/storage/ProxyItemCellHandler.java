package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.item.ItemStack;

import appeng.core.features.registries.entries.BasicCellHandler;

public class ProxyItemCellHandler extends BasicCellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() != null && is.getItem() instanceof ProxyItemStorageCell;
    }
}
