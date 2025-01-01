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
package com.reandroid.dex.id;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class StringId extends IdItem implements IntegerReference, Comparable<StringId> {

    private StringData stringData;

    public StringId(){
        super(4);
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return EmptyIterator.of();
    }
    @Override
    public SectionType<StringId> getSectionType(){
        return SectionType.STRING_ID;
    }
    @Override
    public StringKey getKey() {
        StringData stringData = getStringData();
        if (stringData != null) {
            return stringData.getKey();
        }
        return null;
    }
    @Override
    public void setKey(Key key) {
        requireStringData();
        StringKey oldKey = getKey();
        StringData stringData = getStringData();
        StringKey update = stringData.updateString((StringKey) key);
        if (oldKey != update) {
            keyChanged(oldKey);
        }
    }

    @Override
    public void onRemovedInternal() {
        StringData stringData = this.stringData;
        if (stringData != null) {
            stringData.removeSelf(this);
            this.stringData = null;
        }
    }

    public StringData getStringData() {
        return stringData;
    }
    public void linkStringData(StringData stringData) {
        if(this.stringData == stringData){
            return;
        }
        if(this.stringData != null){
            throw new IllegalArgumentException("String data already linked");
        }
        this.stringData = stringData;
        stringData.setOffsetReference(this);
    }

    @Override
    public void set(int value) {
        putInteger(getBytesInternal(), 0, value);
    }
    @Override
    public int get() {
        return getInteger(getBytesInternal(), 0);
    }
    @Override
    public void refresh() {
        set(this.stringData.getOffset());
    }
    @Override
    void cacheItems() {
    }
    private void requireStringData() {
        StringData stringData = getStringData();
        if (stringData == null) {
            if (isRemoved()) {
                throw new IllegalArgumentException("Removed string id, index = " +
                        getIndex() + ", offset = " + get());
            }
            throw new IllegalArgumentException("Unlinked string id, index = " +
                    getIndex() + ", offset = " + get());
        }
    }
    public String getString() {
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setString(String text) {
        setKey(StringKey.create(text));
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        getStringData().onReadBytes(reader);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        StringData stringData = getStringData();
        if (stringData != null) {
            stringData.append(writer);
        }
    }
    @Override
    public int compareTo(StringId stringId) {
        if(stringId == null){
            return -1;
        }
        if (stringId == this) {
            return 0;
        }
        // compare only index (not offset=idx) bc StringData is already sorted
        return SectionTool.compareIndex(getStringData(), stringId.getStringData());
    }

    @Override
    public String toString() {
        StringData stringData = this.getStringData();
        if (stringData != null) {
            return stringData.toString();
        }
        if (isRemoved()) {
            return "REMOVED";
        }
        return "Unlinked index = " + getIndex() + ", offset = " + get();
    }

    public static boolean equals(StringId stringId1, StringId stringId2) {
        if (stringId1 == stringId2) {
            return true;
        }
        if (stringId1 == null) {
            return false;
        }
        return ObjectsUtil.equals(stringId1.getStringData(), stringId2.getStringData());
    }
}
