package com.glodblock.github.client.gui;

import appeng.api.config.*;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.*;
import appeng.client.me.InternalSlotME;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationType;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import codechicken.nei.LayoutManager;
import codechicken.nei.util.TextHistory;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.container.FCBaseFluidMonitorContain;
import com.glodblock.github.client.me.FluidRepo;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class GuiFCBaseFluidMonitor extends AEBaseMEGui
        implements ISortSource, IConfigManagerHost, IDropToFillTextField {

    public static int craftingGridOffsetX;
    public static int craftingGridOffsetY;

    private static String memoryText = "";
    private final FluidRepo repo;
    private final int offsetX = 9;
    private final int lowerTextureOffset = 0;
    private final boolean viewCell;
    private final ItemStack[] myCurrentViewCells = new ItemStack[5];
    public IConfigManager configSrc;
    public FCBaseFluidMonitorContain monitorableContainer;
    public GuiTabButton craftingStatusBtn;
    private GuiImgButton craftingStatusImgBtn;
    private FCGuiTextField searchField;
    private int perRow = 9;
    private int reservedSpace = 0;
    private boolean customSortOrder = true;
    private int rows = 0;
    private int maxRows = Integer.MAX_VALUE;
    private int standardSize;
    private GuiImgButton ViewBox;
    private GuiImgButton SortByBox;
    private GuiImgButton SortDirBox;
    private GuiImgButton searchBoxSettings;
    private GuiImgButton terminalStyleBox;
    private GuiImgButton searchStringSave;

    public GuiFCBaseFluidMonitor(
            final InventoryPlayer inventoryPlayer, final ITerminalHost te, final FCBaseFluidMonitorContain c) {

        super(c);

        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new FluidRepo(scrollbar, this);

        this.xSize = 195;
        this.ySize = 204;

        this.standardSize = this.xSize;

        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        (this.monitorableContainer = (FCBaseFluidMonitorContain) this.inventorySlots).setGui(this);

        this.viewCell = te instanceof IViewCellStorage;
    }

    public void postUpdate(final List<IAEFluidStack> list) {
        for (final IAEFluidStack is : list) {
            IAEItemStack stack = AEItemStack.create(ItemFluidDrop.newDisplayStack(is.getFluidStack()));
            stack.setStackSize(is.getStackSize());
            this.repo.postUpdate(stack);
        }

        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(this.rows * 18 - 2);
        this.getScrollBar()
                .setRange(
                        0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    private void reinitalize() {
        this.buttonList.clear();
        this.initGui();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        this.maxRows = this.getMaxRows();
        this.perRow = AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL
                ? 9
                : 9 + ((this.width - this.standardSize) / 18);

        final boolean hasNEI = IntegrationRegistry.INSTANCE.isEnabled(IntegrationType.NEI);

        final int NEI = hasNEI ? 0 : 0;
        int top = hasNEI ? 22 : 0;

        final int magicNumber = 114 + 1;
        final int extraSpace = this.height - magicNumber - NEI - top - this.reservedSpace;

        this.rows = (int) Math.floor(extraSpace / 18.0);
        if (this.rows > this.maxRows) {
            top += (this.rows - this.maxRows) * 18 / 2;
            this.rows = this.maxRows;
        }

        if (hasNEI) {
            this.rows--;
        }

        if (this.rows < 3) {
            this.rows = 3;
        }

        this.getMeSlots().clear();
        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                this.getMeSlots()
                        .add(new InternalSlotME(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
            }
        }

        if (AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) != TerminalStyle.FULL) {
            this.xSize = this.standardSize + ((this.perRow - 9) * 18);
        } else {
            this.xSize = this.standardSize;
        }

        super.initGui();
        // full size : 204
        // extra slots : 72
        // slot 18

        this.ySize = magicNumber + this.rows * 18 + this.reservedSpace;
        // this.guiTop = top;
        final int unusedSpace = this.height - this.ySize;
        this.guiTop = (int) Math.floor(unusedSpace / (unusedSpace < 0 ? 3.8f : 2.0f));

        int offset = this.guiTop + 8;

        if (this.customSortOrder) {
            this.buttonList.add(
                    this.SortByBox = new GuiImgButton(
                            this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting(Settings.SORT_BY)));
            offset += 20;
        }

        //        this.buttonList.add( this.ViewBox = new GuiImgButton( this.guiLeft - 18, offset, Settings.VIEW_MODE,
        // this.configSrc.getSetting( Settings.VIEW_MODE ) ) );
        //        offset += 20;

        this.buttonList.add(
                this.SortDirBox = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.SORT_DIRECTION,
                        this.configSrc.getSetting(Settings.SORT_DIRECTION)));
        offset += 20;

        this.buttonList.add(
                this.searchBoxSettings = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.SEARCH_MODE,
                        AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE)));
        offset += 20;

        if (ModAndClassUtil.isSaveText) {
            this.buttonList.add(
                    this.searchStringSave = new GuiImgButton(
                            this.guiLeft - 18,
                            offset,
                            Settings.SAVE_SEARCH,
                            AEConfig.instance.preserveSearchBar ? YesNo.YES : YesNo.NO));
            offset += 20;
        }

        this.buttonList.add(
                this.terminalStyleBox = new GuiImgButton(
                        this.guiLeft - 18,
                        offset,
                        Settings.TERMINAL_STYLE,
                        AEConfig.instance.settings.getSetting(Settings.TERMINAL_STYLE)));
        offset += 20;

        this.searchField = new FCGuiTextField(
                this.fontRendererObj, this.guiLeft + Math.max(80, this.offsetX), this.guiTop + 4, 90, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setVisible(true);
        if (ModAndClassUtil.isSearchStringTooltip)
            searchField.setMessage(ButtonToolTips.SearchStringTooltip.getLocal());

        if (this.viewCell) {
            if (ModAndClassUtil.isCraftStatus
                    && AEConfig.instance
                            .getConfigManager()
                            .getSetting(Settings.CRAFTING_STATUS)
                            .equals(CraftingStatus.BUTTON)) {
                this.buttonList.add(
                        this.craftingStatusImgBtn = new GuiImgButton(
                                this.guiLeft - 18,
                                offset,
                                Settings.CRAFTING_STATUS,
                                AEConfig.instance.settings.getSetting(Settings.CRAFTING_STATUS)));
            } else {
                this.buttonList.add(
                        this.craftingStatusBtn = new GuiTabButton(
                                this.guiLeft + 170,
                                this.guiTop - 4,
                                2 + 11 * 16,
                                GuiText.CraftingStatus.getLocal(),
                                itemRender));
                this.craftingStatusBtn.setHideEdge(13); // GuiTabButton implementation //
            }
        }

        // Enum setting = AEConfig.INSTANCE.getSetting( "Terminal", SearchBoxMode.class, SearchBoxMode.AUTOSEARCH );
        final Enum<?> setting = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        this.searchField.setFocused(SearchBoxMode.AUTOSEARCH == setting || SearchBoxMode.NEI_AUTOSEARCH == setting);

        if (ModAndClassUtil.isSearchBar && (AEConfig.instance.preserveSearchBar || this.isSubGui())) {
            setSearchString(memoryText, false);
        }
        if (this.isSubGui()) {
            this.repo.updateView();
            this.setScrollBar();
        }

        craftingGridOffsetX = Integer.MAX_VALUE;
        craftingGridOffsetY = Integer.MAX_VALUE;

        for (final Object s : this.inventorySlots.inventorySlots) {
            if (s instanceof AppEngSlot) {
                if (((Slot) s).xDisplayPosition < 195) {
                    this.repositionSlot((AppEngSlot) s);
                }
            }

            if (s instanceof SlotCraftingMatrix || s instanceof SlotFakeCraftingMatrix) {
                final Slot g = (Slot) s;
                if (g.xDisplayPosition > 0 && g.yDisplayPosition > 0) {
                    craftingGridOffsetX = Math.min(craftingGridOffsetX, g.xDisplayPosition);
                    craftingGridOffsetY = Math.min(craftingGridOffsetY, g.yDisplayPosition);
                }
            }
        }

        craftingGridOffsetX -= 25;
        craftingGridOffsetY -= 6;
    }

    public void setSearchString(String memoryText, boolean updateView) {
        this.searchField.setText(memoryText);
        this.repo.setSearchString(memoryText);
        if (updateView) {
            this.repo.updateView();
            this.setScrollBar();
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                this.getGuiDisplayName(I18n.format(NameConst.GUI_FLUID_TERMINAL)), 8, 6, 4210752);
        this.fontRendererObj.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) {
        final Enum<?> searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
        if (searchMode != SearchBoxMode.AUTOSEARCH && searchMode != SearchBoxMode.NEI_AUTOSEARCH) {
            this.searchField.mouseClicked(xCoord, yCoord, btn);
        }
        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            setSearchString("", true);
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void handleMouseInput() {
        final int wheel = Mouse.getEventDWheel();
        final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
        final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight;

        if (wheel != 0 && this.searchField.isMouseIn(x, y) && isCtrlKeyDown()) {
            final Enum<?> searchMode = AEConfig.instance.settings.getSetting(Settings.SEARCH_MODE);
            if (searchMode == SearchBoxMode.NEI_MANUAL_SEARCH || searchMode == SearchBoxMode.NEI_AUTOSEARCH) {
                try {
                    Field cls = LayoutManager.searchField.getClass().getDeclaredField("history");
                    cls.setAccessible(true);
                    TextHistory history = ((TextHistory) cls.get("get"));
                    TextHistory.Direction direction;
                    if (wheel > 0) {
                        direction = TextHistory.Direction.PREVIOUS;
                    } else {
                        direction = TextHistory.Direction.NEXT;
                    }
                    history.get(direction, this.searchField.getText())
                            .map(newText -> {
                                setSearchString(newText, true);
                                return true;
                            })
                            .orElse(false);
                    return;
                } catch (NoSuchFieldException | IllegalAccessException e) {
                }
            }
        }
        super.handleMouseInput();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        memoryText = this.searchField.getText();
    }

    public void bindTextureBack(final String file) {
        final ResourceLocation loc = new ResourceLocation(FluidCraft.MODID, "textures/" + file);
        this.mc.getTextureManager().bindTexture(loc);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {

        this.bindTextureBack(this.getBackground());
        final int x_width = 195;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        if (this.viewCell) {
            this.drawTexturedModalRect(offsetX + x_width, offsetY, x_width, 0, 46, 128);
        }

        for (int x = 0; x < this.rows; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        this.drawTexturedModalRect(
                offsetX,
                offsetY + 16 + this.rows * 18 + this.lowerTextureOffset,
                0,
                106 - 18 - 18,
                x_width,
                99 + this.reservedSpace - this.lowerTextureOffset);

        if (this.viewCell) {
            boolean update = false;

            for (int i = 0; i < 5; i++) {
                if (this.myCurrentViewCells[i]
                        != this.monitorableContainer.getCellViewSlot(i).getStack()) {
                    update = true;
                    this.myCurrentViewCells[i] =
                            this.monitorableContainer.getCellViewSlot(i).getStack();
                }
            }

            if (update) {
                this.repo.setViewCell(this.myCurrentViewCells);
            }
        }

        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    protected String getBackground() {
        return "gui/terminal.png";
    }

    @Override
    protected boolean isPowered() {
        return this.repo.hasPower();
    }

    int getMaxRows() {
        return AEConfig.instance.getConfigManager().getSetting(Settings.TERMINAL_STYLE) == TerminalStyle.SMALL
                ? 6
                : Integer.MAX_VALUE;
    }

    protected void repositionSlot(final AppEngSlot s) {
        s.yDisplayPosition = s.getY() + this.ySize - 78 - 5;
    }

    @Override
    protected void keyTyped(final char character, final int key) {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchField.getText().isEmpty()) {
                return;
            }

            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    public void updateScreen() {
        this.repo.setPower(this.monitorableContainer.isPowered());
        super.updateScreen();
    }

    @Override
    public Enum<?> getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public Enum<?> getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public Enum<?> getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        if (this.SortByBox != null) {
            this.SortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
        }

        if (this.SortDirBox != null) {
            this.SortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
        }

        if (this.ViewBox != null) {
            this.ViewBox.set(this.configSrc.getSetting(Settings.VIEW_MODE));
        }

        this.repo.updateView();
    }

    public int getReservedSpace() {
        return this.reservedSpace;
    }

    public void setReservedSpace(final int reservedSpace) {
        this.reservedSpace = reservedSpace;
    }

    public boolean isCustomSortOrder() {
        return this.customSortOrder;
    }

    public void setCustomSortOrder(final boolean customSortOrder) {
        this.customSortOrder = customSortOrder;
    }

    public int getStandardSize() {
        return this.standardSize;
    }

    public void setStandardSize(final int standardSize) {
        this.standardSize = standardSize;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float btn) {
        super.drawScreen(mouseX, mouseY, btn);
        if (ModAndClassUtil.isSearchBar && AEConfig.instance.preserveSearchBar && searchField != null)
            handleTooltip(mouseX, mouseY, searchField.new TooltipProvider());
    }

    public boolean isOverSearchField(int x, int y) {
        return searchField.isMouseIn(x, y);
    }

    public boolean hideItemPanelSlot(int tx, int ty, int tw, int th) {

        if (this.viewCell) {
            int rw = 33;
            int rh = 14 + myCurrentViewCells.length * 18;

            if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
                return false;
            }

            int rx = this.guiLeft + this.xSize;
            int ry = this.guiTop + 0;

            rw += rx;
            rh += ry;
            tw += tx;
            th += ty;

            //      overflow || intersect
            return (rw < rx || rw > tx) && (rh < ry || rh > ty) && (tw < tx || tw > rx) && (th < ty || th > ry);
        }

        return false;
    }

    @Override
    public boolean isOverTextField(int mousex, int mousey) {
        return searchField.isMouseIn(mousex, mousey);
    }

    @Override
    public void setTextFieldValue(String displayName, int mousex, int mousey, ItemStack stack) {
        if (Util.getFluidFromItem(stack) != null) {
            setSearchString(Util.getFluidFromItem(stack).getFluid().getLocalizedName(), true);
        }
    }
}
