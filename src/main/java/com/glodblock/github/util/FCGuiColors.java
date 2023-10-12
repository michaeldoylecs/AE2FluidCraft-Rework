/*
 * This file is part of Applied Energistics 2. Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved. Applied
 * Energistics 2 is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Applied Energistics 2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details. You should have received a copy of the GNU Lesser General Public License along with
 * Applied Energistics 2. If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package com.glodblock.github.util;

import net.minecraft.util.StatCollector;

import appeng.core.AELog;

public enum FCGuiColors {

    // ARGB Colors: Name and default value
    StateNone(0x00000000),
    StateIdle(0xFF55FF55),
    StateCraft(0xFFFFFF55),
    StateExport(0xFFAA00AA),
    StateError(0xFFFF5555);

    private final String root;
    private final int color;

    FCGuiColors() {
        this.root = "ae2fc.gui.color";
        this.color = 0x000000;
    }

    FCGuiColors(final int hex) {
        this.root = "ae2fc.gui.color";
        this.color = hex;
    }

    public int getColor() {
        String hex = StatCollector.translateToLocal(this.getUnlocalized());
        int color = this.color;

        if (hex.length() <= 8) {
            try {
                color = Integer.parseUnsignedInt(hex, 16);
            } catch (final NumberFormatException e) {
                AELog.warn("Couldn't format color correctly for: " + this.root + " -> " + hex);
            }
        }
        return color;
    }

    public String getUnlocalized() {
        return this.root + '.' + this.toString();
    }
}
