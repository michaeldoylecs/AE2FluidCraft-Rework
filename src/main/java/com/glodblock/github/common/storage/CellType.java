package com.glodblock.github.common.storage;

import java.util.EnumSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import appeng.core.features.AEFeature;

public enum CellType {

    Cell1kPart(0, AEFeature.StorageCells),
    Cell4kPart(1, AEFeature.StorageCells),
    Cell16kPart(2, AEFeature.StorageCells),
    Cell64kPart(3, AEFeature.StorageCells),
    Cell256kPart(4, AEFeature.StorageCells),
    Cell1024kPart(5, AEFeature.StorageCells),
    Cell4096kPart(6, AEFeature.StorageCells),
    Cell16384kPart(7, AEFeature.StorageCells);

    private final EnumSet<AEFeature> features;
    private int damageValue;
    private Item itemInstance;

    CellType(final int metaValue, final AEFeature part) {
        this.setDamageValue(metaValue);
        this.features = EnumSet.of(part);
    }

    public int getDamageValue() {
        return this.damageValue;
    }

    void setDamageValue(final int damageValue) {
        this.damageValue = damageValue;
    }

    EnumSet<AEFeature> getFeature() {
        return this.features;
    }

    public ItemStack stack(final int size) {
        return new ItemStack(this.getItemInstance(), size, this.getDamageValue());
    }

    public Item getItemInstance() {
        return this.itemInstance;
    }

    public void setItemInstance(final Item itemInstance) {
        this.itemInstance = itemInstance;
    }

    public static EnumChatFormatting getTypeColor(int type) {
        return getTypeColor(CellType.values()[type % CellType.values().length]);
    }

    public static EnumChatFormatting getTypeColor(CellType type) {
        switch (type) {
            case Cell1kPart:
                return EnumChatFormatting.GOLD;
            case Cell4kPart:
                return EnumChatFormatting.YELLOW;
            case Cell16kPart:
                return EnumChatFormatting.GREEN;
            case Cell64kPart:
                return EnumChatFormatting.AQUA;
            case Cell256kPart:
                return EnumChatFormatting.BLUE;
            case Cell1024kPart:
                return EnumChatFormatting.LIGHT_PURPLE;
            case Cell4096kPart:
                return EnumChatFormatting.RED;
            case Cell16384kPart:
                return EnumChatFormatting.DARK_PURPLE;
        }
        return EnumChatFormatting.WHITE;
    }
}
