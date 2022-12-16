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
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SpecTypePair extends BlockContainer<Block>
        implements JSONConvert<JSONObject> , Comparable<SpecTypePair>{
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
    public void sortTypes(){
        getTypeBlockArray().sort();
    }
    public void removeEmptyTypeBlocks(){
        getTypeBlockArray().removeEmptyBlocks();
    }
    public boolean isEmpty(){
        return getTypeBlockArray().isEmpty();
    }
    public int countTypeBlocks(){
        return getTypeBlockArray().childesCount();
    }
    public EntryBlock getOrCreateEntry(short entryId, String qualifiers){
        return getTypeBlockArray().getOrCreateEntry(entryId, qualifiers);
    }
    public EntryBlock getEntry(short entryId, String qualifiers){
        return getTypeBlockArray().getEntry(entryId, qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(String qualifiers){
        return getTypeBlockArray().getOrCreate(qualifiers);
    }
    public TypeBlock getTypeBlock(String qualifiers){
        return getTypeBlockArray().getTypeBlock(qualifiers);
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
        Iterator<TypeBlock> itr = mTypeBlockArray.iterator(true);
        while (itr.hasNext()){
            TypeBlock typeBlock=itr.next();
            EntryBlock entryBlock=typeBlock.getEntryBlock(entryId);
            if(entryBlock==null||entryBlock.isNull()){
                continue;
            }
            results.add(entryBlock);
        }
        return results;
    }
    public Collection<TypeBlock> listTypeBlocks(){
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
    public int getHighestEntryCount(){
        int specEntryCount=getSpecBlock().getEntryCount();
        int typeEntryCount=getTypeBlockArray().getHighestEntryCount();
        if(specEntryCount>typeEntryCount){
            return specEntryCount;
        }
        return typeEntryCount;
    }
    public TypeString getTypeString(){
        return getTypeBlockArray().getTypeString();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", getSpecBlock().getTypeId());
        jsonObject.put("types", getTypeBlockArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getSpecBlock().setTypeId((byte) json.getInt("id"));
        getTypeBlockArray().fromJson(json.getJSONArray("types"));
    }
    public void merge(SpecTypePair typePair){
        if(typePair==null||typePair==this){
            return;
        }
        if(getTypeId() != typePair.getTypeId()){
            throw new IllegalArgumentException("Can not merge different id types: "
                    +getTypeId()+"!="+typePair.getTypeId());
        }
        getTypeBlockArray().merge(typePair.getTypeBlockArray());
    }
    @Override
    public int compareTo(SpecTypePair specTypePair) {
        return Integer.compare(getTypeId(), specTypePair.getTypeId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", getTypeId()));
        builder.append(" (");
        TypeString ts = getTypeString();
        if(ts!=null){
            builder.append(ts.get());
        }else {
            builder.append("null");
        }
        builder.append(") config count=");
        builder.append(getTypeBlockArray().childesCount());
        return builder.toString();
    }
}
