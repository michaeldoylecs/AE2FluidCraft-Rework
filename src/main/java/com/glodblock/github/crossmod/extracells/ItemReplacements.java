package com.glodblock.github.crossmod.extracells;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import com.glodblock.github.crossmod.extracells.parts.*;
import com.glodblock.github.loader.ItemAndBlockHolder;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Shell class to organize proxy replacements and hide the ugliness
 */
public class ItemReplacements {

    protected static Map<String, ProxyItem> registry;

    static void init() {
        registry = new HashMap<>(23);
        ProxyFluidBusIO.init();
        ItemReplacements.proxyItems();
        ItemReplacements.proxyPartItems();
    }
    /**
     * Register proxy items (non-parts)
     */
    static void proxyItems() {
        deprecateFluidStorage("storage.fluid", 0, ItemAndBlockHolder.CELL1KM);
        deprecateFluidStorage("storage.fluid", 1, ItemAndBlockHolder.CELL4KM);
        deprecateFluidStorage("storage.fluid", 3, ItemAndBlockHolder.CELL64KM);
        deprecateFluidStorage("storage.fluid", 4, ItemAndBlockHolder.CELL256KM);
        deprecateFluidStorage("storage.fluid", 2, ItemAndBlockHolder.CELL16KM);
        deprecateFluidStorage("storage.fluid", 5, ItemAndBlockHolder.CELL1024KM);
        deprecateFluidStorage("storage.fluid", 6, ItemAndBlockHolder.CELL4096KM);
        deprecateFluidStorage("storage.fluid", 7, ItemAndBlockHolder.CELL16384KM);
        // Thanks, AEApi.
        deprecateItemStorage("storage.physical", 0, AEApi.instance().definitions().items().cell256k());
        deprecateItemStorage("storage.physical", 1, AEApi.instance().definitions().items().cell1024k());
        deprecateItemStorage("storage.physical", 2, AEApi.instance().definitions().items().cell4096k());
        deprecateItemStorage("storage.physical", 3, AEApi.instance().definitions().items().cell16384k());
        deprecateItemStorage("storage.physical", 4, AEApi.instance().definitions().items().cellContainer());
        deprecateItemStorage("storage.physical.advanced.singularity", 0,
            AEApi.instance().definitions().items().cellSingularity());
        deprecateItemStorage("storage.physical.advanced.quantum", 0,
            AEApi.instance().definitions().items().cellQuantum());
        deprecateItemStorage("storage.physical.void", 0,
            AEApi.instance().definitions().items().cellVoid());
        deprecateItem("pattern.fluid", ItemAndBlockHolder.PATTERN);
        deprecateItem("terminal.fluid.wireless", ItemAndBlockHolder.WIRELESS_FLUID_TERM);
        /* Storage casings */
        deprecateItem("storage.casing", 0,
            AEApi.instance().definitions().materials().emptyAdvancedStorageCell());
        deprecateItem("storage.casing", 1, ItemAndBlockHolder.CELL_HOUSING, 2);
        /* Storage components (1k component, etc.) */
        deprecateItem("storage.component", 0, AEApi.instance().definitions().materials().cell256kPart());
        deprecateItem("storage.component", 1, AEApi.instance().definitions().materials().cell1024kPart());
        deprecateItem("storage.component", 2, AEApi.instance().definitions().materials().cell4096kPart());
        deprecateItem("storage.component", 3, AEApi.instance().definitions().materials().cell16384kPart());
        deprecateItem("storage.component", 4, ItemAndBlockHolder.CELL_PART, 0);
        deprecateItem("storage.component", 5, ItemAndBlockHolder.CELL_PART, 1);
        deprecateItem("storage.component", 6, ItemAndBlockHolder.CELL_PART, 2);
        deprecateItem("storage.component", 7, ItemAndBlockHolder.CELL_PART, 3);
        deprecateItem("storage.component", 8, ItemAndBlockHolder.CELL_PART, 4);
        deprecateItem("storage.component", 9, ItemAndBlockHolder.CELL_PART, 5);
        deprecateItem("storage.component", 10, ItemAndBlockHolder.CELL_PART, 6);
        deprecateItem("terminal.universal.wireless", ItemAndBlockHolder.WIRELESS_ULTRA_TERM);

    }

    /**
     * Register proxy part items.
     */
    static void proxyPartItems() {
        deprecateItemPart(0, ItemAndBlockHolder.FLUID_EXPORT_BUS, ProxyFluidBusIO::new);
        deprecateItemPart(1, ItemAndBlockHolder.FLUID_IMPORT_BUS, ProxyFluidBusIO::new);
        deprecateItemPart(2, ItemAndBlockHolder.FLUID_STORAGE_BUS, ProxyFluidStorage::new);
        deprecateItemPart(3, ItemAndBlockHolder.FLUID_TERM, ProxyPart::new);
        deprecateItemPart(4, ItemAndBlockHolder.FLUID_LEVEL_EMITTER, ProxyLevelEmitter::new);
        deprecateItemPart(9, ItemAndBlockHolder.FLUID_INTERFACE, ProxyFluidInterface::new);
        deprecateItemPart(10, ItemAndBlockHolder.FLUID_STORAGE_MONITOR, ProxyStorageMonitor::new);
        deprecateItemPart(11, ItemAndBlockHolder.FLUID_CONVERSION_MONITOR, ProxyStorageMonitor::new);
    }

    private static ProxyItem getOrBuildItem(String srcName) {
        final String fullName = "extracells:" + srcName;
        ProxyItem proxy = registry.get(fullName);
        if (proxy == null) {
            proxy = new ProxyItem(srcName);
            registry.put(fullName, proxy);
            proxy.register();
        }
        return proxy;
    }

    /**
     * Deprecate a simple item.
     * @param srcName name of the to-be-replaced item without the mod id prefix
     * @param srcMeta meta of the to-be-replaced item
     * @param replacement item that will replace src
     * @param targetMeta meta of the item that will replace src
     */
    private static void deprecateItem(String srcName, int srcMeta, Item replacement, int targetMeta) {
        getOrBuildItem(srcName).addMetaReplacement(srcMeta, replacement, targetMeta);
    }

    private static void deprecateItem(String srcName, Item replacement) {
        deprecateItem(srcName, 0, replacement, 0);
    }

    private static void deprecateItem(String srcName, int srcMeta, IItemDefinition replacement) {
        if (replacement.isEnabled()) {
            deprecateItem(srcName, srcMeta,
                replacement.maybeItem().get(),
                replacement.maybeStack(1).get().getItemDamage());
        }
    }

    private static void deprecateItemPart(int srcMeta, Item replacement, Function<ProxyPartItem, ProxyPart> partBuilder) {
        final String fullName = "extracells:part.base";
        ProxyPartItem proxyItem = (ProxyPartItem) registry.get(fullName);
        if (proxyItem == null) {
            proxyItem = new ProxyPartItem("part.base");
            registry.put(fullName, proxyItem);
            proxyItem.register();
        }
        proxyItem.addItemPart(srcMeta, replacement, partBuilder);
    }

    /**
     * Deprecate a storage item.
     */
    private static void deprecateFluidStorage(String srcName, int srcMeta, Item replacement) {
        ProxyItem item = getOrBuildItem(srcName);
        ProxyItem.ProxyReplacement storage = new FluidStorageReplacement(replacement, 0);
        item.addMetaReplacement(srcMeta, storage);
    }

    /**
     * Deprecate a storage item. This goes through AEAPI, as AE2FC doesn't have any item cells.
     * If the item is disabled in AEAPI, the replacement will not be registered!
     */
    private static void deprecateItemStorage(String srcName, int srcMeta, IItemDefinition replacement) {
        if (replacement.isEnabled()) {
            ProxyItem item = getOrBuildItem(srcName);
            ProxyItem.ProxyReplacement storage = new ItemStorageReplacement(
                replacement.maybeItem().get(), replacement.maybeStack(1).get().getItemDamage());
            item.addMetaReplacement(srcMeta, storage);
        }
    }
}

class FluidStorageReplacement extends ProxyItem.ProxyReplacement {
    FluidStorageReplacement(Item replacement, int replacementMeta) {
        super(replacement, replacementMeta);
    }

    @Override
    NBTTagCompound replaceNBT(NBTTagCompound compound) {
        System.out.println(compound);
        return compound;
    }
}

class ItemStorageReplacement extends ProxyItem.ProxyReplacement {
    ItemStorageReplacement(Item replacement, int replacementMeta) {
        super(replacement, replacementMeta);
    }

    @Override
    NBTTagCompound replaceNBT(NBTTagCompound compound) {
        System.out.println(compound);
        return compound;
    }
}
