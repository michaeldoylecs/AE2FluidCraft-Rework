package com.glodblock.github.network;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.item.WirelessCraftingTerminalInventory;
import com.glodblock.github.util.Util;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class CPacketSelectBlockWithdraw implements IMessage {

    private int blockX;
    private int blockY;
    private int blockZ;

    public CPacketSelectBlockWithdraw() {
        // Required for FML
    }

    public CPacketSelectBlockWithdraw(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.blockX = buf.readInt();
        this.blockY = buf.readInt();
        this.blockZ = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.blockX);
        buf.writeInt(this.blockY);
        buf.writeInt(this.blockZ);
    }

    public static class Handler implements IMessageHandler<CPacketSelectBlockWithdraw, IMessage> {

        @Override
        public IMessage onMessage(CPacketSelectBlockWithdraw message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.inventory == null) {
                return null;
            }
            World world = player.worldObj;

            // Ensure the player has the wireless terminal
            ImmutablePair<Integer, ItemStack> terminalAndInventorySlot = Util.getUltraWirelessTerm(player);
            if (terminalAndInventorySlot == null) {
                return null;
            }

            ItemStack terminalStack = terminalAndInventorySlot.getRight();
            if (terminalStack == null || !(terminalStack.getItem() instanceof ItemWirelessUltraTerminal)) {
                return null;
            }

            // Create the terminal inventory handler
            WirelessCraftingTerminalInventory terminalInventory = new WirelessCraftingTerminalInventory(
                    terminalStack,
                    terminalAndInventorySlot.getLeft(),
                    Util.getWirelessGrid(terminalStack), // This provides the IGrid
                    player);

            // Get the target block
            Block targetBlock = world.getBlock(message.blockX, message.blockY, message.blockZ);
            if (targetBlock == Blocks.air) {
                return null; // Don't try to withdraw air
            }

            Item targetItem = Item.getItemFromBlock(targetBlock);
            if (targetItem == null) {
                return null; // Should not happen for non-air blocks
            }

            // Create an ItemStack for the target block, try to get a full stack
            ItemStack targetItemStack = new ItemStack(
                    targetItem,
                    1,
                    world.getBlockMetadata(message.blockX, message.blockY, message.blockZ));
            targetItemStack.stackSize = targetItem.getItemStackLimit(targetItemStack);

            IAEItemStack targetAeItemStack = AEApi.instance().storage().createItemStack(targetItemStack);
            if (targetAeItemStack == null) {
                return null;
            }

            // Extract items from the network
            IAEStack<?> extractedStack = terminalInventory.extractItems(targetAeItemStack);

            if (extractedStack instanceof IAEItemStack extractedAeItemStack && extractedStack.getStackSize() > 0) {
                ItemStack itemsToGive = extractedAeItemStack.getItemStack();
                if (itemsToGive != null && itemsToGive.stackSize > 0) {
                    player.inventory.addItemStackToInventory(itemsToGive);
                    player.inventory.markDirty(); // Mark inventory as dirty to sync with client
                    // If player has a container open, update it
                    if (player.openContainer != null) {
                        player.openContainer.detectAndSendChanges();
                    }
                }
            }
            return null; // No reply packet needed
        }
    }
}
