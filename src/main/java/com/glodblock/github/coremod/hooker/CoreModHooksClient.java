package com.glodblock.github.coremod.hooker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.client.gui.GuiInterfaceTerminalWireless;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CoreModHooksClient {

    public static void clientPacketData(NBTTagCompound data) {
        GuiScreen gs = Minecraft.getMinecraft().currentScreen;
        if (gs instanceof GuiInterfaceTerminalWireless) {
            ((GuiInterfaceTerminalWireless) gs).postUpdate(data);
        }
    }

}
