package com.glodblock.github.client.gui.container;

import appeng.api.AEApi;
import appeng.api.config.*;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.container.slot.SlotRestrictedInput;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.network.CPacketFluidUpdate;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.network.SPacketMEInventoryUpdate;
import com.glodblock.github.util.Util;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.commons.lang3.tuple.MutablePair;

public class FCBaseFluidMonitorContain extends AEBaseContainer
        implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEFluidStack> {
    private final SlotRestrictedInput[] cellView = new SlotRestrictedInput[5];
    private final IMEMonitor<IAEFluidStack> monitor;
    private final IItemList<IAEFluidStack> items = AEApi.instance().storage().createFluidList();
    private final IConfigManager clientCM;
    private final ITerminalHost host;

    @GuiSync(99)
    public boolean canAccessViewCells = false;

    @GuiSync(98)
    public boolean hasPower = false;

    private IConfigManagerHost gui;
    private IConfigManager serverCM;
    private IGridNode networkNode;

    public FCBaseFluidMonitorContain(final InventoryPlayer ip, final ITerminalHost monitorable) {
        this(ip, monitorable, true);
    }

    protected FCBaseFluidMonitorContain(
            final InventoryPlayer ip, final ITerminalHost monitorable, final boolean bindInventory) {
        super(
                ip,
                monitorable instanceof TileEntity ? (TileEntity) monitorable : null,
                monitorable instanceof IPart ? (IPart) monitorable : null);
        this.host = monitorable;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        if (Platform.isServer()) {
            this.serverCM = monitorable.getConfigManager();

            this.monitor = monitorable.getFluidInventory();
            if (this.monitor != null) {
                this.monitor.addListener(this, null);

                //                this.setCellInventory( this.monitor );

                if (monitorable instanceof IPortableCell) {
                    this.setPowerSource((IEnergySource) monitorable);
                } else if (monitorable instanceof IMEChest) {
                    this.setPowerSource((IEnergySource) monitorable);
                } else if (monitorable instanceof IGridHost) {
                    final IGridNode node = ((IGridHost) monitorable).getGridNode(ForgeDirection.UNKNOWN);
                    if (node != null) {
                        this.networkNode = node;
                        final IGrid g = node.getGrid();
                        if (g != null) {
                            this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                        }
                    }
                }
            } else {
                this.setValidContainer(false);
            }
        } else {
            this.monitor = null;
        }

        this.canAccessViewCells = false;
        if (monitorable instanceof IViewCellStorage) {
            for (int y = 0; y < 5; y++) {
                this.cellView[y] = new SlotRestrictedInput(
                        SlotRestrictedInput.PlacableItemType.VIEW_CELL,
                        ((IViewCellStorage) monitorable).getViewCellStorage(),
                        y,
                        206,
                        y * 18 + 8,
                        this.getInventoryPlayer());
                this.cellView[y].setAllowEdit(this.canAccessViewCells);
                this.addSlotToContainer(this.cellView[y]);
            }
        }

        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
    }

    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            Slot clickSlot = (Slot) this.inventorySlots.get(idx);
            if ((clickSlot instanceof SlotPlayerInv || clickSlot instanceof SlotPlayerHotBar)
                    && clickSlot.getHasStack()
                    && Util.FluidUtil.isFluidContainer(clickSlot.getStack())) {
                ItemStack tis = clickSlot.getStack();
                Map<Integer, IAEFluidStack> tmp = new HashMap<>();
                tmp.put(0, ItemFluidDrop.getAeFluidStack(AEItemStack.create(tis)));
                int index = Util.findItemInPlayerInvSlot(p, clickSlot.getStack());
                FluidCraft.proxy.netHandler.sendToServer(new CPacketFluidUpdate(tmp, tis, index));
            }
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (this.monitor != this.host.getFluidInventory()) {
                this.setValidContainer(false);
            }

            for (final Settings set : this.serverCM.getSettings()) {
                final Enum<?> sideLocal = this.serverCM.getSetting(set);
                final Enum<?> sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    this.clientCM.putSetting(set, sideLocal);
                    for (final Object crafter : this.crafters) {
                        try {
                            NetworkHandler.instance.sendTo(
                                    new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
                        } catch (final IOException e) {
                            AELog.debug(e);
                        }
                    }
                }
            }

            if (!this.items.isEmpty()) {
                final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

                final SPacketMEInventoryUpdate piu = new SPacketMEInventoryUpdate(true);

                for (final IAEFluidStack is : this.items) {
                    final IAEFluidStack send = monitorCache.findPrecise(is);
                    if (send == null) {
                        is.setStackSize(0);
                        piu.appendFluid(is);
                    } else {
                        piu.appendFluid(send);
                    }
                }

                if (!piu.isEmpty()) {
                    this.items.resetStatus();

                    for (final Object c : this.crafters) {
                        if (c instanceof EntityPlayer) {
                            FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
                        }
                    }
                }
            }

            this.updatePowerStatus();

            final boolean oldAccessible = this.canAccessViewCells;
            this.canAccessViewCells =
                    this.host instanceof WirelessTerminalGuiObject || this.hasAccess(SecurityPermissions.BUILD, false);
            if (this.canAccessViewCells != oldAccessible) {
                for (int y = 0; y < 5; y++) {
                    if (this.cellView[y] != null) {
                        this.cellView[y].setAllowEdit(this.canAccessViewCells);
                    }
                }
            }
            super.detectAndSendChanges();
        }
    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(
                        this.getPowerSource().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable t) {
            // :P
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("canAccessViewCells")) {
            for (int y = 0; y < 5; y++) {
                if (this.cellView[y] != null) {
                    this.cellView[y].setAllowEdit(this.canAccessViewCells);
                }
            }
        }

        super.onUpdate(field, oldValue, newValue);
    }

    @Override
    public void addCraftingToCrafters(final ICrafting c) {
        super.addCraftingToCrafters(c);
        this.queueInventory(c);
    }

    private void queueInventory(final ICrafting c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
            SPacketMEInventoryUpdate piu = new SPacketMEInventoryUpdate(true);
            final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

            for (final IAEFluidStack send : monitorCache) {
                try {
                    piu.appendFluid(send);
                } catch (final BufferOverflowException boe) {
                    FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);

                    piu = new SPacketMEInventoryUpdate(true);
                    piu.appendFluid(send);
                }
            }
            FluidCraft.proxy.netHandler.sendTo(piu, (EntityPlayerMP) c);
        }
    }

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public ItemStack[] getViewCells() {
        final ItemStack[] list = new ItemStack[this.cellView.length];

        for (int x = 0; x < this.cellView.length; x++) {
            list[x] = this.cellView[x].getStack();
        }

        return list;
    }

    public SlotRestrictedInput getCellViewSlot(final int index) {
        return this.cellView[index];
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(
            IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
        for (final IAEFluidStack is : change) {
            this.items.add(is);
        }
    }

    private void dropItem(ItemStack is) {
        if (is.stackSize <= 0) return;
        ItemStack itemStack = is.copy();
        int i = itemStack.getMaxStackSize();
        while (itemStack.stackSize > 0) {
            if (i > itemStack.stackSize) {
                if (!getPlayerInv().addItemStackToInventory(itemStack.copy())) {
                    getPlayerInv().player.entityDropItem(itemStack.copy(), 0);
                }
                break;
            } else {
                itemStack.stackSize -= i;
                ItemStack item = itemStack.copy();
                item.stackSize = i;
                if (!getPlayerInv().addItemStackToInventory(item)) {
                    getPlayerInv().player.entityDropItem(item, 0);
                }
            }
        }
    }

    private void dropItem(ItemStack itemStack, int stackSize) {
        if (itemStack.stackSize <= 0) return;
        ItemStack is = itemStack.copy();
        is.stackSize = stackSize;
        this.dropItem(is);
    }

    public void postChange(
            Iterable<IAEFluidStack> change, ItemStack fluidContainer, EntityPlayer player, int slotIndex) {
        for (IAEFluidStack fluid : change) {
            IAEFluidStack nfluid = this.monitor.getStorageList().findPrecise(fluid);
            ItemStack out = fluidContainer.copy();
            out.stackSize = 1;
            if (Util.FluidUtil.isEmpty(fluidContainer) && fluid != null) {
                //              add fluid to tanks
                if (nfluid.getStackSize() <= 0) continue;
                final IAEFluidStack toExtract = nfluid.copy();
                MutablePair<Integer, ItemStack> fillStack = Util.FluidUtil.fillStack(out, toExtract.getFluidStack());
                if (fillStack.right == null || fillStack.left <= 0) continue;
                toExtract.setStackSize((long) fillStack.left * fluidContainer.stackSize);
                IAEFluidStack tmp = this.host
                        .getFluidInventory()
                        .extractItems(toExtract, Actionable.SIMULATE, this.getActionSource());
                fillStack.right.stackSize = (int) (tmp.getStackSize() / fillStack.left);
                this.dropItem(fillStack.right);
                out.stackSize = fillStack.right.stackSize;
                if (fillStack.right.getItem() instanceof IFluidContainerItem) {
                    this.host.getFluidInventory().extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                    if ((int) (tmp.getStackSize() % fillStack.left) > 0) {
                        ((IFluidContainerItem) fillStack.right.getItem())
                                .drain(
                                        fillStack.right,
                                        fillStack.left - (int) (tmp.getStackSize() % fillStack.left),
                                        true);
                        this.dropItem(fillStack.right, 1);
                        out.stackSize++;
                    }
                } else if (FluidContainerRegistry.isContainer(fillStack.right)) {
                    toExtract.setStackSize((long) fillStack.right.stackSize * fillStack.left);
                    this.host.getFluidInventory().extractItems(toExtract, Actionable.MODULATE, this.getActionSource());
                }
            } else if (!Util.FluidUtil.isEmpty(fluidContainer)) {
                //              add fluid to ae network
                AEFluidStack fluidStack = Util.getAEFluidFromItem(fluidContainer);
                final IAEFluidStack aeFluidStack = fluidStack.copy();
                // simulate result is incorrect. so I'm using other solution and ec2 both mod have same issues
                final IAEFluidStack notInserted = this.host
                        .getFluidInventory()
                        .injectItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                MutablePair<Integer, ItemStack> drainStack =
                        Util.FluidUtil.drainStack(out.copy(), aeFluidStack.getFluidStack());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    if (fluidStack.getStackSize() == notInserted.getStackSize()) continue;
                    aeFluidStack.decStackSize(notInserted.getStackSize());

                    if (drainStack.left > aeFluidStack.getStackSize()
                            && FluidContainerRegistry.isContainer(drainStack.right)) {
                        aeFluidStack.setStackSize(drainStack.left - aeFluidStack.getStackSize());
                        this.host
                                .getFluidInventory()
                                .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                        continue;
                    }

                    this.dropItem(
                            drainStack.right, (int) (aeFluidStack.getStackSize() / drainStack.left)); // drop empty item
                    out.stackSize = (int) (notInserted.getStackSize() / drainStack.left);
                    if (drainStack.right.getItem() instanceof IFluidContainerItem) {
                        if (notInserted.getStackSize() % drainStack.left > 0) {
                            fluidStack.setStackSize((notInserted.getStackSize() % drainStack.left));
                            ((IFluidContainerItem) drainStack.right.getItem())
                                    .fill(drainStack.right, fluidStack.getFluidStack(), true);
                            this.dropItem(drainStack.right, 1);
                        }
                    } else if (FluidContainerRegistry.isContainer(drainStack.right)) {
                        if (notInserted.getStackSize() % drainStack.left > 0) {
                            aeFluidStack.setStackSize(aeFluidStack.getStackSize() % drainStack.left);
                            this.host
                                    .getFluidInventory()
                                    .extractItems(aeFluidStack, Actionable.MODULATE, this.getActionSource());
                            out.stackSize++;
                        }
                    }
                    if (slotIndex == -1) out.stackSize = fluidContainer.stackSize - out.stackSize;
                } else {
                    out.stackSize = (int) (fluidContainer.stackSize - (aeFluidStack.getStackSize() / drainStack.left));
                    this.dropItem(drainStack.right, fluidContainer.stackSize); // drop empty item
                    if (slotIndex == -1) out.stackSize++;
                }
            } else {
                continue;
            }
            if (slotIndex == -1) {
                player.inventory.getItemStack().stackSize = player.inventory.getItemStack().stackSize - out.stackSize;
                if (player.inventory.getItemStack().stackSize > 0) {
                    FluidCraft.proxy.netHandler.sendTo(
                            new SPacketFluidUpdate(new HashMap<>(), player.inventory.getItemStack()),
                            (EntityPlayerMP) player);
                } else {
                    player.inventory.setItemStack(null);
                    FluidCraft.proxy.netHandler.sendTo(
                            new SPacketFluidUpdate(new HashMap<>()), (EntityPlayerMP) player);
                }
            } else {
                player.inventory.setInventorySlotContents(slotIndex, out.stackSize > 0 ? out : null);
            }
        }
        this.detectAndSendChanges();
    }

    @Override
    public void onListUpdate() {
        for (final Object c : this.crafters) {
            if (c instanceof ICrafting) {
                final ICrafting cr = (ICrafting) c;
                this.queueInventory(cr);
            }
        }
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }
}
