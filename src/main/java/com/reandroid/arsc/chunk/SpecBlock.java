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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.array.TypeBlockArray;
import com.reandroid.common.Mergeable;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.header.SpecHeader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.SpecFlag;
import com.reandroid.arsc.item.SpecFlagsArray;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;


public class SpecBlock extends Chunk<SpecHeader> implements
        Mergeable<SpecBlock>, JSONConvert<JSONArray> {

    private final SpecFlagsArray specFlagsArray;

    SpecBlock(SpecHeader header) {
        super(header, 1);

        this.specFlagsArray = new SpecFlagsArray(header.count());
        addChild(specFlagsArray);
    }
    public SpecBlock() {
        this(new SpecHeader());
    }

    public void destroy() {
        setParent(null);
        getSpecFlagsArray().clear();
    }
    public SpecFlag getSpecFlag(int id) {
        return getSpecFlagsArray().getFlag(id);
    }
    public SpecFlag getOrCreateFlag(int id) {
        return getSpecFlagsArray().getOrCreateFlag(id);
    }
    public SpecFlagsArray getSpecFlagsArray() {
        return specFlagsArray;
    }
    public int getId() {
        return getHeaderBlock().getId();
    }
    public void setId(int id) {
        getHeaderBlock().setId(id);
        getTypeBlockArray().setId(id);
    }
    public IntegerReference typesCount() {
        return getHeaderBlock().typesCount();
    }
    public TypeBlockArray getTypeBlockArray() {
        SpecTypePair specTypePair = getSpecTypePair();
        if (specTypePair != null) {
            return specTypePair.getTypeBlockArray();
        }
        return null;
    }
    SpecTypePair getSpecTypePair() {
        return getParent(SpecTypePair.class);
    }
    public int getEntryCount() {
        return specFlagsArray.size();
    }
    public void setEntryCount(int count) {
        specFlagsArray.setSize(count);
    }
    @Override
    protected void onChunkRefreshed() {
        specFlagsArray.refresh();
        refreshTypesCount();
    }
    private void refreshTypesCount() {
        IntegerReference typesCount = typesCount();
        TypeBlockArray typeBlockArray = getTypeBlockArray();
        if (typeBlockArray != null) {
            typesCount.set(typeBlockArray.size());
        }
    }

    @Override
    public void merge(SpecBlock specBlock) {
        if (specBlock == null || specBlock == this) {
            return;
        }
        IntegerReference typesCount = this.typesCount();
        if (typesCount.get() == 0) {
            typesCount.set(specBlock.typesCount().get());
        }
        this.getSpecFlagsArray().merge(specBlock.getSpecFlagsArray());
    }

    @Override
    public JSONArray toJson() {
        return getSpecFlagsArray().toJson();
    }

    @Override
    public void fromJson(JSONArray json) {
        getSpecFlagsArray().fromJson(json);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        TypeBlockArray typeBlockArray = getTypeBlockArray();
        if (typeBlockArray != null) {
            builder.append(", typesCount=");
            builder.append(typeBlockArray.size());
        }
        return builder.toString();
    }

    public enum Flag {

        SPEC_PUBLIC(0x40000000),
        SPEC_STAGED_API(0x20000000);

        private final int flag;

        Flag(int flag) {
            this.flag = flag;
        }

        public int getFlag() {
            return flag;
        }
        public boolean isSet(int flag) {
            return (this.flag & flag) == this.flag;
        }

        public static String toString(int flagValue) {
            StringBuilder builder = new StringBuilder();
            Flag flag = SPEC_PUBLIC;
            if (flag.isSet(flagValue)) {
                builder.append(flag.name());
            }
            flag = SPEC_STAGED_API;
            if (flag.isSet(flagValue)) {
                if (builder.length() != 0) {
                    builder.append('|');
                }
                builder.append(flag.name());
            }
            return builder.toString();
        }
    }

    public static final String NAME_spec = "spec";
    public static final String NAME_spec_flags = "spec_flags";
    public static final String NAME_flag = "flag";
}
