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
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;

import java.util.Iterator;
import java.util.function.Predicate;

public abstract class ProfileFile extends FixedBlockContainer
        implements LinkableProfileItem, JSONConvert<JSONObject> {

    public ProfileFile(int childesCount) {
        super(childesCount);
    }

    public abstract ProfileMagic magic();
    public abstract ProfileVersion version();
    public abstract ProfileBody body();

    public ProfileData get(String name) {
        return body().get(name);
    }
    public Iterator<? extends ProfileData> iterator() {
        return body().iterator();
    }
    public boolean removeIfName(Predicate<String> predicate) {
        return body().removeIfName(predicate);
    }
    public boolean removeData(String name) {
        return body().removeData(name);
    }

    @Override
    public void link(DexFile dexFile) {
        body().link(dexFile);
    }
    @Override
    public void update(DexFile dexFile) {
        body().update(dexFile);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("magic", magic().get());
        jsonObject.put("version", version().name());
        jsonObject.put("body", body().toJson());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) {
        magic().set(json.getInt("magic"));
        version().name(json.getString("version"));
        body().fromJson(json.getJSONArray("body"));
    }

    @Override
    public String toString() {
        return "magic=" + magic() +
                ", version=" + version() +
                ", body=" + body() ;
    }

    public static final String PATH_PROF = ObjectsUtil.of("assets/dexopt/baseline.prof");
    public static final String PATH_PROFM = ObjectsUtil.of("assets/dexopt/baseline.profm");
}
