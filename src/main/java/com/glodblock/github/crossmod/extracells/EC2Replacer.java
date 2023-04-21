package com.glodblock.github.crossmod.extracells;

import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EC2Replacer {

    private static Map<String, EC2ProxyBlock> blocks;

    /**
     * Should be called if and only if replaceEC2 config is true and EC2 not present. Registers proxy
     * items for the entire EC2 mod. After running this once, you should set replaceEC2 back to false.
     */
    public static void initReplacer() {
        blocks = new HashMap<>(23);
        ItemReplacements.init();
    }

    private static void deprecateBlock(String name, int meta, Block replacement) {
        deprecateBlock(name, meta, replacement, 0);
    }

    private static void deprecateBlock(String name, int meta, Block replacement, int metaReplacement) {
        EC2ProxyBlock block = new EC2ProxyBlock(name, meta, replacement, metaReplacement);
        blocks.put("extracells:" + name, block);
        block.register();
    }

    public static void replaceExtraCells(FMLMissingMappingsEvent event) {
        List<FMLMissingMappingsEvent.MissingMapping> mappings = event.getAll();
        for (FMLMissingMappingsEvent.MissingMapping m : mappings) {
            if (ItemReplacements.registry.containsKey(m.name)) {
                Item toRemap = ItemReplacements.registry.get(m.name);
                m.remap(toRemap);
            } else if (blocks.containsKey(m.name)) {
                Block toRemap = blocks.get(m.name);
                m.remap(toRemap);
            }
        }
    }
}
