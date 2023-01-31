package com.glodblock.github.common.item;

import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.items.tools.powered.ToolWirelessTerminal;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.util.BlockPos;

public class ItemBaseWirelessTerminal extends ToolWirelessTerminal implements IItemInventory {

    protected GuiType type;

    public ItemBaseWirelessTerminal(GuiType t) {
        super();
        this.type = Optional.of(t).get();
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        IWirelessTermRegistry term = AEApi.instance().registries().wireless();
        if (!term.isWirelessTerminal(item)) {
            player.addChatMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
            return item;
        }
        final IWirelessTermHandler handler = term.getWirelessTerminalHandler(item);
        final String unparsedKey = handler.getEncryptionKey(item);
        if (unparsedKey.isEmpty()) {
            player.addChatMessage(PlayerMessages.DeviceNotLinked.get());
            return item;
        }
        final long parsedKey = Long.parseLong(unparsedKey);
        final ILocatable securityStation = AEApi.instance().registries().locatable().getLocatableBy(parsedKey);
        if (securityStation == null) {
            player.addChatMessage(PlayerMessages.StationCanNotBeLocated.get());
            return item;
        }
        if (handler.hasPower(player, 0.5, item)) {
            InventoryHandler.openGui(
                    player,
                    w,
                    new BlockPos(player.inventory.currentItem, 0, 0),
                    ForgeDirection.UNKNOWN,
                    this.type);
        } else {
            player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
        }

        return item;
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() == this;
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        return null;
    }

    public void setGuiType(GuiType type) {
        this.type = type;
    }

    public GuiType guiGuiType() {
        return this.type;
    }
}
