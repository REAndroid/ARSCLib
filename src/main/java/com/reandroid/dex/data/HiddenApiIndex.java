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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.IndirectInteger;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.id.ClassId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.TypeKey;


class HiddenApiIndex extends BlockItem implements Comparable<HiddenApiIndex>, BlockRefresh {

    private final IntegerReference dataOffset;

    private final ClassId classId;
    private HiddenApiData hiddenApiData;

    public HiddenApiIndex(ClassId classId) {
        super(4);
        this.dataOffset = new IndirectInteger(this, 0);
        this.classId = classId;
    }

    public HiddenApiFlagValue get(Key key) {
        HiddenApiData hiddenApiData = getHiddenApiData();
        if(hiddenApiData != null) {
            return hiddenApiData.get(getClassId().getDef(key));
        }
        return null;
    }
    public IntegerReference getDataOffset() {
        return dataOffset;
    }
    public boolean hasValidDataOffset(){
        return dataOffset.get() != 0;
    }

    public HiddenApiData getHiddenApiData() {
        return hiddenApiData;
    }
    void linkData(HiddenApiData hiddenApiData) {
        this.hiddenApiData = hiddenApiData;
        hiddenApiData.setOffsetReference(getDataOffset());
        hiddenApiData.setClassId(getClassId());
    }
    boolean isAllNoRestrictions() {
        HiddenApiData apiData = getHiddenApiData();
        return apiData == null || apiData.isAllNoRestrictions();
    }

    public TypeKey getClassType(){
        ClassId classId = getClassId();
        if(classId != null){
            return classId.getKey();
        }
        return null;
    }
    public ClassId getClassId() {
        return classId;
    }

    public void removeSelf(){
        HiddenApiIndexList parent = getParentInstance(HiddenApiIndexList.class);
        if(parent != null){
            parent.remove(this);
        }
    }

    @Override
    public boolean isNull() {
        ClassId classId = getClassId();
        return classId == null || classId.isRemoved();
    }

    @Override
    public void refresh() {
        ClassId classId = getClassId();
        if(classId.isRemoved()){
            removeSelf();
            return;
        }
        HiddenApiData apiData = getHiddenApiData();
        int offset;
        if(apiData == null){
            offset = 0;
        }else {
            offset = apiData.getOffset();
        }
        dataOffset.set(offset);
    }
    @Override
    public int compareTo(HiddenApiIndex hiddenApiIndex) {
        if(hiddenApiIndex == null){
            return -1;
        }
        return SectionTool.compareIdx(getClassId(), hiddenApiIndex.getClassId());
    }

    @Override
    public String toString() {
        return getClassType() + " {" + getDataOffset() + "}";
    }

}
