package com.glodblock.github.client.gui;

import appeng.api.storage.ITerminalHost;
import appeng.container.slot.AppEngSlot;
import com.glodblock.github.client.gui.container.FCBaseFluidMonitorContain;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;


public class GuiBaseFluidTerminal extends GuiFCBaseFluidMonitor {
    protected EntityPlayer player;

    public GuiBaseFluidTerminal(InventoryPlayer inventoryPlayer, ITerminalHost te, FCBaseFluidMonitorContain c) {
        super(inventoryPlayer, te, new FCBaseFluidMonitorContain(inventoryPlayer, te));
        player = inventoryPlayer.player;
    }

    @Override
    protected String getBackground() {
        return "gui/terminal.png";
    }

    @Override
    protected void repositionSlot(final AppEngSlot s) {
        if (s.isPlayerSide()) {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
        } else {
            s.yDisplayPosition = s.getY() + this.ySize - 78 - 3;
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == craftingStatusBtn) {
            InventoryHandler.switchGui(GuiType.FLUID_PAT_TERM_CRAFTING_STATUS);
        } else {
            super.actionPerformed(btn);
        }
    }

}
