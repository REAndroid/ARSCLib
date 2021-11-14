package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.base.BlockContainer;
import com.reandroid.lib.arsc.value.BaseResValue;

public class ResValueContainer extends BlockContainer<BaseResValue> {
    private final BaseResValue[] mChildes;
    public ResValueContainer(){
        super();
        mChildes=new BaseResValue[1];
    }
    @Override
    protected void onRefreshed(){
    }
    @Override
    public int childesCount() {
        return mChildes.length;
    }
    @Override
    public BaseResValue[] getChildes() {
        return mChildes;
    }
    public void setResValue(BaseResValue resValue){
        BaseResValue old=getResValue();
        if(old!=null){
            old.setIndex(-1);
            old.setParent(null);
        }
        mChildes[0]=resValue;
        if(resValue==null){
            return;
        }
        resValue.setIndex(0);
        resValue.setParent(this);
    }
    public BaseResValue getResValue(){
        if(mChildes.length==0){
            return null;
        }
        return mChildes[0];
    }
}
