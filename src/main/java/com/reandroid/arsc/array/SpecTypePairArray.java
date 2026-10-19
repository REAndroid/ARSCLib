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
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.common.Mergeable;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;

import java.util.HashMap;
import java.util.Map;

public class SpecTypePairArray extends BlockArray<SpecTypePair>
        implements Mergeable<SpecTypePairArray>, JSONConvert<JSONArray> {

    private final int[] idMap;

    public SpecTypePairArray() {
        super();
        this.idMap = new int[0xff];
    }

    public void trimConfigSizes(int resConfigSize) {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).trimConfigSize(resConfigSize);
        }
    }
    public void sort() {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).sortTypes();
        }
        sortSpecs();
    }
    public void sortSpecs() {
        if (needsSort(CompareUtil.getComparableComparator())) {
            sort(CompareUtil.getComparableComparator());
        }
    }

    public void removeBlank() {
        removeIf(SpecTypePair::removeBlank);
    }
    public boolean isBlank() {
        int size = size();
        for (int i = 0; i < size; i++) {
            SpecTypePair typePair = get(i);
            if (!typePair.isBlank()) {
                return false;
            }
        }
        return true;
    }
    public Entry getOrCreateEntry(int typeId, int entryId, String qualifiers) {
        return getOrCreateTypeBlock(typeId, qualifiers).getOrCreateEntry(entryId);
    }
    public Entry getOrCreateEntry(int typeId, int entryId, ResConfig resConfig) {
        return getOrCreateTypeBlock(typeId, resConfig).getOrCreateEntry(entryId);
    }
    public Entry getEntry(int typeId, int entryId, String qualifiers) {
        TypeBlock typeBlock = getTypeBlock(typeId, qualifiers);
        if (typeBlock != null) {
            return typeBlock.getEntry(entryId);
        }
        return null;
    }
    public Entry getEntry(ResConfig resConfig, int typeId, int entryId) {
        SpecTypePair specTypePair = getSpecTypePair(typeId);
        if (specTypePair != null) {
            return specTypePair.getEntry(resConfig, entryId);
        }
        return null;
    }
    public TypeBlock getOrCreateTypeBlock(int typeId, String qualifiers) {
        return getOrCreate(typeId).getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(int typeId, ResConfig resConfig) {
        return getOrCreate(typeId).getOrCreateTypeBlock(resConfig);
    }
    public TypeBlock getTypeBlock(int typeId, String qualifiers) {
        SpecTypePair pair = getSpecTypePair(typeId);
        if (pair != null) {
            return pair.getTypeBlock(qualifiers);
        }
        return null;
    }
    public TypeBlock getOrCreate(int typeId, ResConfig resConfig) {
        return getOrCreate(typeId).getTypeBlockArray().getOrCreate(resConfig);
    }
    public SpecTypePair getOrCreate(int typeId) {
        SpecTypePair pair = getSpecTypePair(typeId);
        if (pair == null) {
            pair = createNext();
            pair.setId(typeId);
            if (typeId > 0 && typeId <= 0xff) {
                idMap[typeId - 1] = pair.getIndex();
            }
        }
        return pair;
    }
    public SpecTypePair getOrCreate(String typeName) {
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if (specTypePair != null) {
            return specTypePair;
        }
        TypeString typeString = getOrCreateTypeString(typeName);
        int id = typeString.getId();
        specTypePair = createNext();
        specTypePair.setId(id);
        idMap[id - 1] = specTypePair.getIndex();
        return specTypePair;
    }
    public TypeBlock getOrCreateTypeBlock(String typeName, ResConfig resConfig) {
        return getOrCreate(typeName).getOrCreateTypeBlock(resConfig);
    }
    public TypeBlock getOrCreateTypeBlock(String typeName, String qualifiers) {
        return getOrCreate(typeName).getOrCreateTypeBlock(qualifiers);
    }
    public SpecTypePair getSpecTypePair(int typeId) {
        if (typeId <= 0 || typeId > 0xff) {
            return null;
        }
        int[] idMap = this.idMap;
        SpecTypePair specTypePair = get(idMap[typeId - 1]);
        if (specTypePair != null && specTypePair.getId() == typeId) {
            return specTypePair;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            specTypePair = get(i);
            int id = specTypePair.getId();
            if (id != 0) {
                idMap[id - 1] = specTypePair.getIndex();
            }
            if (typeId == id) {
                return specTypePair;
            }
        }
        return null;
    }
    public SpecTypePair getSpecTypePair(String typeName) {
        if (typeName == null) {
            return null;
        }
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock != null) {
            return getSpecTypePair(packageBlock.typeIdOf(typeName));
        }
        return null;
    }
    public Entry getAnyEntry(int typeId, int entryId) {
        if (typeId == 0) {
            return null;
        }
        SpecTypePair specTypePair = getSpecTypePair(typeId);
        if (specTypePair != null) {
            return specTypePair.getAnyEntry(entryId);
        }
        return null;
    }
    public Entry getAnyEntry(String typeName, String entryName) {
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if (specTypePair != null) {
            return specTypePair.getAnyEntry(entryName);
        }
        return null;
    }
    public Entry getEntry(String qualifiers, String typeName, String entryName) {
        ResConfig resConfig = new ResConfig();
        resConfig.parseQualifiers(qualifiers);
        return getEntry(resConfig, typeName, entryName);
    }
    public Entry getEntry(ResConfig resConfig, String typeName, String entryName) {
        SpecTypePair specTypePair = getSpecTypePair(typeName);
        if (specTypePair != null) {
            return specTypePair.getEntry(resConfig, entryName);
        }
        return null;
    }
    @Override
    public SpecTypePair newInstance() {
        return new SpecTypePair();
    }

    @Override
    protected void onRefreshed() {

    }
    @Override
    protected void onPreRefresh() {
        validateEntryCounts();
    }

    private void validateEntryCounts() {
        Map<Integer, Integer> entryCountMap = mapHighestEntryCount();
        for (Map.Entry<Integer, Integer> entry : entryCountMap.entrySet()) {
            getSpecTypePair(entry.getKey())
                    .setEntryCount(entry.getValue());
        }
    }
    private Map<Integer, Integer> mapHighestEntryCount() {
        Map<Integer, Integer> results = new HashMap<>();
        int size = size();
        for (int i = 0; i < size; i++) {
            SpecTypePair typePair = get(i);
            int count = typePair.getHighestEntryCount();
            int id = typePair.getId();
            Integer exist = results.get(id);
            if (exist == null || count > exist) {
                results.put(id, count);
            }
        }
        return results;
    }
    private TypeString getOrCreateTypeString(String typeName) {
        TypeStringPool typeStringPool = getTypeStringPool();
        if (typeStringPool == null) {
            return null;
        }
        TypeString typeString = typeStringPool.getString(typeName);
        if (typeString != null) {
            return typeString;
        }
        int id = typeStringPool.getLastId() + 1;
        return typeStringPool.getOrCreate(id, typeName);
    }
    private TypeStringPool getTypeStringPool() {
        PackageBlock packageBlock = getPackageBlock();
        if (packageBlock != null) {
            return packageBlock.getTypeStringPool();
        }
        return null;
    }
    private PackageBlock getPackageBlock() {
        return getParentInstance(PackageBlock.class);
    }

    public boolean isCompatibleTo(SpecTypePairArray pairArray) {
        if (pairArray == null) {
            return false;
        }
        if (isBlank() || pairArray.isBlank() || equals(pairArray)) {
            return true;
        }
        int size = pairArray.size();
        for (int i = 0; i < size; i++) {
            SpecTypePair typePair = pairArray.get(i);
            SpecTypePair compId = getSpecTypePair(typePair.getId());
            if (compId != null && !typePair.isCompatibleTo(compId)) {
                return false;
            }
            SpecTypePair compName = getSpecTypePair(typePair.getTypeName());
            if (compName != null && compName != compId && !typePair.isCompatibleTo(compName)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public JSONArray toJson() {
        return toJson(false);
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json == null) {
            return;
        }
        int length = json.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = json.getJSONObject(i);
            getOrCreate(jsonObject.getInt(TypeBlock.NAME_id))
                    .fromJson(jsonObject);
        }
    }
    public JSONArray toJson(boolean specsOnly) {
        int size = size();
        JSONArray jsonArray = new JSONArray(size);
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = get(i).toJson(specsOnly);
            if (jsonObject != null) {
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }
    @Override
    public void merge(SpecTypePairArray pairArray) {
        if (pairArray == null || pairArray == this) {
            return;
        }
        int size = pairArray.size();
        for (int i = 0; i < size; i++) {
            SpecTypePair typePair = pairArray.get(i);
            if (!typePair.isBlank()) {
                getOrCreate(typePair.getId()).merge(typePair);
            }
        }
    }
}
