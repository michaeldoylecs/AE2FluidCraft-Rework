package com.glodblock.github.client.gui.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.gui.GuiLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.AeItemStackHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;


public class ContainerLevelMaintainer extends AEBaseContainer {
    private final TileLevelMaintainer tile;
    private final FakeSlot[] craftingSlots = new FakeSlot[TileLevelMaintainer.REQ_COUNT];

    public ContainerLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(ipl, tile);
        this.tile = tile;
        AeItemStackHandler crafting = new AeItemStackHandler(tile.getCraftingSlots());
        for (int y = 0; y < TileLevelMaintainer.REQ_COUNT; y++) {
            FakeSlot slot = new FakeSlot(crafting, y, 17, 19 + y * 20);
            addSlotToContainer(slot);
            craftingSlots[y] = slot;
        }
        bindPlayerInventory(ipl, 0, 132);
    }

    public TileLevelMaintainer getTile() {
        return tile;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int idx) {
        for (ContainerFluidPatternEncoder.SlotFluidConvertingFake slot : craftingSlots) {
            if (!slot.getHasStack()) {
                slot.setAeStack(AEItemStack.create(((Slot) this.inventorySlots.get(idx)).getStack()), true);
                if (Platform.isClient()) {
                    GuiLevelMaintainer gui = (GuiLevelMaintainer) Minecraft.getMinecraft().currentScreen;
                    gui.updateAmount(slot.getSlotIndex(), ((Slot) this.inventorySlots.get(idx)).getStack().stackSize);
                }
                break;
            }
        }
        this.detectAndSendChanges();
        return null;
    }


    public void handleClientInteraction(int action, int idx, long size) {
        if (action == 0) {
            tile.updateQuantity(idx, size);
        } else if (action == 1) {
            tile.updateBatchSize(idx, size);
        }
    }

    @Override
    public void onSlotChange(final Slot s) {
        if (Platform.isServer()) {
            for (final Object crafter : this.crafters) {
                final ICrafting icrafting = (ICrafting) crafter;

                for (final Object g : this.inventorySlots) {
                    if (g instanceof OptionalSlotFake || g instanceof SlotFakeCraftingMatrix) {
                        final Slot sri = (Slot) g;
                        icrafting.sendSlotContents(this, sri.slotNumber, sri.getStack());
                    }
                }
                ((EntityPlayerMP) icrafting).isChangingQuantityOnly = false;
            }
            this.detectAndSendChanges();
        }
    }

    public static class FakeSlot extends ContainerFluidPatternEncoder.SlotFluidConvertingFake {

        public FakeSlot(AeItemStackHandler inv, int idx, int x, int y) {
            super(inv, idx, x, y);
        }
    }
}
