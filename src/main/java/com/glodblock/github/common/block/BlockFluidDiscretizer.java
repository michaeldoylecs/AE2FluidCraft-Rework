package com.glodblock.github.common.block;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidDiscretizer;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BlockFluidDiscretizer extends FCBaseBlock {

    public BlockFluidDiscretizer() {
        super(Material.iron, NameConst.BLOCK_FLUID_DISCRETIZER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidDiscretizer.class);
    }

    public BlockFluidDiscretizer register() {
        GameRegistry.registerBlock(this, FCBaseItemBlock.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        GameRegistry.registerTileEntity(TileFluidDiscretizer.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addInformation(
            final ItemStack itemStack,
            final EntityPlayer player,
            final List<String> toolTip,
            final boolean advancedToolTips) {

        if (isShiftKeyDown()) {
            toolTip.addAll(this.listFormattedStringToWidth(NameConst.i18n(NameConst.TT_FLUID_DISCRETIZER_DESC)));
        } else {
            toolTip.add(NameConst.i18n(NameConst.TT_SHIFT_FOR_MORE));
        }
    }
}
