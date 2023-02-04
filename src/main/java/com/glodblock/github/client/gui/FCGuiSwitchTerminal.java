package com.glodblock.github.client.gui;

import java.util.List;

import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.ModAndClassUtil;

public class FCGuiSwitchTerminal {

    private GuiFCImgButton fluidBtn;
    private GuiFCImgButton essentiaBtn;
    private GuiFCImgButton craftBtn;
    private GuiFCImgButton patternBtn;
    private final GuiType originGui;

    public FCGuiSwitchTerminal(GuiType gui) {
        this.originGui = gui;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addButtons(List buttonList, int offsetX, int offsetY) {
        int y = 0;
        if (this.originGui != GuiType.WIRELESS_FLUID_TERMINAL) {
            buttonList.add(fluidBtn = new GuiFCImgButton(offsetX, offsetY + y, "FLUID_TEM", "YES"));
            y += 20;
        }
        if (ModAndClassUtil.ThE && this.originGui != GuiType.WIRELESS_ESSENTIA_TERMINAL) {
            buttonList.add(essentiaBtn = new GuiFCImgButton(offsetX, offsetY + y, "ESSENTIA_TEM", "YES"));
            y += 20;
        }
        buttonList.add(craftBtn = new GuiFCImgButton(offsetX, offsetY + y, "CRAFT_TEM", "YES"));
        y += 20;
        buttonList.add(patternBtn = new GuiFCImgButton(offsetX, offsetY + y, "PATTERN_TEM", "YES"));
    }

}
