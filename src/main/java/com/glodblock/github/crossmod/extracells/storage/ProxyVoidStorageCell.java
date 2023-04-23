package com.glodblock.github.crossmod.extracells.storage;

import appeng.api.AEApi;
import appeng.core.localization.GuiText;
import appeng.items.storage.ItemVoidStorageCell;
import com.glodblock.github.crossmod.extracells.ProxyItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ProxyVoidStorageCell extends ItemVoidStorageCell {

    private final String name = AEApi.instance().definitions().items().cellVoid().maybeStack(1).get().getUnlocalizedName();
    @Override
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List<String> info, boolean displayMoreInfo) {
        info.add(EnumChatFormatting.RED + "Extra Cells Placeholder for:");
        info.add(EnumChatFormatting.AQUA + name);
        info.add(EnumChatFormatting.GOLD + "Put in your inventory to get a replacement.");
        info.add(EnumChatFormatting.GOLD + "It will disappear if no replacement was found.");
        info.add(EnumChatFormatting.RED + "Report missing conversions on the Github.");
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
        if (!worldIn.isRemote && entityIn instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entityIn;
            for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                ItemStack s = player.inventory.getStackInSlot(i);
                if (s == stack) {
                    ItemStack replaceStack = AEApi.instance().definitions().items().cellVoid().maybeStack(1).get();
                    player.inventory.setInventorySlotContents(i, replaceStack);
                    break;
                }
            }
        }

    }
}
