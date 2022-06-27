package com.reandroid.lib.arsc.value.attribute;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.common.EntryStore;


public class AttributeBagItem {
    private final ResValueBagItem mBagItem;
    public AttributeBagItem(ResValueBagItem bagItem){
        this.mBagItem=bagItem;
    }
    public int getData(){
        return getBagItem().getData();
    }
    public String getNameOrHex(){
        return getNameOrHex(null);
    }
    public String getNameOrHex(EntryStore entryStore){
        String name=getName(entryStore);
        if(name==null){
            name=String.format("@0x%08x", getBagItem().getId());
        }
        return name;
    }
    public String getName(){
        return getName(null);
    }
    public String getName(EntryStore entryStore){
        if(isType()){
            return null;
        }
        ResValueBagItem item=getBagItem();
        int id=item.getId();
        EntryBlock parentEntry=item.getEntryBlock();
        if(parentEntry!=null){
            PackageBlock packageBlock=parentEntry.getPackageBlock();
            if(packageBlock!=null){
                EntryGroup entryGroup=packageBlock.getEntryGroup(id);
                if(entryGroup!=null){
                    String name=entryGroup.getSpecName();
                    if(name!=null){
                        return name;
                    }
                }
            }
        }
        if(entryStore!=null){
            EntryGroup entryGroup=entryStore.getEntryGroup(id);
            if(entryGroup!=null){
                return entryGroup.getSpecName();
            }
        }
        return null;
    }
    public ResValueBagItem getBagItem() {
        return mBagItem;
    }
    public AttributeItemType getItemType(){
        if(!isType()){
            return null;
        }
        ResValueBagItem item=getBagItem();
        return AttributeItemType.valueOf(item.getIdLow());
    }
    public boolean isType(){
        ResValueBagItem item=getBagItem();
        return item.getIdHigh()==0x0100;
    }
    public AttributeValueType[] getValueTypes(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return null;
        }
        ResValueBagItem item=getBagItem();
        short low=item.getDataLow();
        return AttributeValueType.valuesOf(low);
    }
    public Integer getBound(){
        AttributeItemType type=getItemType();
        if(type==null || type==AttributeItemType.FORMAT){
            return null;
        }
        ResValueBagItem item=getBagItem();
        return item.getData();
    }
    public boolean isEnum(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return false;
        }
        ResValueBagItem item=getBagItem();
        short low=item.getDataHigh();
        return low==0x0001;
    }
    public boolean isFlag(){
        AttributeItemType type=getItemType();
        if(type!=AttributeItemType.FORMAT){
            return false;
        }
        ResValueBagItem item=getBagItem();
        short low=item.getDataHigh();
        return low==0x0002;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        AttributeItemType type=getItemType();
        if(type!=null){
            builder.append(type.toString());
            if(type==AttributeItemType.FORMAT){
                if(isEnum()){
                    builder.append("(enum)");
                }else if(isFlag()){
                    builder.append("(flag)");
                }
                String value=AttributeValueType.toString(getValueTypes());
                if(value!=null){
                    builder.append("=");
                    builder.append(value);
                }
                return builder.toString();
            }
            Integer bound=getBound();
            builder.append("=");
            builder.append(bound);
            return builder.toString();
        }
        ResValueBagItem item=getBagItem();
        builder.append(getNameOrHex());
        builder.append("=").append(String.format("0x%x", item.getData()));
        return builder.toString();
    }

    public static String toString(EntryStore entryStore, AttributeBagItem[] bagItems)  {
        if(bagItems==null){
            return null;
        }
        int len=bagItems.length;
        if(len==0){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        boolean appendOnce=false;
        for (int i = 0; i < len; i++) {
            AttributeBagItem item=bagItems[i];
            if(item==null){
                continue;
            }
            if(appendOnce){
                builder.append("|");
            }
            String name=item.getNameOrHex(entryStore);
            builder.append(name);
            appendOnce=true;
        }
        if(appendOnce){
            return builder.toString();
        }
        return null;
    }
    public static AttributeBagItem[] create(ResValueBagItem[] resValueBagItems){
        if(resValueBagItems==null){
            return null;
        }
        AttributeBagItem format=null;
        int len=resValueBagItems.length;
        AttributeBagItem[] bagItems=new AttributeBagItem[len];
        for(int i=0;i<len;i++){
            AttributeBagItem item=new AttributeBagItem(resValueBagItems[i]);
            bagItems[i]=item;
            if(format==null){
                if(AttributeItemType.FORMAT==item.getItemType()){
                    format=item;
                }
            }
        }
        if(format!=null){
            return bagItems;
        }
        return null;
    }


}
