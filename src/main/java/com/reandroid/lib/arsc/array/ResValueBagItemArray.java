package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.value.ResValueBagItem;

public class ResValueBagItemArray extends BlockArray<ResValueBagItem> {
    public ResValueBagItemArray(){
        super();
    }
    @Override
    public ResValueBagItem newInstance() {
        return new ResValueBagItem();
    }

    @Override
    public ResValueBagItem[] newInstance(int len) {
        return new ResValueBagItem[len];
    }

    @Override
    protected void onRefreshed() {

    }
}
