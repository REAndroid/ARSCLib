package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.SpecBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.value.ResConfig;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

public class TypeBlockArray extends BlockArray<TypeBlock> {
    private byte mTypeId;
    public TypeBlockArray(){
        super();
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
}
