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
package com.reandroid.dex.index;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyItemCreate;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class StringId extends IdSectionEntry implements IntegerReference, Comparable<StringId>, KeyItemCreate {

    private StringData stringData;

    public StringId(){
        super(4);
    }

    @Override
    public StringKey getKey(){
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getKey();
        }
        return null;
    }
    @Override
    public void setKey(Key key){
        StringData stringData = getStringData();
        if(stringData == null){
            stringData = getOrCreateSection(SectionType.STRING_DATA).createItem();
            linkStringData(stringData);
        }
        stringData.setKey(key);
    }
    @Override
    public void removeSelf() {
        StringData stringData = this.stringData;
        if(stringData != null){
            int usage = stringData.getUsageType();
            stringData.clearUsageType();
            usage = usage >>> 1;
            stringData.addUsageType(usage);
            if(usage == 0){
                stringData.removeSelf(this);
            }
        }
        super.removeSelf();
        this.stringData = null;
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
        int usage = stringData.getUsageType();
        stringData.addUsageType((usage << 1) | 1);
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
        set(stringData.getOffset());
    }
    @Override
    void cacheItems() {

    }
    public String getQuotedString(){
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getQuotedString();
        }
        return null;
    }
    public String getString() {
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        StringData stringData = getStringData();
        if(stringData != null){
            stringData.append(writer);
        }
    }
    @Override
    public int compareTo(StringId stringId) {
        if(stringId == null){
            return -1;
        }
        if(stringId == this){
            return 0;
        }
        return Integer.compare(getStringData().getIndex(), stringId.getStringData().getIndex());
    }

    @Override
    public String toString() {
        StringData stringData = this.stringData;
        if(stringData != null){
            return stringData.toString();
        }
        return Integer.toString(get());
    }

    public static boolean equals(StringId stringId1, StringId stringId2) {
        if(stringId1 == stringId2) {
            return true;
        }
        if(stringId1 == null) {
            return false;
        }
        return StringData.equals(stringId1.getStringData(), stringId2.getStringData());
    }
}
