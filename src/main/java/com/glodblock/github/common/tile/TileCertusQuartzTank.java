package com.glodblock.github.common.tile;

import buildcraft.factory.TileTank;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class TileCertusQuartzTank extends TileTank {
    public static final int CAPACITY = FluidContainerRegistry.BUCKET_VOLUME * 32;

    public TileCertusQuartzTank() {
        super();
        this.setCapacity();
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        readFromNBTWithoutCoords(data);
        this.setCapacity();
    }

    public void readFromNBTWithoutCoords(NBTTagCompound tag) {
        this.tank.readFromNBT(tag);
    }

    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        writeToNBTWithoutCoords(data);
    }

    public void setCapacity() {
        this.tank.setCapacity(CAPACITY);
    }

    public void writeToNBTWithoutCoords(NBTTagCompound tag) {
        this.tank.writeToNBT(tag);
    }
}
