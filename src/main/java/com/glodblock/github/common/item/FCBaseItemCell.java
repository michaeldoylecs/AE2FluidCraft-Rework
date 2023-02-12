package com.glodblock.github.common.item;

import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Optional;

public abstract class FCBaseItemCell extends AEBaseItem implements IStorageFluidCell {

    protected final CellType component;
    protected final long totalBytes;
    protected final int perType;
    protected final double idleDrain;
    private final ReadableNumberConverter format = ReadableNumberConverter.INSTANCE;

    @SuppressWarnings("Guava")
    public FCBaseItemCell(long bytes, int perType, double drain) {
        super(Optional.of(bytes / 1024 + "k"));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
        this.totalBytes = bytes;
        this.perType = perType;
        this.idleDrain = drain;
        this.component = null;
    }

    @SuppressWarnings("Guava")
    public FCBaseItemCell(final CellType whichCell, final long kilobytes) {
        super(Optional.of(kilobytes + "k"));
        this.setFeature(EnumSet.of(AEFeature.StorageCells));
        this.setMaxStackSize(1);
        this.totalBytes = kilobytes * 1024;
        this.component = whichCell;

        switch (this.component) {
            case Cell1kPart:
                this.idleDrain = 0.5;
                this.perType = 8;
                break;
            case Cell4kPart:
                this.idleDrain = 1.0;
                this.perType = 8;
                break;
            case Cell16kPart:
                this.idleDrain = 1.5;
                this.perType = 8;
                break;
            case Cell64kPart:
                this.idleDrain = 2.0;
                this.perType = 8;
                break;
            case Cell256kPart:
                this.idleDrain = 2.5;
                this.perType = 8;
                break;
            case Cell1024kPart:
                this.idleDrain = 3.0;
                this.perType = 8;
                break;
            case Cell4096kPart:
                this.idleDrain = 3.5;
                this.perType = 8;
                break;
            case Cell16384kPart:
                this.idleDrain = 4.0;
                this.perType = 8;
                break;
            default:
                this.idleDrain = 0.0;
                this.perType = 8;
        }
    }

    @Override
    public long getBytes(ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        if (Config.blacklistEssentiaGas && ModAndClassUtil.ThE && requestedAddition != null) {
            return ModAndClassUtil.essentiaGas.isInstance(requestedAddition.getFluid());
        }
        return false;
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines,
            final boolean displayMoreInfo) {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell()
                .getCellInventory(stack, null, StorageChannel.FLUIDS);

        if (inventory instanceof IFluidCellInventoryHandler) {
            final IFluidCellInventoryHandler handler = (IFluidCellInventoryHandler) inventory;
            final IFluidCellInventory cellInventory = handler.getCellInv();

            if (cellInventory != null) {
                lines.add(
                        EnumChatFormatting.WHITE + String.valueOf(cellInventory.getUsedBytes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + NumberFormat.getInstance().format(cellInventory.getTotalBytes())
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.BytesUsed.getLocal());
                lines.add(
                        EnumChatFormatting.WHITE + String.valueOf(cellInventory.getStoredFluidTypes())
                                + EnumChatFormatting.GRAY
                                + " "
                                + GuiText.Of.getLocal()
                                + " "
                                + EnumChatFormatting.DARK_GREEN
                                + cellInventory.getTotalFluidTypes()
                                + " "
                                + EnumChatFormatting.GRAY
                                + GuiText.Types.getLocal());

                if (GuiScreen.isCtrlKeyDown()) {
                    if (cellInventory.getStoredFluidTypes() > 0) {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_CONTENTS));
                        for (IAEFluidStack fluid : cellInventory.getContents()) {
                            if (fluid != null) {
                                lines.add(
                                        String.format(
                                                "  %s x%s mB",
                                                fluid.getFluidStack().getLocalizedName(),
                                                format.toWideReadableForm(fluid.getStackSize())));
                            }
                        }
                    } else {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_CELL_EMPTY));
                    }
                } else {
                    lines.add(StatCollector.translateToLocal(NameConst.TT_CTRL_FOR_MORE));
                }

                if (handler.isPreformatted()) {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included
                            : GuiText.Excluded).getLocal();
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());

                    if (GuiScreen.isShiftKeyDown()) {
                        lines.add(GuiText.Filter.getLocal() + ": ");
                        for (IAEFluidStack aeFluidStack : handler.getPartitionInv()) {
                            if (aeFluidStack != null) lines.add("  " + aeFluidStack.getFluidStack().getLocalizedName());
                        }
                    } else {
                        lines.add(StatCollector.translateToLocal(NameConst.TT_SHIFT_FOR_MORE));
                    }
                }
            }
        }
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 1;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 0);
    }

    @Override
    public IInventory getConfigInventory(ItemStack is) {
        return new CellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
