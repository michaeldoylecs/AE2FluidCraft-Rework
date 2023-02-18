package com.glodblock.github.inventory.item;

import appeng.api.storage.data.IAEItemStack;

public interface IItemPatternTerminal extends IItemTerminal {

    boolean isInverted();

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

    boolean isAutoFillPattern();

    void setAutoFillPattern(boolean canFill);
}
