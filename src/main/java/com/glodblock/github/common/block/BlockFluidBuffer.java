package com.glodblock.github.common.block;

import appeng.api.storage.data.IAEFluidStack;
import appeng.block.AEBaseItemBlock;
import appeng.util.Platform;
import com.glodblock.github.client.render.RenderBlockFluidBuffer;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidBuffer;
import com.glodblock.github.crossmod.waila.Tooltip;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class BlockFluidBuffer extends FCBaseBlock {

    public BlockFluidBuffer() {
        super(Material.iron, NameConst.BLOCK_FLUID_BUFFER);
        setTileEntity(TileFluidBuffer.class);
        setFullBlock(false);
        setOpaque(false);
        this.lightOpacity = 3;
    }

    @Override
    public boolean onActivated(
            World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
        ItemStack itemStack = player.inventory.getCurrentItem();
        FluidStack fs = Util.getFluidFromItem(itemStack);
        if (Platform.isServer()) {
            TileFluidBuffer tile = getTileEntity(world, x, y, z);
            if (tile == null) return false;
            IAEFluidStack ias = tile.getAEStoreFluidStack();
            if (fs == null && ias != null) {
                player.addChatMessage(new ChatComponentText(
                        Tooltip.fluidFormat(ias.getFluid().getLocalizedName(), ias.getStackSize())));
                return false;
            } else {
                tile.setFluid(fs);
                return true;
            }
        }
        return fs != null;
    }

    public BlockFluidBuffer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_FLUID_BUFFER);
        GameRegistry.registerTileEntity(TileFluidBuffer.class, NameConst.BLOCK_FLUID_BUFFER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockFluidBuffer getRenderer() {
        return new RenderBlockFluidBuffer();
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
