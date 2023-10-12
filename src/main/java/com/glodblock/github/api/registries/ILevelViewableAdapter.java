package com.glodblock.github.api.registries;

import appeng.api.networking.IGridHost;

public interface ILevelViewableAdapter extends ILevelViewable {

    ILevelViewable adapt(IGridHost gridHost);
}
