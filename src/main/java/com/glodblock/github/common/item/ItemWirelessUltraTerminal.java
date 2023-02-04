package com.glodblock.github.common.item;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.WirelessCraftingTerminalInventory;
import com.glodblock.github.inventory.item.WirelessFluidTerminalInventory;
import com.glodblock.github.inventory.item.WirelessPatternTerminalInventory;
import com.glodblock.github.loader.IRegister;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemWirelessUltraTerminal extends ItemBaseWirelessTerminal
        implements IRegister<ItemWirelessUltraTerminal> {

    public final static String MODE = "mode";

    public ItemWirelessUltraTerminal() {
        super(GuiType.WIRELESS_ESSENTIA_TERMINAL);
        AEApi.instance().registries().wireless().registerWirelessHandler(this);
        this.setFeature(EnumSet.of(AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools));
        setUnlocalizedName(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL).toString());
    }

    @Override
    public ItemWirelessUltraTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_ULTRA_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            IGridNode gridNode = Util.getWirelessGrid(stack);
            if (gridNode != null) {
                if (this.type == GuiType.WIRELESS_FLUID_PATTERN_TERMINAL) {
                    return new WirelessPatternTerminalInventory(stack, x, gridNode, player);
                }
                if (this.type == GuiType.WIRELESS_CRAFTING_TERMINAL) {
                    return new WirelessCraftingTerminalInventory(stack, x, gridNode, player);
                } else {
                    return new WirelessFluidTerminalInventory(stack, x, gridNode, player);
                }
            }
        } catch (Exception e) {
            if (Platform.isClient()) player.addChatMessage(PlayerMessages.OutOfRange.get());
        }
        return null;
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack item, final World w, final EntityPlayer player) {
        readMode(item);
        return super.onItemRightClick(item, w, player);
    }

    public void readMode(ItemStack stack) {
        NBTTagCompound data = Platform.openNbtData(stack);
        if (data.hasKey(MODE)) {
            String GUI = data.getString(MODE);
            try {
                this.type = GuiType.valueOf(GUI);
            } catch (IllegalArgumentException e) {
                this.type = GuiType.WIRELESS_CRAFTING_TERMINAL;
            }
        } else {
            this.type = GuiType.WIRELESS_CRAFTING_TERMINAL;
        }
    }

    public void setMode(String mode, ItemStack stack) {
        NBTTagCompound data = Platform.openNbtData(stack);
        data.setString(MODE, mode);
    }
}
