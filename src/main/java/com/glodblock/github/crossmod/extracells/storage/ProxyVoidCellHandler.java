package com.glodblock.github.crossmod.extracells.storage;

import appeng.core.features.registries.entries.VoidCellHandler;
import net.minecraft.item.ItemStack;

public class ProxyVoidCellHandler extends VoidCellHandler {
    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() != null && is.getItem() instanceof ProxyVoidStorageCell;
    }
}
