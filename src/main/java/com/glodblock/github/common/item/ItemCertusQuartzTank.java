package com.glodblock.github.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

import com.glodblock.github.common.tile.TileCertusQuartzTank;

public class ItemCertusQuartzTank extends BaseItemBlockContainer implements IFluidContainerItem {

    private final int capacity = 32000;

    public ItemCertusQuartzTank(Block block) {
        super(block);
    }

    @Override
    public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain) {
        if (container.stackSize != 1 || container.stackTagCompound == null
                || !container.stackTagCompound.hasKey("tileEntity")
                || container.stackTagCompound.getCompoundTag("tileEntity").hasKey("Empty")) {
            return null;
        }

        FluidStack stack = FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("tileEntity"));
        if (stack == null) {
            return null;
        }

        int currentAmount = stack.amount;
        stack.amount = Math.min(stack.amount, maxDrain);
        if (doDrain) {
            if (currentAmount == stack.amount) {
                container.stackTagCompound.removeTag("tileEntity");

                if (container.stackTagCompound.hasNoTags()) {
                    container.stackTagCompound = null;
                }
                return stack;
            }

            NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("tileEntity");
            fluidTag.setInteger("Amount", currentAmount - stack.amount);
            container.stackTagCompound.setTag("tileEntity", fluidTag);
        }
        return stack;
    }

    @Override
    public int fill(ItemStack container, FluidStack resource, boolean doFill) {
        if (resource == null || container.stackSize != 1) {
            return 0;
        }

        if (!doFill) {
            if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("tileEntity")) {
                return Math.min(this.capacity, resource.amount);
            }

            FluidStack stack = FluidStack
                    .loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("tileEntity"));

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

        if (!container.stackTagCompound.hasKey("tileEntity")
                || container.stackTagCompound.getCompoundTag("tileEntity").hasKey("Empty")) {
            NBTTagCompound fluidTag = resource.writeToNBT(new NBTTagCompound());

            if (this.capacity < resource.amount) {
                fluidTag.setInteger("Amount", this.capacity);
                container.stackTagCompound.setTag("tileEntity", fluidTag);
                return this.capacity;
            }

            container.stackTagCompound.setTag("tileEntity", fluidTag);
            return resource.amount;
        }

        NBTTagCompound fluidTag = container.stackTagCompound.getCompoundTag("tileEntity");
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

        container.stackTagCompound.setTag("tileEntity", stack.writeToNBT(fluidTag));
        return filled;
    }

    @Override
    public int getCapacity(ItemStack container) {
        return this.capacity;
    }

    @Override
    public FluidStack getFluid(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("tileEntity")) {
            return null;
        }
        return FluidStack.loadFluidStackFromNBT(container.stackTagCompound.getCompoundTag("tileEntity"));
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
            ((TileCertusQuartzTank) world.getTileEntity(x, y, z))
                    .readFromNBTWithoutCoords(stack.getTagCompound().getCompoundTag("tileEntity"));
        }
        return true;
    }
}
