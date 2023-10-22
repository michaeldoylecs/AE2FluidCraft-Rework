package com.glodblock.github.nei;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.client.gui.GuiFluidMonitor;
import com.glodblock.github.client.gui.GuiItemMonitor;
import com.glodblock.github.client.gui.GuiLevelMaintainer;

import codechicken.nei.api.INEIGuiAdapter;

public class AE2FC_NEIGuiHandler extends INEIGuiAdapter {

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        if (gui instanceof GuiItemMonitor) {
            return ((GuiItemMonitor) gui).hideItemPanelSlot(x, y, w, h);
        } else if (gui instanceof GuiFluidMonitor) {
            return ((GuiFluidMonitor) gui).hideItemPanelSlot(x, y, w, h);
        }
        return false;
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        if (gui instanceof GuiLevelMaintainer guiLevelMaintainer) {
            return guiLevelMaintainer.handleDragNDrop(gui, mouseX, mouseY, draggedStack, button);
        }
        return super.handleDragNDrop(gui, mouseX, mouseY, draggedStack, button);
    }
}
