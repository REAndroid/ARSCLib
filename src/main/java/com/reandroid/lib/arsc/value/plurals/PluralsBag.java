package com.reandroid.lib.arsc.value.plurals;

import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;

public class PluralsBag {
    private final PluralsBagItem[] mBagItems;
    private PluralsBag(PluralsBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public PluralsBagItem[] getBagItems() {
        return mBagItems;
    }
    public String getName(){
        EntryBlock entryBlock=getBagItems()[0].getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        SpecString spec = entryBlock.getSpecString();
        if(spec==null){
            return null;
        }
        return spec.get();
    }
    public String getTypeName(){
        EntryBlock entryBlock=getBagItems()[0].getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        TypeString typeString = entryBlock.getTypeString();
        if(typeString==null){
            return null;
        }
        return typeString.get();
    }
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<");
        String type=getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\">");
        PluralsBagItem[] allItems = getBagItems();
        for(int i=0;i<allItems.length;i++){
            builder.append("\n    ");
            builder.append(allItems[i].toString());
        }
        builder.append("\n</");
        builder.append(type);
        builder.append(">");
        return builder.toString();
    }

    /** TODO: find another method to check instead of checking type name (plurals),
     * just like {@link AttributeBag} **/
    public static boolean isPlurals(ResValueBag resValueBag){
        if(resValueBag==null){
            return false;
        }
        EntryBlock entryBlock= resValueBag.getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        TypeString typeString = entryBlock.getTypeString();
        if(typeString==null){
            return false;
        }
        if(!NAME.equals(typeString.get())){
            return false;
        }
        return PluralsBagItem.create(resValueBag.getBagItems()) != null;
    }

    public static PluralsBag create(ResValueBag resValueBag){
        if(resValueBag==null){
            return null;
        }
        PluralsBagItem[] bagItems=PluralsBagItem.create(resValueBag.getBagItems());
        if(bagItems==null){
            return null;
        }
        return new PluralsBag(bagItems);
    }
    public static final String NAME="plurals";
}
