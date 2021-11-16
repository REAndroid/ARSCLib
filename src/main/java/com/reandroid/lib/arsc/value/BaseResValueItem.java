package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.item.ReferenceItem;

public abstract class BaseResValueItem extends BaseResValue implements ResValueItem {

    private ReferenceItem mReferenceItem;
    BaseResValueItem(int bytesLength) {
        super(bytesLength);
    }
    public ReferenceItem getTableStringReference(){
        if(getValueType()!=ValueType.STRING){
            return null;
        }
        if(mReferenceItem==null){
            mReferenceItem=createReferenceItem();
        }
        return mReferenceItem;
    }
    boolean removeTableReference(){
        ReferenceItem ref=mReferenceItem;
        if(ref==null){
            return false;
        }
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        mReferenceItem=null;
        return entryBlock.removeTableReference(ref);
    }
    private ReferenceItem createReferenceItem(){
        return new ReferenceItem() {
            @Override
            public void set(int val) {
                if(getValueType()==ValueType.STRING){
                    BaseResValueItem.this.setData(val);
                }
            }
            @Override
            public int get() {
                if(getValueType()==ValueType.STRING){
                    return BaseResValueItem.this.getData();
                }
                return -1;
            }
        };
    }

    public ValueType getValueType(){
        return ValueType.valueOf(getType());
    }
    @Override
    public void setType(ValueType valueType){
        byte type=0;
        if(valueType!=null){
            type=valueType.getByte();
        }
        setType(type);
    }
}
