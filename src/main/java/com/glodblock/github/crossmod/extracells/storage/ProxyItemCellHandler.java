package com.glodblock.github.crossmod.extracells.storage;

import appeng.core.features.registries.entries.BasicCellHandler;
import net.minecraft.item.ItemStack;

public class ProxyItemCellHandler extends BasicCellHandler {

    @Override
    public boolean isCell(ItemStack is) {
        return is != null && is.getItem() != null && is.getItem() instanceof ProxyItemStorageCell;
    }
}
