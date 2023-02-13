package com.glodblock.github.coremod.hooker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiInterfaceTerminalWireless;
import com.glodblock.github.common.item.ItemWirelessInterfaceTerminal;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.CPacketSwitchGuis;

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

    public static void reopenInterfaceTerminal() {
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        ItemStack c = p.getCurrentEquippedItem();
        if (c.getItem() instanceof ItemWirelessInterfaceTerminal || c.getItem() instanceof ItemWirelessUltraTerminal) {
            FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(GuiType.WIRELESS_INTERFACE_TERMINAL, true));
        }
    }

}
