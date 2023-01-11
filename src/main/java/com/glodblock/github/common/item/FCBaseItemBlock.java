package com.glodblock.github.common.item;

import appeng.block.AEBaseItemBlock;
import com.glodblock.github.common.block.FCBaseBlock;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class FCBaseItemBlock extends AEBaseItemBlock {
    public FCBaseItemBlock(Block id) {
        super(id);
    }

    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void addCheckedInformation(
            final ItemStack itemStack,
            final EntityPlayer player,
            final List<String> toolTip,
            final boolean advancedToolTips) {
        FCBaseBlock block = (FCBaseBlock) GameData.getBlockRegistry()
                .getRaw(GameRegistry.findUniqueIdentifierFor(itemStack.getItem())
                        .toString());
        block.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
        super.addCheckedInformation(itemStack, player, toolTip, advancedToolTips);
    }
}
