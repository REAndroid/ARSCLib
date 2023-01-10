 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.EntryBlockArray;
import com.reandroid.lib.arsc.array.TypeBlockArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.pool.TypeStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeBlock extends BaseChunk
        implements BlockLoad, JSONConvert<JSONObject>, Comparable<TypeBlock> {
    private final ByteItem mTypeId;
    private final ByteItem mTypeFlags;
    private final IntegerItem mEntryCount;
    private final ResConfig mResConfig;
    private final EntryBlockArray mEntryArray;
    private TypeString mTypeString;
    public TypeBlock() {
        super(ChunkType.TYPE, 2);
        this.mTypeId=new ByteItem();
        this.mTypeFlags=new ByteItem();
        ShortItem reserved = new ShortItem();
        this.mEntryCount=new IntegerItem();

        IntegerItem entriesStart = new IntegerItem();
        this.mResConfig =new ResConfig();
        IntegerArray entryOffsets = new IntegerArray();
        this.mEntryArray = new EntryBlockArray(entryOffsets, mEntryCount, entriesStart);

        mTypeFlags.setBlockLoad(this);

        addToHeader(mTypeId);
        addToHeader(mTypeFlags);
        addToHeader(reserved);
        addToHeader(mEntryCount);
        addToHeader(entriesStart);
        addToHeader(mResConfig);

        addChild(entryOffsets);
        addChild(mEntryArray);
    }
    public PackageBlock getPackageBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof SpecTypePair){
                return ((SpecTypePair)parent).getPackageBlock();
            }
            parent=parent.getParent();
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
    public TypeString getTypeString(){
        if(mTypeString!=null){
            if(mTypeString.getId()==getTypeId()){
                return mTypeString;
            }
            mTypeString=null;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TypeStringPool typeStringPool=packageBlock.getTypeStringPool();
        mTypeString=typeStringPool.getById(getTypeIdInt());
        return mTypeString;
    }
    public byte getTypeId(){
        return mTypeId.get();
    }
    public int getTypeIdInt(){
        return (0xff & mTypeId.get());
    }
    public void setTypeId(int id){
        setTypeId((byte) (0xff & id));
    }
    public void setTypeId(byte id){
        mTypeId.set(id);
    }
    public void setTypeName(String name){
        TypeStringPool typeStringPool=getTypeStringPool();
        int id=getTypeIdInt();
        TypeString typeString=typeStringPool.getById(id);
        if(typeString==null){
            typeString=typeStringPool.getOrCreate(id, name);
        }
        typeString.set(name);
    }
    private TypeStringPool getTypeStringPool(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock!=null){
            return packageBlock.getTypeStringPool();
        }
        return null;
    }
    public void setEntryCount(int count){
        if(count == mEntryCount.get()){
            return;
        }
        mEntryCount.set(count);
        onSetEntryCount(count);
    }
    public boolean isEmpty(){
        return getEntryBlockArray().isEmpty();
    }
    public boolean isDefault(){
        return getResConfig().isDefault();
    }
    public String getQualifiers(){
        return getResConfig().getQualifiers();
    }
    public void setQualifiers(String qualifiers){
        getResConfig().parseQualifiers(qualifiers);
    }
    public int countNonNullEntries(){
        return getEntryBlockArray().countNonNull();
    }
    public SpecTypePair getParentSpecTypePair(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof SpecTypePair){
                return (SpecTypePair)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public void cleanEntries(){
        PackageBlock packageBlock=getPackageBlock();
        List<EntryBlock> allEntries=listEntries(true);
        for(EntryBlock entryBlock:allEntries){
            if(packageBlock!=null){
                packageBlock.removeEntryGroup(entryBlock);
            }
            entryBlock.setNull(true);
        }
    }
    public void removeEntry(EntryBlock entryBlock){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock!=null){
            packageBlock.removeEntryGroup(entryBlock);
        }
        entryBlock.setNull(true);
    }
    public EntryBlock getOrCreateEntry(short entryId){
        return getEntryBlockArray().getOrCreate(entryId);
    }
    public EntryBlock getEntry(short entryId){
        return getEntryBlockArray().getEntry(entryId);
    }
    public ResConfig getResConfig(){
        return mResConfig;
    }
    public EntryBlockArray getEntryBlockArray(){
        return mEntryArray;
    }
    public List<EntryBlock> listEntries(){
        return listEntries(false);
    }
    public List<EntryBlock> listEntries(boolean skipNullBlock){
        List<EntryBlock> results=new ArrayList<>();
        Iterator<EntryBlock> itr = getEntryBlockArray().iterator(skipNullBlock);
        while (itr.hasNext()){
            EntryBlock block=itr.next();
            results.add(block);
        }
        return results;
    }
    public EntryBlock getEntryBlock(int entryId){
        return getEntryBlockArray().get(entryId);
    }

    private void onSetEntryCount(int count) {
        getEntryBlockArray().setChildesCount(count);
    }
    @Override
    protected void onChunkRefreshed() {
        getEntryBlockArray().refreshCountAndStart();
    }
    @Override
    protected void onPreRefreshRefresh(){
        mResConfig.refresh();
        super.onPreRefreshRefresh();
    }
    /*
    * method Block.addBytes is inefficient for large size byte array
    * so let's override here because this block is the largest
    */
    @Override
    public byte[] getBytes(){
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        try {
            writeBytes(os);
            os.close();
        } catch (IOException ignored) {
        }
        return os.toByteArray();
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_id, getTypeIdInt());
        jsonObject.put(NAME_name, getTypeName());
        jsonObject.put(NAME_config, getResConfig().toJson());
        jsonObject.put(NAME_entries, getEntryBlockArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setTypeId(json.getInt(NAME_id));
        String name = json.optString(NAME_name);
        if(name!=null){
            setTypeName(name);
        }
        getEntryBlockArray()
                .fromJson(json.getJSONArray(NAME_entries));
        getResConfig()
                .fromJson(json.getJSONObject(NAME_config));
    }
    public void merge(TypeBlock typeBlock){
        if(typeBlock==null||typeBlock==this){
            return;
        }
        if(getTypeId() != typeBlock.getTypeId()){
            throw new IllegalArgumentException("Can not merge different id types: "
                    +getTypeId()+"!="+typeBlock.getTypeId());
        }
        setTypeName(typeBlock.getTypeName());
        getEntryBlockArray().merge(typeBlock.getEntryBlockArray());
    }
    @Override
    public int compareTo(TypeBlock typeBlock) {
        int id1=getTypeIdInt();
        int id2=typeBlock.getTypeIdInt();
        if(id1!=id2){
            return Integer.compare(id1, id2);
        }
        return getResConfig().compareTo(typeBlock.getResConfig());
    }
    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public EntryBlock searchByEntryName(String entryName){
        return getEntryBlockArray().searchByEntryName(entryName);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==mTypeFlags){
            if(mTypeFlags.get()==0x1){
                //ResTable_sparseTypeEntry ?
            }
        }
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getResConfig().toString());
        builder.append(" ");
        builder.append(super.toString());
        return builder.toString();
    }

    public static final String NAME_name = "name";
    public static final String NAME_config = "config";
    public static final String NAME_id = "id";
    public static final String NAME_entries = "entries";
}
