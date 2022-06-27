package com.reandroid.lib.arsc.base;

public interface BlockArrayCreator<T extends Block> extends BlockCreator<T>{
    T[] newInstance(int len);
}
