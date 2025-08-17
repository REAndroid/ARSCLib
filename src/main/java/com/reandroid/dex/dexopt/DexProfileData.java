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

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public abstract class DexProfileData extends FixedBlockContainer implements ProfileData {

    private boolean initialized;

    public DexProfileData(int childesCount) {
        super(childesCount);
    }

    public abstract long getChecksum();
    public abstract void setChecksum(long crc32);

    public abstract HotMethodRegionList hotMethodList();
    public abstract ProfileClassList classList();
    public abstract MethodBitmap methodBitmap();

    @Override
    public boolean isInitialized() {
        return initialized;
    }
    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        setInitialized(true);
    }

    @Override
    public void link(DexFile dexFile) {
        hotMethodList().link(dexFile);
        classList().link(dexFile);
        methodBitmap().link(dexFile);
    }
    @Override
    public void update(DexFile dexFile) {
        hotMethodList().update(dexFile);
        classList().update(dexFile);
        methodBitmap().update(dexFile, isInitialized());
        setInitialized(true);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", getName());
        jsonObject.put("checksum", getChecksum());
        jsonObject.put("hot_methods", hotMethodList().toJson());
        jsonObject.put("classes", classList().toJson());
        jsonObject.put("method_ids", methodBitmap().size());
        jsonObject.put("method_bitmap", methodBitmap().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setName(json.getString("name"));
        setChecksum(json.getLong("checksum"));
        hotMethodList().fromJson(json.optJSONArray("hot_methods"));
        classList().fromJson(json.optJSONArray("classes"));
        methodBitmap().setSize(json.getInt("method_ids"));
        methodBitmap().fromJson(json.optJSONArray("method_bitmap"));
        setInitialized(true);
    }
    @Override
    public String toString() {
        return "name=" + getName() +
                ", checksum=" + HexUtil.toHex(getChecksum(), 8) +
                ", methodList=" + hotMethodList() +
                ", classList=" + classList() +
                ", bitmap=" + methodBitmap();
    }
}
