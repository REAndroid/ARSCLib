package com.reandroid.lib.arsc.group;

import com.reandroid.lib.arsc.base.BlockArrayCreator;
import com.reandroid.lib.arsc.item.StringItem;

public class StringGroup<T extends StringItem> extends ItemGroup<T>{
    public StringGroup(BlockArrayCreator<T> blockArrayCreator, String name){
        super(blockArrayCreator, name);
    }
}
