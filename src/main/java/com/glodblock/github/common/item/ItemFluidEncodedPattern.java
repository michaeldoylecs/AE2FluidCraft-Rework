package com.glodblock.github.common.item;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemFluidEncodedPattern extends ItemEncodedPattern {

    public ItemFluidEncodedPattern() {
        super();
        this.setUnlocalizedName(NameConst.ITEM_FLUID_ENCODED_PATTERN);
        this.setTextureName(FluidCraft.MODID + ":" + NameConst.ITEM_FLUID_ENCODED_PATTERN);
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        FluidPatternDetails pattern = new FluidPatternDetails(is);
        return pattern.readFromStack() ? pattern : null;
    }

    public ItemFluidEncodedPattern register() {
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_ENCODED_PATTERN, FluidCraft.MODID);
        return this;
    }

    @Override
    public void addCheckedInformation(
            final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo) {
        NBTTagCompound data = stack.getTagCompound();
        final boolean combine = data.getBoolean("combine");
        final boolean prio = data.getBoolean("prioritize");
        super.addCheckedInformation(stack, player, lines, displayMoreInfo);
        //        lines.add(String.format("%s: %s",combine ?
        // StatCollector.translateToLocal(NameConst.TT_KEY+"combine"):StatCollector.translateToLocal(NameConst.TT_KEY+"not_combine"),combine ? GuiText.Yes.getLocal() : GuiText.No.getLocal()));
        //        lines.add(String.format("%s: %s",prio ?
        // StatCollector.translateToLocal(NameConst.TT_KEY+"prio"):StatCollector.translateToLocal(NameConst.TT_KEY+"not_prio"),prio ? GuiText.Yes.getLocal() : GuiText.No.getLocal()));
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }
}
