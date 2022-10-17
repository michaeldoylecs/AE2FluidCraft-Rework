package com.glodblock.github.client.gui.container;

import appeng.api.storage.ITerminalHost;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerFluidTerminal extends FCBaseFluidMonitorContain {
    public ContainerFluidTerminal(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable);
    }
}
