package com.glodblock.github.loader;

import com.glodblock.github.client.render.ItemCertusQuartzTankRender;
import com.glodblock.github.client.render.ItemDropRender;
import com.glodblock.github.client.render.ItemPacketRender;
import com.glodblock.github.client.render.ItemWalrusRender;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        new ItemDropRender();
        new ItemPacketRender();
        new ItemWalrusRender();
        new ItemCertusQuartzTankRender();
    }
}
