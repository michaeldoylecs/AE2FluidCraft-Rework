package com.glodblock.github.common.parts;

import appeng.client.texture.CableBusTextures;
import appeng.helpers.Reflected;
import com.glodblock.github.common.parts.base.FCPartMonitor;
import net.minecraft.item.ItemStack;

public class PartFluidStorageMonitor extends FCPartMonitor {

    private static final CableBusTextures FRONT_BRIGHT_ICON = CableBusTextures.PartStorageMonitor_Bright;
    private static final CableBusTextures FRONT_DARK_ICON = CableBusTextures.PartStorageMonitor_Dark;
    private static final CableBusTextures FRONT_COLORED_ICON = CableBusTextures.PartStorageMonitor_Colored;
    private static final CableBusTextures FRONT_COLORED_ICON_LOCKED =
            CableBusTextures.PartStorageMonitor_Colored_Locked;

    @Reflected
    public PartFluidStorageMonitor(final ItemStack is) {
        super(is);
    }

    @Override
    public CableBusTextures getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public CableBusTextures getFrontColored() {
        return this.isLocked() ? FRONT_COLORED_ICON_LOCKED : FRONT_COLORED_ICON;
    }

    @Override
    public CableBusTextures getFrontDark() {
        return FRONT_DARK_ICON;
    }
}
