package com.reandroid.lib.arsc.container;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.array.TypeBlockArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockContainer;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.SpecBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpecTypePair extends BlockContainer<Block> {
    private final Block[] mChildes;
    private final SpecBlock mSpecBlock;
    private final TypeBlockArray mTypeBlockArray;
    public SpecTypePair(SpecBlock specBlock, TypeBlockArray typeBlockArray){
        this.mSpecBlock = specBlock;
        this.mTypeBlockArray = typeBlockArray;
        this.mChildes=new Block[]{specBlock, typeBlockArray};
        mSpecBlock.setIndex(0);
        mTypeBlockArray.setIndex(1);
        mSpecBlock.setParent(this);
        mTypeBlockArray.setParent(this);
    }
    public SpecTypePair(){
        this(new SpecBlock(), new TypeBlockArray());
    }
    public List<ResConfig> listResConfig(){
        return mTypeBlockArray.listResConfig();
    }
    public byte getTypeId(){
        return mSpecBlock.getTypeId();
    }
    public void setTypeId(byte id){
        mSpecBlock.setTypeId(id);
        mTypeBlockArray.setTypeId(id);
    }
    public SpecBlock getSpecBlock(){
        return mSpecBlock;
    }
    public TypeBlockArray getTypeBlockArray(){
        return mTypeBlockArray;
    }
    public PackageBlock getPackageBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof PackageBlock){
                return (PackageBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public List<EntryBlock> listEntries(int entryId){
        List<EntryBlock> results=new ArrayList<>();
        for(TypeBlock typeBlock:listTypeBlocks()){
            EntryBlock entryBlock=typeBlock.getEntryBlock(entryId);
            if(entryBlock==null||entryBlock.isNull()){
                continue;
            }
            results.add(entryBlock);
        }
        return results;
    }
    public List<TypeBlock> listTypeBlocks(){
        return mTypeBlockArray.listItems();
    }

    @Override
    protected void onRefreshed() {

    }
    @Override
    public int childesCount() {
        return mChildes.length;
    }
    @Override
    public Block[] getChildes() {
        return mChildes;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType!=ChunkType.SPEC){
            readUnexpectedNonSpecBlock(reader, headerBlock);
        }
        mSpecBlock.readBytes(reader);
        mTypeBlockArray.readBytes(reader);
    }
    private void readUnexpectedNonSpecBlock(BlockReader reader, HeaderBlock headerBlock) throws IOException{
        throw new IOException("Unexpected block: "+headerBlock.toString()+", Should be: "+ChunkType.SPEC);
    }

}
