package com.glodblock.github.common.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public abstract class BaseBlockContainer extends BlockContainer {

    public BaseBlockContainer(Material p_i45386_1_) {
        super(p_i45386_1_);
    }

    public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
            boolean advancedToolTips) {}
}
