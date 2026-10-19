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
package com.reandroid.arsc.item;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.common.Mergeable;
import com.reandroid.arsc.chunk.SpecBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.value.Entry;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class SpecFlagsArray extends IntegerArrayBlock implements BlockRefresh,
        Mergeable<SpecFlagsArray>, JSONConvert<JSONArray> {
    
    private final IntegerReference entryCount;
    
    public SpecFlagsArray(IntegerReference entryCount) {
        super();
        this.entryCount = entryCount;
    }

    public SpecFlag getFlag(int id) {
        id = id & 0xffff;
        if (id >= size()) {
            return null;
        }
        return new SpecFlag(this, id);
    }
    public SpecFlag getOrCreateFlag(int id) {
        id = id & 0xffff;
        ensureArraySize(id + 1);
        return new SpecFlag(this, id);
    }
    public void set(int id, int value) {
        id = 0xffff & id;
        ensureArraySize(id + 1);
        super.put(id, value);
    }
    @Override
    public int get(int entryId) {
        return super.get(entryId & 0xffff);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        setSize(entryCount.get());
        super.onReadBytes(reader);
    }

    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        setSize(entryCount.get());
        return super.readBytes(inputStream);
    }

    @Override
    protected void onLengthChanged(int length) {
        super.onLengthChanged(length);
        length = length / 4;
        if (length != entryCount.get()) {
            entryCount.set(length);
        }
    }

    @Override
    public void refresh() {
        entryCount.set(size());
    }

    @Override
    public void merge(SpecFlagsArray specFlagsArray) {
        if (specFlagsArray == null || specFlagsArray == this) {
            return;
        }
        int size = specFlagsArray.size();
        if (this.size() == 0) {
            setBytes(specFlagsArray);
        } else {
            this.ensureArraySize(size);
            for (int i = 0; i < size; i++) {
                int flag = specFlagsArray.get(i);
                if (flag != 0) {
                    put(i,this.get(i) | flag);
                }
            }
        }
    }

    @Override
    public JSONArray toJson() {
        int size = size();
        if (size == 0) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(size);
        for (int i = 0; i < size; i++) {
            int value = get(i);
            if (value == 0) {
                continue;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Entry.NAME_id, i);
            jsonObject.put(SpecBlock.NAME_flag, value);
            jsonArray.put(jsonObject);
        }
        if (jsonArray.length() == 0) {
            return null;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        if (json == null || json.length() == 0) {
            clear();
            return;
        }
        int length = json.length();
        length = length - 1;
        // to minimise calling new array creation during ensureSize,
        // start from last entry
        flagFromJson(json.getJSONObject(length));

        for (int i = 0; i < length; i++) {
            flagFromJson(json.getJSONObject(i));
        }
    }
    private void flagFromJson(JSONObject jsonObject) {
        set(jsonObject.getInt(Entry.NAME_id),
                jsonObject.getInt(SpecBlock.NAME_flag));
    }
}
