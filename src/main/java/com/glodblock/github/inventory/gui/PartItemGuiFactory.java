package com.glodblock.github.inventory.gui;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.MutablePair;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;

import com.glodblock.github.inventory.item.IItemInventory;
import com.glodblock.github.util.Util;

public abstract class PartItemGuiFactory<T> extends PartGuiFactory<T> {

    PartItemGuiFactory(Class<T> invClass) {
        super(invClass);
    }

    @Nullable
    protected T getItemInventory(Object inv) {
        return invClass.isInstance(inv) ? invClass.cast(inv) : null;
    }

    @Nullable
    @Override
    public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        MutablePair<Util.GuiHelper.GuiType, Integer> result = Util.GuiHelper.decodeType(y);

        if (result.left == Util.GuiHelper.GuiType.ITEM) {
            ItemStack item = player.getHeldItem();
            if (item == null || !(item.getItem() instanceof IItemInventory)) {
                return null;
            }
            T inv = getItemInventory(
                    ((IItemInventory) item.getItem()).getInventory(item, world, x, result.right, z, player));
            if (inv == null) {
                return null;
            }
            Object gui = createServerGui(player, inv);
            if (gui instanceof AEBaseContainer) {
                ContainerOpenContext ctx = new ContainerOpenContext(inv);
                ctx.setWorld(world);
                ctx.setX(x);
                ctx.setY(y);
                ctx.setZ(z);
                ctx.setSide(face);
                ((AEBaseContainer) gui).setOpenContext(ctx);
            }
            return gui;
        } else {
            return super.createServerGui(player, world, x, result.right, z, face);
        }

    }

    @Nullable
    @Override
    public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, ForgeDirection face) {
        MutablePair<Util.GuiHelper.GuiType, Integer> result = Util.GuiHelper.decodeType(y);
        if (result.left == Util.GuiHelper.GuiType.ITEM) {
            ItemStack item = player.getHeldItem();
            if (item == null || !(item.getItem() instanceof IItemInventory)) {
                return null;
            }
            T inv = getItemInventory(
                    ((IItemInventory) item.getItem()).getInventory(item, world, x, result.right, z, player));
            return inv != null ? createClientGui(player, inv) : null;
        } else {
            return super.createClientGui(player, world, x, result.right, z, face);
        }

    }

}
