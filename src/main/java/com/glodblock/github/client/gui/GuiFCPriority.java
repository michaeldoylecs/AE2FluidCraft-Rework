package com.glodblock.github.client.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import com.glodblock.github.common.parts.PartFluidStorageBus;
import com.glodblock.github.inventory.IDualHost;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerPriority;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.IPriorityHost;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;

public class GuiFCPriority extends AEBaseGui {

    protected GuiTextField amountBox;
    protected GuiTabButton originalGuiBtn;
    protected GuiButton plus1;
    protected GuiButton plus10;
    protected GuiButton plus100;
    protected GuiButton plus1000;
    protected GuiButton minus1;
    protected GuiButton minus10;
    protected GuiButton minus100;
    protected GuiButton minus1000;
    protected GuiType originalGui;
    protected ItemStack myIcon;

    public GuiFCPriority(final InventoryPlayer inventoryPlayer, final IPriorityHost te) {
        super(new ContainerPriority(inventoryPlayer, te));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        final int a = AEConfig.instance.priorityByStacksAmounts(0);
        final int b = AEConfig.instance.priorityByStacksAmounts(1);
        final int c = AEConfig.instance.priorityByStacksAmounts(2);
        final int d = AEConfig.instance.priorityByStacksAmounts(3);

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d));

        setOriginGUI(((AEBaseContainer) this.inventorySlots).getTarget());
        if (this.originalGui != null && this.myIcon != null) {
            this.buttonList.add(
                    this.originalGuiBtn = new GuiTabButton(
                            this.guiLeft + 151,
                            this.guiTop - 4,
                            this.myIcon,
                            this.myIcon.getDisplayName(),
                            itemRender));
            this.originalGuiBtn.setHideEdge(13);
        }

        this.amountBox = new GuiTextField(
                this.fontRendererObj,
                this.guiLeft + 62,
                this.guiTop + 57,
                59,
                this.fontRendererObj.FONT_HEIGHT);
        this.amountBox.setEnableBackgroundDrawing(false);
        this.amountBox.setMaxStringLength(16);
        this.amountBox.setTextColor(0xFFFFFF);
        this.amountBox.setVisible(true);
        this.amountBox.setFocused(true);

        ((ContainerPriority) this.inventorySlots).setTextField(this.amountBox);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.Priority.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.bindTexture(getBackground());
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.amountBox.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        if (btn == this.originalGuiBtn) {
            InventoryHandler.switchGui(originalGui);
        }
        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10
                || btn == this.minus100
                || btn == this.minus1000;

        if (isPlus || isMinus) {
            this.addQty(this.getQty(btn));
        }
    }

    protected void addQty(final int i) {
        try {
            this.amountBox.setText(Long.toString(getAmount() + i));
        } catch (final NumberFormatException ignore) {}
        try {
            NetworkHandler.instance
                    .sendToServer(new PacketValueConfig("PriorityHost.Priority", String.valueOf(getAmount())));
        } catch (IOException e) {
            AELog.debug(e);
        }
    }

    protected void setOriginGUI(Object target) {
        if (target instanceof IDualHost) {
            this.myIcon = ItemAndBlockHolder.INTERFACE.stack();
            this.originalGui = GuiType.DUAL_INTERFACE;
        } else if (target instanceof PartFluidStorageBus) {
            this.myIcon = ItemAndBlockHolder.FLUID_STORAGE_BUS.stack();
            this.originalGui = GuiType.FLUID_STORAGE_BUS;
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            this.amountBox.textboxKeyTyped(character, key);
            super.keyTyped(character, key);
        }
        try {
            NetworkHandler.instance
                    .sendToServer(new PacketValueConfig("PriorityHost.Priority", String.valueOf(getAmount())));
        } catch (IOException e) {
            AELog.debug(e);
        }
    }

    protected int getAmount() {
        try {
            String out = this.amountBox.getText();
            double result = Calculator.conversion(out);
            if (Double.isNaN(result)) {
                return 0;
            } else {
                return (int) ArithHelper.round(result, 0);
            }
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    protected String getBackground() {
        return "guis/priority.png";
    }
}
