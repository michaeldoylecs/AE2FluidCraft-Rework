package com.glodblock.github.client.render;

import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

public class ItemCertusQuartzTankRender implements IItemRenderer {

    public ItemCertusQuartzTankRender() {

        //
        // MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(ItemAndBlockHolder.CERTUS_QUARTZ_TANK),
        // this);
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {}
}
