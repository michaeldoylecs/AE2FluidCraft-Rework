package com.glodblock.github.crossmod.extracells;

import com.glodblock.github.FluidCraft;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class EC2ProxyItem extends Item {

    protected final String name;

    protected final Map<Integer, ProxyReplacement> replacements;
    public EC2ProxyItem(String ec2itemName) {
        this.name = ec2itemName;
        this.replacements = new HashMap<>();
    }

    void register() {
        GameRegistry.registerItem(this, "ec2placeholder." + name, FluidCraft.MODID);
    }

    protected void addMetaReplacement(int srcMeta, Item replacement, int targetMeta, Function<NBTTagCompound, NBTTagCompound> nbtTransformer) {
        this.replacements.put(srcMeta, new ProxyReplacement(replacement, targetMeta, nbtTransformer));
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack s = player.inventory.getStackInSlot(i);
                if (s == stack) {
                    int meta = stack.getItemDamage();
                    ProxyReplacement r = replacements.get(meta);
                    if (r == null) {
                        player.inventory.setInventorySlotContents(i, null);
                    } else {
                        ItemStack replaceStack = new ItemStack(r.replacement, stack.stackSize, r.meta);
                        if (replaceStack.hasTagCompound()) {
                            replaceStack.setTagCompound(r.nbtTransformer.apply(replaceStack.getTagCompound()));
                        }
                        player.inventory.setInventorySlotContents(i, replaceStack);
                    }
                }
            }
        }
    }
    private static class ProxyReplacement {
        private final Item replacement;
        private final int meta;
        protected final Function<NBTTagCompound, NBTTagCompound> nbtTransformer;
        ProxyReplacement(Item replacement, int meta, Function<NBTTagCompound, NBTTagCompound> nbtTransformer) {
            this.replacement = replacement;
            this.meta = meta;
            this.nbtTransformer = nbtTransformer;
        }
    }
}
