package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;

import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.util.Util;

public class ContainerFluidPatternExWireless extends ContainerFluidPatternTerminalEx {

    private double powerMultiplier = 0.5;
    private final IWirelessTerminal civ;
    private int ticks = 0;
    private final int slot;

    public ContainerFluidPatternExWireless(InventoryPlayer ip, IWirelessTerminal monitorable) {
        super(ip, monitorable);
        if (monitorable != null) {
            final int slotIndex = monitorable.getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.civ = monitorable;
    }

    public void detectAndSendChanges() {
        this.ticks = Util
                .drainItemPower(this, this.getPlayerInv(), this.slot, this.ticks, this.getPowerMultiplier(), this.civ);
        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }
}
