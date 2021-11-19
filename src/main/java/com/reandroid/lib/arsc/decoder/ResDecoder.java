package com.reandroid.lib.arsc.decoder;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;

public abstract class ResDecoder<InputBlock extends Block, Output> implements ResourceNameProvider{
    private final ResourceNameStore resourceNameStore;
    public ResDecoder(ResourceNameStore store){
        this.resourceNameStore=store;
    }
    public String decodeAttribute(int attrResId, int rawValue){
        ResValueBag valueBag=getAttributeBag(attrResId);
        if(valueBag==null){
            return null;
        }
        ResValueBagItem[] bagItems=valueBag.getBagItems();
        if(bagItems==null||bagItems.length==0){
            return null;
        }
        int len=bagItems.length;
        ResValueBagItem firstAttrChild=bagItems[0];
        if(len==1){
            return null;
        }
        if(isFlagAttr(firstAttrChild)){
            for(int i=1;i<len;i++){
                ResValueBagItem bagItem=bagItems[i];
                int data=bagItem.getData();
                if(data!=rawValue){
                    continue;
                }
                int id=bagItem.getId();
                return getResourceName(id, false);
            }
            return null;
        }
        StringBuilder builder=new StringBuilder();
        boolean appendOnce=false;
        for(int i=1;i<len;i++){
            ResValueBagItem bagItem=bagItems[i];
            int data=bagItem.getData();
            if((data&rawValue)==0){
                continue;
            }
            int id=bagItem.getId();
            String name=getResourceName(id, false);
            if(appendOnce){
                builder.append("|");
            }
            builder.append(name);
            builder.append(data);
            appendOnce=true;
        }
        if(!appendOnce){
            return null;
        }
        builder.append(":");
        builder.append(rawValue);
        builder.append(firstAttrChild.getValueType());
        return builder.toString();
    }
    private boolean isFlagAttr(ResValueBagItem firstAttrChild){
        ValueType valueType=firstAttrChild.getValueType();
        return valueType==ValueType.FIRST_INT;
    }
    public ResourceNameStore getResourceNameStore() {
        return resourceNameStore;
    }
    @Override
    public String getResourceFullName(int resId, boolean includePackageName) {
        return getResourceNameStore().getResourceFullName(resId, includePackageName);
    }
    @Override
    public String getResourceName(int resId, boolean includePackageName) {
        return getResourceNameStore().getResourceName(resId, includePackageName);
    }
    @Override
    public ResValueBag getAttributeBag(int resId) {
        return getResourceNameStore().getAttributeBag(resId);
    }

    public abstract Output decode(byte currentPackageId, InputBlock inputBlock);
}
