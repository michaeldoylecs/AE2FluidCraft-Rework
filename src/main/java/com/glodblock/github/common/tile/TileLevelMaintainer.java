package com.glodblock.github.common.tile;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.CraftingLink;
import appeng.helpers.NonNullArrayIterator;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.AeStackInventoryImpl;
import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.concurrent.Future;

public class TileLevelMaintainer extends AENetworkTile implements IAEAppEngInventory, IGridTickable, ICraftingRequester {

    public static final int REQ_COUNT = 5;
    public final InventoryRequest requests = new InventoryRequest(this);
    private final BaseActionSource source;


    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
    }

    public AeStackInventory<IAEItemStack> getRequestSlots() {
        return requests.getRequestStacks();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return ImmutableSet.copyOf(new NonNullArrayIterator<ICraftingLink>(this.requests.getLinks()));
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        try {
            if (this.getProxy().isActive()) {
                final IEnergyGrid energy = this.getProxy().getEnergy();
                final double power = Math.ceil(ItemFluidDrop.isFluidStack(items) ? items.getStackSize() / 1000D : items.getStackSize());
                if (energy.extractAEPower(power, mode, PowerMultiplier.CONFIG) > power - 0.01) {
                    if (ItemFluidDrop.isFluidStack(items)) {
                        IAEFluidStack notInserted = this.getProxy().getStorage().getFluidInventory().injectItems(ItemFluidDrop.getAeFluidStack(items), mode, this.source);
                        items.setStackSize(notInserted.getStackSize());
                        return items;
                    } else {
                        return this.getProxy().getStorage().getItemInventory().injectItems(items, mode, this.source);
                    }
                }
            }
        } catch (GridAccessException e) {
            AELog.debug(e);
        }
        return items;
    }
    @Override
    public void jobStateChange(ICraftingLink link) {
        if (this.requests.getLinks() != null) {
            for (int x = 0; x < this.requests.getLinks().length; x++) {
                if (this.requests.getLinks()[x] == link) {
                    this.requests.getLinks()[x] = link;
                    return;
                }
            }
        }
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 120, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        return this.canDoBusWork() ? this.doWork() : TickRateModulation.IDLE;
    }
    private TickRateModulation doWork() {
        if (!this.getProxy().isActive() || !this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }
        boolean didSomething = false;
        try {
            ICraftingGrid cg = this.getProxy().getCrafting();
            IGrid grid = this.getProxy().getGrid();
            final IItemList<IAEItemStack> inv = this.getProxy().getStorage().getItemInventory().getStorageList();
            for (int i = 0; i < REQ_COUNT; i++) {
                IAEItemStack is = requests.getQuantity(i);
                if (is != null && requests.getBatchSize(i) > 0) {
                    IAEItemStack craftItem = requests.getCraftItem(i);
                    if (cg.canEmitFor(craftItem) || cg.isRequesting(craftItem) ||
                        (inv.findPrecise(is) != null && inv.findPrecise(is).getStackSize() >= is.getStackSize()) || !this.requests.isDone(i)
                    )
                        continue;
                    // do crafting
                    Future<ICraftingJob> jobTask = requests.getJobs()[i];
                    if (jobTask == null) {
                        requests.getJobs()[i] = cg.beginCraftingJob(getWorldObj(), grid, this.source, craftItem, null);
                    } else if (jobTask.isDone()) {
                        try {
                            ICraftingJob job = jobTask.get();
                            if (job != null) {
                                ICraftingLink link = cg.submitJob(job, this, null, false, this.source);
                                didSomething = true;
                                requests.jobs[i] = null;
                                if (link != null) {
                                    requests.getLinks()[i] = link;
                                }
                            }
                        } catch (Exception ignored) {

                        }
                    }
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }
        return didSomething ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    public void saveChanges() {
        super.saveChanges();
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {
        try {
            getProxy().getTick().alertDevice(getProxy().getNode());
        } catch (GridAccessException e) {
            // NO-OP
        }
    }

    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    public InventoryRequest getRequestInventory() {
        return this.requests;
    }

    public IInventory getInventory() {
        return (IInventory) this.requests.getRequestStacks();
    }

    public void updateQuantity(int idx, long size) {
        this.requests.updateQuantity(idx, size);
    }

    public void updateBatchSize(int idx, long size) {
        this.requests.updateBatchSize(idx, size);
    }

    private void readLinkFromNBT(NBTTagCompound data) {
        try {
            for (int i = 0; i < this.requests.getLinks().length; i++) {
                final NBTTagCompound link = data.getCompoundTag("links-" + i);
                if (link != null && link.hasNoTags()) {
                    this.requests.getLinks()[i] = null;
                } else {
                    this.requests.getLinks()[i] = AEApi.instance().storage().loadCraftingLink(link, this);
                }
            }
        } catch (Exception e) {
            // :P You don't have job to do XD
        }
    }

    private NBTTagCompound writeLinkToNBT(NBTTagCompound data) {
        for (int i = 0; i < this.requests.getLinks().length; i++) {
            if (this.requests.getLinks()[i] != null) {
                CraftingLink link = (CraftingLink) this.requests.getLinks()[i];
                NBTTagCompound linkData = new NBTTagCompound();
                link.writeToNBT(linkData);
                data.setTag("links-" + i, linkData);
            }
        }
        return data;
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        this.requests.requestStacks.writeToNbt(data, "Inventory");
        this.requests.requestBatches.writeToNbt(data, "Batch");
        this.requests.requestQtys.writeToNbt(data, "Count");
        return writeLinkToNBT(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.requests.requestStacks.readFromNbt(data, "Inventory");
        this.requests.requestBatches.readFromNbt(data, "Batch");
        this.requests.requestQtys.readFromNbt(data, "Count");
        this.readLinkFromNBT(data);
    }

    public static class InventoryRequest {
        private final AeStackInventoryImpl<IAEItemStack> requestStacks;
        private final AeStackInventoryImpl<IAEItemStack> requestBatches;
        private final AeStackInventoryImpl<IAEItemStack> requestQtys;
        private final Future<ICraftingJob>[] jobs;
        private final ICraftingLink[] links;

        public InventoryRequest(TileLevelMaintainer tile) {
            this.requestStacks = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.requestBatches = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.requestQtys = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.jobs = new Future[REQ_COUNT];
            this.links = new ICraftingLink[REQ_COUNT];
        }

        public boolean isDone(int index) {
            if (this.getLinks()[index] != null) {
                return this.getLinks()[index].isDone() || this.getLinks()[index].isCanceled();
            }
            return true;
        }

        public Future<ICraftingJob>[] getJobs() {
            return this.jobs;
        }

        public ICraftingLink[] getLinks() {
            return links;
        }

        public AeStackInventory<IAEItemStack> getRequestQtys() {
            return this.requestQtys;
        }

        public AeStackInventory<IAEItemStack> getRequestBatches() {
            return this.requestBatches;
        }

        public AeStackInventory<IAEItemStack> getRequestStacks() {
            return this.requestStacks;
        }

        public void updateQuantity(int idx, long size) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            if (is != null) {
                IAEItemStack i = is.copy();
                i.setStackSize(size);
                this.getRequestQtys().setStack(idx, i);
            }
        }

        public void updateBatchSize(int idx, long size) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            if (is != null) {
                IAEItemStack i = is.copy();
                i.setStackSize(size);
                this.getRequestBatches().setStack(idx, i);
            }
        }

        public IAEItemStack getQuantity(int idx) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            if (is == null) return null;
            IAEItemStack qis = this.getRequestQtys().getStack(idx);
            if (qis == null || !is.isSameType(qis)) return null;
            return this.getRequestQtys().getStack(idx);
        }

        public long getBatchSize(int idx) {
            IAEItemStack is = this.getRequestStacks().getStack(idx);
            IAEItemStack is1 = this.getRequestBatches().getStack(idx);
            if (is != null && is1 != null && is.isSameType(is1)) {
                return is1.getStackSize();
            }
            return 0;
        }

        public IAEItemStack getCraftItem(int idx) {
            IAEItemStack is = this.getQuantity(idx);
            if (is != null) {
                IAEItemStack ias = is.copy();
                ias.setStackSize(this.getBatchSize(idx));
                return ias;
            }
            return null;
        }
    }
}
