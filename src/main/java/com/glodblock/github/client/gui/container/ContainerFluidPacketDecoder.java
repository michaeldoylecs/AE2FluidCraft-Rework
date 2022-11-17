package com.glodblock.github.client.gui.container;

import appeng.container.AEBaseContainer;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.inventory.slot.FCSlotRestrictedInput;
import net.minecraft.entity.player.InventoryPlayer;

import static com.glodblock.github.loader.ItemAndBlockHolder.PACKET;

public class ContainerFluidPacketDecoder extends AEBaseContainer {

    public ContainerFluidPacketDecoder(InventoryPlayer ipl, TileFluidPacketDecoder tile) {
        super(ipl, tile);
        addSlotToContainer(new FCSlotRestrictedInput(PACKET.stack(), tile.getInventory(), 0, 80, 35, ipl));
        bindPlayerInventory(ipl, 0, 84);
    }

}
