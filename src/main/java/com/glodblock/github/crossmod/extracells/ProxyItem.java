package com.glodblock.github.crossmod.extracells;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.FCBaseItem;
import com.glodblock.github.common.item.ItemMultiFluidStorageCell;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EC2ProxyItem - Item that "holds" the data of a missing item.
 */
public class ProxyItem extends FCBaseItem {

    protected final String name;

    /**
     * Maps a metadata value to a replacement.
     */
    protected final Map<Integer, ProxyItemEntry> replacements;
    public ProxyItem(String ec2itemName) {
        this.name = ec2itemName;
        this.replacements = new HashMap<>();
    }

    @Override
    public FCBaseItem register() {
        GameRegistry.registerItem(this, "ec2placeholder." + name, FluidCraft.MODID);
        return this;
    }

    protected void addMetaReplacement(int srcMeta, Item replacement, int targetMeta) {
        this.replacements.put(srcMeta, new ProxyItemEntry(replacement, targetMeta));
    }

    protected void addMetaReplacement(int srcMeta, ProxyItemEntry replacement) {
        this.replacements.put(srcMeta, replacement);
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean p_77624_4_) {
        info.add("§7(Extra Cells Placeholder): " + name);
        info.add("§7Put in your inventory to get a replacement (or disappear if incompatible)");
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack s = player.inventory.getStackInSlot(i);
                if (s == stack) {
                    int meta = stack.getItemDamage();
                    ProxyItemEntry r = replacements.get(meta);
                    if (r == null) {
                        player.inventory.setInventorySlotContents(i, null);
                    } else {
                        ItemStack replaceStack = new ItemStack(r.replacement, stack.stackSize, r.replacementMeta);
                        if (stack.hasTagCompound()) {
                            replaceStack.setTagCompound(r.replaceNBT(stack.getTagCompound()));
                        }
                        player.inventory.setInventorySlotContents(i, replaceStack);
                    }
                }
            }
        }
    }

    /**
     * Base class for a mapping from source (Item, meta) -> replacement (Item, meta).
     * Optionally, override the {@link ProxyItemEntry#replaceNBT(NBTTagCompound)}
     * method for custom NBT migration.
     */
    protected static class ProxyItemEntry {

        protected final Item replacement;
        protected final int replacementMeta;

        /**
         * Creates a Proxy Replacement.
         * @param replacement Item that will replace the other.
         * @param replacementMeta Metadata/damage value of the replacement
         */
        protected ProxyItemEntry(Item replacement, int replacementMeta) {
            this.replacement = replacement;
            this.replacementMeta = replacementMeta;
        }

        NBTTagCompound replaceNBT(NBTTagCompound compound) {
            return compound;
        }
    }

    protected static class ItemStorageEntry extends ProxyItemEntry {
        ItemStorageEntry(Item replacement, int replacementMeta) {
            super(replacement, replacementMeta);
        }

        @Override
        NBTTagCompound replaceNBT(NBTTagCompound compound) {
            System.out.println(compound);
            return compound;
        }
    }

    protected static class ProxyFluidStorageEntry extends ProxyItemEntry {
        public final long maxBytes;
        public final int bytesPerType;
        public final double idleDrain;
        protected ProxyFluidStorageEntry(ItemMultiFluidStorageCell replacement, long kilobytes, int bytesPerType, double idleDrain) {
            super(replacement, 0);
            this.maxBytes = kilobytes * 1024;
            this.bytesPerType = bytesPerType;
            this.idleDrain = idleDrain;
        }

        @Override
        NBTTagCompound replaceNBT(NBTTagCompound compound) {
            if (compound != null && compound.hasKey("ecc")) {
                compound.removeTag("ecc");
            }
            return compound;
        }
    }
}
