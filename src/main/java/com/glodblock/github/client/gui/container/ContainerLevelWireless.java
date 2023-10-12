package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.item.IWirelessTerminal;

public class ContainerLevelWireless extends ContainerLevelTerminal {

    public ContainerLevelWireless(InventoryPlayer ip, IWirelessTerminal monitorable) {
        super(ip, monitorable);
    }

    @Override
    protected boolean isWirelessTerminal() {
        return true;
    }
}
