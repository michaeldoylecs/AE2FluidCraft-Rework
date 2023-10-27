package com.glodblock.github.coremod.registries.adapters;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;

import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.api.registries.ILevelViewableAdapter;
import com.glodblock.github.common.tile.TileLevelMaintainer.State;
import com.glodblock.github.common.tile.TileLevelMaintainer.TLMTags;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventoryImpl;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.parts.automation.PartLevelEmitter;
import appeng.util.item.AEItemStack;

public class PartLevelEmitterAdapter implements ILevelViewable, ILevelViewableAdapter {

    private PartLevelEmitter delegate;

    private static final int SLOT_IN = 0;

    public PartLevelEmitterAdapter() {}

    public ILevelViewable adapt(IGridHost gridHost) {
        if (gridHost instanceof PartLevelEmitter levelEmitter) this.delegate = levelEmitter;
        return this;
    }

    @NotNull
    static public IInventory getPatchedInventory(IInventory inventory, long quantity, State state) {
        ItemStack itemStack = inventory.getStackInSlot(SLOT_IN);
        if (itemStack == null) return inventory;
        NBTTagCompound data = !itemStack.hasTagCompound() ? new NBTTagCompound() : itemStack.getTagCompound();
        data.setLong(TLMTags.Quantity.tagName, quantity);
        data.setInteger(TLMTags.State.tagName, state.ordinal());
        itemStack.setTagCompound(data);

        AeStackInventoryImpl<IAEItemStack> inv = new AeStackInventoryImpl<>(StorageChannel.ITEMS, 1);
        inv.setStack(SLOT_IN, AEItemStack.create(itemStack));

        return new AeItemStackHandler(inv);
    }

    @Override
    public DimensionalCoord getLocation() {
        return delegate.getLocation();
    }

    @Override
    public TileEntity getTile() {
        return delegate.getTile();
    }

    @Override
    public IInventory getInventoryByName(String name) {
        long value = delegate.getReportingValue();
        State state = delegate.isProvidingStrongPower() > 0 ? State.Craft : State.Idle;

        return getPatchedInventory(delegate.getInventoryByName(name), value, state);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return delegate.getGridNode(dir);
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return delegate.getCableConnectionType(dir);
    }

    @Override
    public ForgeDirection getSide() {
        return delegate.getSide();
    }

    @Override
    public void securityBreak() {
        delegate.securityBreak();
    }

    @Override
    public String getCustomName() {
        return delegate.getCustomName();
    }

    @Override
    public boolean hasCustomName() {
        return delegate.hasCustomName();
    }

    @Override
    public void setCustomName(String name) {
        delegate.hasCustomName();
    }

    @Override
    public ItemStack getSelfItemStack() {
        return delegate.getItemStack();
    }

    @Override
    public ItemStack getDisplayItemStack() {
        return delegate.getCrafterIcon();
    }
}
