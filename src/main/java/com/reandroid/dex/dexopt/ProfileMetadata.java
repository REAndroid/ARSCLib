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
import com.reandroid.arsc.item.StringReference;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONObject;

import java.io.IOException;


public abstract class ProfileMetadata extends FixedBlockContainer implements ProfileData {

    private boolean initialized;

    public ProfileMetadata(int childesCount) {
        super(childesCount);
    }

    public abstract ProfileClassList classList();
    public abstract StringReference name();

    @Override
    public String getName() {
        return name().get();
    }
    @Override
    public void setName(String name) {
        this.name().set(name);
    }
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
        classList().link(dexFile);
    }
    @Override
    public void update(DexFile dexFile) {
        classList().update(dexFile);
        setInitialized(true);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", getName());
        jsonObject.put("classes", classList().toJson());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        name().set(json.getString("name"));
        classList().fromJson(json.optJSONArray("classes"));
        setInitialized(true);
    }

    @Override
    public String toString() {
        return "name=" + getName() +
                ", classes=" + classList();
    }
}
