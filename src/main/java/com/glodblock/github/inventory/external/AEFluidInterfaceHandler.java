package com.glodblock.github.inventory.external;

import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.tile.networking.TileCableBus;
import com.glodblock.github.common.parts.PartFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class AEFluidInterfaceHandler implements IExternalStorageHandler {

    @Override
    public boolean canHandle(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource mySrc) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable) {
                return true;
            } else return te instanceof TileCableBus && ((TileCableBus) te).getPart(d.getOpposite()) instanceof ITileStorageMonitorable;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IMEInventory getInventory(TileEntity te, ForgeDirection d, StorageChannel channel, BaseActionSource src) {
        if (channel == StorageChannel.FLUIDS) {
            if (te instanceof ITileStorageMonitorable) {
                return ((ITileStorageMonitorable) te).getMonitorable(d, src).getFluidInventory();
            }
            else if (te instanceof TileCableBus && ((TileCableBus) te).getPart(d.getOpposite()) instanceof ITileStorageMonitorable) {
                ITileStorageMonitorable part = (ITileStorageMonitorable) ((TileCableBus) te).getPart(d.getOpposite());
                return part.getMonitorable(d, src).getFluidInventory();
            }
        }
        return null;
    }

}
