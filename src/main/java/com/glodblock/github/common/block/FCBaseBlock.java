package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import com.glodblock.github.FluidCraft;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class FCBaseBlock extends AEBaseTileBlock {

    public FCBaseBlock(Material mat, String name) {
        super(mat);
        this.setBlockName(name);
        this.setBlockTextureName(FluidCraft.MODID + ":" + name);
    }

    @Override
    public void setTileEntity(final Class<? extends TileEntity> clazz) {
        super.setTileEntity(clazz);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public void setFeature(final EnumSet<AEFeature> f) {
        super.setFeature(f);
    }

    @SuppressWarnings("unchecked")
    @SideOnly(Side.CLIENT)
    public void addInformation(
            final ItemStack itemStack,
            final EntityPlayer player,
            final List<String> toolTip,
            final boolean advancedToolTips) {}

    protected final List listFormattedStringToWidth(String str) {
        return Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(str, 150);
    }

    public void addCheckedInformation(
            ItemStack itemStack, EntityPlayer player, List<String> toolTip, boolean advancedToolTips) {
        this.addInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
