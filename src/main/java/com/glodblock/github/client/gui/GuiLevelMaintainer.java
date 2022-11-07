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
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Collections;
import java.util.List;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiLevelMaintainer extends AEBaseGui implements INEIGuiHandler {

    private static final ResourceLocation TEX_BG = FluidCraft.resource("textures/gui/level_maintainer.png");
    private final ContainerLevelMaintainer cont;
    private final Widget[] qtyInputs = new Widget[TileLevelMaintainer.REQ_COUNT];
    private final Widget[] batchInputs = new Widget[TileLevelMaintainer.REQ_COUNT];
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
            qtyInputs[i].textField.setText(String.valueOf(is.getStackSize()));
            batchInputs[i].textField.setText(String.valueOf(size));
        }
    }

    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        if (ModAndClassUtil.isSearchBar) {
            for (Widget i : qtyInputs) {
                handleTooltip(mouseX, mouseY, i.textField.new TooltipProvider());
            }
            for (Widget i : batchInputs) {
                handleTooltip(mouseX, mouseY, i.textField.new TooltipProvider());
            }
        }
    }

    public void initGui() {
        super.initGui();
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyInputs[i] = new Widget(
                new FCGuiTextField(this.fontRendererObj, guiLeft + 37, guiTop + 21 + 20 * i, 52, 16),
                new GuiFCImgButton(guiLeft + 37 + 47, guiTop + 20 + 20 * i, "SUBMIT", "SUBMIT"), I18n.format(NameConst.TT_REQUEST_SIZE),
                i, 0);
            this.buttonList.add(qtyInputs[i].btn());
            batchInputs[i] = new Widget(
                new FCGuiTextField(this.fontRendererObj, guiLeft + 100, guiTop + 21 + 20 * i, 52, 16),
                new GuiFCImgButton(guiLeft + 100 + 47, guiTop + 20 + 20 * i, "SUBMIT", "SUBMIT"), I18n.format(NameConst.TT_BATCH_SIZE),
                i, 1
            );
            this.buttonList.add(batchInputs[i].btn());
        }
        FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(-1));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(TEX_BG);
        drawTexturedModalRect(offsetX, offsetY, 0, 0, 176, ySize);
        for (int i = 0; i < TileLevelMaintainer.REQ_COUNT; i++) {
            qtyInputs[i].draw();
            batchInputs[i].draw();
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        fontRendererObj.drawString(getGuiDisplayName(I18n.format(NameConst.GUI_LEVEL_MAINTAINER)), 8, 6, 0x404040);
        mouseRegions.render(mouseX, mouseY);
    }

    @Override
    public void func_146977_a(final Slot s) {
        if (drawSlot0(s))
            super.func_146977_a(s);
    }

    public boolean drawSlot0(Slot slot) {
        if (slot instanceof SlotFake) {
            IAEItemStack stack = ((ContainerLevelMaintainer.FakeSlot) slot).getAeStack();
            super.func_146977_a(new SlotSingleItem(slot));
            if (stack == null) return true;
            IAEItemStack fake = stack.copy();
            if (this.qtyInputs[slot.getSlotIndex()].textField.getText().matches("[0-9]+")) {
                fake.setStackSize(Long.parseLong(this.qtyInputs[slot.getSlotIndex()].textField.getText()));
            } else {
                fake.setStackSize(0);
            }
            stackSizeRenderer.setAeStack(fake);
            stackSizeRenderer.renderItemOverlayIntoGUI(fontRendererObj, mc.getTextureManager(), stack.getItemStack(), slot.xDisplayPosition, slot.yDisplayPosition);
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
            for (Widget i : qtyInputs) {
                if (i.textField.isMouseIn(xCoord, yCoord)) {
                    i.textField.setFocused(true);
                    this.input = i.textField;
                    super.mouseClicked(xCoord, yCoord, btn);
                    return;
                }
            }
            for (Widget i : batchInputs) {
                if (i.textField.isMouseIn(xCoord, yCoord)) {
                    i.textField.setFocused(true);
                    this.input = i.textField;
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
            if (!this.input.getText().matches("[0-9]+")) {
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
        if (slot instanceof ContainerLevelMaintainer.FakeSlot && this.cont.getPlayerInv().getItemStack() != null) {
            this.qtyInputs[slot.getSlotIndex()].textField.setText(String.valueOf(this.cont.getPlayerInv().getItemStack().stackSize));
            return;
        }
        super.handleMouseClick(slot, slotIdx, ctrlDown, mouseButton);
    }

    public void updateAmount(int idx, int stackSize) {
        this.qtyInputs[idx].textField.setText(String.valueOf(stackSize));
    }

    private boolean sendToServer(Widget widget) {
        if (((ContainerLevelMaintainer.FakeSlot) cont.inventorySlots.get(widget.idx)).getHasStack()) {
            if (!widget.textField.getText().isEmpty() && widget.textField.getText().matches("[0-9]+")) {
                widget.textField.setText(widget.textField.getText().replaceAll("^(0+)", ""));
                FluidCraft.proxy.netHandler.sendToServer(new CPacketLevelMaintainer(widget.action, widget.idx, widget.textField.getText()));
                widget.textField.setTextColor(0xFFFFFF);
                return true;
            } else {
                widget.textField.setTextColor(0xFF0000);
                return false;
            }
        }
        return false;
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        super.actionPerformed(btn);
        for (Widget widget : qtyInputs) {
            if (widget.button == btn) {
                sendToServer(widget);
                return;
            }
        }
        for (Widget widget : batchInputs) {
            if (widget.button == btn) {
                sendToServer(widget);
                return;
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

    private class Widget {
        public final int idx;
        public final int action;
        private FCGuiTextField textField;
        private GuiFCImgButton button;

        public Widget(FCGuiTextField textField, GuiFCImgButton button, String tooltip, int idx, int action) {
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
