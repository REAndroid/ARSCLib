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

import com.reandroid.arsc.array.EntryArray;
import com.reandroid.arsc.array.IntegerOffsetArray;
import com.reandroid.arsc.array.SparseOffsetsArray;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.ValueItem;
import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.MergingIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeBlock extends Chunk<TypeHeader>
        implements JSONConvert<JSONObject>, Comparable<TypeBlock> {

    private final EntryArray mEntryArray;
    private TypeString mTypeString;
    public TypeBlock(boolean sparse) {
        super(new TypeHeader(sparse), 2);
        TypeHeader header = getHeaderBlock();

        IntegerOffsetArray entryOffsets;
        if(sparse){
            entryOffsets = new SparseOffsetsArray();
        }else {
            entryOffsets = new IntegerOffsetArray();
        }
        this.mEntryArray = new EntryArray(entryOffsets,
                header.getCountItem(), header.getEntriesStart());

        addChild(entryOffsets);
        addChild(mEntryArray);
    }

    public Iterator<ValueItem> allValues(){
        return new MergingIterator<>( new ComputeIterator<>(getEntries(),
                Entry::allValues));
    }
    public boolean isTypeAttr(){
        TypeString typeString = getTypeString();
        if(typeString != null){
            return typeString.isTypeAttr();
        }
        return false;
    }
    public boolean isTypeId(){
        TypeString typeString = getTypeString();
        if(typeString != null){
            return typeString.isTypeId();
        }
        return false;
    }
    public String buildUniqueDirectoryName(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock != null && packageBlock.hasValidTypeNames()){
            return getTypeName() + getResConfig().getQualifiers();
        }
        return "type_" + HexUtil.toHex2(getTypeId())
                + getResConfig().getQualifiers();
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        EntryArray entryArray = getEntryArray();
        entryArray.linkTableStringsInternal(tableStringPool);
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        EntryArray entryArray = getEntryArray();
        entryArray.linkSpecStringsInternal(specStringPool);
    }
    public boolean isSparse(){
        return getHeaderBlock().isSparse();
    }
    public void destroy(){
        getEntryArray().destroy();
        setId(0);
        setParent(null);
    }
    public boolean removeNullEntries(int startId){
        startId = 0x0000ffff & startId;
        EntryArray entryArray = getEntryArray();
        entryArray.removeAllNull(startId);
        return entryArray.getChildrenCount() == startId;
    }
    public PackageBlock getPackageBlock(){
        SpecTypePair specTypePair = getParent(SpecTypePair.class);
        if(specTypePair!=null){
            return specTypePair.getPackageBlock();
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
        IntegerItem entryCount = getHeaderBlock().getCountItem();
        if(count == entryCount.get()){
            return;
        }
        entryCount.set(count);
        onSetEntryCount(count);
    }
    public boolean isEmpty(){
        return getEntryArray().isEmpty();
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
        return getEntryArray().countNonNull();
    }
    public SpecTypePair getParentSpecTypePair(){
        return getParent(SpecTypePair.class);
    }
    public void cleanEntries(){
        Iterator<Entry> iterator = getEntries();
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            entry.setNull(true);
        }
    }
    public Entry getOrCreateDefinedEntry(String name){
        Entry entry = getEntry(name);
        if(entry != null){
            return entry;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        int id = packageBlock.resolveResourceId(getId(), name);
        if(id == 0){
            return null;
        }
        SpecStringPool stringPool = packageBlock.getSpecStringPool();
        SpecString specString = stringPool.getOrCreate(name);
        id = id & 0xffff;
        entry = getOrCreateEntry((short) id);
        entry.setSpecReference(specString);
        return entry;
    }
    public Entry getOrCreateEntry(String name){
        Entry entry = getEntry(name);
        if(entry != null){
            return entry;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        int id = packageBlock.resolveResourceId(getId(), name);
        if(id != 0){
            id = id & 0xffff;
        } else {
            id = getParentSpecTypePair().getHighestEntryId() + 1;
        }

        SpecStringPool stringPool = packageBlock.getSpecStringPool();
        SpecString specString = stringPool.getOrCreate(name);
        entry = getOrCreateEntry((short) id);
        entry.setSpecReference(specString);
        return entry;
    }
    public Entry getOrCreateEntry(short entryId){
        return getEntryArray().getOrCreate(entryId);
    }
    public Entry getEntry(short entryId){
        return getEntryArray().getEntry(entryId);
    }
    public Iterator<Entry> getEntries(){
        return getEntryArray().iterator(false);
    }
    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     */
    public Entry getEntry(String entryName){
        return getEntryArray().getEntry(entryName);
    }
    public Boolean hasComplexEntry(){
        SpecTypePair specTypePair = getParentSpecTypePair();
        if(specTypePair != null){
            return specTypePair.hasComplexEntry();
        }
        return null;
    }
    public ResConfig getResConfig(){
        return getHeaderBlock().getConfig();
    }
    public EntryArray getEntryArray(){
        return mEntryArray;
    }
    public void ensureEntriesCount(int count){
        EntryArray entryArray = getEntryArray();
        entryArray.ensureSize(count);
        entryArray.refreshCount();
    }
    public List<Entry> listEntries(boolean skipNullBlock){
        List<Entry> results=new ArrayList<>();
        Iterator<Entry> itr = getEntryArray().iterator(skipNullBlock);
        while (itr.hasNext()){
            Entry block=itr.next();
            results.add(block);
        }
        return results;
    }
    public Entry getEntry(int entryId){
        return getEntryArray().getEntry(entryId);
    }

    private void onSetEntryCount(int count) {
        getEntryArray().setChildrenCount(count);
    }
    @Override
    protected void onChunkRefreshed() {
        getEntryArray().refreshCountAndStart();
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
        JSONObject jsonObject = new JSONObject();
        if(isSparse()){
            jsonObject.put(NAME_is_sparse, true);
        }
        jsonObject.put(NAME_id, getId());
        jsonObject.put(NAME_name, getTypeName());
        jsonObject.put(NAME_config, getResConfig().toJson());
        jsonObject.put(NAME_entries, getEntryArray().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt(NAME_id));
        String name = json.optString(NAME_name);
        if(name!=null){
            setTypeName(name);
        }
        getEntryArray()
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
        getEntryArray().merge(typeBlock.getEntryArray());
    }
    @Override
    public int compareTo(TypeBlock typeBlock) {
        int id1 = getId();
        int id2 = typeBlock.getId();
        if(id1 != id2){
            return Integer.compare(id1, id2);
        }
        String q1 = (isSparse() ? "1" : "0")
                + getResConfig().getQualifiers();
        String q2 = (typeBlock.isSparse() ? "1" : "0")
                + typeBlock.getResConfig().getQualifiers();
        return q1.compareTo(q2);
    }
    public boolean isEqualTypeName(String typeName){
        return isEqualTypeName(getTypeName(), typeName);
    }

    /**
     * To be removed, use getEntry(String entryName)
     */
    @Deprecated
    public Entry searchByEntryName(String entryName){
        return getEntryArray().getEntry(entryName);
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getTypeName());
        builder.append('{');
        builder.append(getHeaderBlock());
        builder.append('}');
        return builder.toString();
    }

    public static boolean isEqualTypeName(String name1, String name2){
        if(name1 == null){
            return name2 == null;
        }
        if(name2 == null){
            return false;
        }
        if(name1.equals(name2)){
            return true;
        }
        return trimTypeName(name1).equals(trimTypeName(name2));
    }
    private static String trimTypeName(String typeName){
        while (typeName.length() > 0 && isWildTypeNamePrefix(typeName.charAt(0))){
            typeName = typeName.substring(1);
        }
        return typeName;
    }
    private static boolean isWildTypeNamePrefix(char ch){
        switch (ch){
            case '^':
            case '*':
            case '+':
                return true;
            default:
                return false;
        }
    }

    public static final String NAME_name = "name";
    public static final String NAME_config = "config";
    public static final String NAME_id = "id";
    public static final String NAME_entries = "entries";
    public static final String NAME_is_sparse = "is_sparse";
}
