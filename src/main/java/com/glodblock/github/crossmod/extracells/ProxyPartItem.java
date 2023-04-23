package com.glodblock.github.crossmod.extracells;

import java.util.function.Function;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;

public class ProxyPartItem extends ProxyItem implements IPartItem {

    /**
     * Creates a ProxyPartItem associated with the ProxyPart instance. This instance is reused on each call to
     * {@link #createPartFromItemStack(ItemStack)}.
     * 
     * @param ec2itemName extra cells internal name
     */
    public ProxyPartItem(String ec2itemName) {
        super(ec2itemName);
    }

    /**
     *
     * @param srcMeta
     * @param replacement
     * @param part
     */
    protected void addItemPart(int srcMeta, Item replacement, Function<ProxyPartItem, ProxyPart> part) {
        this.replacements.put(srcMeta, new PartReplacement(replacement, part));
    }

    @Nullable
    @Override
    public IPart createPartFromItemStack(ItemStack is) {
        final int meta = is.getItemDamage();
        if (this.replacements.get(meta) instanceof PartReplacement) {
            PartReplacement part = (PartReplacement) this.replacements.get(meta);
            return part.proxyPart.apply(this);
        }
        return null;
    }
}

/**
 * Item replacement entry for ItemParts. All targets replace into 0.
 */
class PartReplacement extends ProxyItem.ProxyItemEntry {

    Function<ProxyPartItem, ProxyPart> proxyPart;

    PartReplacement(Item replacement, Function<ProxyPartItem, ProxyPart> proxyPart) {
        super(replacement, 0);
        this.proxyPart = proxyPart;
    }
}
