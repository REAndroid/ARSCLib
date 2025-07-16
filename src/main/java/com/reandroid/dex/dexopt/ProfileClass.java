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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;

public class ProfileClass extends ShortItem implements LinkableProfileItem,
        KeyReference, Comparable<ProfileClass>, JSONConvert<JSONObject> {

    private int idx;
    private TypeKey typeKey;
    private boolean invalid;

    public ProfileClass() {
        super();
        this.idx = -1;
    }

    @Override
    public TypeKey getKey() {
        return typeKey;
    }
    @Override
    public void setKey(Key typeKey) {
        this.typeKey = (TypeKey) typeKey;
    }
    public int getIdx() {
        return idx;
    }
    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    public void link(DexFile dexFile) {
        TypeId typeId = dexFile.getItem(SectionType.TYPE_ID, getIdx());
        if (typeId != null) {
            setKey(typeId.getKey());
            invalid = false;
        } else {
            invalid = true;
        }
    }
    @Override
    public void update(DexFile dexFile) {
        TypeId typeId = dexFile.getItem(SectionType.TYPE_ID, getKey());
        if (typeId != null) {
            setIdx(typeId.getIdx());
            invalid = false;
        } else {
            invalid = true;
        }
    }
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    public int compareTo(ProfileClass profileClass) {
        if (profileClass == this) {
            return 0;
        }
        return CompareUtil.compare(getIdx(), profileClass.getIdx());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getIdx());
        TypeKey key = getKey();
        if (key != null) {
            jsonObject.put("key", key.getTypeName());
        }
        if (isInvalid()) {
            jsonObject.put("invalid", true);
        }
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        setIdx(json.optInt("id"));
        setKey(TypeKey.create(json.optString("key")));
        invalid = json.optBoolean("invalid", false);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isInvalid()) {
            builder.append("INVALID ");
        }
        builder.append(getIdx());
        TypeKey key = getKey();
        if (key != null) {
            builder.append(" [");
            builder.append(key);
            builder.append(']');
        }
        return builder.toString();
    }

    public static final Creator<ProfileClass> CREATOR = ProfileClass::new;
}
