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
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;

public class InlineCache extends FixedBlockContainer
        implements LinkableProfileItem, JSONConvert<JSONObject> {

    private final IntegerReference dexPc;
    private final IntegerReference dexPcMapSize;
    private final BlockList<InlineCachePcMap> pcMapList;

    public InlineCache() {
        super(3);
        this.dexPc = new ShortItem();
        IntegerReference dexPcMapSize = new ByteItem();
        this.dexPcMapSize = dexPcMapSize;
        IntegerReference count = new IntegerReference() {
            @Override
            public int get() {
                int i = dexPcMapSize.get();
                if (!MethodEncodingType.isPcMapSize(i)) {
                    i = 0;
                }
                return i;
            }
            @Override
            public void set(int value) {
                dexPcMapSize.set(value);
            }
            @Override
            public String toString() {
                return Integer.toString(get());
            }
        };

        this.pcMapList = new SizedBlockList<>(count, InlineCachePcMap.CREATOR);

        addChild(0, (Block) dexPc);
        addChild(1, (Block) dexPcMapSize);
        addChild(2, pcMapList);
    }

    public IntegerReference dexPc() {
        return dexPc;
    }
    public BlockList<InlineCachePcMap> pcMapList() {
        return pcMapList;
    }
    public MethodEncodingType getEncodingType() {
        return MethodEncodingType.valueOf(dexPcMapSize.get());
    }
    public void setEncodingType(MethodEncodingType type) {
        if (type != null) {
            dexPcMapSize.set(type.flag());
        }
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public void link(DexFile dexFile) {
        LinkableProfileItem.linkAll(dexFile, pcMapList.iterator());
    }
    @Override
    public void update(DexFile dexFile) {
        LinkableProfileItem.updateAll(dexFile, pcMapList.iterator());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pc", dexPc().get());
        MethodEncodingType type = getEncodingType();
        if (type != null) {
            jsonObject.put("type", type.name());
        }
        jsonObject.put("pc_maps", BlockList.toJsonArray(pcMapList()));
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        dexPc().set(json.getInt("pc"));
        setEncodingType(MethodEncodingType.valueOf(json.optString("type")));
        BlockList.fromJsonArray(pcMapList(), json.optJSONArray("pc_maps"));
    }

    @Override
    public String toString() {
        return "InlineCache{" +
                "dexPc=" + dexPc +
                "type=" + getEncodingType() +
                ", pcMapList=" + pcMapList +
                '}';
    }

    public static final Creator<InlineCache> CREATOR = InlineCache::new;


}
