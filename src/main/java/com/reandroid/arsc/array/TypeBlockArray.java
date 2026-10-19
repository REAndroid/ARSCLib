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
package com.reandroid.arsc.array;

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.SpecOrTypeHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.UniqueIterator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TypeBlockArray extends BlockArray<TypeBlock> implements Creator<TypeBlock>,
        JSONConvert<JSONArray> {
    
    private int mTypeId;
    private Boolean mHasComplexEntry;
    private Map<String, TypeBlock> mQualifiersMap;

    public TypeBlockArray() {
        super();
    }

    public Boolean hasComplexEntry() {
        if (mHasComplexEntry != null) {
            return mHasComplexEntry;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            if (typeBlock.isBlank()) {
                continue;
            }
            Boolean hasComplex = typeBlock.getEntryArray().hasComplexEntry();
            if (hasComplex != null) {
                mHasComplexEntry = hasComplex;
                break;
            }
        }
        return mHasComplexEntry;
    }
    @Override
    public void destroy() {
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            if (typeBlock != null) {
                typeBlock.destroy();
            }
        }
        clear();
    }
    public void sort() {
        if (needsSort(CompareUtil.getComparableComparator())) {
            sort(CompareUtil.getComparableComparator());
        }
    }
    public boolean removeNullEntries(int startId) {
        boolean result = true;
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            boolean removed = typeBlock.removeNullEntries(startId);
            result = result && removed;
        }
        return result;
    }
    public boolean removeBlank() {
        removeIf(TypeBlock::isBlank);
        return isBlank();
    }

    public void trimConfigSize(int resConfigSize) {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).getResConfig().trimToSize(resConfigSize);
        }
    }

    @Override
    public void onPreRemove(TypeBlock block) {
        if (block.getParent() == this) {
            block.clear();
        }
        super.onPreRemove(block);
    }

    public Entry getOrCreateEntry(String qualifiers, int entryId) {
        TypeBlock typeBlock = getOrCreate(qualifiers);
        return typeBlock.getOrCreateEntry(entryId & 0xffff);
    }
    public Entry getOrCreateEntry(ResConfig resConfig, int entryId) {
        TypeBlock typeBlock = getOrCreate(resConfig);
        return typeBlock.getOrCreateEntry(entryId & 0xffff);
    }
    public boolean isBlank() {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (!get(i).isBlank()) {
                return false;
            }
        }
        return true;
    }
    public Entry getEntry(ResConfig resConfig, String entryName) {
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if (typeBlock != null) {
            return typeBlock.getEntry(entryName);
        }
        return null;
    }
    public Entry getEntry(ResConfig resConfig, int entryId) {
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if (typeBlock != null) {
            return typeBlock.getEntry(entryId);
        }
        return null;
    }
    public Entry getEntry(String qualifiers, int entryId) {
        TypeBlock typeBlock = getTypeBlock(qualifiers);
        if (typeBlock != null) {
            return typeBlock.getEntry(entryId);
        }
        return null;
    }
    public Entry getAnyEntry(int entryId) {
        Entry result = null;
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            Entry entry = typeBlock.getEntry(entryId);
            if (entry == null) {
                continue;
            }
            if (!entry.isNull()) {
                return entry;
            }
            if (result == null) {
                result = entry;
            }
        }
        return result;
    }
    public Entry getAnyEntry(String name) {
        int size = size();
        for (int i = 0; i < size; i++) {
            Entry entry = get(i).getEntry(name);
            if (entry != null) {
                return entry;
            }
        }
        return null;
    }
    public TypeBlock getOrCreate(ResConfig resConfig) {
        TypeBlock typeBlock = getTypeBlock(resConfig);
        if (typeBlock != null) {
            return typeBlock;
        }
        typeBlock = createNext();
        typeBlock.setId(getId());
        ResConfig config = typeBlock.getResConfig();
        config.copyFrom(resConfig);
        return typeBlock;
    }
    public TypeBlock getOrCreate(String qualifiers) {
        TypeBlock typeBlock = getTypeBlock(qualifiers);
        if (typeBlock != null) {
            return typeBlock;
        }
        int count = getHighestEntryCount();
        typeBlock = createNext();
        typeBlock.ensureEntriesCount(count);
        ResConfig config = typeBlock.getResConfig();
        config.parseQualifiers(qualifiers);
        return typeBlock;
    }
    public TypeBlock getTypeBlock(String qualifiers) {
        if (qualifiers == null || size() == 0) {
            return null;
        }
        Map<String, TypeBlock> map = getQualifiersMap();
        TypeBlock typeBlock = map.get(qualifiers);
        if (typeBlock != null) {
            if (qualifiers.equals(typeBlock.getQualifiers()) && typeBlock.getParent() != null) {
                return typeBlock;
            }
            map.remove(qualifiers);
        }
        int size = size();
        ResConfig resConfig = ResConfig.parse(qualifiers);
        for (int i = 0; i < size; i++) {
            typeBlock = get(i);
            if (resConfig.equals(typeBlock.getResConfig())) {
                map.putIfAbsent(qualifiers, typeBlock);
                return typeBlock;
            }
        }
        return null;
    }

    public TypeBlock getTypeBlock(ResConfig resConfig) {
        if (resConfig == null || size() == 0) {
            return null;
        }
        String qualifiers = resConfig.getQualifiers();
        Map<String, TypeBlock> map = getQualifiersMap();
        TypeBlock typeBlock = map.get(qualifiers);
        if (typeBlock != null) {
            if (qualifiers.equals(typeBlock.getQualifiers()) && typeBlock.getParent() != null) {
                return typeBlock;
            }
            map.remove(qualifiers);
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            typeBlock = get(i);
            if (resConfig.equals(typeBlock.getResConfig())) {
                map.put(qualifiers, typeBlock);
                return typeBlock;
            }
        }
        return null;
    }
    private Map<String, TypeBlock> getQualifiersMap() {
        Map<String, TypeBlock> map = this.mQualifiersMap;
        if (map == null) {
            int size = this.size();
            map = new HashMap<>(size);
            this.mQualifiersMap = map;
            for (int i = 0; i < size; i++) {
                TypeBlock typeBlock = get(i);
                map.putIfAbsent(typeBlock.getQualifiers(), typeBlock);
            }
        }
        return map;
    }
    public void setId(int id) {
        this.mTypeId = id;
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).setId(id);
        }
    }
    public int getId() {
        SpecBlock specBlock = getSpecBlock();
        if (specBlock != null) {
            int id = specBlock.getId();
            if (id != 0) {
                return id;
            }
        }
        if (mTypeId != 0) {
            return mTypeId;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            int id = typeBlock.getId();
            if (id == 0) {
                continue;
            }
            if (specBlock != null) {
                specBlock.setId(id);
            }
            mTypeId = id;
            return id;
        }
        return 0;
    }

    // Use uniqueConfigs()
    @Deprecated
    public Set<ResConfig> listResConfig() {
        int size = size();
        Set<ResConfig> unique = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            unique.add(get(i).getResConfig());
        }
        return unique;
    }

    public Iterator<ResConfig> uniqueConfigs() {
        return UniqueIterator.of(getResConfigs());
    }
    public Iterator<ResConfig> getResConfigs() {
        return new ComputeIterator<>(super.iterator(true),
                TypeBlock::getResConfig);
    }
    public Iterator<TypeBlock> iteratorNonEmpty() {
        return super.iterator(typeBlock -> {
            if (typeBlock == null || typeBlock.isNull()) {
                return false;
            }
            return !typeBlock.isBlank();
        });
    }
    public Iterator<TypeBlock> iterator(ResConfig resConfig) {
        return iterator(typeBlock -> typeBlock.getResConfig().equals(resConfig));
    }
    public boolean hasDuplicateResConfig(boolean ignoreEmpty) {
        Set<Integer> uniqueHashSet = new HashSet<>();
        Iterator<TypeBlock> iterator;
        if (ignoreEmpty) {
            iterator = iteratorNonEmpty();
        } else {
            iterator = iterator(true);
        }
        while (iterator.hasNext()) {
            Integer hash = iterator.next()
                    .getResConfig().hashCode();
            if (uniqueHashSet.contains(hash)) {
                return true;
            }
            uniqueHashSet.add(hash);
        }
        return false;
    }
    private SpecBlock getSpecBlock() {
        SpecTypePair parent = getParent(SpecTypePair.class);
        if (parent != null) {
            return parent.getSpecBlock();
        }
        return null;
    }

    @Override
    public Creator<TypeBlock> getCreator() {
        return this;
    }

    @Override
    public TypeBlock newInstance() {
        return new TypeBlock(getId());
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        this.mQualifiersMap = null;
    }
    @Override
    public void onChanged() {
        super.onChanged();
        mQualifiersMap = null;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        readTypes(reader);
    }
    public void readTypes(BlockReader reader) throws IOException {
        boolean readOk = true;
        while (readOk) {
            readOk = readNextType(reader);
        }
        this.mQualifiersMap = null;
    }
    private boolean readNextType(BlockReader reader) throws IOException {
        if (reader.available() < 10) {
            return false;
        }
        SpecOrTypeHeader header = SpecOrTypeHeader.read(reader);
        if (header.getChunkType() != ChunkType.TYPE) {
            return false;
        }
        int id = getId();
        if (id != 0 && header.getId() != id) {
            return false;
        }
        int position = reader.getPosition();
        createNext().readBytes(reader);
        return reader.getPosition() > position;
    }
    public int getHighestEntryId() {
        int result = -1;
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            int high = typeBlock.getEntryArray().getHighestEntryId();
            if (high > result) {
                result = high;
            }
        }
        return result;
    }
    public int getHighestEntryCount() {
        int result = 0;
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            int count = typeBlock.getEntryArray().size();
            if (count > result) {
                result = count;
            }
        }
        return result;
    }
    public void setEntryCount(int count) {
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            if (!typeBlock.isSparse()) {
                typeBlock.setEntryCount(count);
            }
        }
    }
    public TypeString getTypeString() {
        int size = size();
        for (int i = 0; i < size; i++) {
            TypeBlock typeBlock = get(i);
            TypeString typeString = typeBlock.getTypeString();
            if (typeString != null) {
                return typeString;
            }
        }
        return null;
    }
    @Override
    public JSONArray toJson() {
        return BlockList.toJsonArray(this);
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json != null) {
            int length = json.length();
            for (int i = 0; i < length; i++) {
                createNext().fromJson(json.getJSONObject(i));
            }
        }
    }
    public void merge(TypeBlockArray typeBlockArray) {
        if (typeBlockArray == null || typeBlockArray == this) {
            return;
        }
        int size = typeBlockArray.size();
        if (this.size() == 0) {
            setSize(size);
            for (int i = 0; i < size; i++) {
                get(i).merge(typeBlockArray.get(i));
            }
        } else {
            if (size > this.size()) {
                ensureCapacity(size);
            }
            for (int i = 0; i < size; i++) {
                TypeBlock typeBlock = typeBlockArray.get(i);
                getOrCreate(typeBlock.getResConfig()).merge(typeBlock);
            }
        }
    }
}
