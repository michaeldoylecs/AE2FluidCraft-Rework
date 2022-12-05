package com.glodblock.github.common.block;

import appeng.api.config.SecurityPermissions;
import appeng.block.AEBaseItemBlock;
import appeng.util.Platform;
import com.glodblock.github.client.render.RenderBlockLevelMaintainer;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileLevelMaintainer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockLevelMaintainer extends FCBaseBlock {

    public BlockLevelMaintainer() {
        super(Material.iron, NameConst.BLOCK_LEVEL_MAINTAINER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileLevelMaintainer.class);
    }

    public BlockLevelMaintainer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_LEVEL_MAINTAINER);
        GameRegistry.registerTileEntity(TileLevelMaintainer.class, NameConst.BLOCK_LEVEL_MAINTAINER);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected RenderBlockLevelMaintainer getRenderer() {
        return new RenderBlockLevelMaintainer();
    }

    @Override
    public boolean onActivated(
            World world, int x, int y, int z, EntityPlayer player, int facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return false;
        }
        TileLevelMaintainer tile = getTileEntity(world, x, y, z);
        if (tile != null) {
            if (Platform.isServer()) {
                if (Util.hasPermission(player, SecurityPermissions.CRAFT, tile)) {
                    InventoryHandler.openGui(
                            player,
                            world,
                            new BlockPos(x, y, z),
                            EnumFacing.getFront(facing),
                            GuiType.LEVEL_MAINTAINER);
                } else {
                    player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
                }
            }
            return true;
        }
        return false;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }
}
