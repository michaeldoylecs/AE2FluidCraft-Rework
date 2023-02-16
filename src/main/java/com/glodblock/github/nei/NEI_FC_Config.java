package com.glodblock.github.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.*;
import com.glodblock.github.nei.recipes.FluidRecipe;

public class NEI_FC_Config implements IConfigureNEI {

    @Override
    public void loadConfig() {
        API.registerNEIGuiHandler(new NEIGuiHandler());
        API.addSearchProvider(new NEIItemFilter());
        for (String identifier : FluidRecipe.getSupportRecipes()) {
            // that NEE handlers take priority
            if (!API.hasGuiOverlayHandler(GuiFluidCraftingWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidCraftingWireless.class,
                        FluidCraftingTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternWireless.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternExWireless.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternExWireless.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternTerminal.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternTerminal.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
            if (!API.hasGuiOverlayHandler(GuiFluidPatternTerminalEx.class, identifier)) {
                API.registerGuiOverlayHandler(
                        GuiFluidPatternTerminalEx.class,
                        FluidPatternTerminalRecipeTransferHandler.INSTANCE,
                        identifier);
            }
        }
    }

    @Override
    public String getName() {
        return FluidCraft.MODNAME;
    }

    @Override
    public String getVersion() {
        return FluidCraft.VERSION;
    }
}
