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
package com.reandroid.dex.model;

import com.reandroid.dex.sections.MapItem;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;

public class DexSectionInfo {

    private final DexLayout dexLayout;
    private final MapItem mapItem;

    public DexSectionInfo(DexLayout dexLayout, MapItem mapItem) {
        this.dexLayout = dexLayout;
        this.mapItem = mapItem;
    }

    public DexLayout getDexLayout() {
        return dexLayout;
    }

    private MapItem getMapItem() {
        return mapItem;
    }
    public int getIndex() {
        return getMapItem().getIndex();
    }
    public int getType() {
        return getMapItem().getType().get();
    }
    public SectionType<?> getSectionType() {
        return getMapItem().getSectionType();
    }
    public int getCount() {
        return getMapItem().getCountValue();
    }
    public int getOffset() {
        return getMapItem().getOffsetValue();
    }
    public String getName() {
        SectionType<?> sectionType = getSectionType();
        if (sectionType != null) {
            return sectionType.getName();
        }
        return HexUtil.toHex("UNKNOWN_", getType(), 1);
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("index", getIndex());
        jsonObject.put("name", getName());
        jsonObject.put("count", getCount());
        jsonObject.put("offset", getOffset());
        return jsonObject;
    }
    public String print(boolean hex) {
        StringBuilder builder = new StringBuilder();
        String name = getName();
        builder.append(name);
        builder.append(' ');
        int fill = 24 - name.length();
        for(int i = 0; i < fill; i++){
            builder.append('-');
        }
        builder.append("[");
        String num;
        int pad;
        if (hex) {
            num = HexUtil.toHex(getCount(), 1);
            pad = 8;
        } else {
            num = Integer.toString(getCount());
            pad = 6;
        }
        builder.append(num);
        fill = pad - num.length();
        for (int i = 0; i < fill; i++) {
            builder.append(' ');
        }
        builder.append(',');
        builder.append(' ');
        if (hex) {
            num = HexUtil.toHex(getOffset(), 8);
            pad = 10;
        } else {
            num = Integer.toString(getOffset());
            pad = 8;
        }
        fill = pad - num.length();
        for (int i = 0; i < fill; i++) {
            builder.append(' ');
        }
        builder.append(num);
        builder.append(']');
        return builder.toString();
    }

    @Override
    public String toString() {
        return print(false);
    }
}
