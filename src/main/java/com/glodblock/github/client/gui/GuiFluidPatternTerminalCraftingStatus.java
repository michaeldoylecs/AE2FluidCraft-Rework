package com.glodblock.github.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.storage.ITerminalHost;
import appeng.client.gui.implementations.GuiCraftingStatus;
import appeng.client.gui.widgets.GuiTabButton;

import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.common.parts.PartFluidTerminal;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.item.WirelessFluidTerminal;
import com.glodblock.github.inventory.item.WirelessPatternTerminal;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.Ae2ReflectClient;

public class GuiFluidPatternTerminalCraftingStatus extends GuiCraftingStatus {

    private GuiTabButton originalGuiBtn;
    private final ITerminalHost host;

    public GuiFluidPatternTerminalCraftingStatus(InventoryPlayer inventoryPlayer, ITerminalHost te) {
        super(inventoryPlayer, te);
        host = te;
    }

    @Override
    public void initGui() {
        if (host instanceof PartFluidPatternTerminal)
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL, 1));
        else if (host instanceof PartFluidPatternTerminalEx)
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL_EX, 1));
        else if (host instanceof PartFluidTerminal) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.FLUID_TERM, 1));
        } else if (host instanceof WirelessFluidTerminal) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_FLUID_TERM, 1));
        } else if (host instanceof WirelessPatternTerminal) {
            Ae2ReflectClient.rewriteIcon(this, new ItemStack(ItemAndBlockHolder.WIRELESS_PATTERN_TERM, 1));
        }

        super.initGui();
        originalGuiBtn = Ae2ReflectClient.getOriginalGuiButton(this);
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == originalGuiBtn) {
            if (host instanceof PartFluidPatternTerminal) InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL);
            else if (host instanceof PartFluidPatternTerminalEx)
                InventoryHandler.switchGui(GuiType.FLUID_PATTERN_TERMINAL_EX);
            else if (host instanceof PartFluidTerminal) InventoryHandler.switchGui(GuiType.FLUID_TERMINAL);
            else if (host instanceof WirelessFluidTerminal) InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_TERMINAL);
            else if (host instanceof WirelessPatternTerminal)
                InventoryHandler.switchGui(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
        } else {
            super.actionPerformed(btn);
        }
    }
}
