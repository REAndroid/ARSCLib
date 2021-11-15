package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.util.Iterator;

public class SpecTypePairArray extends BlockArray<SpecTypePair> {
    public SpecTypePairArray(){
        super();
    }

    public EntryBlock getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreateTypeBlock(typeId, qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public EntryBlock getEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(typeId, qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair=getPair(typeId);
        if(pair==null){
            return null;
        }
        return pair.getTypeBlock(qualifiers);
    }
    public SpecTypePair getOrCreate(byte typeId){
        SpecTypePair pair=getPair(typeId);
        if(pair!=null){
            return pair;
        }
        pair=createNext();
        pair.setTypeId(typeId);
        return pair;
    }
    public SpecTypePair getPair(byte typeId){
        SpecTypePair[] items=getChildes();
        if(items==null){
            return null;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            SpecTypePair pair=items[i];
            if(pair==null){
                continue;
            }
            if(pair.getTypeId()==typeId){
                return pair;
            }
        }
        return null;
    }
    public byte getTypeId(){
        SpecTypePair[] items=getChildes();
        if(items==null){
            return 0;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            SpecTypePair pair=items[i];
            if(pair!=null){
                return pair.getTypeId();
            }
        }
        return 0;
    }
    @Override
    public SpecTypePair newInstance() {
        SpecTypePair pair=new SpecTypePair();
        pair.setTypeId(getTypeId());
        return pair;
    }
    @Override
    public SpecTypePair[] newInstance(int len) {
        return new SpecTypePair[len];
    }
    @Override
    protected void onRefreshed() {

    }
}
