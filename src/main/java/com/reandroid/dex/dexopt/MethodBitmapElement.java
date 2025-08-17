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

import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyReference;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

public class MethodBitmapElement implements LinkableProfileItem,
        KeyReference, JSONConvert<JSONObject> {

    private final BooleanBit startup;
    private final BooleanBit postStartup;

    public MethodBitmapElement(BooleanBit startup, BooleanBit postStartup) {
        this.startup = startup;
        this.postStartup = postStartup;
    }

    public int getIdx() {
        return startup.getIndex();
    }
    public void setIdx(int idx) {
        if (idx == getIdx()) {
            return;
        }
        MethodBitmap bitmap = getParentBitmap();
        if (bitmap != null) {
            bitmap.moveTo(this, idx);
        }
    }

    @Override
    public MethodKey getKey() {
        return (MethodKey) startup.getTag();
    }
    @Override
    public void setKey(Key key) {
        startup.setTag(key);
    }

    public BooleanBit startup() {
        return startup;
    }
    public BooleanBit postStartup() {
        return postStartup;
    }
    public boolean isStartup() {
        return startup.get();
    }
    public void setStartup(boolean value) {
        startup.set(value);
    }
    public boolean isPostStartup() {
        return postStartup.get();
    }
    public void setPostStartup(boolean value) {
        postStartup.set(value);
    }
    public int getFlags() {
        int result = 0;
        if (isStartup()) {
            result |= MethodEncodingType.STARTUP.flag();
        }
        if (isPostStartup()) {
            result |= MethodEncodingType.POST_STARTUP.flag();
        }
        return result;
    }
    public void removeSelf() {
        MethodBitmap bitmap = getParentBitmap();
        if (bitmap != null) {
            bitmap.remove(this);
        }
    }

    public boolean isInvalid() {
        Boolean invalid = (Boolean) postStartup.getTag();
        if (invalid != null) {
            return invalid;
        }
        return false;
    }
    private void setInvalid(boolean invalid) {
        postStartup.setTag(invalid ? Boolean.TRUE : null);
    }
    private MethodBitmap getParentBitmap() {
        return startup.getParentInstance(MethodBitmap.class);
    }


    @Override
    public void link(DexFile dexFile) {
        MethodId methodId = dexFile.getItem(SectionType.METHOD_ID, getIdx());
        MethodKey key;
        boolean invalid;
        if (methodId != null) {
            key = methodId.getKey();
            invalid = false;
        } else {
            key = null;
            invalid = true;
        }
        setKey(key);
        setInvalid(invalid);
    }

    @Override
    public void update(DexFile dexFile) {
        MethodId methodId = dexFile.getItem(SectionType.METHOD_ID, getKey());
        boolean invalid;
        if (methodId != null) {
            setIdx(methodId.getIdx());
            invalid = false;
        } else {
            invalid = true;
        }
        setInvalid(invalid);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getIdx());
        if (isStartup()) {
            jsonObject.put("startup", true);
        }
        if (isPostStartup()) {
            jsonObject.put("post_startup", true);
        }
        MethodKey key = getKey();
        if (key != null) {
            jsonObject.put("key", key.toString());
        }
        if (isInvalid()) {
            jsonObject.put("invalid", true);
        }
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        setKey(MethodKey.parse(json.optString("key")));
        setStartup(json.optBoolean("startup", false));
        setPostStartup(json.optBoolean("post_startup", false));
        setInvalid(json.optBoolean("invalid", false));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MethodBitmapElement element = (MethodBitmapElement) obj;
        return this.startup == element.startup;
    }

    @Override
    public int hashCode() {
        return getIdx();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isInvalid()) {
            builder.append("INVALID ");
        }
        builder.append(getIdx());
        builder.append(" (");
        builder.append(isStartup());
        builder.append(", ");
        builder.append(isPostStartup());
        builder.append(")");
        MethodKey key = getKey();
        if (key != null) {
            builder.append(", ");
            builder.append(key);
        }
        return builder.toString();
    }
}
