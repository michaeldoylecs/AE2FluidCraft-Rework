package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.item.ItemStack;

import appeng.core.features.registries.entries.VoidCellHandler;

public class ProxyVoidCellHandler extends VoidCellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() != null && is.getItem() instanceof ProxyVoidStorageCell;
    }
}
