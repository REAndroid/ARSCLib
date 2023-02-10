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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.array.EntryBlockArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.EntryBlock;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeBlock extends Chunk<TypeHeader>
        implements BlockLoad, JSONConvert<JSONObject>, Comparable<TypeBlock> {

    private final EntryBlockArray mEntryArray;
    private TypeString mTypeString;
    public TypeBlock() {
        super(new TypeHeader(), 2);
        TypeHeader header = getHeaderBlock();

        IntegerArray entryOffsets = new IntegerArray();
        this.mEntryArray = new EntryBlockArray(entryOffsets,
                header.getCount(), header.getEntriesStart());

        header.getFlags().setBlockLoad(this);

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
        mTypeString=typeStringPool.getById(getId());
        return mTypeString;
    }
    public byte getTypeId(){
        return getHeaderBlock().getId().get();
    }
    public int getId(){
        return getHeaderBlock().getId().unsignedInt();
    }
    public void setId(int id){
        setTypeId((byte) (0xff & id));
    }
    public void setTypeId(byte id){
        getHeaderBlock().getId().set(id);
    }
    public void setTypeName(String name){
        TypeStringPool typeStringPool=getTypeStringPool();
        int id= getId();
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
        IntegerItem entryCount = getHeaderBlock().getCount();
        if(count == entryCount.get()){
            return;
        }
        entryCount.set(count);
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
    public EntryBlock getOrCreateEntry(String name){
        for(EntryBlock entryBlock:getEntryBlockArray().listItems()){
            if(name.equals(entryBlock.getName())){
                return entryBlock;
            }
        }
        SpecTypePair specTypePair = getParentSpecTypePair();
        EntryBlock exist=specTypePair.getAnyEntry(name);
        int id;
        if(exist!=null){
            id=exist.getIndex();
        }else {
            id = specTypePair.getHighestEntryCount();
        }
        SpecString specString = getPackageBlock()
                .getSpecStringPool().getOrCreate(name);
        EntryBlock entryBlock = getOrCreateEntry((short) id);
        if(entryBlock.isNull()){
            entryBlock.setValueAsRaw(ValueType.NULL, 0);
        }
        entryBlock.setSpecReference(specString.getIndex());
        return entryBlock;
    }
    public EntryBlock getOrCreateEntry(short entryId){
        return getEntryBlockArray().getOrCreate(entryId);
    }
    public EntryBlock getEntry(short entryId){
        return getEntryBlockArray().getEntry(entryId);
    }
    public ResConfig getResConfig(){
        return getHeaderBlock().getConfig();
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
        getHeaderBlock().getConfig().refresh();
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
        jsonObject.put(NAME_id, getId());
        jsonObject.put(NAME_name, getTypeName());
        jsonObject.put(NAME_config, getResConfig().toJson());
        jsonObject.put(NAME_entries, getEntryBlockArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt(NAME_id));
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
        int id1= getId();
        int id2=typeBlock.getId();
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
        if(sender==getHeaderBlock().getFlags()){
            if(getHeaderBlock().getFlags().unsignedInt()==0x1){
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
