package com.glodblock.github.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketRenamer implements IMessage {

    private int x;
    private int y;
    private int z;
    private int dim;
    private ForgeDirection side;

    public CPacketRenamer() {}

    public CPacketRenamer(int x, int y, int z, int dim, ForgeDirection side) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.side = side;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.dim = buf.readInt();
        this.side = ForgeDirection.getOrientation(buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.dim);
        buf.writeInt(side.ordinal());
    }

    public static class Handler implements IMessageHandler<CPacketRenamer, IMessage> {

        @Override
        public IMessage onMessage(CPacketRenamer message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            TileEntity tile = DimensionManager.getWorld(message.dim).getTileEntity(message.x, message.y, message.z);
            if (tile != null) {
                Platform.openGUI(player, tile, message.side, GuiBridge.GUI_RENAMER);
            }
            return null;
        }
    }
}
