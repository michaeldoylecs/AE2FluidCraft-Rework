package com.glodblock.github.common.tile;

import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.helpers.MultiCraftingTracker;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.AeStackInventoryImpl;
import com.google.common.collect.ImmutableSet;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.concurrent.Future;

public class TileLevelMaintainer extends AENetworkTile implements IAEAppEngInventory, IStackWatcherHost, IGridTickable, ICraftingRequester {

    public static final int REQ_COUNT = 5;
    public final InventoryRequest requests = new InventoryRequest(this);
    private final BaseActionSource source;
    private final MultiCraftingTracker craftingTracker = new MultiCraftingTracker(this, REQ_COUNT);


    public TileLevelMaintainer() {
        getProxy().setIdlePowerUsage(1D);
        getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.source = new MachineSource(this);
    }

    public AeStackInventory<IAEItemStack> getCraftingSlots() {
        return requests.getCraftingSlots();
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.craftingTracker.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
        return null;
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        this.craftingTracker.jobStateChange(link);
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
                    if (inv.findPrecise(is) == null || inv.findPrecise(is).getStackSize() >= is.getStackSize() || cg.isRequesting(craftItem))
                        continue;

                    // do crafting

                    Future<ICraftingJob> jobTask = requests.getJobs()[i];

                    if (jobTask == null) {
                        requests.getJobs()[i] = cg.beginCraftingJob(getWorldObj(), grid, this.source, craftItem, null);
                    } else if (jobTask.isDone()) {
                        try {
                            ICraftingJob job = jobTask.get();
                            if (job != null) {
                                ICraftingLink link = cg.submitJob(job, null, null, false, this.source);
                                didSomething = true;
                                requests.jobs[i] = null;
                                if (link != null) {
                                    requests.links[i] = link;
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

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {

    }

    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    @Override
    public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan) {
    }

    public InventoryRequest getRequestInventory() {
        return this.requests;
    }

    public IInventory getInventory() {
        return (IInventory) this.requests.getCraftingSlots();
    }

    public void updateQuantity(int idx, long size) {
        this.requests.updateQuantity(idx, size);
    }

    public void updateBatchSize(int idx, long size) {
        this.requests.updateBatchSize(idx, size);
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public NBTTagCompound writeToNBTEvent(NBTTagCompound data) {
        this.requests.crafting.writeToNbt(data, "Inventory");
        this.requests.batchSize.writeToNbt(data, "BatchSize");
        return data;
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBTEvent(NBTTagCompound data) {
        this.requests.crafting.readFromNbt(data, "Inventory");
        this.requests.batchSize.readFromNbt(data, "BatchSize");
    }

    public static class InventoryRequest {
        private final AeStackInventoryImpl<IAEItemStack> crafting;
        private final AeStackInventoryImpl<IAEItemStack> batchSize;
        private Future<ICraftingJob>[] jobs;
        private ICraftingLink[] links;

        public InventoryRequest(TileLevelMaintainer tile) {
            this.crafting = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.batchSize = new AeStackInventoryImpl<>(StorageChannel.ITEMS, REQ_COUNT, tile);
            this.jobs = new Future[REQ_COUNT];
            this.links = new ICraftingLink[REQ_COUNT];
        }

        public Future<ICraftingJob>[] getJobs() {
            return jobs;
        }

        public ICraftingLink[] getLinks() {
            return links;
        }

        public AeStackInventory<IAEItemStack> getBatchInputs() {
            return this.batchSize;
        }

        public AeStackInventory<IAEItemStack> getCraftingSlots() {
            return this.crafting;
        }

        public void updateQuantity(int idx, long size) {
            IAEItemStack is = this.crafting.getStack(idx);
            if (is != null) {
                is.setStackSize(size);
            }
        }

        public void updateBatchSize(int idx, long size) {
            IAEItemStack is = this.crafting.getStack(idx);
            if (is != null) {
                IAEItemStack i = is.copy();
                i.setStackSize(size);
                this.batchSize.setStack(idx, i);
            }
        }

        public IAEItemStack getQuantity(int idx) {
            return this.crafting.getStack(idx);
        }

        public long getBatchSize(int idx) {
            IAEItemStack is = this.crafting.getStack(idx);
            IAEItemStack is1 = this.batchSize.getStack(idx);
            if (is != null && is1 != null && is.isSameType(is1)) {
                return is1.getStackSize();
            }
            return 0;
        }

        public IAEItemStack getCraftItem(int idx) {
            IAEItemStack is = this.crafting.getStack(idx);
            if (is != null) {
                IAEItemStack ias = is.copy();
                ias.setStackSize(this.getBatchSize(idx));
                return ias;
            }
            return null;
        }
    }
}
