package com.glodblock.github.loader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.lwjgl.input.Keyboard;

import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.util.Util;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeybindLoader implements Runnable {

    public static KeyBinding openTerminal;

    @Override
    public void run() {
        openTerminal = new KeyBinding("key.OpenTerminal", Keyboard.CHAR_NONE, "itemGroup.ae2fc");
        ClientRegistry.registerKeyBinding(openTerminal);
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Minecraft.getMinecraft().currentScreen != null) return;
        EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
        if (p.openContainer == null) {
            return;
        }

        if (openTerminal.isPressed()) {
            ImmutablePair<Integer, ItemStack> term = Util.getUltraWirelessTerm(p);
            if (term != null && term.getRight().getItem() instanceof ItemWirelessUltraTerminal) {
                ItemWirelessUltraTerminal.switchTerminal(
                        p,
                        ((ItemWirelessUltraTerminal) term.getRight().getItem()).guiGuiType(term.getRight()));
            }
        }
    }
}
