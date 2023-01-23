package com.glodblock.github.inventory;

import appeng.api.config.FuzzyMode;
import appeng.api.config.InsertionMode;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PLiquids;
import appeng.tile.misc.TileInterface;
import appeng.util.InventoryAdaptor;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.ItemSlot;
import cofh.api.transport.IItemDuct;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidExportBus;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.Util;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidConvertingInventoryAdaptor extends InventoryAdaptor {

    public FluidConvertingInventoryAdaptor(
            @Nullable InventoryAdaptor invItems,
            @Nullable IFluidHandler invFluids,
            ForgeDirection facing,
            BlockPos pos,
            boolean isOnmi,
            Object eioConduct) {
        this.invItems = invItems;
        this.invFluids = invFluids;
        this.side = facing;
        this.posInterface = pos;
        this.eioDuct = eioConduct;
        this.onmi = isOnmi;
    }

    private final InventoryAdaptor invItems;
    private final IFluidHandler invFluids;
    private final ForgeDirection side;
    private final BlockPos posInterface;
    private final Object eioDuct;
    private final boolean onmi;

    public static InventoryAdaptor wrap(TileEntity capProvider, ForgeDirection face) {
        // sometimes i wish 1.7.10 has cap system.
        TileEntity inter = capProvider
                .getWorldObj()
                .getTileEntity(
                        capProvider.xCoord + face.offsetX,
                        capProvider.yCoord + face.offsetY,
                        capProvider.zCoord + face.offsetZ);
        if (!Config.noFluidPacket
                && !(inter instanceof TileFluidInterface
                        || Util.getPart(inter, face.getOpposite()) instanceof PartFluidInterface
                        || Util.getPart(inter, face.getOpposite()) instanceof PartFluidExportBus))
            return InventoryAdaptor.getAdaptor(capProvider, face);
        InventoryAdaptor item = InventoryAdaptor.getAdaptor(capProvider, face);
        IFluidHandler fluid = capProvider instanceof IFluidHandler ? (IFluidHandler) capProvider : null;
        boolean onmi = false;
        if (inter instanceof TileInterface) {
            onmi = ((TileInterface) inter).getTargets().size() > 1;
        }
        Object conduct = null;
        if (ModAndClassUtil.COFH && capProvider instanceof IItemDuct) {
            conduct = capProvider;
        }
        return new FluidConvertingInventoryAdaptor(item, fluid, face, new BlockPos(inter), onmi, conduct);
    }

    public ItemStack addItems(ItemStack toBeAdded, InsertionMode insertionMode) {
        FluidStack fluid = Util.getFluidFromVirtual(toBeAdded);
        if (fluid != null) {
            if (invFluids != null) {
                if (invFluids.canFill(side, fluid.getFluid())) {
                    int filled = invFluids.fill(side, fluid, true);
                    if (filled > 0) {
                        fluid.amount -= filled;
                        return ItemFluidPacket.newStack(fluid);
                    }
                }
            }
            return toBeAdded;
        }
        if (eioDuct != null) {
            return ((IItemDuct) eioDuct).insertItem(side, toBeAdded);
        }
        return invItems != null ? invItems.addItems(toBeAdded, insertionMode) : toBeAdded;
    }

    @Override
    public ItemStack addItems(ItemStack toBeAdded) {
        return addItems(toBeAdded, InsertionMode.DEFAULT);
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated) {
        return simulateAdd(toBeSimulated, InsertionMode.DEFAULT);
    }

    @Override
    public ItemStack simulateAdd(ItemStack toBeSimulated, InsertionMode insertionMode) {
        FluidStack fluid = Util.getFluidFromVirtual(toBeSimulated);
        if (fluid != null) {
            if (onmi) {
                boolean sus = false;
                for (ForgeDirection dir : ForgeDirection.values()) {
                    TileEntity te = posInterface.getOffSet(dir).getTileEntity();
                    if (te instanceof IFluidHandler) {
                        int filled = ((IFluidHandler) te).fill(dir.getOpposite(), fluid, false);
                        if (filled > 0) {
                            sus = true;
                            break;
                        }
                    }
                }
                return sus ? null : toBeSimulated;
            }
            if (invFluids != null) {
                int filled = invFluids.fill(side, fluid, false);
                if (filled > 0) {
                    fluid.amount -= filled;
                    return ItemFluidPacket.newStack(fluid);
                }
            }
            return toBeSimulated;
        }
        // Assert EIO conduct can hold all item, as it is the origin practice in AE2
        if (eioDuct != null) {
            return null;
        }
        return invItems != null ? invItems.simulateAdd(toBeSimulated, insertionMode) : toBeSimulated;
    }

    @Override
    public ItemStack removeItems(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.removeItems(amount, filter, destination) : null;
    }

    @Override
    public ItemStack simulateRemove(int amount, ItemStack filter, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateRemove(amount, filter, destination) : null;
    }

    @Override
    public ItemStack removeSimilarItems(
            int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.removeSimilarItems(amount, filter, fuzzyMode, destination) : null;
    }

    @Override
    public ItemStack simulateSimilarRemove(
            int amount, ItemStack filter, FuzzyMode fuzzyMode, IInventoryDestination destination) {
        return invItems != null ? invItems.simulateSimilarRemove(amount, filter, fuzzyMode, destination) : null;
    }

    @Override
    public boolean containsItems() {
        if (invFluids == null && invItems == null) {
            // If this entity doesn't have fluid or item inventory, we just view it as full of things.
            return true;
        }
        if (invFluids != null && invFluids.getTankInfo(this.side) != null) {
            List<FluidTankInfo[]> tankInfos = new LinkedList<>();
            if (Util.getPart(invFluids, this.side) instanceof PartP2PLiquids) {
                // read other ends of p2p for blocking mode
                PartP2PLiquids invFluidsP2P = (PartP2PLiquids) Util.getPart(invFluids, this.side);
                try {
                    Iterator<PartP2PLiquids> it = invFluidsP2P.getOutputs().iterator();
                    boolean checkedInput = false;
                    while (it.hasNext() || !checkedInput) {
                        PartP2PLiquids p2p;
                        if (it.hasNext()) {
                            p2p = it.next();
                        } else {
                            p2p = invFluidsP2P.getInput();
                            checkedInput = true;
                        }
                        if (p2p == invFluidsP2P || p2p == null) continue;
                        IFluidHandler target = Ae2Reflect.getP2PLiquidTarget(p2p);
                        if (target == null) continue;
                        tankInfos.add(target.getTankInfo(p2p.getSide().getOpposite()));
                    }
                } catch (GridAccessException ignore) {
                }
            } else {
                tankInfos.add(invFluids.getTankInfo(this.side));
            }
            boolean hasTank = false;
            for (FluidTankInfo[] tankInfoArray : tankInfos) {
                for (FluidTankInfo tank : tankInfoArray) {
                    hasTank = true;
                    FluidStack fluid = tank.fluid;
                    if (fluid != null && fluid.amount > 0) {
                        return true;
                    }
                }
            }
            if (!hasTank && invItems == null) {
                // If this entity doesn't have fluid or item inventory, we just view it as full of things.
                return true;
            }
        }
        return invItems != null && invItems.containsItems();
    }

    public boolean hasSlots() {
        return (invFluids != null && invFluids.getTankInfo(side).length > 0) || (invItems != null);
    }

    @Override
    public Iterator<ItemSlot> iterator() {
        return new SlotIterator(
                invFluids != null ? invFluids.getTankInfo(side) : new FluidTankInfo[0],
                invItems != null ? invItems.iterator() : Collections.emptyIterator());
    }

    private static class SlotIterator implements Iterator<ItemSlot> {

        private final FluidTankInfo[] tanks;
        private final Iterator<ItemSlot> itemSlots;
        private int nextSlotIndex = 0;

        SlotIterator(FluidTankInfo[] tanks, Iterator<ItemSlot> itemSlots) {
            this.tanks = tanks;
            this.itemSlots = itemSlots;
        }

        @Override
        public boolean hasNext() {
            return nextSlotIndex < tanks.length || itemSlots.hasNext();
        }

        @Override
        public ItemSlot next() {
            if (nextSlotIndex < tanks.length) {
                FluidStack fluid = tanks[nextSlotIndex].fluid;
                ItemSlot slot = new ItemSlot();
                slot.setSlot(nextSlotIndex++);
                slot.setItemStack(fluid != null ? ItemFluidPacket.newStack(fluid) : null);
                Ae2Reflect.setItemSlotExtractable(slot, false);
                return slot;
            } else {
                ItemSlot slot = itemSlots.next();
                slot.setSlot(nextSlotIndex++);
                return slot;
            }
        }
    }
}
