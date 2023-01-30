package com.glodblock.github.common.item;

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

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.inventory.item.WirelessTerminal;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemWirelessFluidTerminal extends ToolWirelessTerminal
        implements IItemInventory, IRegister<ItemWirelessFluidTerminal> {

    public ItemWirelessFluidTerminal() {
        super();
        AEApi.instance().registries().wireless().registerWirelessHandler(this);
        setUnlocalizedName(NameConst.ITEM_WIRELESS_FLUID_TERMINAL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_WIRELESS_FLUID_TERMINAL).toString());
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
                    new BlockPos((int) player.posX, (int) player.posY, (int) player.posZ),
                    ForgeDirection.UNKNOWN,
                    GuiType.WIRELESS_FLUID_TERMINAL);
        } else {
            player.addChatMessage(PlayerMessages.DeviceNotPowered.get());
        }

        return item;
    }

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() == this;
    }

    @Override
    public ItemWirelessFluidTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_FLUID_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            return new WirelessTerminal(stack, new BlockPos(x, y, z, world), player);
        } catch (Exception e) {
            player.addChatMessage(PlayerMessages.OutOfRange.get());
            return null;
        }
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
