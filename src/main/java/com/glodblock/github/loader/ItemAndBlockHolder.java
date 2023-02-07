package com.glodblock.github.loader;

import com.glodblock.github.common.Config;
import com.glodblock.github.common.block.*;
import com.glodblock.github.common.item.*;
import com.glodblock.github.common.storage.CellType;

public class ItemAndBlockHolder {

    public static BlockCertusQuartzTank CERTUS_QUARTZ_TANK = new BlockCertusQuartzTank().register();
    public static BlockFluidAutoFiller FLUID_AUTO_FILLER = new BlockFluidAutoFiller().register();
    public static BlockFluidDiscretizer DISCRETIZER = new BlockFluidDiscretizer().register();
    public static BlockLevelMaintainer LEVEL_MAINTAINER = new BlockLevelMaintainer().register();
    public static BlockFluidPatternEncoder ENCODER = new BlockFluidPatternEncoder().register();
    public static BlockFluidPacketDecoder DECODER = new BlockFluidPacketDecoder().register();
    public static BlockFluidInterface INTERFACE = new BlockFluidInterface().register();
    public static BlockIngredientBuffer BUFFER = new BlockIngredientBuffer().register();
    public static BlockFluidBuffer FLUID_BUFFER = new BlockFluidBuffer().register();
    public static BlockLargeIngredientBuffer LARGE_BUFFER = new BlockLargeIngredientBuffer().register();
    public static BlockOCPatternEditor OC_EDITOR = new BlockOCPatternEditor().register();
    public static BlockWalrus WALRUS = new BlockWalrus().register();
    public static ItemFluidDrop DROP = new ItemFluidDrop().register();
    public static ItemFluidEncodedPattern PATTERN = new ItemFluidEncodedPattern().register();
    public static ItemPartFluidTerminal FLUID_TERM = new ItemPartFluidTerminal().register();
    public static ItemPartFluidPatternTerminal FLUID_TERMINAL = new ItemPartFluidPatternTerminal().register();
    public static ItemPartFluidPatternTerminalEx FLUID_TERMINAL_EX = new ItemPartFluidPatternTerminalEx().register();
    public static ItemPartFluidInterface FLUID_INTERFACE = new ItemPartFluidInterface().register();
    public static ItemFluidImportBus FLUID_IMPORT_BUS = new ItemFluidImportBus().register();
    public static ItemFluidExportBus FLUID_EXPORT_BUS = new ItemFluidExportBus().register();
    public static ItemPartFluidStorageBus FLUID_STORAGE_BUS = new ItemPartFluidStorageBus().register();
    public static ItemFluidLevelEmitter FLUID_LEVEL_EMITTER = new ItemFluidLevelEmitter().register();
    public static ItemFluidStorageMonitor FLUID_STORAGE_MONITOR = new ItemFluidStorageMonitor().register();
    public static ItemFluidConversionMonitor FLUID_CONVERSION_MONITOR = new ItemFluidConversionMonitor().register();
    public static ItemPortableFluidCell PORTABLE_FLUID_CELL = new ItemPortableFluidCell().register();
    public static ItemWirelessFluidTerminal WIRELESS_FLUID_TERM = new ItemWirelessFluidTerminal().register();
    public static ItemWirelessUltraTerminal WIRELESS_ULTRA_TERM = new ItemWirelessUltraTerminal().register();
    public static ItemWirelessPatternTerminal WIRELESS_PATTERN_TERM = new ItemWirelessPatternTerminal().register();
    public static ItemWirelessInterfaceTerminal WIRELESS_INTERFACE_TERM = new ItemWirelessInterfaceTerminal()
            .register();
    public static ItemFluidPacket PACKET = new ItemFluidPacket().register();
    public static ItemBasicFluidStorageCell CELL1K = new ItemBasicFluidStorageCell(CellType.Cell1kPart, 1).register();
    public static ItemBasicFluidStorageCell CELL4K = new ItemBasicFluidStorageCell(CellType.Cell4kPart, 4).register();
    public static ItemBasicFluidStorageCell CELL16K = new ItemBasicFluidStorageCell(CellType.Cell16kPart, 16)
            .register();
    public static ItemBasicFluidStorageCell CELL64K = new ItemBasicFluidStorageCell(CellType.Cell64kPart, 64)
            .register();
    public static ItemBasicFluidStorageCell CELL256K = new ItemBasicFluidStorageCell(CellType.Cell256kPart, 256)
            .register();
    public static ItemBasicFluidStorageCell CELL1024K = new ItemBasicFluidStorageCell(CellType.Cell1024kPart, 1024)
            .register();
    public static ItemBasicFluidStorageCell CELL4096K = new ItemBasicFluidStorageCell(CellType.Cell4096kPart, 4096)
            .register();
    public static ItemBasicFluidStorageCell CELL16384K = new ItemBasicFluidStorageCell(CellType.Cell16384kPart, 16384)
            .register();
    public static ItemBasicFluidStoragePart CELL_PART = new ItemBasicFluidStoragePart().register();

    public static void loadSetting() {
        if (!Config.fluidCells) return;
        CellType.Cell1kPart.setItemInstance(CELL_PART);
        CellType.Cell4kPart.setItemInstance(CELL_PART);
        CellType.Cell16kPart.setItemInstance(CELL_PART);
        CellType.Cell64kPart.setItemInstance(CELL_PART);
        CellType.Cell256kPart.setItemInstance(CELL_PART);
        CellType.Cell1024kPart.setItemInstance(CELL_PART);
        CellType.Cell4096kPart.setItemInstance(CELL_PART);
        CellType.Cell16384kPart.setItemInstance(CELL_PART);
    }
}
