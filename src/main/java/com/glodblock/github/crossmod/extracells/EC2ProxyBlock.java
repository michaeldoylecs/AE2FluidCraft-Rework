package com.glodblock.github.crossmod.extracells;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

public class EC2ProxyBlock extends Block {

    protected final String name;
    protected final int meta;
    protected final Block replacement;
    protected final int metaReplacement;

    public EC2ProxyBlock(String ec2itemName, int meta, Block replacement, int metaReplacement) {
        super(Material.air); //???
        this.name = ec2itemName;
        this.meta = meta;
        this.replacement = replacement;
        this.metaReplacement = metaReplacement;
    }

    void register() {
        GameRegistry.registerBlock(this, EC2ProxyItemBlock.class, "ec2placeholder." + name);
    }

    private static class EC2ProxyItemBlock extends ItemBlock {

        public EC2ProxyItemBlock(Block block) {
            super(block);
        }
    }
}
