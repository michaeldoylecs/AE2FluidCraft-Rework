package com.glodblock.github.crossmod.extracells;

import appeng.api.AEApi;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.base.Optional;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class EC2Replacer {

    private static Map<String, EC2ProxyItem> items;
    private static Map<String, EC2ProxyBlock> blocks;

    /**
     * Should be called if and only if replaceEC2 config is true and EC2 not present. Registers proxy
     * items for the entire EC2 mod. After running this once, you should set replaceEC2 back to false.
     */
    public static void initReplacer() {
        items = new HashMap<>(23);
        blocks = new HashMap<>(23);
        deprecateItem("part.base", 0, ItemAndBlockHolder.FLUID_EXPORT_BUS, EC2Replacer::noop);
        deprecateItem("part.base", 1, ItemAndBlockHolder.FLUID_IMPORT_BUS, EC2Replacer::noop);
        deprecateItem("part.base", 2, ItemAndBlockHolder.FLUID_STORAGE_BUS, EC2Replacer::noop);
        deprecateItem("part.base", 3, ItemAndBlockHolder.FLUID_TERM, EC2Replacer::noop);
        deprecateItem("part.base", 4, ItemAndBlockHolder.FLUID_LEVEL_EMITTER, EC2Replacer::noop);
        deprecateItem("part.base", 9, ItemAndBlockHolder.FLUID_INTERFACE, EC2Replacer::noop);
        deprecateItem("part.base", 10, ItemAndBlockHolder.FLUID_STORAGE_MONITOR, EC2Replacer::noop);
        deprecateItem("part.base", 11, ItemAndBlockHolder.FLUID_CONVERSION_MONITOR, EC2Replacer::noop);
        deprecateItem("storage.fluid", 0, ItemAndBlockHolder.CELL1KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 1, ItemAndBlockHolder.CELL4KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 2, ItemAndBlockHolder.CELL16KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 3, ItemAndBlockHolder.CELL64KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 4, ItemAndBlockHolder.CELL256KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 5, ItemAndBlockHolder.CELL1024KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 6, ItemAndBlockHolder.CELL4096KM, EC2Replacer::transformFluidCellData);
        deprecateItem("storage.fluid", 7, ItemAndBlockHolder.CELL16384KM, EC2Replacer::transformFluidCellData);
        // Thanks, AEApi.
        if (AEApi.instance().definitions().items().cell256k().isEnabled()) {
            deprecateItem("storage.physical", 0,
                AEApi.instance().definitions().items().cell256k().maybeItem().get(),
                AEApi.instance().definitions().items().cell256k().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cell1024k().isEnabled()) {
            deprecateItem("storage.physical", 1,
                AEApi.instance().definitions().items().cell1024k().maybeItem().get(),
                AEApi.instance().definitions().items().cell1024k().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cell4096k().isEnabled()) {
            deprecateItem("storage.physical", 2,
                AEApi.instance().definitions().items().cell4096k().maybeItem().get(),
                AEApi.instance().definitions().items().cell4096k().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cell16384k().isEnabled()) {
            deprecateItem("storage.physical", 3,
                AEApi.instance().definitions().items().cell16384k().maybeItem().get(),
                AEApi.instance().definitions().items().cell16384k().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cellContainer().isEnabled()) {
            deprecateItem("storage.physical", 4,
                AEApi.instance().definitions().items().cellContainer().maybeItem().get(),
                AEApi.instance().definitions().items().cellContainer().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cellSingularity().isEnabled()) {
            deprecateItem("storage.physical.advanced.singularity", 0,
                AEApi.instance().definitions().items().cellSingularity().maybeItem().get(),
                AEApi.instance().definitions().items().cellSingularity().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cellQuantum().isEnabled()) {
            deprecateItem("storage.physical.advanced.quantum", 0,
                AEApi.instance().definitions().items().cellQuantum().maybeItem().get(),
                AEApi.instance().definitions().items().cellQuantum().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        if (AEApi.instance().definitions().items().cellVoid().isEnabled()) {
            deprecateItem("storage.physical.advanced.singularity", 0,
                AEApi.instance().definitions().items().cellVoid().maybeItem().get(),
                AEApi.instance().definitions().items().cellVoid().maybeStack(1).get().getItemDamage(),
                EC2Replacer::transformItemCellData);
        }
        deprecateItem("pattern.fluid", 0, ItemAndBlockHolder.PATTERN, tag -> tag);
        deprecateItem("terminal.fluid.wireless", 0, ItemAndBlockHolder.WIRELESS_FLUID_TERM, tag -> tag);
        if (AEApi.instance().definitions().materials().emptyAdvancedStorageCell().isEnabled()) {
            deprecateItem("storage.casing", 0,
                AEApi.instance().definitions().materials().emptyAdvancedStorageCell().maybeItem().get(),
                AEApi.instance().definitions().materials().emptyAdvancedStorageCell().maybeStack(1).get().getItemDamage(),
                EC2Replacer::noop);
        }
        deprecateItem("storage.casing", 1, ItemAndBlockHolder.CELL_HOUSING, 2, EC2Replacer::noop);
        if (AEApi.instance().definitions().materials().cell256kPart().isEnabled()) {
            deprecateItem("storage.component", 0,
                AEApi.instance().definitions().materials().cell256kPart().maybeItem().get(),
                AEApi.instance().definitions().materials().cell256kPart().maybeStack(1).get().getItemDamage(),
                EC2Replacer::noop);
        }
        if (AEApi.instance().definitions().materials().cell1024kPart().isEnabled()) {
            deprecateItem("storage.component", 1,
                AEApi.instance().definitions().materials().cell1024kPart().maybeItem().get(),
                AEApi.instance().definitions().materials().cell1024kPart().maybeStack(1).get().getItemDamage(),
                EC2Replacer::noop);
        }
        if (AEApi.instance().definitions().materials().cell4096kPart().isEnabled()) {
            deprecateItem("storage.component", 2,
                AEApi.instance().definitions().materials().cell4096kPart().maybeItem().get(),
                AEApi.instance().definitions().materials().cell4096kPart().maybeStack(1).get().getItemDamage(),
                EC2Replacer::noop);
        }
        if (AEApi.instance().definitions().materials().cell16384kPart().isEnabled()) {
            deprecateItem("storage.component", 3,
                AEApi.instance().definitions().materials().cell16384kPart().maybeItem().get(),
                AEApi.instance().definitions().materials().cell16384kPart().maybeStack(1).get().getItemDamage(),
                EC2Replacer::noop);
        }
        deprecateItem("storage.component", 4, ItemAndBlockHolder.CELL_PART, 0, EC2Replacer::noop);
        deprecateItem("storage.component", 5, ItemAndBlockHolder.CELL_PART, 1, EC2Replacer::noop);
        deprecateItem("storage.component", 6, ItemAndBlockHolder.CELL_PART, 2, EC2Replacer::noop);
        deprecateItem("storage.component", 7, ItemAndBlockHolder.CELL_PART, 3, EC2Replacer::noop);
        deprecateItem("storage.component", 8, ItemAndBlockHolder.CELL_PART, 4, EC2Replacer::noop);
        deprecateItem("storage.component", 9, ItemAndBlockHolder.CELL_PART, 5, EC2Replacer::noop);
        deprecateItem("storage.component", 10, ItemAndBlockHolder.CELL_PART, 6, EC2Replacer::noop);
        deprecateItem("terminal.universal.wireless", 0, ItemAndBlockHolder.WIRELESS_ULTRA_TERM, tag -> tag);
    }

    private static void deprecateItem(String name, int meta, Item replacement, Function<NBTTagCompound, NBTTagCompound> nbtTransformer) {
        deprecateItem(name, meta, replacement, 0, nbtTransformer);
    }

    private static void deprecateItem(String name, int meta, Item replacement, int targetMeta, Function<NBTTagCompound, NBTTagCompound> nbtTransformer) {
        final String fullName = "extracells:" + name;
        EC2ProxyItem proxy = items.get(fullName);
        if (proxy == null) {
            proxy = new EC2ProxyItem(name);
            items.put(fullName, proxy);
            proxy.register();
        }
        proxy.addMetaReplacement(meta, replacement, targetMeta, nbtTransformer);
    }

    private static void deprecateBlock(String name, int meta, Block replacement) {
        deprecateBlock(name, meta, replacement, 0);
    }

    private static void deprecateBlock(String name, int meta, Block replacement, int metaReplacement) {
        EC2ProxyBlock block = new EC2ProxyBlock(name, meta, replacement, metaReplacement);
        blocks.put("extracells:" + name, block);
    }

    public static void replaceExtraCells(FMLMissingMappingsEvent event) {
        List<FMLMissingMappingsEvent.MissingMapping> mappings = event.getAll();
        for (FMLMissingMappingsEvent.MissingMapping m : mappings) {
            if (items.containsKey(m.name)) {
                Item toRemap = items.get(m.name);
                m.remap(toRemap);
            } else if (blocks.containsKey(m.name)) {
                Block toRemap = blocks.get(m.name);
                m.remap(toRemap);
            }
        }
    }

    private static NBTTagCompound noop(NBTTagCompound compound) {
        return compound;
    }

    private static NBTTagCompound transformFluidCellData(NBTTagCompound compound) {
        return compound; //for now no op
    }

    private static NBTTagCompound transformItemCellData(NBTTagCompound compound) {
        return compound;
    }
}
