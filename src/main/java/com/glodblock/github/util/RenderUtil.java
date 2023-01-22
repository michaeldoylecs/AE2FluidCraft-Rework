package com.glodblock.github.util;

import appeng.api.storage.data.IAEFluidStack;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public final class RenderUtil {

    public static void renderItemIcon(IIcon icon, double size, double z, float nx, float ny, float nz) {
        renderItemIcon(icon, 0.0D, 0.0D, size, size, z, nx, ny, nz);
    }

    public static void renderItemIcon(
            IIcon icon,
            double xStart,
            double yStart,
            double xEnd,
            double yEnd,
            double z,
            float nx,
            float ny,
            float nz) {
        if (icon == null) {
            return;
        }
        Tessellator.instance.startDrawingQuads();
        Tessellator.instance.setNormal(nx, ny, nz);
        if (nz > 0.0F) {
            Tessellator.instance.addVertexWithUV(xStart, yStart, z, icon.getMinU(), icon.getMinV());
            Tessellator.instance.addVertexWithUV(xEnd, yStart, z, icon.getMaxU(), icon.getMinV());
            Tessellator.instance.addVertexWithUV(xEnd, yEnd, z, icon.getMaxU(), icon.getMaxV());
            Tessellator.instance.addVertexWithUV(xStart, yEnd, z, icon.getMinU(), icon.getMaxV());
        } else {
            Tessellator.instance.addVertexWithUV(xStart, yEnd, z, icon.getMinU(), icon.getMaxV());
            Tessellator.instance.addVertexWithUV(xEnd, yEnd, z, icon.getMaxU(), icon.getMaxV());
            Tessellator.instance.addVertexWithUV(xEnd, yStart, z, icon.getMaxU(), icon.getMinV());
            Tessellator.instance.addVertexWithUV(xStart, yStart, z, icon.getMinU(), icon.getMinV());
        }
        Tessellator.instance.draw();
    }

    public static void renderFluidIntoGui(
            Gui gui, int x, int y, int width, int height, @Nullable IAEFluidStack aeFluidStack, int capacity) {
        if (aeFluidStack != null) {
            GL11.glDisable(2896);
            GL11.glColor3f(1.0F, 1.0F, 1.0F);
            int hi = (int) (height * ((double) aeFluidStack.getStackSize() / capacity));
            if (aeFluidStack.getStackSize() > 0 && hi > 0) {
                Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
                IIcon fluidIcon = aeFluidStack.getFluid().getStillIcon();
                GL11.glColor3f(
                        (float) (aeFluidStack.getFluid().getColor() >> 16 & 255) / 255.0F,
                        (float) (aeFluidStack.getFluid().getColor() >> 8 & 255) / 255.0F,
                        (float) (aeFluidStack.getFluid().getColor() & 255) / 255.0F);
                for (int th = 0; th <= hi; th += 16) {
                    if (hi - th <= 0) break;
                    gui.drawTexturedModelRectFromIcon(
                            x, y + height - Math.min(16, hi - th) - th, fluidIcon, width, Math.min(16, hi - th));
                }
                GL11.glColor3f(1.0F, 1.0F, 1.0F);
            }
        }
    }
}
