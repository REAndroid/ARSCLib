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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.model.DexFile;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;

public class HotMethodRegionList extends SizedBlockList<HotMethodRegion>
        implements LinkableProfileItem, JSONConvert<JSONArray> {

    public HotMethodRegionList(IntegerReference bytesSize) {
        super(bytesSize, HotMethodRegion.CREATOR);
    }

    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }
    public void removeInvalids() {
        removeIf(HotMethodRegion::isInvalid);
    }
    @Override
    public void link(DexFile dexFile) {
        LinkableProfileItem.linkAll(dexFile, iterator());
    }
    @Override
    public void update(DexFile dexFile) {
        LinkableProfileItem.updateAll(dexFile, iterator());
        removeInvalids();
        sort();
        updateIdx();
    }
    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        initIdx();
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        sort();
        updateIdx();
    }

    private void initIdx() {
        int lastId = 0;
        int size = size();
        for (int i = 0; i < size; i ++) {
            HotMethodRegion item = get(i);
            int idx = lastId + item.diffWithLastMethodDexIndex().get();
            item.setIdx(idx);
            lastId = idx;
        }
    }
    private void updateIdx() {
        int lastId = 0;
        int size = size();
        for (int i = 0; i < size; i ++) {
            HotMethodRegion item = get(i);
            int idx = item.getIdx();
            item.diffWithLastMethodDexIndex().set(idx - lastId);
            lastId = idx;
        }
    }

    @Override
    public JSONArray toJson() {
        return BlockList.toJsonArray(this);
    }

    @Override
    public void fromJson(JSONArray json) {
        BlockList.fromJsonArray(this, json);
        sort();
        updateIdx();
    }
}
