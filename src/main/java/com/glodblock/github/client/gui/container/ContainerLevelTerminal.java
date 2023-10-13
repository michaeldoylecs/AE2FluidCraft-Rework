package com.glodblock.github.client.gui.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.api.registries.ILevelViewable;
import com.glodblock.github.client.gui.container.base.FCBaseContainer;
import com.glodblock.github.common.parts.PartLevelTerminal;
import com.glodblock.github.coremod.registries.LevelTerminalRegistry;
import com.glodblock.github.inventory.item.IWirelessTerminal;
import com.glodblock.github.network.SPacketLevelTerminalUpdate;
import com.google.common.primitives.Ints;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.InventoryAction;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorPlayerHand;
import appeng.util.inv.ItemSlot;

public class ContainerLevelTerminal extends FCBaseContainer {

    /**
     * this stuff is all server side
     */
    private int nextId = 0;

    private final Map<ILevelViewable, ContainerLevelTerminal.InvTracker> tracked = new HashMap<>();
    private final Map<Long, ContainerLevelTerminal.InvTracker> trackedById = new HashMap<>();

    private IGrid grid;
    private SPacketLevelTerminalUpdate dirty;
    private boolean isDirty;
    private boolean wasOff;
    private int ticks = 0;

    public ContainerLevelTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        super(ip, monitorable);
        if (Platform.isServer()) {
            if (monitorable instanceof PartLevelTerminal terminal) {
                grid = terminal.getActionableNode().getGrid();
            } else if (monitorable instanceof IWirelessTerminal wirelessTerminal) {
                grid = wirelessTerminal.getActionableNode().getGrid();
            }
            dirty = this.updateList();
            if (dirty != null) {
                FluidCraft.proxy.netHandler.sendTo(dirty, (EntityPlayerMP) ip.player);
                this.isDirty = true;
            } else {
                dirty = new SPacketLevelTerminalUpdate();
            }
        }

        this.bindPlayerInventory(ip, 14, 0);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();

        if (this.grid == null) {
            return;
        }

        final IActionHost host = this.getActionHost();

        if (!host.getActionableNode().isActive()) {
            if (!wasOff) {
                SPacketLevelTerminalUpdate update = new SPacketLevelTerminalUpdate();

                update.setDisconnect();
                wasOff = true;
                FluidCraft.proxy.netHandler.sendTo(update, (EntityPlayerMP) this.getPlayerInv().player);
            }
            return;
        }
        wasOff = false;

        if (isDirty) {
            FluidCraft.proxy.netHandler.sendTo(dirty, (EntityPlayerMP) this.getPlayerInv().player);
            dirty = new SPacketLevelTerminalUpdate();
            isDirty = false;
        } else if (++ticks % 20 == 0) {
            ticks = 0;
            SPacketLevelTerminalUpdate update = this.updateList();
            if (update != null) {
                FluidCraft.proxy.netHandler.sendTo(update, (EntityPlayerMP) this.getPlayerInv().player);
            }
        }
    }

    /**
     * Merge from slot -> player inv. Returns the items not added.
     */
    private ItemStack mergeToPlayerInventory(InventoryAdaptor playerInv, ItemStack stack) {
        if (stack == null) return null;
        for (ItemSlot slot : playerInv) {
            if (Platform.isSameItemPrecise(slot.getItemStack(), stack)) {
                if (slot.getItemStack().stackSize < slot.getItemStack().getMaxStackSize()) {
                    ++slot.getItemStack().stackSize;
                    return null;
                }
            }
        }
        return playerInv.addItems(stack);
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slotIdx, final long id) {
        final ContainerLevelTerminal.InvTracker invTracker = this.trackedById.get(id);
        if (invTracker != null) {
            final ItemStack handStack = player.inventory.getItemStack();

            if (handStack != null && !(handStack.getItem() instanceof ItemEncodedPattern)) {
                // Why even bother if we're not dealing with an encoded pattern in hand
                return;
            }

            final ItemStack slotStack = invTracker.requests.getStackInSlot(slotIdx);
            final InventoryAdaptor playerHand = new AdaptorPlayerHand(player);

            switch (action) {
                /* Set down/pickup. This is the same as SPLIT_OR_PLACE_SINGLE as our max stack sizes are 1 in slots. */
                case PICKUP_OR_SET_DOWN -> {
                    if (handStack != null) {
                        for (int s = 0; s < invTracker.requests.getSizeInventory(); s++) {
                            /* Is there a duplicate pattern here? */
                            if (Platform.isSameItemPrecise(invTracker.requests.getStackInSlot(s), handStack)) {
                                /* We're done here - dupe found. */
                                return;
                            }
                        }
                    }

                    if (slotStack == null) {
                        /* Insert to container, if valid */
                        if (handStack == null) {
                            /* Nothing happens */
                            return;
                        }
                        invTracker.requests.setInventorySlotContents(slotIdx, playerHand.removeItems(1, null, null));
                    } else {
                        /* Exchange? */
                        if (handStack != null && handStack.stackSize > 1) {
                            /* Exchange is impossible, abort */
                            return;
                        }
                        invTracker.requests.setInventorySlotContents(slotIdx, playerHand.removeItems(1, null, null));
                        playerHand.addItems(slotStack.copy());
                    }
                    syncLevelTerminalSlot(invTracker, id, slotIdx, invTracker.requests.getStackInSlot(slotIdx));
                }
                /* Shift click from slotIdx -> player. Player -> slotIdx is not supported. */
                case SHIFT_CLICK -> {
                    InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player.inventory, ForgeDirection.UNKNOWN);
                    ItemStack leftOver = mergeToPlayerInventory(playerInv, slotStack);

                    if (leftOver == null) {
                        invTracker.requests.setInventorySlotContents(slotIdx, null);
                        syncLevelTerminalSlot(invTracker, id, slotIdx, null);
                    }
                }
                /* Move all blank patterns -> player */
                case MOVE_REGION -> {
                    final InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                    List<Integer> valid = new ArrayList<>();

                    for (int i = 0; i < invTracker.requests.getSizeInventory(); i++) {
                        ItemStack toExtract = invTracker.requests.getStackInSlot(i);

                        if (toExtract == null) {
                            continue;
                        }

                        ItemStack leftOver = mergeToPlayerInventory(playerInv, toExtract);

                        if (leftOver != null) {
                            break;
                        } else {
                            invTracker.requests.setInventorySlotContents(i, null);
                        }
                        valid.add(i);
                    }
                    if (valid.size() > 0) {
                        int[] validIndices = Ints.toArray(valid);
                        NBTTagList tag = new NBTTagList();
                        for (int i = 0; i < valid.size(); ++i) {
                            tag.appendTag(new NBTTagCompound());
                        }
                        dirty.addOverwriteEntry(id).setItems(validIndices, tag);
                        isDirty = true;
                    }
                }
                case CREATIVE_DUPLICATE -> {
                    if (player.capabilities.isCreativeMode) {
                        playerHand.addItems(handStack);
                    }
                }
                default -> {
                    return;
                }
            }

            this.updateHeld(player);
        }
    }

    private void syncLevelTerminalSlot(ContainerLevelTerminal.InvTracker inv, long id, int slot, ItemStack stack) {
        int[] validIndices = { slot };
        NBTTagList list = new NBTTagList();
        NBTTagCompound item = new NBTTagCompound();

        if (stack != null) {
            stack.writeToNBT(item);
        }
        list.appendTag(item);
        inv.updateNBT();
        this.dirty.addOverwriteEntry(id).setItems(validIndices, list);
        this.isDirty = true;
    }

    private SPacketLevelTerminalUpdate updateList() {
        SPacketLevelTerminalUpdate update = null;
        var supported = LevelTerminalRegistry.instance().getSupportedClasses();
        Set<ILevelViewable> visited = new HashSet<>();

        for (Class<? extends ILevelViewable> clz : supported) {
            boolean isAdopted = LevelTerminalRegistry.instance().isAdopted(clz);
            Class<? extends IGridHost> machineClass = isAdopted ? LevelTerminalRegistry.instance().getAdopted(clz)
                    : clz;
            for (IGridNode gridNode : grid.getMachines(machineClass)) {
                final ILevelViewable machine = isAdopted
                        ? LevelTerminalRegistry.instance().getAdapter(clz).adapt(gridNode.getMachine())
                        : (ILevelViewable) gridNode.getMachine();

                /* First check if we are already tracking this node */
                if (tracked.containsKey(machine)) {
                    /* Check for updates */
                    ContainerLevelTerminal.InvTracker knownTracker = tracked.get(machine);

                    /* Name changed? */
                    String name = machine.getCustomName();

                    if (!Objects.equals(knownTracker.name, name)) {
                        if (update == null) update = new SPacketLevelTerminalUpdate();
                        update.addRenamedEntry(knownTracker.id, name);
                        knownTracker.name = name;
                    }

                    /* Status changed? */
                    boolean isActive = gridNode.isActive() || machine.shouldDisplay();

                    if (!knownTracker.online && isActive) {
                        /* Node offline -> online */
                        knownTracker.online = true;
                        if (update == null) update = new SPacketLevelTerminalUpdate();
                        knownTracker.updateNBT();
                        update.addOverwriteEntry(knownTracker.id).setOnline(true)
                                .setItems(knownTracker.requests.getSizeInventory(), knownTracker.inventoryNbt);
                    } else if (knownTracker.online && !isActive) {
                        /* Node online -> offline */
                        knownTracker.online = false;
                        if (update == null) update = new SPacketLevelTerminalUpdate();
                        update.addOverwriteEntry(knownTracker.id).setOnline(false);
                    }

                    for (int i = 0; i < knownTracker.requests.getSizeInventory(); i++) {
                        ItemStack knowmItemStack = ItemStack
                                .loadItemStackFromNBT(knownTracker.inventoryNbt.getCompoundTagAt(i));
                        ItemStack machineItemStack = machine.getInventoryByName("config").getStackInSlot(i);
                        if (isDifferent(knowmItemStack, machineItemStack)) {
                            if (update == null) update = new SPacketLevelTerminalUpdate();
                            knownTracker.updateNBT();
                            update.addOverwriteEntry(knownTracker.id)
                                    .setItems(knownTracker.requests.getSizeInventory(), knownTracker.inventoryNbt);
                        }
                    }
                } else {
                    /* Add a new entry */
                    if (update == null) update = new SPacketLevelTerminalUpdate();
                    ContainerLevelTerminal.InvTracker entry = new ContainerLevelTerminal.InvTracker(
                            nextId++,
                            machine,
                            gridNode.isActive());
                    update.addNewEntry(entry.id, entry.name, entry.online)
                            .setLocation(entry.x, entry.y, entry.z, entry.dim, entry.side.ordinal())
                            .setItems(entry.rows, entry.rowSize, entry.inventoryNbt)
                            .setViewItemStack(machine.getSelfItemStack(), machine.getDisplayItemStack());
                    tracked.put(machine, entry);
                    trackedById.put(entry.id, entry);
                }
                visited.add(machine);
            }
        }

        /* Now find any entries that we need to remove */
        Iterator<Map.Entry<ILevelViewable, ContainerLevelTerminal.InvTracker>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (visited.contains(entry.getKey())) {
                continue;
            }

            if (update == null) update = new SPacketLevelTerminalUpdate();

            trackedById.remove(entry.getValue().id);
            it.remove();
            update.addRemovalEntry(entry.getValue().id);
        }
        return update;
    }

    @Override
    protected boolean isWirelessTerminal() {
        return false;
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if (a == null && b == null) {
            return false;
        }

        if (a == null || b == null) {
            return true;
        }

        return !ItemStack.areItemStacksEqual(a, b);
    }

    private static class InvTracker {

        private final long id;
        private String name;
        private final IInventory requests;
        private final int rowSize;
        private final int rows;
        private final int x;
        private final int y;
        private final int z;
        private final int dim;
        private final ForgeDirection side;
        private boolean online;
        private NBTTagList inventoryNbt;

        public InvTracker(long id, ILevelViewable machine, boolean online) {

            DimensionalCoord location = machine.getLocation();

            this.id = id;
            this.name = machine.getCustomName();
            this.requests = machine.getInventoryByName("config");
            this.rowSize = machine.rowSize();
            this.rows = machine.rows();
            this.x = location.x;
            this.y = location.y;
            this.z = location.z;
            this.dim = location.getDimension();
            this.side = machine.getSide();
            this.online = online;
            this.inventoryNbt = new NBTTagList();
            updateNBT();
        }

        private void updateNBT() {
            this.inventoryNbt = new NBTTagList();
            for (int i = 0; i < this.rows; ++i) {
                for (int j = 0; j < this.rowSize; ++j) {
                    final int offset = this.rowSize * i;
                    ItemStack stack = this.requests.getStackInSlot(offset + j);

                    if (stack != null) {
                        this.inventoryNbt.appendTag(stack.writeToNBT(new NBTTagCompound()));
                    } else {
                        this.inventoryNbt.appendTag(new NBTTagCompound());
                    }
                }
            }
        }

    }
}
