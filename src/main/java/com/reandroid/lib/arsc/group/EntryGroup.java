package com.reandroid.lib.arsc.group;

import com.reandroid.lib.arsc.base.BlockArrayCreator;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.pool.SpecStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.util.Iterator;

public class EntryGroup extends ItemGroup<EntryBlock> {
    private final int resourceId;
    public EntryGroup(int resId) {
        super(create(), String.format("0x%08x", resId));
        this.resourceId=resId;
    }
    public int getResourceId(){
        return resourceId;
    }
    public boolean renameSpec(String name){
        EntryBlock[] items=getItems();
        if(items==null || name==null){
            return false;
        }
        SpecStringPool specStringPool=getSpecStringPool();
        if(specStringPool==null){
            return false;
        }
        if(isAllSameSpec()){
            String oldName=getSpecName();
            if(name.equals(oldName)){
                return false;
            }
        }
        SpecString specString=specStringPool.getOrCreate(name);
        return renameSpec(specString.getIndex());
    }
    public short getEntryId(){
        return (short) (getResourceId() & 0xffff);
    }
    private boolean isAllSameSpec(){
        EntryBlock first=null;
        for(EntryBlock entryBlock:listItems()){
            if(first==null){
                first=entryBlock;
                continue;
            }
            if(first.getSpecReference()!=entryBlock.getSpecReference()){
                return false;
            }
        }
        return true;
    }
    public boolean renameSpec(int specReference){
        EntryBlock[] items=getItems();
        if(items==null){
            return false;
        }
        boolean renameOk=false;
        for(EntryBlock block:items){
            if(block==null||block.isNull()){
                continue;
            }
            if(block.getSpecReference()==specReference){
                continue;
            }
            block.setSpecReference(specReference);
            renameOk=true;
        }
        return renameOk;
    }
    public EntryBlock pickOne(){
        EntryBlock defEntryBlock=getDefault();
        if(defEntryBlock!=null){
            return defEntryBlock;
        }
        Iterator<EntryBlock> itr=iterator(true);
        while (itr.hasNext()){
            return itr.next();
        }
        return null;
    }
    public EntryBlock getDefault(){
        Iterator<EntryBlock> itr=iterator(true);
        while (itr.hasNext()){
            EntryBlock entryBlock=itr.next();
            if(entryBlock.isDefault()){
                return entryBlock;
            }
        }
        return null;
    }
    public TypeString getTypeString(){
        EntryBlock entryBlock=pickOne();
        if(entryBlock!=null){
            return entryBlock.getTypeString();
        }
        return null;
    }
    public SpecString getSpecString(){
        EntryBlock entryBlock=pickOne();
        if(entryBlock!=null){
            return entryBlock.getSpecString();
        }
        return null;
    }
    public String getTypeName(){
        TypeString typeString=getTypeString();
        if(typeString==null){
            return null;
        }
        return typeString.get();
    }
    public String getSpecName(){
        SpecString specString=getSpecString();
        if(specString==null){
            return null;
        }
        return specString.get();
    }
    private SpecStringPool getSpecStringPool(){
        EntryBlock entryBlock=get(0);
        if(entryBlock==null){
            return null;
        }
        TypeBlock typeBlock=entryBlock.getTypeBlock();
        if(typeBlock==null){
            return null;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        return packageBlock.getSpecStringPool();
    }
    @Override
    public String toString(){
        EntryBlock entryBlock=pickOne();
        if(entryBlock==null){
            return super.toString();
        }
        return super.toString()+"{"+entryBlock.toString()+"}";
    }
    private static BlockArrayCreator<EntryBlock> create(){
        return new BlockArrayCreator<EntryBlock>(){
            @Override
            public EntryBlock newInstance() {
                return new EntryBlock();
            }

            @Override
            public EntryBlock[] newInstance(int len) {
                return new EntryBlock[len];
            }
        };
    }

}
