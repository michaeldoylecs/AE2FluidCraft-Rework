package com.glodblock.github.inventory;

import appeng.core.AELog;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Since there is no ae2 available now for compiling, we copied it over, and it will need to be removed in the future.
 */
@Deprecated
public class BiggerAppEngInventory extends AppEngInternalInventory {

    private final ItemStack[] inv;

    public BiggerAppEngInventory(IAEAppEngInventory inventory, int size) {
        super(inventory, size);
        inv = ObfuscationReflectionHelper.getPrivateValue(AppEngInternalInventory.class, this, "inv");
    }

    protected void writeToNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = new NBTTagCompound();

                if (this.inv[x] != null) {
                    this.inv[x].writeToNBT(c);
                    c.setInteger("Count", this.inv[x].stackSize);
                }

                target.setTag("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final NBTTagCompound target) {
        for (int x = 0; x < this.getSizeInventory(); x++) {
            try {
                final NBTTagCompound c = target.getCompoundTag("#" + x);

                if (c != null) {
                    ItemStack stack = ItemStack.loadItemStackFromNBT(c);
                    if (stack != null) {
                        stack.stackSize = c.getInteger("Count");
                    }
                    this.inv[x] = stack;
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

}
