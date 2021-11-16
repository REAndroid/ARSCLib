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
        String oldName=getSpecName();
        if(name.equals(oldName)){
            return false;
        }
        SpecString specString=specStringPool.getOrCreate(name);
        return renameSpec(specString.getIndex());
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
    public TypeString getTypeString(){
        Iterator<EntryBlock> itr=iterator(true);
        while (itr.hasNext()){
            EntryBlock entryBlock=itr.next();
            return entryBlock.getTypeString();
        }
        return null;
    }
    public SpecString getSpecString(){
        Iterator<EntryBlock> itr=iterator(true);
        while (itr.hasNext()){
            EntryBlock entryBlock=itr.next();
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
