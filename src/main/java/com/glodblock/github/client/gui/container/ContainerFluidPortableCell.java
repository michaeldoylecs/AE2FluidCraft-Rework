package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.item.IFluidPortableCell;

public class ContainerFluidPortableCell extends ContainerFluidMonitor {

    public ContainerFluidPortableCell(final InventoryPlayer ip, final IFluidPortableCell monitorable) {
        super(ip, monitorable, false);
        this.bindPlayerInventory(ip, 0, 0);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }

}
