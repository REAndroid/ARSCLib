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

import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.TypeHeader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.list.EntryItemList;
import com.reandroid.arsc.list.EntryItemOffsetList;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ValueItem;
import com.reandroid.common.Mergeable;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.IterableIterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class TypeBlock extends Chunk<TypeHeader>
        implements Mergeable<TypeBlock>, Iterable<Entry>, JSONConvert<JSONObject>, Comparable<TypeBlock> {

    private final EntryItemList mEntryArray;
    private TypeString mTypeString;


    TypeBlock(TypeHeader header, int id) {
        super(header, 2);

        header.setId(id);

        EntryItemOffsetList entryOffsets = new EntryItemOffsetList(header.count());
        this.mEntryArray = new EntryItemList(header, entryOffsets);

        addChild(entryOffsets);
        addChild(mEntryArray);
    }
    public TypeBlock(int id) {
        this(new TypeHeader(), id);
    }
    public TypeBlock() {
        this(0);
    }

    public Iterator<ValueItem> visitValues() {
        return new IterableIterator<Entry, ValueItem>(iterator()) {
            @Override
            public Iterator<ValueItem> iterator(Entry element) {
                return element.visitValues();
            }
        };
    }
    public boolean isTypeAttr() {
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.isTypeAttr();
        }
        return false;
    }
    public boolean isTypeId() {
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.isTypeId();
        }
        return false;
    }
    public String buildUniqueDirectoryName() {
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock != null && packageBlock.hasValidTypeNames()) {
            return getTypeName() + getResConfig().getQualifiers();
        }
        return "type_" + HexUtil.toHex(getId(), 2)
                + getResConfig().getQualifiers();
    }
    public boolean isSparse() {
        return getHeaderBlock().isSparse();
    }
    public boolean isOffset16() {
        return getHeaderBlock().isOffset16();
    }
    public void destroy() {
        getEntryArray().destroy();
        setId(0);
        setParent(null);
    }
    public boolean removeNullEntries(int startId) {
        startId = 0xffff & startId;
        EntryItemList entryArray = getEntryArray();
        entryArray.removeAllNull(startId);
        return entryArray.size() == startId;
    }
    public PackageBlock getPackageBlock() {
        SpecTypePair specTypePair = getParent(SpecTypePair.class);
        if (specTypePair != null) {
            return specTypePair.getPackageBlock();
        }
        return null;
    }
    public String getTypeName() {
        TypeString typeString = getTypeString();
        if (typeString != null) {
            return typeString.get();
        }
        return null;
    }
    public TypeString getTypeString() {
        if (mTypeString != null) {
            if (mTypeString.getId() == getId()) {
                return mTypeString;
            }
            mTypeString=null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        TypeStringPool typeStringPool = packageBlock.getTypeStringPool();
        mTypeString = typeStringPool.getById(getId());
        return mTypeString;
    }
    public int getId() {
        return getHeaderBlock().getId();
    }
    public void setId(int id) {
        getHeaderBlock().setId(id);
    }
    public void setTypeName(String name) {
        TypeStringPool typeStringPool = getTypeStringPool();
        int id = getId();
        TypeString typeString = typeStringPool.getById(id);
        if (typeString == null) {
            typeString = typeStringPool.getOrCreate(id, name);
        }
        typeString.set(name);
    }
    private TypeStringPool getTypeStringPool() {
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock != null) {
            return packageBlock.getTypeStringPool();
        }
        return ObjectsUtil.cast(null);
    }
    public void setEntryCount(int count) {
        IntegerReference entryCount = getHeaderBlock().count();
        if (count == entryCount.get()) {
            return;
        }
        entryCount.set(count);
        onSetEntryCount(count);
    }
    public boolean isBlank() {
        return getEntryArray().isBlank();
    }
    public boolean isDefault() {
        return getResConfig().isDefault();
    }
    public String getQualifiers() {
        return getResConfig().getQualifiers();
    }
    public void setQualifiers(String qualifiers) {
        getResConfig().parseQualifiers(qualifiers);
    }
    public SpecTypePair getParentSpecTypePair() {
        return getParent(SpecTypePair.class);
    }
    public Entry getOrCreateDefinedEntry(String name) {
        Entry entry = getEntry(name);
        if (entry != null) {
            return entry;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        int id = packageBlock.resolveResourceId(getId(), name);
        if (id == 0) {
            return null;
        }
        SpecStringPool stringPool = packageBlock.getSpecStringPool();
        SpecString specString = stringPool.getOrCreate(name);
        entry = getOrCreateEntry(id & 0xffff);
        entry.setSpecReference(specString);
        return entry;
    }
    public Entry getOrCreateEntry(String name) {
        if (name == null) {
            return null;
        }
        Entry entry = getEntry(name);
        if (entry != null) {
            return entry;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock == null) {
            return null;
        }
        int id = packageBlock.resolveResourceId(getId(), name);
        if (id != 0) {
            id = id & 0xffff;
        } else {
            id = getParentSpecTypePair().getHighestEntryId() + 1;
        }

        SpecStringPool stringPool = packageBlock.getSpecStringPool();
        SpecString specString = stringPool.getOrCreate(name);
        entry = getOrCreateEntry(id & 0xffff);
        entry.setSpecReference(specString);
        return entry;
    }
    public Entry getOrCreateEntry(int entryId) {
        return getEntryArray().getOrCreate(entryId);
    }
    public int realSize() {
        return getEntryArray().countNonNull();
    }
    public int size() {
        return getEntryArray().size();
    }
    @Override
    public Iterator<Entry> iterator() {
        return getEntryArray().iterator(false);
    }
    public void clear() {
        getEntryArray().clear();
    }
    /**
     * It is allowed to have duplicate entry name therefore it is not recommend to use this.
     */
    public Entry getEntry(String entryName) {
        return getEntryArray().getEntry(entryName);
    }
    public Boolean hasComplexEntry() {
        SpecTypePair specTypePair = getParentSpecTypePair();
        if (specTypePair != null) {
            return specTypePair.hasComplexEntry();
        }
        return null;
    }
    public ResConfig getResConfig() {
        return getHeaderBlock().getConfig();
    }
    public EntryItemList getEntryArray() {
        return mEntryArray;
    }
    public void ensureEntriesCount(int count) {
        getEntryArray().ensureSize(count);
    }
    public List<Entry> listEntries(boolean skipNullBlock) {
        return CollectionUtil.toList(getEntryArray().iterator(skipNullBlock));
    }
    public Entry getEntry(int entryId) {
        return getEntryArray().getEntry(entryId);
    }

    private void onSetEntryCount(int count) {
        getEntryArray().setSize(count);
    }
    @Override
    protected void onChunkRefreshed() {
    }
    @Override
    protected void onPreRefresh() {
        getResConfig().refresh();
        super.onPreRefresh();
    }
     // method Block.addBytes is inefficient for large size byte array
     // so let's override here because this block is the largest
    @Override
    public byte[] getBytes() {
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
        if (isSparse()) {
            jsonObject.put(NAME_is_sparse, true);
        }
        if (isOffset16()) {
            jsonObject.put(NAME_is_offset16, true);
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
        if (name != null) {
            setTypeName(name);
        }
        if (isBlank()) {
            getHeaderBlock().setOffsetType(
                    json.optBoolean(NAME_is_sparse, false),
                    json.optBoolean(NAME_is_offset16, false));
        }
        getResConfig().fromJson(json.getJSONObject(NAME_config));
        getEntryArray().fromJson(json.getJSONArray(NAME_entries));
    }
    @Override
    public void merge(TypeBlock typeBlock) {
        if (typeBlock == null || typeBlock == this) {
            return;
        }
        if (getId() != typeBlock.getId()) {
            throw new IllegalArgumentException("Can not merge different id types: "
                    + getId() + "!=" + typeBlock.getId());
        }
        setTypeName(typeBlock.getTypeName());
        getResConfig().merge(typeBlock.getResConfig());
        if (this.isBlank()) {
            this.getHeaderBlock().setOffsetType(
                    typeBlock.getHeaderBlock().getOffsetType());
        }
        getEntryArray().merge(typeBlock.getEntryArray());
    }
    @Override
    public int compareTo(TypeBlock typeBlock) {
        if (typeBlock == this) {
            return 0;
        }
        int i = CompareUtil.compare(this.getId(), typeBlock.getId());
        if (i == 0) {
            i = CompareUtil.compare(this.getQualifiers(), typeBlock.getQualifiers());
            if (i == 0) {
                i = CompareUtil.compare(this.isSparse(), typeBlock.isSparse());
            }
        }
        return i;
    }
    public boolean isEqualTypeName(String typeName) {
        return isEqualTypeName(getTypeName(), typeName);
    }
    @Override
    public String toString() {
        return getTypeName() + '{' +  getHeaderBlock() + '}';
    }

    public static boolean canHaveResourceFile(String typeName) {
        return !isEqualTypeName("string", typeName);
    }
    public static boolean isEqualTypeName(String name1, String name2) {
        if (name1 == null) {
            return name2 == null;
        }
        if (name2 == null) {
            return false;
        }
        if (name1.equals(name2)) {
            return true;
        }
        return trimTypeName(name1).equals(trimTypeName(name2));
    }
    private static String trimTypeName(String typeName) {
        while (typeName.length() > 0 && isWildTypeNamePrefix(typeName.charAt(0))) {
            typeName = typeName.substring(1);
        }
        return typeName;
    }
    private static boolean isWildTypeNamePrefix(char ch) {
        switch (ch) {
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
    public static final String NAME_is_offset16 = "is_offset16";
}
