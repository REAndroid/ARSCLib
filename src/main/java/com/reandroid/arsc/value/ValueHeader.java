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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.ParentChunk;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.*;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;

public class ValueHeader extends BlockItem implements JSONConvert<JSONObject> {
    private ReferenceItem mStringReference;
    public ValueHeader(int size){
        super(size);
        writeSize();
        putInteger(getBytesInternal(), OFFSET_SPEC_REFERENCE, -1);
    }

    void linkSpecStringsInternal(SpecStringPool specStringPool){
        int key = getKey();
        SpecString specString = specStringPool.get(key);
        if(specString == null){
            mStringReference = null;
            return;
        }
        if(mStringReference != null){
            specString.removeReference(mStringReference);
        }
        ReferenceItem stringReference = new ReferenceBlock<>(this, OFFSET_SPEC_REFERENCE);
        mStringReference = stringReference;
        specString.addReference(stringReference);
    }
    public void onRemoved(){
        unLinkStringReference();
    }
    public String getName(){
        StringItem stringItem = getNameString();
        if(stringItem!=null){
            return stringItem.get();
        }
        return null;
    }
    public boolean isComplex(){
        return getBit(getBytesInternal(), OFFSET_FLAGS,0);
    }
    public void setComplex(boolean complex){
        putBit(getBytesInternal(), OFFSET_FLAGS, 0, complex);
    }
    public void setPublic(boolean b){
        putBit(getBytesInternal(), OFFSET_FLAGS,1, b);
    }
    public boolean isPublic(){
        return getBit(getBytesInternal(), OFFSET_FLAGS,1);
    }
    public void setWeak(boolean b){
        putBit(getBytesInternal(), OFFSET_FLAGS, 2, b);
    }
    public boolean isWeak(){
        return getBit(getBytesInternal(), OFFSET_FLAGS,2);
    }

    public int getKey(){
        return getInteger(getBytesInternal(), OFFSET_SPEC_REFERENCE);
    }
    public void setKey(int key){
        if(key == getKey()){
            return;
        }
        unLinkStringReference();
        putInteger(getBytesInternal(), OFFSET_SPEC_REFERENCE, key);
        linkStringReference();
    }
    public void setKey(StringItem stringItem){
        if(ignoreUpdateKey(stringItem)){
            return;
        }
        unLinkStringReference();
        int key = -1;
        if(stringItem!=null){
            key=stringItem.getIndex();
        }
        putInteger(getBytesInternal(), OFFSET_SPEC_REFERENCE, key);
        linkStringReference(stringItem);
    }
    private boolean ignoreUpdateKey(StringItem stringItem){
        int key = getKey();
        ReferenceItem referenceItem = this.mStringReference;
        if(stringItem == null){
            return referenceItem == null && key == -1;
        }
        if(referenceItem == null || key != stringItem.getIndex()){
            return false;
        }
        return getSpecString(key) == stringItem;
    }
    public void setSize(int size){
        super.setBytesLength(size, false);
        writeSize();
    }
    public int getSize(){
        return getBytesInternal().length;
    }
    int readSize(){
        if(getSize()<2){
            return 0;
        }
        return 0xffff & getShort(getBytesInternal(), OFFSET_SIZE);
    }
    private void writeSize(){
        int size = getSize();
        if(size>1){
            putShort(getBytesInternal(), OFFSET_SIZE, (short) size);
        }
    }

    private void linkStringReference(){
        StringPool<?> specStringPool = getSpecStringPool();
        if(specStringPool == null || specStringPool.isStringLinkLocked()){
            return;
        }
        linkStringReference(specStringPool.get(getKey()));
    }
    private void linkStringReference(StringItem stringItem){
        unLinkStringReference();
        if(stringItem==null){
            return;
        }
        ReferenceItem stringReference = new ReferenceBlock<>(this, OFFSET_SPEC_REFERENCE);
        mStringReference = stringReference;
        stringItem.addReference(stringReference);
    }
    private void unLinkStringReference(){
        ReferenceItem stringReference = mStringReference;
        if(stringReference==null){
            return;
        }
        mStringReference = null;
        StringItem stringItem = getNameString();
        if(stringItem == null){
            return;
        }
        stringItem.removeReference(stringReference);
    }
    public StringItem getNameString(){
        return getSpecString(getKey());
    }
    private StringItem getSpecString(int key){
        if(key < 0){
            return null;
        }
        StringPool<?> specStringPool = getSpecStringPool();
        if(specStringPool==null){
            return null;
        }
        return specStringPool.get(key);
    }
    private StringPool<?> getSpecStringPool(){
        Block parent = getParent();
        while (parent!=null){
            if(parent instanceof ParentChunk){
                return ((ParentChunk) parent).getSpecStringPool();
            }
            parent = parent.getParent();
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        int size = reader.readUnsignedShort();
        setBytesLength(size, false);
        reader.readFully(getBytesInternal());
    }
    private void setName(String name){
        if(name==null){
            name = "";
        }
        StringPool<?> stringPool = getSpecStringPool();
        if(stringPool==null){
            return;
        }
        StringItem stringItem = stringPool.getOrCreate(name);
        setKey(stringItem);
    }
    public void merge(ValueHeader valueHeader){
        if(valueHeader == null || valueHeader ==this){
            return;
        }
        setComplex(valueHeader.isComplex());
        setWeak(valueHeader.isWeak());
        setPublic(valueHeader.isPublic());
        setName(valueHeader.getName());
    }
    public void toJson(JSONObject jsonObject) {
        jsonObject.put(NAME_entry_name, getName());
        if(isWeak()){
            jsonObject.put(NAME_is_weak, true);
        }
        if(isPublic()){
            jsonObject.put(NAME_is_public, true);
        }
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        toJson(jsonObject);
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setWeak(json.optBoolean(NAME_is_weak, false));
        setPublic(json.optBoolean(NAME_is_public, false));
        setName(json.optString(NAME_entry_name));
    }
    @Override
    public String toString(){
        if(isNull()){
            return "null";
        }
        StringBuilder builder=new StringBuilder();
        int byte_size = getSize();
        int read_size = readSize();
        if(byte_size!=8){
            builder.append("size=").append(byte_size);
        }
        if(byte_size!=read_size){
            builder.append(", readSize=").append(read_size);
        }
        if(isComplex()){
            builder.append(", complex");
        }
        if(isPublic()){
            builder.append(", public");
        }
        if(isWeak()){
            builder.append(", weak");
        }
        String name = getName();
        if(name!=null){
            builder.append(", name=").append(name);
        }else {
            builder.append(", key=").append(getKey());
        }
        return builder.toString();
    }

    private static final int OFFSET_SIZE = 0;
    private static final int OFFSET_FLAGS = 2;
    private static final int OFFSET_SPEC_REFERENCE = 4;


    public static final String NAME_is_complex = "is_complex";
    public static final String NAME_is_public = "is_public";
    public static final String NAME_is_weak = "is_weak";

    public static final String NAME_entry_name = "entry_name";
}
