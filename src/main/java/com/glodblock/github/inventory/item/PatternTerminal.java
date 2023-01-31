package com.glodblock.github.inventory.item;

import net.minecraft.inventory.IInventory;

import appeng.api.storage.data.IAEItemStack;

public interface PatternTerminal {

    boolean isInverted();

    IInventory getInventoryByName(final String name);

    boolean canBeSubstitute();

    boolean isPrioritize();

    boolean isSubstitution();

    boolean shouldCombine();

    void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput);

    void setCraftingRecipe(final boolean craftingMode);

    void setSubstitution(boolean canSubstitute);

    void setBeSubstitute(boolean canBeSubstitute);

    void setCombineMode(boolean shouldCombine);

    void setPrioritization(boolean canPrioritize);

    void setInverted(boolean inverted);

    int getActivePage();

    void setActivePage(int activePage);

    boolean isCraftingRecipe();

    void sortCraftingItems();

    default void saveSettings() {}
}
