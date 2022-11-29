package com.glodblock.github.crossmod.waila;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.util.Util;
import mcp.mobius.waila.handlers.nei.TooltipHandlerWaila;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class FCTooltipHandlerWaila extends TooltipHandlerWaila {
    @Override
    public List<String> handleItemDisplayName(GuiContainer arg0, ItemStack itemstack, List<String> currenttip) {
        return currenttip;
    }

    @Override
    public List<String> handleItemTooltip(GuiContainer arg0, ItemStack itemstack, int arg2, int arg3, List<String> currenttip) {
        if (itemstack != null && itemstack.getItem() instanceof ItemFluidDrop && currenttip.size() > 0) {
            FluidStack fs = ItemFluidDrop.getFluidStack(itemstack);
            if (fs == null) return currenttip;

            int fsid = Util.getFluidID(fs.getFluid());
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
                currenttip.set(0, String.format("%s (#%s) %s", fs.getFluid().getLocalizedName(), fsid, fsid));
            } else {
                currenttip.set(0, String.format("%s %s", fs.getFluid().getLocalizedName(), fsid));
            }

            String modName = Util.getFluidModName(fs.getFluid());
            if (modName != null && !modName.equals(""))
                currenttip.set(currenttip.size() - 1, "\u00a79\u00a7o" + modName);
        }
        return currenttip;
    }

    @Override
    public List<String> handleTooltip(GuiContainer arg0, int arg1, int arg2, List<String> currenttip) {
        return currenttip;
    }
}
