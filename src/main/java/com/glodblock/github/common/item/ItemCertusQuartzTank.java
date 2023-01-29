package com.glodblock.github.common.item;

import static com.glodblock.github.common.tile.TileCertusQuartzTank.CAPACITY;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.crossmod.waila.Tooltip;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemCertusQuartzTank extends ItemBlock implements IFluidContainerItem {

    private final String tagKey = "tank";

    private final int capacity = CAPACITY;

    public ItemCertusQuartzTank(Block block) {
        super(block);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        if (stack != null && stack.hasTagCompound()) {
            if (FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag(tagKey)) != null) {
                FluidStack fs = FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag(tagKey));
                list.add(Tooltip.fluidFormat(fs.getFluid().getLocalizedName(fs), fs.amount));
            }
        }
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(tagKey)
                || container.stackTagCompound.getCompoundTag(tagKey).hasKey("Empty")) {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(tagKey));
        if (stack == null) {
            return null;
        }

        int currentAmount = stack.amount;
        stack.amount = Math.min(stack.amount, maxDrain);
        if (doDrain) {
            if (currentAmount == stack.amount) {
                container.stackTagCompound.removeTag(tagKey);

                if (container.stackTagCompound.hasNoTags()) {
                    container.stackTagCompound = null;
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag(tagKey);
            fluidTag.setInteger("Amount", currentAmount - stack.amount);
            container.stackTagCompound.setTag(tagKey, fluidTag);
        }
        return stack;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (resource == null) {
            return 0;
        }

        if (!doFill) {
            if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(tagKey)) {
                return Math.min(this.capacity, resource.amount);
            }

            FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(tagKey));

            if (stack == null) {
                return Math.min(this.capacity, resource.amount);
            }

            if (!stack.isFluidEqual(resource)) {
                return 0;
            }

            return Math.min(this.capacity - stack.amount, resource.amount);
        }

        if (container.stackTagCompound == null) {
            container.stackTagCompound = new NBTTagCompound();
        }

        if (!container.stackTagCompound.hasKey(tagKey)
                || container.stackTagCompound.getCompoundTag(tagKey).hasKey("Empty")) {
            NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

            if (this.capacity < resource.amount) {
                fluidTag.setInteger("Amount", this.capacity);
                container.stackTagCompound.setTag(tagKey, fluidTag);
                return this.capacity;
            }

            container.stackTagCompound.setTag(tagKey, fluidTag);
            return resource.amount;
        }

        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag(tagKey);
        FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);

        if (!stack.isFluidEqual(resource)) {
            return 0;
        }

        int filled = this.capacity - stack.amount;
        if (resource.amount < filled) {
            stack.amount += resource.amount;
            filled = resource.amount;
        } else {
            stack.amount = this.capacity;
        }

        container.stackTagCompound.setTag(tagKey, stack.writeToNBT(fluidTag));
        return filled;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return this.capacity;
    }

    @Override
    public FluidStack getFluid(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey(tagKey)) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag(tagKey));
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ, int metadata) {
        if (!world.setBlock(x, y, z, this.field_150939_a, metadata, 3)) {
            return false;
        }

        if (world.getBlock(x, y, z) == this.field_150939_a) {
            this.field_150939_a.onBlockPlacedBy(world, x, y, z, player, stack);
            this.field_150939_a.onPostBlockPlaced(world, x, y, z, metadata);
        }

        if (stack != null && stack.hasTagCompound()) {
            ((TileCertusQuartzTank) world.getTileEntity(x, y, z)).readFromNBTWithoutCoords(stack.getTagCompound());
        }
        return true;
    }
}
