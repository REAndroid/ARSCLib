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
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.item.StringReference;
import com.reandroid.dex.model.DexFile;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.json.JSONObject;


public class ProfileMetadataV2 extends ProfileMetadata {

    private final IntegerReference profileIndex;
    private final StringReference name;
    private final IntegerReference typeIdCount;
    private final ProfileClassList classList;

    public ProfileMetadataV2() {
        super(6);

        profileIndex = new ShortItem();
        ShortItem dexNameSize = new ShortItem();
        this.name = new ProfString(dexNameSize);

        this.typeIdCount = new IntegerItem();
        ShortItem classSetSize = new ShortItem();
        this.classList = new ProfileClassList(classSetSize);

        addChild(0, (Block) profileIndex);
        addChild(1, dexNameSize);
        addChild(2, (Block) name);
        addChild(3, (Block) typeIdCount);
        addChild(4, classSetSize);
        addChild(5, classList);
    }

    public IntegerReference profileIndex() {
        return profileIndex;
    }
    public IntegerReference typeIdCount() {
        return typeIdCount;
    }
    @Override
    public ProfileClassList classList() {
        return classList;
    }
    @Override
    public StringReference name() {
        return name;
    }

    @Override
    public void update(DexFile dexFile) {
        if (!isInitialized()) {
            profileIndex().set(getIndex());
        }
        typeIdCount().set(dexFile.getCount(SectionType.TYPE_ID));
        super.update(dexFile);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = super.toJson();
        jsonObject.put("profile_index", profileIndex().get());
        jsonObject.put("types", typeIdCount().get());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        profileIndex().set(json.getInt("profile_index"));
        typeIdCount().set(json.getInt("types"));
    }

    @Override
    public String toString() {
        return super.toString() +
                ", profileIndex=" + profileIndex() +
                ", types=" + typeIdCount();
    }
}
