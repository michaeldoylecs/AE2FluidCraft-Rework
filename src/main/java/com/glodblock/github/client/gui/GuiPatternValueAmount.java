package com.glodblock.github.client.gui;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IParts;
import appeng.api.storage.ITerminalHost;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.container.AEBaseContainer;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.helpers.Reflected;
import appeng.util.calculators.ArithHelper;
import appeng.util.calculators.Calculator;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerPatternValueAmount;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.network.CPacketPatternValueSet;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class GuiPatternValueAmount extends AEBaseGui {

    private GuiTextField amountToSet;
    private GuiTabButton originalGuiBtn;

    private GuiButton set;

    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;

    private GuiType originalGui;
    private final int valueIndex;
    private final int originalAmount;

    @Reflected
    public GuiPatternValueAmount(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        super(new ContainerPatternValueAmount(inventoryPlayer, te));
        GuiContainer gui = (GuiContainer) Minecraft.getMinecraft().currentScreen;
        if (gui != null && gui.theSlot != null && gui.theSlot.getHasStack()) {
            Slot slot = gui.theSlot;
            originalAmount = this.getOriginalAmount(slot.getStack());
            valueIndex = slot.slotNumber;
        } else {
            valueIndex = -1;
            originalAmount = 0;
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        final int a = AEConfig.instance.craftItemsByStackAmounts(0);
        final int b = AEConfig.instance.craftItemsByStackAmounts(1);
        final int c = AEConfig.instance.craftItemsByStackAmounts(2);
        final int d = AEConfig.instance.craftItemsByStackAmounts(3);

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 26, 22, 20, "+" + a));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 26, 28, 20, "+" + b));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 26, 32, 20, "+" + c));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 26, 38, 20, "+" + d));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 75, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 75, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 75, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 75, 38, 20, "-" + d));

        this.buttonList.add(this.set = new GuiButton(0, this.guiLeft + 128, this.guiTop + 51, 38, 20, GuiText.Next.getLocal()));


        ItemStack myIcon = null;
        final Object target = ((AEBaseContainer) this.inventorySlots).getTarget();
        final IDefinitions definitions = AEApi.instance().definitions();
        final IParts parts = definitions.parts();

        if (target instanceof PartFluidPatternTerminal) {
            myIcon = new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL, 1);
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL;
        }

        if (target instanceof PartFluidPatternTerminalEx) {
            myIcon = new ItemStack(ItemAndBlockHolder.FLUID_TERMINAL_EX, 1);
            this.originalGui = GuiType.FLUID_PATTERN_TERMINAL_EX;
        }

        if (this.originalGui != null && myIcon != null) {
            this.buttonList.add(this.originalGuiBtn = new GuiTabButton(this.guiLeft + 154, this.guiTop, myIcon, myIcon.getDisplayName(), itemRender));
        }

        this.amountToSet = new GuiTextField(this.fontRendererObj, this.guiLeft + 62, this.guiTop + 57, 59, this.fontRendererObj.FONT_HEIGHT);
        this.amountToSet.setEnableBackgroundDrawing(false);
        this.amountToSet.setMaxStringLength(16);
        this.amountToSet.setTextColor(0xFFFFFF);
        this.amountToSet.setVisible(true);
        this.amountToSet.setFocused(true);
        this.amountToSet.setText(String.valueOf(originalAmount));
        this.amountToSet.setSelectionPos(0);

    }


    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(GuiText.SelectAmount.getLocal(), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.set.displayString = I18n.format("ae2fc.gui.button.set");
        this.set.enabled = valueIndex >= 0;

        this.bindTexture("guis/craftAmt.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        try {
            String out = this.amountToSet.getText();

            double resultD = Calculator.conversion(out);
            int resultI;

            if (resultD <= 0 || Double.isNaN(resultD)) {
                resultI = 0;
            } else {
                resultI = (int) ArithHelper.round(resultD, 0);
            }

            this.set.enabled = resultI > 0;
        } catch (final NumberFormatException e) {
            this.set.enabled = false;
        }

        this.amountToSet.drawTextBox();
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if (key == 28) {
                this.actionPerformed(this.set);
            }
            this.amountToSet.textboxKeyTyped(character, key);
            super.keyTyped(character, key);
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);

        try {

            if (btn == this.originalGuiBtn) {
                FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(this.originalGui));
            }

            if (btn == this.set && btn.enabled) {
                double resultD = Calculator.conversion(this.amountToSet.getText());
                int resultI;

                if (resultD <= 0 || Double.isNaN(resultD)) {
                    resultI = 1;
                } else {
                    resultI = (int) ArithHelper.round(resultD, 0);
                }

                FluidCraft.proxy.netHandler.sendToServer(new CPacketPatternValueSet(originalGui.ordinal(), resultI, valueIndex));
            }
        } catch (final NumberFormatException e) {
            // nope..
            this.amountToSet.setText("1");
        }

        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;

        if (isPlus || isMinus) {
            this.addQty(this.getQty(btn));
        }
    }

    private void addQty(final int i) {
        try {
            String out = this.amountToSet.getText();

            double resultD = Calculator.conversion(out);
            int resultI;

            if (resultD <= 0 || Double.isNaN(resultD)) {
                resultI = 0;
            } else {
                resultI = (int) ArithHelper.round(resultD, 0);
            }

            if (resultI == 1 && i > 1) {
                resultI = 0;
            }

            resultI += i;
            if (resultI < 1) {
                resultI = 1;
            }

            out = Integer.toString(resultI);

            this.amountToSet.setText(out);
        } catch (final NumberFormatException e) {
            // :P
        }
    }

    private int getOriginalAmount(ItemStack stack) {
        if (Util.isFluidPacket(stack)) {
            FluidStack fluidStack = ItemFluidPacket.getFluidStack(stack);
            return fluidStack.amount;
        } else {
            return stack.stackSize;
        }
    }

}
