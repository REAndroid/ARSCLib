package com.reandroid.lib.arsc.base;

public interface BlockCreator<T extends Block> {
    T newInstance();
}
