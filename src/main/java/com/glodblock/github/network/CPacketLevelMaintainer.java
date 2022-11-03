package com.glodblock.github.network;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

public class CPacketLevelMaintainer implements IMessage {

    private int action;
    private long size;
    private int slotIndex;

    public CPacketLevelMaintainer() {
    }

    public CPacketLevelMaintainer(int action) {
        this.action = action;
        this.size = 0;
        this.slotIndex = 0;
    }

    public CPacketLevelMaintainer(int action, int slotIndex, long size) {
        this.action = action;
        this.size = size;
        this.slotIndex = slotIndex;
    }

    public CPacketLevelMaintainer(int action, int slotIndex, String size) {
        this.action = action;
        this.slotIndex = slotIndex;
        this.size = Long.parseLong(size);
    }

    public static IAEItemStack setTag(IAEItemStack ias, long size, int slotIndex) {
        NBTTagCompound data = new NBTTagCompound();
        ItemStack is = ias.getItemStack();
        data.setLong("BatchSize", size);
        data.setLong("Index", slotIndex);
        is.setTagCompound(data);
        IAEItemStack iaeItemStack = AEItemStack.create(is);
        iaeItemStack.setStackSize(ias.getStackSize());
        return iaeItemStack;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.action = buf.readInt();
        this.slotIndex = buf.readInt();
        this.size = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.action);
        buf.writeInt(this.slotIndex);
        buf.writeLong(this.size);
    }

    public static class Handler implements IMessageHandler<CPacketLevelMaintainer, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(CPacketLevelMaintainer message, MessageContext ctx) {
            if (ctx.getServerHandler().playerEntity.openContainer instanceof ContainerLevelMaintainer) {
                EntityPlayerMP player = ctx.getServerHandler().playerEntity;
                final ContainerLevelMaintainer cca = (ContainerLevelMaintainer) ctx.getServerHandler().playerEntity.openContainer;
                if (message.action == -1) {
                    SPacketMEInventoryUpdate piu = new SPacketMEInventoryUpdate(false);
                    for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
                        IAEItemStack is = cca.getTile().requests.getCraftingSlots().getStack(i);
                        IAEItemStack is1 = cca.getTile().requests.getBatchInputs().getStack(i);
                        if (is != null) {
                            piu.appendItem(setTag(is, is1 != null ? is1.getStackSize() : 0, i));
                        }
                    }
                    FluidCraft.proxy.netHandler.sendTo(piu, player);
                } else {
                    cca.handleClientInteraction(message.action, message.slotIndex, message.size);
                }

            }
            return null;
        }
    }
}
