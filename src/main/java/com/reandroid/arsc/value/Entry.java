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
package com.reandroid.arsc.value;

import com.reandroid.arsc.array.EntryArray;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;


import java.io.IOException;
import java.io.OutputStream;

public class Entry extends Block implements JSONConvert<JSONObject> {
    private TableEntry<?, ?> mTableEntry;

    public Entry(){
        super();
    }

    public void linkTableStringsInternal(TableStringPool tableStringPool){
        TableEntry<?, ?> tableEntry = getTableEntry();
        tableEntry.linkTableStringsInternal(tableStringPool);
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        TableEntry<?, ?> tableEntry = getTableEntry();
        ValueHeader header = tableEntry.getHeader();
        header.linkSpecStringsInternal(specStringPool);
    }
    public ResValue getResValue(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry instanceof ResTableEntry){
            return ((ResTableEntry)tableEntry).getValue();
        }
        return null;
    }
    public ResValueMapArray getResValueMapArray(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry instanceof ResTableMapEntry){
            return ((ResTableMapEntry)tableEntry).getValue();
        }
        return null;
    }
    public SpecFlag getSpecFlag(){
        SpecBlock specBlock = getSpecBlock();
        if(specBlock == null){
            return null;
        }
        return specBlock.getSpecFlag(getId());
    }
    public void ensureComplex(boolean isComplex){
        ensureTableEntry(isComplex);
    }
    public int getId(){
        int id = getIndex();
        EntryArray entryArray = getParentInstance(EntryArray.class);
        if(entryArray != null){
            id = entryArray.getEntryId(id);
        }
        return id;
    }
    public String getName(){
        SpecString specString = getSpecString();
        if(specString!=null){
            return specString.get();
        }
        return null;
    }
    public String getTypeName(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeName();
        }
        return null;
    }
    public int getResourceId(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock==null){
            return 0;
        }
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock==null){
            return 0;
        }
        return (packageBlock.getId()<<24)
                | (typeBlock.getId() << 16)
                | getId();
    }
    public int getSpecReference(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry == null){
            return 0;
        }
        return tableEntry.getHeader().getKey();
    }
    public TypeString getTypeString(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeString();
        }
        return null;
    }
    public boolean isDefault(){
        ResConfig resConfig = getResConfig();
        if(resConfig!=null){
            return resConfig.isDefault();
        }
        return false;
    }
    public void setSpecReference(StringItem specReference){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry == null){
            return;
        }
        tableEntry.getHeader().setKey(specReference);
    }
    public void setSpecReference(int ref){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry == null){
            return;
        }
        tableEntry.getHeader().setKey(ref);
    }
    private Entry searchEntry(int resourceId){
        if(resourceId==getResourceId()){
            return this;
        }
        PackageBlock packageBlock= getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock = packageBlock.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        EntryGroup entryGroup = tableBlock.search(resourceId);
        if(entryGroup!=null){
            return entryGroup.pickOne();
        }
        return null;
    }
    public ResValue setValueAsRaw(ValueType valueType, int data){
        TableEntry<?, ?> tableEntry = ensureTableEntry(false);
        ResValue resValue = (ResValue) tableEntry.getValue();
        resValue.setTypeAndData(valueType, data);
        return resValue;
    }
    public ResValue setValueAsBoolean(boolean val){
        int data = val?0xffffffff:0;
        return setValueAsRaw(ValueType.INT_BOOLEAN, data);
    }
    public ResValue setValueAsReference(int resourceId){
        return setValueAsRaw(ValueType.REFERENCE, resourceId);
    }
    public ResValue setValueAsString(String str){
        TableEntry<?, ?> tableEntry = ensureTableEntry(false);
        ResValue resValue = (ResValue) tableEntry.getValue();
        resValue.setValueAsString(str);
        return resValue;
    }
    public SpecString getSpecString(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry == null){
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            return null;
        }
        return packageBlock.getSpecStringPool()
                .get(tableEntry.getHeader().getKey());
    }
    public ResConfig getResConfig(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getResConfig();
        }
        return null;
    }
    public SpecBlock getSpecBlock(){
        TypeBlock typeBlock = getTypeBlock();
        if(typeBlock == null){
            return null;
        }
        SpecTypePair specTypePair = typeBlock.getParentSpecTypePair();
        if(specTypePair==null){
            return null;
        }
        return specTypePair.getSpecBlock();
    }
    public TypeBlock getTypeBlock(){
        return getParent(TypeBlock.class);
    }
    private String getPackageName(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock!=null){
            return packageBlock.getName();
        }
        return null;
    }
    public PackageBlock getPackageBlock(){
        return getParent(PackageBlock.class);
    }
    private TableEntry<?, ?> ensureTableEntry(boolean is_complex){
        TableEntry<?, ?> tableEntry = getTableEntry();

        boolean is_correct_type = (is_complex && tableEntry instanceof ResTableMapEntry) || (!is_complex && tableEntry instanceof ResTableEntry);
        if (tableEntry == null || !is_correct_type) {
            tableEntry = createTableEntry(is_complex);
            setTableEntry(tableEntry);
        }
        return tableEntry;
    }

    public TableEntry<?, ?> getTableEntry(){
        return mTableEntry;
    }
    public ValueHeader getHeader(){
        TableEntry<?, ?> tableEntry = getTableEntry();
        if(tableEntry!=null){
            return tableEntry.getHeader();
        }
        return null;
    }

    @Override
    public boolean isNull(){
        return getTableEntry()==null;
    }
    @Override
    public void setNull(boolean is_null){
        if(is_null){
            setTableEntry(null);
        }
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        return getTableEntry().getBytes();
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        return getTableEntry().countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        if(isNull()){
            return;
        }
        counter.addCount(getTableEntry().countBytes());
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        return getTableEntry().writeBytes(stream);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        TableEntry<?, ?> tableEntry = createTableEntry(reader);
        setTableEntry(tableEntry);
        tableEntry.readBytes(reader);
    }

    public boolean isComplex(){
        return getTableEntry() instanceof CompoundEntry;
    }
    public void setTableEntry(TableEntry<?, ?> tableEntry){
        if(tableEntry==this.mTableEntry){
            return;
        }
        onTableEntryRemoved();
        if(tableEntry==null){
            return;
        }
        tableEntry.setIndex(0);
        tableEntry.setParent(this);
        this.mTableEntry = tableEntry;
        onTableEntryAdded();
    }
    private void onTableEntryAdded(){
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock!=null){
            packageBlock.onEntryAdded(this);
        }
    }
    private void onTableEntryRemoved(){
        TableEntry<?, ?> exist = this.mTableEntry;
        if(exist == null){
            return;
        }
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock!=null){
            packageBlock.removeEntryGroup(this);
        }
        exist.onRemoved();
        exist.setIndex(-1);
        exist.setParent(null);
        this.mTableEntry = null;
    }
    private TableEntry<?, ?> createTableEntry(BlockReader reader) throws IOException {
        int startPosition = reader.getPosition();
        reader.offset(2);
        boolean is_complex = (0x0001 & reader.readShort()) == 0x0001;
        reader.seek(startPosition);
        return createTableEntry(is_complex);
    }
    private TableEntry<?, ?> createTableEntry(boolean is_complex) {
        if(is_complex){
            return new ResTableMapEntry();
        }else {
            return new ResTableEntry();
        }
    }

    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        return getTableEntry().toJson();
    }
    @Override
    public void fromJson(JSONObject json) {
        if(json==null){
            setNull(true);
            return;
        }
        boolean is_complex = json.optBoolean(ValueHeader.NAME_is_complex, false);
        TableEntry<?, ?> entry = createTableEntry(is_complex);
        setTableEntry(entry);
        entry.fromJson(json);
    }

    public void merge(Entry entry){
        if(!shouldMerge(entry)){
            return;
        }
        TableEntry<?, ?> tableEntry = entry.getTableEntry();
        TableEntry<?, ?> existEntry = ensureTableEntry(tableEntry instanceof ResTableMapEntry);
        existEntry.merge(tableEntry);
    }
    private boolean shouldMerge(Entry coming){
        if(coming == null || coming == this || coming.isNull()){
            return false;
        }
        if(this.isNull()){
            return true;
        }
        return getTableEntry().shouldMerge(coming.getTableEntry());
    }

    public String buildResourceName(int resourceId, char prefix, boolean includeType){
        if(resourceId==0){
            return null;
        }
        Entry entry=searchEntry(resourceId);
        return buildResourceName(entry, prefix, includeType);
    }
    public String buildResourceName(Entry entry, char prefix, boolean includeType){
        if(entry==null){
            return null;
        }
        String pkgName=entry.getPackageName();
        if(getResourceId()==entry.getResourceId()){
            pkgName=null;
        }else if(pkgName!=null){
            if(pkgName.equals(this.getPackageName())){
                pkgName=null;
            }
        }
        String type=null;
        if(includeType){
            type=entry.getTypeName();
        }
        String name=entry.getName();
        return buildResourceName(prefix, pkgName, type, name);
    }
    public String getResourceName(){
        return buildResourceName('@',null, getTypeName(), getName());
    }
    public String getResourceName(char prefix){
        return getResourceName(prefix, false, true);
    }
    public String getResourceName(char prefix, boolean includePackage, boolean includeType){
        String pkg=includePackage?getPackageName():null;
        String type=includeType?getTypeName():null;
        return buildResourceName(prefix,pkg, type, getName());
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(HexUtil.toHex8(getResourceId()));
        builder.append(' ');
        ResConfig resConfig = getResConfig();
        if(resConfig!=null){
            builder.append(resConfig);
            builder.append(' ');
        }
        SpecFlag specFlag = getSpecFlag();
        if(specFlag!=null){
            builder.append(specFlag);
            builder.append(' ');
        }
        if(isNull()){
            builder.append("NULL");
            return builder.toString();
        }
        builder.append('@');
        builder.append(getTypeName());
        builder.append('/');
        builder.append(getName());
        return builder.toString();
    }

    public static String buildResourceName(char prefix, String packageName, String type, String name){
        if(name==null){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        if(prefix!=0){
            builder.append(prefix);
        }
        if(packageName!=null){
            builder.append(packageName);
            builder.append(':');
        }
        if(type!=null){
            builder.append(type);
            builder.append('/');
        }
        builder.append(name);
        return builder.toString();
    }

    public static final String NAME_id = "id";
}
