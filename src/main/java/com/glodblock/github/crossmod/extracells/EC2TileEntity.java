package com.glodblock.github.crossmod.extracells;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class EC2TileEntity<T extends TileEntity> extends TileEntity {

    private Object oldTile;
    private Class<? extends TileEntity> newTile;
    public EC2TileEntity(String name, T oldTE, Class<? extends TileEntity> newTE) {
        this.oldTile = oldTE;
        this.newTile = newTE;
    }

    abstract TileEntity remap();

}
