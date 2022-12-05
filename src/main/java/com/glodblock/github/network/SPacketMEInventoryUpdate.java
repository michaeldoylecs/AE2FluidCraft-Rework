package com.glodblock.github.network;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.client.gui.GuiFCBaseFluidMonitor;
import com.glodblock.github.client.gui.GuiFCBaseMonitor;
import com.glodblock.github.client.gui.GuiFluidCraftConfirm;
import com.glodblock.github.client.gui.GuiLevelMaintainer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class SPacketMEInventoryUpdate implements IMessage {

    private List<Object> list;
    private byte ref;
    private boolean isFluid = false;

    public SPacketMEInventoryUpdate() {
        ref = 0;
        list = new LinkedList<>();
    }

    public SPacketMEInventoryUpdate(Boolean isFluid) {
        ref = 0;
        list = new LinkedList<>();
        this.isFluid = isFluid;
    }

    public SPacketMEInventoryUpdate(byte b) {
        ref = b;
        list = new LinkedList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isFluid = buf.readBoolean();
        long amount = buf.readLong();
        ref = buf.readByte();
        list = new LinkedList<>();
        try {
            for (int i = 0; i < amount; i++) {
                if (isFluid) {
                    list.add(AEFluidStack.loadFluidStackFromPacket(buf));
                } else {
                    list.add(AEItemStack.loadItemStackFromPacket(buf));
                }
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (isFluid) {
            buf.writeBoolean(true);
        } else {
            buf.writeBoolean(false);
        }
        buf.writeLong(list.size());
        buf.writeByte(ref);
        try {
            for (Object is : list) {
                if (is instanceof IAEItemStack) ((IAEItemStack) is).writeToPacket(buf);
                if (is instanceof IAEFluidStack) ((IAEFluidStack) is).writeToPacket(buf);
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public void appendItem(final IAEItemStack is) throws BufferOverflowException {
        list.add(is);
    }

    public void appendFluid(final IAEFluidStack is) throws BufferOverflowException {
        list.add(is);
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public static class Handler implements IMessageHandler<SPacketMEInventoryUpdate, IMessage> {

        @Override
        public IMessage onMessage(SPacketMEInventoryUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
            if (gs instanceof GuiFCBaseFluidMonitor) {
                ((GuiFCBaseFluidMonitor) gs).postUpdate((List<IAEFluidStack>) (List<?>) message.list);
            } else if (gs instanceof GuiFCBaseMonitor) {
                ((GuiFCBaseMonitor) gs).postUpdate((List<IAEItemStack>) (List<?>) message.list);
            } else if (gs instanceof GuiFluidCraftConfirm) {
                ((GuiFluidCraftConfirm) gs).postUpdate((List<IAEItemStack>) (List<?>) message.list, message.ref);
            } else if (gs instanceof GuiLevelMaintainer) {
                ((GuiLevelMaintainer) gs).postUpdate((List<IAEItemStack>) (List<?>) message.list);
            }
            return null;
        }
    }
}
