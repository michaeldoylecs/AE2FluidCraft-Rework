package com.glodblock.github.common.block;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.BlockTank;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemCertusQuartzTank;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class BlockCertusQuartzTank extends BlockTank implements IRegister<BlockTank> {

    private IIcon textureStackedSide;

    public BlockCertusQuartzTank() {
        setBlockName(NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        setResistance(1.0F);
        setHardness(0.5F);
        setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
    }

    public ItemStack getDropWithNBT(World world, int x, int y, int z) {
        NBTTagCompound nbt = new NBTTagCompound();
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileCertusQuartzTank) {
            ItemStack dropStack = new ItemStack(ItemAndBlockHolder.CERTUS_QUARTZ_TANK, 1);
            ((TileCertusQuartzTank) te).writeToNBTWithoutCoords(nbt);
            if (!nbt.getCompoundTag("tank").hasKey("Empty")) {
                dropStack.setTagCompound(nbt);
            }

            return dropStack;
        }
        return null;
    }

    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileCertusQuartzTank();
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister par1IconRegister) {
        super.registerBlockIcons(par1IconRegister);
        textureStackedSide = par1IconRegister.registerIcon(FluidCraft.MODID + ":certus_quartz_tank/side_stacked");
    }

    @Override
    public IIcon getIconAbsolute(IBlockAccess iblockaccess, int x, int y, int z, int side, int metadata) {
        if (side >= 2 && (iblockaccess.getBlock(x, y - 1, z) instanceof BlockCertusQuartzTank)) {
            return textureStackedSide;
        } else {
            return super.getIconAbsolute(side, metadata);
        }
    }

    @Override
    public boolean onBlockActivated(
            World worldObj,
            int x,
            int y,
            int z,
            EntityPlayer p,
            int blockID,
            float offsetX,
            float offsetY,
            float offsetZ) {
        ItemStack current = p.inventory.getCurrentItem();

        if (p.isSneaking() && current != null) {
            try {
                if (current.getItem() instanceof IToolWrench
                        && ((IToolWrench) current.getItem()).canWrench(p, x, y, z)) {
                    dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
                    worldObj.setBlockToAir(x, y, z);
                    ((IToolWrench) current.getItem()).wrenchUsed(p, x, y, z);
                    return true;
                }
            } catch (Throwable e) {
                // No IToolWrench
            }
            if (current.getItem() instanceof IAEWrench
                    && ((IAEWrench) current.getItem()).canWrench(current, p, x, y, z)) {
                dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
                worldObj.setBlockToAir(x, y, z);
                return true;
            }
        }

        if (current != null) {
            FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
            TileEntityCertusTank tank = (TileEntityCertusTank) worldObj.getTileEntity(x, y, z);

            if (liquid != null) {
                int amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

                if (amountFilled != 0 && !p.capabilities.isCreativeMode) {
                    if (current.stackSize > 1) {
                        p.inventory.mainInventory[p.inventory.currentItem].stackSize -= 1;
                        p.inventory.addItemStackToInventory(current.getItem().getContainerItem(current));
                    } else {
                        p.inventory.mainInventory[p.inventory.currentItem] =
                                current.getItem().getContainerItem(current);
                    }
                }

                return true;

                // Handle empty containers
            } else {

                FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
                if (available != null) {
                    ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

                    liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

                    if (liquid != null) {
                        tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
                        if (!p.capabilities.isCreativeMode) {
                            if (current.stackSize == 1) {
                                p.inventory.mainInventory[p.inventory.currentItem] = filled;
                            } else {
                                p.inventory.mainInventory[p.inventory.currentItem].stackSize--;
                                if (!p.inventory.addItemStackToInventory(filled)) p.entityDropItem(filled, 0);
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public BlockCertusQuartzTank register() {
        GameRegistry.registerBlock(this, ItemCertusQuartzTank.class, NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        GameRegistry.registerTileEntity(TileCertusQuartzTank.class, NameConst.BLOCK_CERTUS_QUARTZ_TANK);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
