package com.glodblock.github.crossmod.waila.vanilla;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import appeng.integration.modules.waila.BaseWailaDataProvider;

import com.glodblock.github.common.tile.TileCertusQuartzTank;
import com.glodblock.github.crossmod.waila.Tooltip;

public class FluidInvWailaDataProvider extends BaseWailaDataProvider {

    @Override
    public List<String> getWailaBody(final ItemStack itemStack, final List<String> currentToolTip,
            final IWailaDataAccessor accessor, final IWailaConfigHandler config) {
        final TileEntity te = accessor.getTileEntity();
        if (te instanceof TileCertusQuartzTank) {
            for (FluidTankInfo info : ((IFluidHandler) te).getTankInfo(ForgeDirection.UNKNOWN)) {
                if (info.fluid == null) continue;
                currentToolTip.add(Tooltip.fluidFormat(info.fluid.getLocalizedName(), info.fluid.amount));
            }
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(final EntityPlayerMP player, final TileEntity te, final NBTTagCompound tag,
            final World world, final int x, final int y, final int z) {
        if (te instanceof TileCertusQuartzTank) {
            te.writeToNBT(tag);
        }
        return tag;
    }
}
