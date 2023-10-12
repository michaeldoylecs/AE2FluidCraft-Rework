package com.glodblock.github.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

import com.glodblock.github.client.gui.GuiLevelTerminal;

import appeng.core.AELog;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

/**
 * Packet used for level terminal updates. Packet allows the server to send an array of command packets, which are then
 * processed in order. This allows for chaining commands to produce the desired update.
 */
public class SPacketLevelTerminalUpdate implements IMessage {

    public static final int CLEAR_ALL_BIT = 1;
    public static final int DISCONNECT_BIT = 2;

    private final List<PacketEntry> commands = new ArrayList<>();
    private int statusFlags;

    public SPacketLevelTerminalUpdate() {}

    public void fromBytes(ByteBuf buf) {
        this.statusFlags = buf.readByte();
        int numEntries = buf.readInt();

        for (int i = 0; i < numEntries; ++i) {
            PacketType type = PacketType.values()[buf.readByte()];

            try {
                switch (type) {
                    case ADD -> this.commands.add(new PacketAdd(buf));
                    case REMOVE -> this.commands.add(new PacketRemove(buf));
                    case OVERWRITE -> this.commands.add(new PacketOverwrite(buf));
                    case RENAME -> this.commands.add(new PacketRename(buf));
                    default -> throw new IOException("Unknown packet type received of index " + type);
                }
            } catch (Exception e) {
                AELog.debug(e);
                return;
            }
        }
    }

    public void toBytes(ByteBuf buf) {
        try {
            buf.writeByte(statusFlags);
            buf.writeInt(commands.size());
            for (PacketEntry entry : commands) {
                entry.write(buf);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Remove all entries on the terminal. This is done BEFORE any entries are processed, so you can set this to clear
     * old entries, and add new ones after in one packet.
     */
    public void setClear() {
        statusFlags |= CLEAR_ALL_BIT;
    }

    /**
     * The terminal disconnected. Entries are still processed, but indicates for the GUI to darken/turn off the
     * terminal. No further updates will arrive until the terminal reconnects.
     */
    public void setDisconnect() {
        this.statusFlags |= DISCONNECT_BIT;
    }

    /**
     * Adds a new entry. Fill out the rest of the command using the {@link PacketAdd#setItems(int, int, NBTTagList)} and
     * {@link PacketAdd#setLocation(int, int, int, int, int)}.
     *
     * @return the packet, which needs to have information filled out.
     */
    public PacketAdd addNewEntry(long id, String name, boolean online) {
        PacketAdd packet = new PacketAdd(id, name, online);

        commands.add(packet);
        return packet;
    }

    /**
     * Remove the entry with the id from the terminal.
     */
    public void addRemovalEntry(long id) {
        commands.add(new PacketRemove(id));
    }

    /**
     * Rename the entry
     */
    public void addRenamedEntry(long id, String newName) {
        commands.add(new PacketRename(id, newName));
    }

    /**
     * Overwrite the entry with new items or new status
     */
    public PacketOverwrite addOverwriteEntry(long id) {
        PacketOverwrite packet = new PacketOverwrite(id);

        commands.add(packet);
        return packet;
    }

    public static class Handler implements IMessageHandler<SPacketLevelTerminalUpdate, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(SPacketLevelTerminalUpdate message, MessageContext ctx) {
            final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

            if (gs instanceof GuiLevelTerminal levelTerminal) {
                levelTerminal.postUpdate(message.commands, message.statusFlags);
            }

            return null;
        }
    }

    enum PacketType {
        ADD,
        REMOVE,
        OVERWRITE,
        RENAME,
    }

    /**
     * A packet for updating an entry.
     */
    public abstract static class PacketEntry {

        public final long entryId;

        protected PacketEntry(long entryId) {
            this.entryId = entryId;
        }

        protected PacketEntry(ByteBuf buf) throws IOException {
            this.entryId = buf.readLong();
            read(buf);
        }

        /**
         * Needs to write the packet id.
         */
        protected abstract void write(ByteBuf buf) throws IOException;

        /**
         * Reading the entry id is not needed.
         */
        protected abstract void read(ByteBuf buf) throws IOException;
    }

    /**
     * A command for sending a new entry.
     */
    public static class PacketAdd extends PacketEntry {

        public String name;
        public int x, y, z, dim, side;
        public int rows, rowSize;
        public boolean online;
        public ItemStack selfItemStack;
        public ItemStack displayItemStack;
        public NBTTagList items;

        PacketAdd(long id, String name, boolean online) {
            super(id);
            this.name = name;
            this.online = online;
        }

        PacketAdd(ByteBuf buf) throws IOException {
            super(buf);
        }

        public PacketAdd setLocation(int x, int y, int z, int dim, int side) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            this.side = side;
            return this;
        }

        public PacketAdd setItems(int rows, int rowSize, NBTTagList items) {
            this.rows = rows;
            this.rowSize = rowSize;
            this.items = items;

            return this;
        }

        public PacketAdd setViewItemStack(ItemStack selfItemStack, ItemStack displayItemStack) {
            this.selfItemStack = selfItemStack;
            this.displayItemStack = displayItemStack;

            return this;
        }

        @Override
        protected void write(ByteBuf buf) throws IOException {
            buf.writeByte(PacketType.ADD.ordinal());
            buf.writeLong(entryId);
            ByteBufUtils.writeUTF8String(buf, this.name);
            buf.writeInt(x);
            buf.writeInt(y);
            buf.writeInt(z);
            buf.writeInt(dim);
            buf.writeInt(side);
            buf.writeInt(rows);
            buf.writeInt(rowSize);

            ByteBuf tempBuf = Unpooled.directBuffer(256);
            try {
                try (ByteBufOutputStream stream = new ByteBufOutputStream(tempBuf)) {

                    NBTTagCompound wrapper = new NBTTagCompound();

                    if (selfItemStack != null) {
                        wrapper.setTag("self", selfItemStack.writeToNBT(new NBTTagCompound()));
                    }
                    if (displayItemStack != null) {
                        wrapper.setTag("display", displayItemStack.writeToNBT(new NBTTagCompound()));
                    }
                    wrapper.setTag("data", items);
                    CompressedStreamTools.writeCompressed(wrapper, stream);
                }
                buf.writeInt(tempBuf.readableBytes());
                buf.writeBytes(tempBuf);
            } finally {
                tempBuf.release();
            }
        }

        @Override
        protected void read(ByteBuf buf) throws IOException {
            this.name = ByteBufUtils.readUTF8String(buf);
            this.x = buf.readInt();
            this.y = buf.readInt();
            this.z = buf.readInt();
            this.dim = buf.readInt();
            this.side = buf.readInt();
            this.rows = buf.readInt();
            this.rowSize = buf.readInt();
            int payloadSize = buf.readInt();
            try (ByteBufInputStream stream = new ByteBufInputStream(buf, payloadSize)) {
                NBTTagCompound payload = CompressedStreamTools.readCompressed(stream);
                if (payload.hasKey("self", NBT.TAG_COMPOUND)) {
                    this.selfItemStack = ItemStack.loadItemStackFromNBT(payload.getCompoundTag("self"));
                }

                if (payload.hasKey("display", NBT.TAG_COMPOUND)) {
                    this.displayItemStack = ItemStack.loadItemStackFromNBT(payload.getCompoundTag("display"));
                }

                this.items = payload.getTagList("data", NBT.TAG_COMPOUND);
            }
        }
    }

    public static class PacketRemove extends PacketEntry {

        PacketRemove(long id) {
            super(id);
        }

        PacketRemove(ByteBuf buf) throws IOException {
            super(buf);
        }

        @Override
        protected void write(ByteBuf buf) {
            buf.writeByte(PacketType.REMOVE.ordinal());
            buf.writeLong(entryId);
        }

        @Override
        protected void read(ByteBuf buf) {}
    }

    /**
     * Overwrite online status or inventory of the entry.
     */
    public static class PacketOverwrite extends PacketEntry {

        public static final int ONLINE_BIT = 1;
        public static final int ONLINE_VALID = 1 << 1;
        public static final int ITEMS_VALID = 1 << 2;
        public static final int ALL_ITEM_UPDATE_BIT = 1 << 3;
        public boolean onlineValid;
        public boolean online;
        public boolean itemsValid;
        public boolean allItemUpdate;
        public int[] validIndices;
        public NBTTagList items;

        protected PacketOverwrite(long id) {
            super(id);
        }

        protected PacketOverwrite(ByteBuf buf) throws IOException {
            super(buf);
        }

        public PacketOverwrite setOnline(boolean online) {
            this.onlineValid = true;
            this.online = online;
            return this;
        }

        public PacketOverwrite setItems(int[] validIndices, NBTTagList items) {
            this.itemsValid = true;
            this.allItemUpdate = validIndices == null || validIndices.length == 0;
            this.validIndices = validIndices;
            this.items = items;

            return this;
        }

        public PacketOverwrite setItems(int newSize, NBTTagList items) {
            this.itemsValid = true;
            this.allItemUpdate = true;
            this.validIndices = new int[newSize];
            this.items = items;

            return this;
        }

        @Override
        protected void write(ByteBuf buf) throws IOException {
            buf.writeByte(PacketType.OVERWRITE.ordinal());
            buf.writeLong(entryId);

            int flags = 0;

            if (onlineValid) {
                flags |= ONLINE_VALID;
                flags |= online ? ONLINE_BIT : 0;
            }
            if (itemsValid) {
                flags |= ITEMS_VALID;
                if (allItemUpdate) {
                    flags |= ALL_ITEM_UPDATE_BIT;
                    buf.writeByte(flags);
                    buf.writeInt(validIndices.length);
                } else {
                    buf.writeByte(flags);
                    buf.writeInt(validIndices.length);
                    for (int validIndex : validIndices) {
                        buf.writeInt(validIndex);
                    }
                }
                try (ByteBufOutputStream stream = new ByteBufOutputStream(buf)) {

                    NBTTagCompound wrapper = new NBTTagCompound();

                    wrapper.setTag("data", items);
                    CompressedStreamTools.writeCompressed(wrapper, stream);
                }
            } else {
                buf.writeByte(flags);
            }
        }

        @Override
        protected void read(ByteBuf buf) throws IOException {
            int flags = buf.readByte();

            /* Decide whether to use online flag or not */
            if ((flags & ONLINE_VALID) == ONLINE_VALID) {
                this.onlineValid = true;
                this.online = (flags & ONLINE_BIT) == ONLINE_BIT;
            }
            /* Decide whether to read item list or not */
            if ((flags & ITEMS_VALID) == ITEMS_VALID) {
                this.itemsValid = true;
                if ((flags & ALL_ITEM_UPDATE_BIT) == ALL_ITEM_UPDATE_BIT) {
                    this.allItemUpdate = true;
                    int numItems = buf.readInt();
                    this.validIndices = new int[numItems];
                } else {
                    int numItems = buf.readInt();
                    this.validIndices = new int[numItems];
                    for (int i = 0; i < numItems; ++i) {
                        this.validIndices[i] = buf.readInt();
                    }
                }

                try (ByteBufInputStream stream = new ByteBufInputStream(buf)) {
                    this.items = CompressedStreamTools.readCompressed(stream).getTagList("data", NBT.TAG_COMPOUND);
                }
            }
        }
    }

    /**
     * Rename the entry.
     */
    public static class PacketRename extends PacketEntry {

        public String newName;

        protected PacketRename(long id, String newName) {
            super(id);
            this.newName = newName;
        }

        protected PacketRename(ByteBuf buf) throws IOException {
            super(buf);
        }

        @Override
        protected void write(ByteBuf buf) {
            buf.writeByte(PacketType.RENAME.ordinal());
            buf.writeLong(entryId);
            ByteBufUtils.writeUTF8String(buf, newName);
        }

        @Override
        protected void read(ByteBuf buf) {
            newName = ByteBufUtils.readUTF8String(buf);
        }
    }
}
