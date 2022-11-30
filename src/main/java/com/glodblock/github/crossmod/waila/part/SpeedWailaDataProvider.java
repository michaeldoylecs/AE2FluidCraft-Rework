package com.glodblock.github.crossmod.waila.part;

import appeng.api.parts.IPart;
import appeng.integration.modules.waila.part.BasePartWailaDataProvider;
import com.glodblock.github.common.parts.PartSharedFluidBus;
import com.glodblock.github.crossmod.waila.Tooltip;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class SpeedWailaDataProvider extends BasePartWailaDataProvider {

    @Override
    public List<String> getWailaBody(
        final IPart part,
        final List<String> currentToolTip,
        final IWailaDataAccessor accessor,
        final IWailaConfigHandler config) {
        if (part instanceof PartSharedFluidBus) {
            part.readFromNBT(accessor.getNBTData());
            currentToolTip.add(Tooltip.partFluidBusFormat(((PartSharedFluidBus) part).calculateAmountToSend()));
        }
        return currentToolTip;
    }

    @Override
    public NBTTagCompound getNBTData(
        final EntityPlayerMP player,
        final IPart part,
        final TileEntity te,
        final NBTTagCompound tag,
        final World world,
        final int x,
        final int y,
        final int z) {
        if (part instanceof PartSharedFluidBus) {
            part.writeToNBT(tag);
        }
        return tag;
    }
}
