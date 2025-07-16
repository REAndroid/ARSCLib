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

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.CompareUtil;

import java.util.Iterator;

public class HotMethodRegion extends FixedBlockContainer implements
        LinkableProfileItem, Comparable<HotMethodRegion>, JSONConvert<JSONObject> {

    private final IntegerReference diffWithLastMethodDexIndex;
    private final BlockList<InlineCache> inlineCacheList;

    private int idx;
    private MethodKey methodKey;
    private boolean invalid;

    public HotMethodRegion() {
        super(3);
        this.diffWithLastMethodDexIndex = new ShortItem();
        ShortItem inlineCacheSize = new ShortItem();
        this.inlineCacheList = new CountedBlockList<>(InlineCache.CREATOR, inlineCacheSize);
        this.idx = -1;

        addChild(0, (Block) diffWithLastMethodDexIndex);
        addChild(1, inlineCacheSize);
        addChild(2, inlineCacheList);
    }

    public MethodKey getKey() {
        return methodKey;
    }
    public void setKey(Key key) {
        this.methodKey = (MethodKey) key;
    }

    public int getIdx() {
        return idx;
    }
    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int size() {
        return inlineCacheList.size();
    }
    public InlineCache get(int i) {
        return inlineCacheList.get(i);
    }
    public Iterator<InlineCache> iterator() {
        return inlineCacheList.iterator();
    }

    public IntegerReference diffWithLastMethodDexIndex() {
        return diffWithLastMethodDexIndex;
    }

    public boolean isInvalid() {
        return invalid;
    }
    @Override
    public void link(DexFile dexFile) {
        MethodId methodId = dexFile.getItem(SectionType.METHOD_ID, getIdx());
        if (methodId != null) {
            setKey(methodId.getKey());
            invalid = false;
        } else {
            invalid = true;
        }
        LinkableProfileItem.linkAll(dexFile, iterator());
    }
    @Override
    public void update(DexFile dexFile) {
        MethodId methodId = dexFile.getItem(SectionType.METHOD_ID, getKey());
        if (methodId != null) {
            setIdx(methodId.getIdx());
            invalid = false;
        } else {
            invalid = true;
        }
        LinkableProfileItem.updateAll(dexFile, iterator());
    }

    @Override
    public int compareTo(HotMethodRegion region) {
        if (region == this) {
            return 0;
        }
        return CompareUtil.compare(getIdx(), region.getIdx());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        if (isInvalid()) {
            jsonObject.put("invalid", true);
        }
        jsonObject.put("id", getIdx());
        MethodKey key = getKey();
        if (key != null) {
            jsonObject.put("key", key.toString());
        }
        jsonObject.put("inline_caches", BlockList.toJsonArray(inlineCacheList));
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        invalid = json.optBoolean("invalid", false);
        setIdx(json.getInt("id"));
        setKey(MethodKey.parse(json.optString("key")));
        BlockList.fromJsonArray(inlineCacheList, json.optJSONArray("inline_caches"));
    }
    @Override
    public String toString() {
        MethodKey key = getKey();
        if (key != null) {
            return getIdx() + " [" + key + "], inlineCacheList=" + inlineCacheList;
        }
        return "HotMethodRegion{" +
                "diffWithLastMethodDexIndex=" + diffWithLastMethodDexIndex +
                ", inlineCacheList=" + inlineCacheList +
                '}';
    }

    public static final Creator<HotMethodRegion> CREATOR = HotMethodRegion::new;
}
