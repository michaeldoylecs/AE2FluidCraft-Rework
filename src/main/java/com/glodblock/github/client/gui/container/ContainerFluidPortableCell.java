package com.glodblock.github.client.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.util.Platform;

import com.glodblock.github.inventory.item.IFluidPortableCell;

public class ContainerFluidPortableCell extends ContainerFluidMonitor {

    private double powerMultiplier = 0.5;
    private final IFluidPortableCell civ;
    private int ticks = 0;
    private final int slot;

    public ContainerFluidPortableCell(final InventoryPlayer ip, final IFluidPortableCell monitorable) {
        super(ip, monitorable, false);
        if (monitorable != null) {
            final int slotIndex = monitorable.getInventorySlot();
            this.lockPlayerInventorySlot(slotIndex);
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }
        this.civ = monitorable;
        this.bindPlayerInventory(ip, 0, 0);
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem()
                : this.getPlayerInv().getStackInSlot(this.slot);
        if (this.civ != null) {
            if (currentItem != this.civ.getItemStack()) {
                if (currentItem != null) {
                    if (Platform.isSameItem(this.civ.getItemStack(), currentItem)) {
                        this.getPlayerInv()
                                .setInventorySlotContents(this.getPlayerInv().currentItem, this.civ.getItemStack());
                    } else {
                        this.setValidContainer(false);
                    }
                } else {
                    this.setValidContainer(false);
                }
            }
        } else {
            this.setValidContainer(false);
        }
        this.ticks++;
        if (this.ticks > 10 && this.civ != null) {
            this.civ.extractAEPower(
                    this.getPowerMultiplier() * this.ticks,
                    Actionable.MODULATE,
                    PowerMultiplier.CONFIG);
            this.ticks = 0;
        }
        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return this.powerMultiplier;
    }

    void setPowerMultiplier(final double powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }
}
