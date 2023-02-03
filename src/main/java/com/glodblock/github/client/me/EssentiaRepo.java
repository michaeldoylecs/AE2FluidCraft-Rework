package com.glodblock.github.client.me;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.crossmod.thaumcraft.AspectUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Iterator;

public class EssentiaRepo extends FluidRepo {

    public EssentiaRepo(final IScrollSource src, final ISortSource sortSrc) {
        super(src, sortSrc);
    }

    @Override
    public void updateView() {
        super.updateView();
        Iterator<IAEItemStack> it1 = this.view.iterator();
        while (it1.hasNext()) {
            IAEFluidStack fluid = ItemFluidDrop.getAeFluidStack(it1.next());
            if (!AspectUtil.isEssentiaGas(fluid)) {
                it1.remove();
            }
        }
        Iterator<ItemStack> it2 = this.dsp.iterator();
        while (it2.hasNext()) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(it2.next());
            if (!AspectUtil.isEssentiaGas(fluid)) {
                it2.remove();
            }
        }
    }

}
