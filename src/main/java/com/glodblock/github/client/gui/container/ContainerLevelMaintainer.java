package com.glodblock.github.client.gui.container;

import appeng.container.AEBaseContainer;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.AeItemStackHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class ContainerLevelMaintainer extends AEBaseContainer {
    private final TileLevelMaintainer tile;
    private final FakeSlot[] requestSlots = new FakeSlot[TileLevelMaintainer.REQ_COUNT];

    public ContainerLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(ipl, tile);
        this.tile = tile;
        AeItemStackHandler request = new AeItemStackHandler(tile.getRequestSlots());
        for (int y = 0; y < TileLevelMaintainer.REQ_COUNT; y++) {
            FakeSlot slot = new FakeSlot(request, y, 17, 19 + y * 20);
            addSlotToContainer(slot);
            requestSlots[y] = slot;
        }
        bindPlayerInventory(ipl, 0, 132);
    }

    public TileLevelMaintainer getTile() {
        return tile;
    }

    public FakeSlot[] getRequestSlots() {
        return this.requestSlots;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        for (FakeSlot slot : this.getRequestSlots()) {
            if (!slot.getHasStack()) {
                slot.setAeStack(AEItemStack.create(((Slot) this.inventorySlots.get(idx)).getStack()), true);
                break;
            }
        }
        this.detectAndSendChanges();
        return null;
    }

    public static class FakeSlot extends ContainerFluidPatternEncoder.SlotFluidConvertingFake {

        public FakeSlot(AeItemStackHandler inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }
    }
}
