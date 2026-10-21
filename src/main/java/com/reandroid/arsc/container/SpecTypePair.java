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
package com.reandroid.arsc.container;

import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ValueItem;
import com.reandroid.common.Mergeable;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.MergingIterator;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

public class SpecTypePair extends FixedBlockContainer
        implements Iterable<TypeBlock>, Mergeable<SpecTypePair>,
        JSONConvert<JSONObject>, Comparable<SpecTypePair> {

    private final SpecBlock mSpecBlock;
    private final TypeBlockArray mTypeBlockArray;

    public SpecTypePair() {
        super(2);
        this.mSpecBlock = new SpecBlock();
        this.mTypeBlockArray = new TypeBlockArray();

        addChild(0, mSpecBlock);
        addChild(1, mTypeBlockArray);
    }

    public Iterator<ValueItem> visitValues() {
        return new MergingIterator<>(ComputeIterator.of(getTypeBlocks(),
                TypeBlock::visitValues));
    }
    public void trimConfigSize(int resConfigSize) {
        getTypeBlockArray().trimConfigSize(resConfigSize);
    }
    public boolean isTypeAttr() {
        // TODO: find better way
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.isTypeAttr();
        }
        return false;
    }
    public boolean isTypeId() {
        // TODO: find better way
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.isTypeId();
        }
        return false;
    }
    public ResourceEntry getResource(int entryId) {
        if (entryId < 0 || entryId > getHighestEntryId()) {
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        int resourceId = packageBlock.getId() << 24;
        resourceId |= getId() << 16;
        resourceId |= entryId;
        return new ResourceEntry(packageBlock, resourceId);
    }
    public ResourceEntry getResource(String name) {
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        SpecStringPool specStringPool = packageBlock.getSpecStringPool();
        int resourceId = specStringPool
                .resolveResourceId(this, name);
        if (resourceId == 0) {
            return null;
        }
        return new ResourceEntry(packageBlock, resourceId);
    }
    public Iterator<ResourceEntry> getResources() {
        final PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return EmptyIterator.of();
        }
        final int end = getHighestEntryId();
        final int id = (packageBlock.getId() << 24) | (getId() << 16);
        return new Iterator<ResourceEntry>() {
            private int mIndex;
            @Override
            public boolean hasNext() {
                return mIndex <= end;
            }
            @Override
            public ResourceEntry next() {
                if (mIndex > end) {
                    throw new NoSuchElementException();
                }
                int resourceId = id | mIndex;
                mIndex ++;
                return new ResourceEntry(packageBlock, resourceId);
            }
        };
    }
    public Iterator<Entry> getEntries(int entryId) {
        return getEntries(entryId, false);
    }
    public Iterator<Entry> getEntries(int entryId, boolean skipNull) {
        return new ComputeIterator<>(getTypeBlocks(), typeBlock -> {
            Entry entry = typeBlock.getEntry(entryId);
            if (entry == null || (skipNull && entry.isNull())) {
                return null;
            }
            return entry;
        });
    }
    public Iterator<TypeBlock> getTypeBlocks() {
        return getTypeBlockArray().iterator();
    }
    public Iterator<TypeBlock> getTypeBlocks(Predicate<TypeBlock> filter) {
        return getTypeBlockArray().iterator(filter);
    }
    public Iterator<TypeBlock> getTypeBlocks(ResConfig resConfig) {
        return getTypeBlockArray().iterator(resConfig);
    }
    public Boolean hasComplexEntry() {
        return getTypeBlockArray().hasComplexEntry();
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool) {
        TypeBlockArray array = getTypeBlockArray();
        int size = array.size();
        for (int i = 0; i < size; i++) {
            array.get(i).getEntryArray().linkTableStringsInternal(tableStringPool);
        }
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool) {
        TypeBlockArray array = getTypeBlockArray();
        int size = array.size();
        for (int i = 0; i < size; i++) {
            array.get(i).getEntryArray().linkSpecStringsInternal(specStringPool);
        }
    }
    public void destroy() {
        getSpecBlock().destroy();
        getTypeBlockArray().destroy();
    }
    public Entry getAnyEntry(int entryId) {
        return getTypeBlockArray().getAnyEntry(entryId);
    }
    public Entry getAnyEntry(String name) {
        return getTypeBlockArray().getAnyEntry(name);
    }
    public Entry getEntry(ResConfig resConfig, String entryName) {
        return getTypeBlockArray().getEntry(resConfig, entryName);
    }
    public Entry getEntry(ResConfig resConfig, int entryId) {
        return getTypeBlockArray().getEntry(resConfig, entryId);
    }
    public void sortTypes() {
        getTypeBlockArray().sort();
    }
    public boolean removeNullEntries(int startId) {
        startId = 0x0000ffff & startId;
        boolean removed = getTypeBlockArray().removeNullEntries(startId);
        if (!removed) {
            return false;
        }
        getSpecBlock().setEntryCount(startId);
        return true;
    }
    public boolean removeBlank() {
        return getTypeBlockArray().removeBlank();
    }
    public boolean isBlank() {
        return getTypeBlockArray().isBlank();
    }
    public int countTypeBlocks() {
        return getTypeBlockArray().size();
    }

    public Entry getEntry(String qualifiers, int entryId) {
        return getTypeBlockArray().getEntry(qualifiers, entryId);
    }
    public TypeBlock getOrCreateTypeBlock(String qualifiers) {
        return getTypeBlockArray().getOrCreate(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(ResConfig resConfig) {
        return getTypeBlockArray().getOrCreate(resConfig);
    }
    public TypeBlock getTypeBlock(String qualifiers) {
        return getTypeBlockArray().getTypeBlock(qualifiers);
    }
    public TypeBlock getTypeBlock(ResConfig resConfig) {
        return getTypeBlockArray().getTypeBlock(resConfig);
    }
    // Use uniqueConfigs();
    @Deprecated
    public Set<ResConfig> listResConfig() {
        return mTypeBlockArray.listResConfig();
    }
    public Iterator<ResConfig> uniqueConfigs() {
        return mTypeBlockArray.uniqueConfigs();
    }
    public Iterator<ResConfig> getResConfigs() {
        return mTypeBlockArray.getResConfigs();
    }
    public Iterator<TypeBlock> iteratorNonEmpty() {
        return mTypeBlockArray.iteratorNonEmpty();
    }
    public boolean hasDuplicateResConfig(boolean ignoreEmpty) {
        return mTypeBlockArray.hasDuplicateResConfig(ignoreEmpty);
    }

    public int getId() {
        int id = mSpecBlock.getId();
        if (id == 0) {
            id = mTypeBlockArray.getId();
        }
        return id;
    }
    public void setId(int id) {
        mSpecBlock.setId(id);
        mTypeBlockArray.setId(id);
    }
    public String getTypeName() {
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.get();
        }
        return null;
    }

    public SpecBlock getSpecBlock() {
        return mSpecBlock;
    }
    public TypeBlockArray getTypeBlockArray() {
        return mTypeBlockArray;
    }
    public PackageBlock getPackageBlock() {
        return getParent(PackageBlock.class);
    }

    @Override
    public Iterator<TypeBlock> iterator() {
        return getTypeBlockArray().iterator();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        if (headerBlock == null) {
            return;
        }
        ChunkType chunkType = headerBlock.getChunkType();
        if (chunkType == ChunkType.SPEC) {
            getSpecBlock().readBytes(reader);
        } else if (chunkType != ChunkType.TYPE){
            throw new IOException("Unexpected block: " + headerBlock
                    + ", Should be: " + ChunkType.SPEC);
        }
        getTypeBlockArray().readTypes(reader);
    }

    public void setEntryCount(int count) {
        getSpecBlock().setEntryCount(count);
        getTypeBlockArray().setEntryCount(count);
    }
    public int getHighestEntryCount() {
        return getTypeBlockArray().getHighestEntryCount();
    }
    public int getHighestEntryId() {
        return getTypeBlockArray().getHighestEntryId();
    }
    public TypeString getTypeString() {
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock != null) {
            return packageBlock.getTypeStringPool().getById(getId());
        }
        return null;
    }

    public void serializePublicXml(XmlSerializer serializer) throws IOException {
        ResourceEntry last = getResource(getHighestEntryId());
        boolean lastWritten = false;
        if (last != null) {
            lastWritten = last.serializePublicXml(serializer);
        }
        Iterator<ResourceEntry> iterator = getResources();
        while (iterator.hasNext()) {
            ResourceEntry resourceEntry = iterator.next();
            if (lastWritten && resourceEntry.equals(last)) {
                continue;
            }
            resourceEntry.serializePublicXml(serializer);
        }
    }
    public boolean isCompatibleTo(SpecTypePair pair) {
        if (equals(pair)) {
            return true;
        }
        if (getId() != pair.getId()) {
            return false;
        }
        if (isBlank() || pair.isBlank()) {
            return true;
        }
        return ObjectsUtil.equals(getTypeName(), pair.getTypeName());
    }

    @Override
    public JSONObject toJson() {
        return toJson(false);
    }
    @Override
    public void fromJson(JSONObject json) {
        if (json == null) {
            return;
        }
        setId(json.optInt("id", getId()));
        getSpecBlock().fromJson(json.optJSONArray(SpecBlock.NAME_flags));
        getTypeBlockArray().fromJson(json.optJSONArray(NAME_types));
    }
    public JSONObject toJson(boolean specOnly) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getId());
        JSONArray spec = getSpecBlock().toJson();
        if (spec != null) {
            jsonObject.put(SpecBlock.NAME_flags, spec);
        }
        if (!specOnly) {
            JSONArray jsonArray = getTypeBlockArray().toJson();
            if (jsonArray != null) {
                jsonObject.put(NAME_types, jsonArray);
            }
        }
        if (jsonObject.length() < 2) {
            return null;
        }
        return jsonObject;
    }
    @Override
    public void merge(SpecTypePair typePair) {
        if (typePair == null || typePair == this) {
            return;
        }
        if (getId() != typePair.getId()) {
            throw new IllegalArgumentException("Can not merge different id types: "
                    + getId() + "!=" + typePair.getId());
        }
        getSpecBlock().merge(typePair.getSpecBlock());
        getTypeBlockArray().merge(typePair.getTypeBlockArray());
    }
    @Override
    public int compareTo(SpecTypePair specTypePair) {
        return CompareUtil.compare(getId(), specTypePair.getId());
    }
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(HexUtil.toHex(getId(), 2));
        builder.append(" (");
        TypeString ts = getTypeString();
        if (ts != null) {
            builder.append(ts.get());
        } else {
            builder.append("null");
        }
        builder.append(") config count=");
        builder.append(getTypeBlockArray().size());
        return builder.toString();
    }

    public static final String NAME_types = "types";
}
