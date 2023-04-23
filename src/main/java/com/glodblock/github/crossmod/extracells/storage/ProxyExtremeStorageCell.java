package com.glodblock.github.crossmod.extracells.storage;

import net.minecraft.item.ItemStack;

public class ProxyExtremeStorageCell extends ProxyItemStorageCell {
    public ProxyExtremeStorageCell(String ec2itemName) {
        super(ec2itemName);
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 1;
    }
}
