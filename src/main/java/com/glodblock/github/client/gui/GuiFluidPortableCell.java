package com.glodblock.github.client.gui;

import appeng.container.slot.AppEngSlot;
import com.glodblock.github.client.gui.container.ContainerFluidPortableCell;
import com.glodblock.github.inventory.item.IFluidPortableCell;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiFluidPortableCell extends GuiFluidTerminal {

    public GuiFluidPortableCell(final InventoryPlayer inventoryPlayer, final IFluidPortableCell te) {
        super(inventoryPlayer, te, new ContainerFluidPortableCell(inventoryPlayer, te));
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    protected int getMaxRows() {
        return 3;
    }
}
