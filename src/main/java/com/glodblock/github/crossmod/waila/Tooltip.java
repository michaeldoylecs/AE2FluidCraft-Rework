package com.glodblock.github.crossmod.waila;

import appeng.util.Utility;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

public class Tooltip {
    public static String fluidFormat(String name, int amount) {
        return String.format("%s: %s mB", name, Utility.formatNumbers(amount));
    }

    public static String partFluidBusFormat(int amount) {
        return String.format("%s: %s mB/t", I18n.format(NameConst.WAILA_SPEED), Utility.formatNumbers(amount / 5));
    }

    public static String tileFluidInterfaceFormat(String name, int amount, int face) {
        return String.format("%s %s: %s mB", I18n.format(NameConst.GUI_FLUID_INTERFACE + "." + face), name, Utility.formatNumbers(amount));
    }

    public static String tileLevelMaintainerFormat(String name, long quantity, long batch, boolean isEnable) {
        return String.format("%s: %s / %s %s",
            name, Utility.formatNumbers(quantity), Utility.formatNumbers(batch),
            isEnable ? I18n.format(NameConst.WAILA_ENABLE) : I18n.format(NameConst.WAILA_DISABLE)
        );
    }

    public static String partFluidTerminalFluidFormat(FluidStack fs) {
        int fid = Util.getFluidID(fs.getFluid());
        if (fid == -1) {
            return fs.getFluid().getLocalizedName();
        } else if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            return String.format("%s (#%s) %s", fs.getFluid().getLocalizedName(), fid, fid);
        } else {
            return String.format("%s %s", fs.getFluid().getLocalizedName(), fid);
        }
    }
}
