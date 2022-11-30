package com.glodblock.github.crossmod.waila;

import appeng.api.parts.IPartHost;
import appeng.tile.AEBaseTile;
import codechicken.nei.guihook.GuiContainerManager;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaInit {

    public static void run() {
        FMLInterModComms.sendMessage("Waila", "register", WailaInit.class.getName() + ".register");
    }

    public static void register(final IWailaRegistrar registrar) {
        final IWailaDataProvider part = new PartWailaDataProvider();
        registrar.registerBodyProvider(part, IPartHost.class);
        registrar.registerNBTProvider(part, IPartHost.class);
        GuiContainerManager.addTooltipHandler(new FCTooltipHandlerWaila());

        final IWailaDataProvider tile = new TileWailaDataProvider();
        registrar.registerBodyProvider(tile, AEBaseTile.class);
        registrar.registerNBTProvider(tile, AEBaseTile.class);
    }

}
