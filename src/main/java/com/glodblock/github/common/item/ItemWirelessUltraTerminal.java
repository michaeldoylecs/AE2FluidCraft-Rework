package com.glodblock.github.common.item;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.glodblock.github.util.ModAndClassUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemWirelessUltraTerminal extends ItemBaseWirelessTerminal
        implements IRegister<ItemWirelessUltraTerminal> {

    public final static String MODE = "mode_main";
    private final static List<GuiType> guis = new ArrayList<>();

    public ItemWirelessUltraTerminal() {
        super(null);
        AEApi.instance().registries().wireless().registerWirelessHandler(this);
        this.setFeature(EnumSet.of(AEFeature.WirelessAccessTerminal, AEFeature.PoweredTools));
        setUnlocalizedName(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL);
        setTextureName(FluidCraft.resource(NameConst.ITEM_WIRELESS_ULTRA_TERMINAL).toString());
        guis.add(GuiType.WIRELESS_CRAFTING_TERMINAL);
        guis.add(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
        guis.add(GuiType.WIRELESS_FLUID_TERMINAL);
        if (ModAndClassUtil.ThE) {
            guis.add(GuiType.WIRELESS_ESSENTIA_TERMINAL);
        }
    }

    @Override
    public ItemWirelessUltraTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_WIRELESS_ULTRA_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> lines,
            boolean displayMoreInfo) {
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        lines.add(StatCollector.translateToLocal(NameConst.TT_ULTRA_TERMINAL));
        lines.add(StatCollector.translateToLocal(NameConst.TT_ULTRA_TERMINAL + "." + guiGuiType(stack)));
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted(
            "item.wireless_ultra_terminal." +
                guiGuiType(stack) +
                ".name"
        );
    }

    @Override
    public Object getInventory(ItemStack stack, World world, int x, int y, int z, EntityPlayer player) {
        try {
            IGridNode gridNode = Util.getWirelessGrid(stack);
            if (gridNode != null) {
                GuiType gui = readMode(stack);
                if (gui == GuiType.WIRELESS_FLUID_PATTERN_TERMINAL) {
                    return new WirelessPatternTerminalInventory(stack, x, gridNode, player);
                }
                if (gui == GuiType.WIRELESS_CRAFTING_TERMINAL) {
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
        if (player.isSneaking()) {
            setNext(readMode(item), item);
            return item;
        }
        readMode(item);
        return super.onItemRightClick(item, w, player);
    }

    public static GuiType readMode(ItemStack stack) {
        NBTTagCompound data = Platform.openNbtData(stack);
        if (data.hasKey(MODE)) {
            String GUI = data.getString(MODE);
            try {
                return GuiType.valueOf(GUI);
            } catch (IllegalArgumentException e) {
                return GuiType.WIRELESS_CRAFTING_TERMINAL;
            }
        } else {
            return GuiType.WIRELESS_CRAFTING_TERMINAL;
        }
    }

    public void setNext(GuiType type, ItemStack stack) {
        boolean f = false;
        for (GuiType g : guis) {
            if (f) {
                setMode(g.toString(), stack);
                return;
            }
            if (g == type) {
                f = true;
            }
        }
        setMode(guis.get(0).toString(), stack);
    }

    public void setMode(String mode, ItemStack stack) {
        NBTTagCompound data = Platform.openNbtData(stack);
        data.setString(MODE, mode);
    }

    @Override
    public GuiType guiGuiType(ItemStack stack) {
        return readMode(stack);
    }
}
