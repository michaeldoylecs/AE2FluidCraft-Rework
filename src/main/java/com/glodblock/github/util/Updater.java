package com.glodblock.github.util;

@FunctionalInterface
public interface Updater<Input, Output> {

    Output update(Input data);
}
