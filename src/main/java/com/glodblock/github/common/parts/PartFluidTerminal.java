package com.glodblock.github.common.parts;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridHost;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import java.util.Objects;

public class PartFluidTerminal extends FCBasePart {
    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidTerminal_Dark;

    public PartFluidTerminal(ItemStack is) {
        super(is, true);
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    @Override
    public boolean isBooting() {
        return super.isBooting();
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos) {
        TileEntity te = this.getTile();
        BlockPos tePos = new BlockPos(te);
        if (Platform.isWrench(player, player.inventory.getCurrentItem(), tePos.getX(), tePos.getY(), tePos.getZ())) {
            return super.onPartActivate(player, pos);
        }
        if (Platform.isServer()) {
            if (Util.hasPermission(player, SecurityPermissions.INJECT, (IGridHost) this) || Util.hasPermission(player, SecurityPermissions.EXTRACT, (IGridHost) this)) {
                InventoryHandler.openGui(player, te.getWorldObj(), tePos, Objects.requireNonNull(Util.from(getSide())), GuiType.FLUID_TERMINAL);
            } else {
                player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
            }
        }
        return true;
    }

    @Override
    public GuiBridge getGui(final EntityPlayer p) {
        int x = (int) p.posX;
        int y = (int) p.posY;
        int z = (int) p.posZ;
        if (this.getHost().getTile() != null) {
            x = this.getTile().xCoord;
            y = this.getTile().yCoord;
            z = this.getTile().zCoord;
        }

        if (GuiBridge.GUI_WIRELESS_TERM.hasPermissions(this.getHost().getTile(), x, y, z, this.getSide(), p)) {
            return GuiBridge.GUI_WIRELESS_TERM;
        }
        return GuiBridge.GUI_ME;
    }

}
