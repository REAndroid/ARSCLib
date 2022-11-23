package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.SpecBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class TypeBlockArray extends BlockArray<TypeBlock> {
    private byte mTypeId;
    public TypeBlockArray(){
        super();
    }
    public void removeEmptyBlocks(){
        List<TypeBlock> allTypes=new ArrayList<>(listItems());
        boolean foundEmpty=false;
        for(TypeBlock typeBlock:allTypes){
            if(typeBlock.isEmpty()){
                super.remove(typeBlock, false);
                foundEmpty=true;
            }
        }
        if(foundEmpty){
            trimNullBlocks();
        }
    }
    public EntryBlock getOrCreateEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreate(qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public boolean isEmpty(){
        for(TypeBlock typeBlock:listItems()){
            if(typeBlock!=null && !typeBlock.isEmpty()){
                return false;
            }
        }
        return true;
    }
    public EntryBlock getEntry(short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public TypeBlock getOrCreate(String qualifiers){
        TypeBlock typeBlock=getTypeBlock(qualifiers);
        if(typeBlock!=null){
            return typeBlock;
        }
        typeBlock=createNext();
        ResConfig config=typeBlock.getResConfig();
        config.parseQualifiers(qualifiers);
        return typeBlock;
    }
    public TypeBlock getTypeBlock(String qualifiers){
        TypeBlock[] items=getChildes();
        if(items==null){
            return null;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            TypeBlock block=items[i];
            if(block.getResConfig().isEqualQualifiers(qualifiers)){
                return block;
            }
        }
        return null;
    }
    public TypeBlock getTypeBlock(ResConfig config){
        if(config==null){
            return null;
        }
        TypeBlock[] items=getChildes();
        if(items==null){
            return null;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            TypeBlock block=items[i];
            if(config.equals(block.getResConfig())){
                return block;
            }
        }
        return null;
    }
    public void setTypeId(byte id){
        this.mTypeId=id;
        TypeBlock[] allChildes=getChildes();
        if(allChildes==null){
            return;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            TypeBlock typeBlock = allChildes[i];
            typeBlock.setTypeId(id);
        }
    }
    public byte getTypeId(){
        if(mTypeId != 0){
            return mTypeId;
        }
        SpecBlock specBlock=getSpecBlock();
        if(specBlock!=null){
            byte id=specBlock.getTypeId();
            if(id!=0){
                mTypeId=id;
                return id;
            }
        }
        TypeBlock[] allChildes=getChildes();
        if(allChildes==null){
            return 0;
        }
        int max=allChildes.length;
        for(int i=0;i<max;i++){
            TypeBlock typeBlock = allChildes[i];
            byte id=typeBlock.getTypeId();
            if(id==0){
                continue;
            }
            if(specBlock!=null){
                specBlock.setTypeId(id);
            }
            mTypeId=id;
            return id;
        }
        return 0;
    }
    public List<ResConfig> listResConfig(){
        return new AbstractList<ResConfig>() {
            @Override
            public ResConfig get(int i) {
                TypeBlock typeBlock=TypeBlockArray.this.get(i);
                if(typeBlock!=null){
                    return typeBlock.getResConfig();
                }
                return null;
            }

            @Override
            public int size() {
                return TypeBlockArray.this.childesCount();
            }
        };
    }
    private SpecBlock getSpecBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof SpecBlock){
                return (SpecBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    @Override
    protected boolean remove(TypeBlock block, boolean trim){
        if(block==null){
            return false;
        }
        block.cleanEntries();
        return super.remove(block, trim);
    }
    @Override
    public TypeBlock newInstance() {
        byte id=getTypeId();
        TypeBlock typeBlock=new TypeBlock();
        typeBlock.setTypeId(id);
        return typeBlock;
    }
    @Override
    public TypeBlock[] newInstance(int len) {
        return new TypeBlock[len];
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        boolean readOk=true;
        while (readOk){
            readOk=readTypeBlockArray(reader);
        }
    }
    private boolean readTypeBlockArray(BlockReader reader) throws IOException{
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType!=ChunkType.TYPE){
            return false;
        }
        int pos=reader.getPosition();
        TypeBlock typeBlock=createNext();
        typeBlock.readBytes(reader);
        return reader.getPosition()>pos;
    }
    public int getHighestEntryCount(){
        int result=0;
        for(TypeBlock typeBlock:getChildes()){
            int count=typeBlock.getEntryCount();
            if(count>result){
                result=count;
            }
        }
        return result;
    }
    public void setEntryCount(int count){
        for(TypeBlock typeBlock:getChildes()){
            typeBlock.setEntryCount(count);
        }
    }
    public TypeString getTypeString(){
        for(TypeBlock typeBlock:getChildes()){
            TypeString typeString=typeBlock.getTypeString();
            if(typeString!=null){
                return typeString;
            }
        }
        return null;
    }
}
