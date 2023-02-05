package com.glodblock.github.inventory.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.*;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGridNode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.items.contents.WirelessTerminalViewCells;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.ConfigManager;
import appeng.util.Platform;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.ItemBiggerAppEngInventory;
import com.glodblock.github.inventory.WirelessFluidPatternTerminalPatterns;
import com.glodblock.github.util.Util;

public class WirelessPatternTerminalInventory extends MEMonitorHandler<IAEItemStack>
        implements IWirelessPatternTerminal {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;
    private final AppEngInternalInventory viewCell;
    private final StorageChannel channel;
    private final IGridNode grid;

    protected AppEngInternalInventory crafting;
    protected AppEngInternalInventory output;
    protected final WirelessFluidPatternTerminalPatterns pattern;

    protected boolean craftingMode = true;
    protected boolean substitute = false;
    protected boolean combine = false;
    protected boolean prioritize = false;
    protected boolean inverted = false;
    protected boolean beSubstitute = false;
    protected int activePage = 0;

    @SuppressWarnings("unchecked")
    public WirelessPatternTerminalInventory(final ItemStack is, final int slot, IGridNode gridNode,
            EntityPlayer player) {
        super(
                (IMEInventoryHandler<IAEItemStack>) Objects
                        .requireNonNull(Util.getWirelessInv(is, player, StorageChannel.ITEMS)));
        this.ips = (ToolWirelessTerminal) is.getItem();
        this.grid = gridNode;
        this.target = is;
        this.inventorySlot = slot;
        this.viewCell = new WirelessTerminalViewCells(is);
        this.channel = StorageChannel.ITEMS;
        this.pattern = new WirelessFluidPatternTerminalPatterns(is);
        this.crafting = new ItemBiggerAppEngInventory(is, "crafting", 9);
        this.output = new ItemBiggerAppEngInventory(is, "output", 3);
    }

    public StorageChannel getChannel() {
        return this.channel;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);
        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }
        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt));
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this;
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return null;
    }

    public void readFromNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        this.setCraftingRecipe(data.getBoolean("craftingMode"));
        this.setSubstitution(data.getBoolean("substitute"));
        this.setCombineMode(data.getBoolean("combine"));
        this.setBeSubstitute(data.getBoolean("beSubstitute"));
        this.setPrioritization(data.getBoolean("priorization"));
        this.setInverted(data.getBoolean("inverted"));
        this.setActivePage(data.getInteger("activePage"));
    }

    public void writeToNBT() {
        NBTTagCompound data = Platform.openNbtData(this.target);
        data.setBoolean("craftingMode", this.craftingMode);
        data.setBoolean("substitute", this.substitute);
        data.setBoolean("combine", this.combine);
        data.setBoolean("beSubstitute", this.beSubstitute);
        data.setBoolean("priorization", this.prioritize);
        data.setBoolean("inverted", this.inverted);
        data.setInteger("activePage", this.activePage);
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(this.target);
            manager.writeToNBT(data);
        });
        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        out.readFromNBT((NBTTagCompound) Platform.openNbtData(this.target).copy());
        this.readFromNBT();
        return out;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public IInventory getViewCellStorage() {
        return this.viewCell;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.grid;
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.NONE;
    }

    @Override
    public void securityBreak() {
        this.getGridNode(ForgeDirection.UNKNOWN).getMachine().securityBreak();
    }

    @Override
    public void saveChanges() {
        // NO-OP
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack,
            ItemStack newStack) {
        // NO-OP
    }

    private void fixCraftingRecipes() {
        if (this.craftingMode) {
            for (int x = 0; x < this.crafting.getSizeInventory(); x++) {
                final ItemStack is = this.crafting.getStackInSlot(x);
                if (is != null) {
                    is.stackSize = 1;
                }
            }
        }
    }

    @Override
    public boolean isInverted() {
        return this.inverted;
    }

    @Override
    public IInventory getInventoryByName(String name) {
        if (name.equals("crafting")) {
            return this.crafting;
        }

        if (name.equals("output")) {
            return this.output;
        }

        if (name.equals("pattern")) {
            return this.pattern;
        }

        return null;
    }

    @Override
    public boolean canBeSubstitute() {
        return this.beSubstitute;
    }

    @Override
    public boolean isPrioritize() {
        return this.prioritize;
    }

    @Override
    public boolean isSubstitution() {
        return this.substitute;
    }

    @Override
    public boolean shouldCombine() {
        return this.combine;
    }

    @Override
    public void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput) {
        IInventory crafting = this.getInventoryByName("crafting");
        IInventory output = this.getInventoryByName("output");
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            for (int x = 0; x < crafting.getSizeInventory() && x < newCrafting.length; x++) {
                final IAEItemStack item = newCrafting[x];
                crafting.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
            for (int x = 0; x < output.getSizeInventory() && x < newOutput.length; x++) {
                final IAEItemStack item = newOutput[x];
                output.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
        }
    }

    @Override
    public void setCraftingRecipe(boolean craftingMode) {
        this.craftingMode = craftingMode;
        this.fixCraftingRecipes();
    }

    @Override
    public void setSubstitution(boolean canSubstitute) {
        this.substitute = canSubstitute;
    }

    @Override
    public void setBeSubstitute(boolean canBeSubstitute) {
        this.beSubstitute = canBeSubstitute;
    }

    @Override
    public void setCombineMode(boolean shouldCombine) {
        this.combine = shouldCombine;
    }

    @Override
    public void setPrioritization(boolean canPrioritize) {
        this.prioritize = canPrioritize;
    }

    @Override
    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    @Override
    public int getActivePage() {
        return this.activePage;
    }

    @Override
    public void setActivePage(int activePage) {
        this.activePage = activePage;
    }

    @Override
    public boolean isCraftingRecipe() {
        return this.craftingMode;
    }

    @Override
    public void sortCraftingItems() {
        List<ItemStack> items = new ArrayList<ItemStack>();
        List<ItemStack> fluids = new ArrayList<ItemStack>();
        for (ItemStack is : this.crafting) {
            if (is == null) continue;
            if (is.getItem() instanceof ItemFluidPacket) {
                fluids.add(is);
            } else {
                items.add(is);
            }
        }
        if (this.prioritize) {
            fluids.addAll(items);
            items.clear();
        } else {
            items.addAll(fluids);
            fluids.clear();
        }

        for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
            if (this.crafting.getStackInSlot(i) == null) break;
            if (items.isEmpty()) {
                this.crafting.setInventorySlotContents(i, fluids.get(i));
            } else {
                this.crafting.setInventorySlotContents(i, items.get(i));
            }
        }
        saveChanges();
    }

    @Override
    public void saveSettings() {
        this.writeToNBT();
    }

    @Override
    public IGridNode getActionableNode() {
        return this.grid;
    }

}
