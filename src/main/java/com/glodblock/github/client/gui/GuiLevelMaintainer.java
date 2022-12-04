package com.glodblock.github.client.gui;

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.render.AppEngRenderItem;
import appeng.container.slot.SlotFake;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketNEIDragClick;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.ContainerLevelMaintainer;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.gui.MouseRegionManager;
import com.glodblock.github.inventory.slot.SlotSingleItem;
import com.glodblock.github.network.CPacketLevelMaintainer;
import com.glodblock.github.util.Ae2ReflectClient;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.Optional;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiLevelMaintainer extends AEBaseGui implements INEIGuiHandler {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private final ContainerLevelMaintainer cont;
    private final Component[] component = new Component[TileLevelMaintainer.REQ_COUNT];
    private final MouseRegionManager mouseRegions = new MouseRegionManager(this);
    private final AppEngRenderItem stackSizeRenderer = Ae2ReflectClient.getStackSizeRenderer(this);
    private TileLevelMaintainer tile;
    private FCGuiTextField input;

    public GuiLevelMaintainer(InventoryPlayer ipl, TileLevelMaintainer tile) {
        super(new ContainerLevelMaintainer(ipl, tile));
        this.cont = (ContainerLevelMaintainer) inventorySlots;
        this.xSize = 195;
        this.ySize = 214;
        this.tile = tile;
    }

    public void postUpdate(List<IAEItemStack> list) {
        for (IAEItemStack is : list) {
            long size = is.getItemStack().getTagCompound().getLong("Batch");
            int i = is.getItemStack().getTagCompound().getInteger("Index");
            boolean isEnable = is.getItemStack().getTagCompound().getBoolean("Enable");
            component[i].getQty().textField.setText(String.valueOf(is.getStackSize()));
            component[i].getBatch().textField.setText(String.valueOf(size));
            component[i].setEnable(isEnable);
        }
    }

    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        if (ModAndClassUtil.isSearchBar) {
            for (Component com : this.component) {
                handleTooltip(mouseX, mouseY, com.getQty().textField.new TooltipProvider());
                handleTooltip(mouseX, mouseY, com.getBatch().textField.new TooltipProvider());
            }
        }
    }

    public void initGui() {
        super.initGui();
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            component[i] = new Component(
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 37, guiTop + 21 + 20 * i, 52, 16),
                            new GuiFCImgButton(guiLeft + 37 + 47, guiTop + 20 + 20 * i, "SUBMIT", "SUBMIT"),
                            I18n.format(NameConst.TT_REQUEST_SIZE),
                            i,
                            "TileLevelMaintainer.Quantity"),
                    new Widget(
                            new FCGuiTextField(this.fontRendererObj, guiLeft + 100, guiTop + 21 + 20 * i, 52, 16),
                            new GuiFCImgButton(guiLeft + 100 + 47, guiTop + 20 + 20 * i, "SUBMIT", "SUBMIT"),
                            I18n.format(NameConst.TT_BATCH_SIZE),
                            i,
                            "TileLevelMaintainer.Batch"),
                    new GuiFCImgButton(guiLeft + 4, guiTop + 23 + 20 * i, "ENABLE", "ENABLE"),
                    new GuiFCImgButton(guiLeft + 4, guiTop + 23 + 20 * i, "DISABLE", "DISABLE"),
                    this.buttonList);
        }
        FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer("TileLevelMaintainer.refresh"));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            this.component[i].draw();
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot0(s)) super.func_146977_a(s);
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotFake) {
            IAEItemStack stack = ((ContainerLevelMaintainer.FakeSlot) slot).getAeStack();
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return true;
            IAEItemStack fake = stack.copy();
            if (this.component[slot.getSlotIndex()].getQty().textField.getText().matches("[0-9]+")) {
                fake.setStackSize(Long.parseLong(
                        this.component[slot.getSlotIndex()].getQty().textField.getText()));
            } else {
                fake.setStackSize(0);
            }
            stackSizeRenderer.setAeStack(fake);
            stackSizeRenderer.renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    stack.getItemStack(),
                    slot.xDisplayPosition,
                    slot.yDisplayPosition);
            return false;
        }
        return true;
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        if (btn == 0) {
            if (input != null) {
                input.setFocused(false);
            }
            for (Component com : this.component) {
                FCGuiTextField textField = com.isMouseIn(xCoord, yCoord);
                if (textField != null) {
                    textField.setFocused(true);
                    this.input = textField;
                    super.mouseClicked(xCoord, yCoord, btn);
                    return;
                }
            }
            this.input = null;
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (this.input == null) {
            super.keyTyped(character, key);
            return;
        }
        if (!this.checkHotbarKeys(key)) {
            if (!((character == ' ') && this.input.getText().isEmpty())) {
                this.input.textboxKeyTyped(character, key);
            }
            super.keyTyped(character, key);
            if (!this.input.getText().matches("^[0-9]+")) {
                this.input.setTextColor(0xFF0000);
            } else {
                this.input.setTextColor(0xFFFFFF);
            }
            if (key == Keyboard.KEY_RETURN) {
                this.input.setFocused(false);
                this.input = null;
            }
        }
    }

    @Override
    protected void handleMouseClick(final Slot slot, final int slotIdx, final int ctrlDown, final int mouseButton) {
        if (slot instanceof ContainerLevelMaintainer.FakeSlot
                && this.cont.getPlayerInv().getItemStack() != null) {
            this.component[slot.getSlotIndex()]
                    .getQty()
                    .textField
                    .setText(String.valueOf(this.cont.getPlayerInv().getItemStack().stackSize));
            return;
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    public void updateAmount(int idx, int stackSize) {
        this.component[idx].getQty().textField.setText(String.valueOf(stackSize));
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        for (Component com : this.component) {
            if (com.sendToServer(btn)) {
                break;
            }
        }
    }

    @Override
    public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
        return currentVisibility;
    }

    @Override
    public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack item) {
        return Collections.emptyList();
    }

    @Override
    public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
        return null;
    }

    private Rectangle getSlotArea(SlotFake slot) {
        return new Rectangle(guiLeft + slot.getX(), guiTop + slot.getY(), 16, 16);
    }

    @Override
    public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
        for (ContainerLevelMaintainer.FakeSlot slot : this.cont.getRequestSlots()) {
            if (getSlotArea(slot).contains(mouseX, mouseY)) {
                slot.putStack(draggedStack);
                NetworkHandler.instance.sendToServer(new PacketNEIDragClick(draggedStack, slot.getSlotIndex()));
                if (draggedStack != null) {
                    this.updateAmount(slot.getSlotIndex(), draggedStack.stackSize);
                    draggedStack.stackSize = 0;
                }
                return true;
            }
        }
        if (draggedStack != null) {
            draggedStack.stackSize = 0;
        }
        return false;
    }

    @Override
    public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
        return false;
    }

    private class Component {
        public boolean isEnable = true;
        private Widget qty;
        private Widget batch;
        private GuiFCImgButton disable;
        private GuiFCImgButton enable;

        public Component(
                Widget qtyInput,
                Widget batchInput,
                GuiFCImgButton enableBtn,
                GuiFCImgButton disableBtn,
                List buttonList) {
            this.qty = qtyInput;
            this.batch = batchInput;
            this.enable = enableBtn;
            this.disable = disableBtn;
            this.enable.setHalfSize(true);
            this.disable.setHalfSize(true);
            buttonList.add(this.qty.button);
            buttonList.add(this.batch.button);
            buttonList.add(this.enable);
            buttonList.add(this.disable);
        }

        public int getIndex() {
            return this.getQty().idx;
        }

        public void setEnable(boolean enable) {
            this.isEnable = enable;
        }

        private boolean send(Widget widget) {
            if (((ContainerLevelMaintainer.FakeSlot) cont.inventorySlots.get(widget.idx)).getHasStack()) {
                if (!widget.textField.getText().isEmpty()
                        && widget.textField.getText().matches("^[0-9]+")) {
                    String str = widget.textField.getText().replaceAll("^(0+)", "");
                    widget.textField.setText(str.isEmpty() ? "0" : str);
                    FluidCraft.proxy.netHandler.sendToServer(
                            new CPacketLevelMaintainer(widget.action, widget.idx, widget.textField.getText()));
                    widget.textField.setTextColor(0xFFFFFF);
                    return true;
                } else {
                    widget.textField.setTextColor(0xFF0000);
                    return false;
                }
            }
            return false;
        }

        protected boolean sendToServer(GuiButton btn) {
            if (this.getQty().button == btn) {
                return this.send(this.getQty());
            }
            if (this.getBatch().button == btn) {
                return this.send(this.getBatch());
            }
            if (this.enable == btn) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketLevelMaintainer("TileLevelMaintainer.Enable", this.getIndex()));
            } else if (this.disable == btn) {
                FluidCraft.proxy.netHandler.sendToServer(
                        new CPacketLevelMaintainer("TileLevelMaintainer.Disable", this.getIndex()));
            }
            return false;
        }

        public FCGuiTextField isMouseIn(final int xCoord, final int yCoord) {
            if (this.getQty().textField.isMouseIn(xCoord, yCoord)) return this.getQty().textField;
            if (this.getBatch().textField.isMouseIn(xCoord, yCoord)) return this.getBatch().textField;
            return null;
        }

        public Widget getQty() {
            return this.qty;
        }

        public Widget getBatch() {
            return this.batch;
        }

        public void draw() {
            this.getQty().draw();
            this.getBatch().draw();
            if (this.isEnable) {
                this.enable.visible = true;
                this.disable.visible = false;
            } else {
                this.enable.visible = false;
                this.disable.visible = true;
            }
        }
    }

    private class Widget {
        public final int idx;
        public final String action;
        private FCGuiTextField textField;
        private GuiFCImgButton button;

        public Widget(FCGuiTextField textField, GuiFCImgButton button, String tooltip, int idx, String action) {
            this.textField = textField;
            this.textField.setMessage(tooltip);
            this.textField.setEnableBackgroundDrawing(false);
            this.textField.setText("0");
            this.textField.setMaxStringLength(10); // it's enough to useful
            this.button = button;
            this.idx = idx;
            this.action = action;
        }

        public void draw() {
            this.textField.drawTextBox();
        }

        public GuiFCImgButton btn() {
            return this.button;
        }

        public void setTooltip(String message) {
            this.textField.setMessage(message);
        }
    }
}
