package com.glodblock.github.common.item;

import java.util.HashMap;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBasicFluidStorageCell extends FCBaseItemCell
        implements IStorageFluidCell, IRegister<ItemBasicFluidStorageCell> {

    private static final HashMap<Integer, IIcon> icon = new HashMap<>();

    public ItemBasicFluidStorageCell(final CellType whichCell, final long kilobytes) {
        super(whichCell, kilobytes);
        setUnlocalizedName(NameConst.ITEM_FLUID_STORAGE + kilobytes);
    }

    public ItemStack getComponent() {
        return component.stack(1);
    }

    public ItemStack getHousing() {
        return component.getHousing(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted(
                "item.fluid_storage." + this.totalBytes / 1024 + ".name",
                CellType.getTypeColor(this.component),
                EnumChatFormatting.RESET);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icon.put(
                (int) (this.totalBytes / 1024),
                iconRegister
                        .registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_STORAGE + "." + this.totalBytes / 1024));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int id = (int) (this.totalBytes / 1024);
        return icon.get(id);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player) {
        this.disassembleDrive(stack, world, player);
        return stack;
    }

    @SuppressWarnings("unchecked")
    private boolean disassembleDrive(final ItemStack stack, final World world, final EntityPlayer player) {
        if (player.isSneaking()) {
            if (Platform.isClient()) {
                return false;
            }
            final InventoryPlayer playerInventory = player.inventory;
            final IMEInventoryHandler<?> inv = AEApi.instance().registries().cell()
                    .getCellInventory(stack, null, StorageChannel.FLUIDS);
            if (inv != null && playerInventory.getCurrentItem() == stack) {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
                final IItemList<IAEFluidStack> list = inv.getAvailableItems(StorageChannel.FLUIDS.createList());
                if (list.isEmpty() && ia != null) {
                    playerInventory.setInventorySlotContents(playerInventory.currentItem, null);

                    // drop core
                    final ItemStack extraB = ia.addItems(this.component.stack(1));
                    if (extraB != null) {
                        player.dropPlayerItemWithRandomChoice(extraB, false);
                    }

                    // drop upgrades
                    final IInventory upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSizeInventory(); upgradeIndex++) {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        final ItemStack leftStack = ia.addItems(upgradeStack);
                        if (leftStack != null && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropPlayerItemWithRandomChoice(upgradeStack, false);
                        }
                    }

                    // drop empty storage cell case
                    final ItemStack extraA = ia.addItems(this.component.getHousing(1));
                    if (extraA != null) {
                        player.dropPlayerItemWithRandomChoice(this.component.getHousing(1), false);
                    }
                    if (player.inventoryContainer != null) {
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onItemUseFirst(final ItemStack stack, final EntityPlayer player, final World world, final int x,
            final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ) {
        if (ForgeEventFactory.onItemUseStart(player, stack, 1) <= 0) return true;
        return this.disassembleDrive(stack, world, player);
    }

    @Override
    public ItemStack getContainerItem(final ItemStack itemStack) {
        if (this.getHousing() != null) {
            return this.getHousing();
        }
        throw new MissingDefinition("Tried to use empty storage cells while basic storage cells are defined.");
    }

    @Override
    public boolean hasContainerItem(final ItemStack stack) {
        return AEConfig.instance.isFeatureEnabled(AEFeature.EnableDisassemblyCrafting);
    }

    @Override
    public ItemBasicFluidStorageCell register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_STORAGE + this.totalBytes / 1024, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }
}
