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
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;

public class InlineCachePcMap extends FixedBlockContainer
        implements LinkableProfileItem, JSONConvert<JSONObject> {

    private final IntegerReference profileIndex;
    private final ProfileClassList classList;

    public InlineCachePcMap() {
        super(3);
        this.profileIndex = new ByteItem();
        ByteItem numClasses = new ByteItem();
        this.classList = new ProfileClassList(numClasses);

        addChild(0, (Block) profileIndex);
        addChild(1, numClasses);
        addChild(2, classList);
    }

    public IntegerReference profileIndex() {
        return profileIndex;
    }
    public ProfileClassList classList() {
        return classList;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public void link(DexFile dexFile) {
        classList().link(dexFile);
    }
    @Override
    public void update(DexFile dexFile) {
        classList().update(dexFile);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("profile_index", profileIndex().get());
        jsonObject.put("classes", classList().toJson());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        profileIndex().set(json.getInt("profile_index"));
        classList().fromJson(json.optJSONArray("classes"));
    }
    @Override
    public String toString() {
        return "InlineCachePcMap{" +
                "profileIndex=" + profileIndex +
                ", classList=" + classList +
                '}';
    }

    public static final Creator<InlineCachePcMap> CREATOR = InlineCachePcMap::new;
}
