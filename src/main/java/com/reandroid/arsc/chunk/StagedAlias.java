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

import com.reandroid.apk.xmlencoder.EncodeException;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.coder.CoderHex;
import com.reandroid.arsc.coder.CoderInteger;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.value.StagedAliasEntry;
import com.reandroid.arsc.value.StagedAliasInfo;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONItem;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StagedAlias extends EntriesChunk<StagedAliasEntry> implements XmlSerializable, XmlReader {

    private Map<Integer, StagedAliasEntry> stagedResIdMap;

    public StagedAlias() {
        super(ChunkType.STAGED_ALIAS, StagedAliasEntry.CREATOR);
        itemList().setHasSameByteSizeEntries(true);
    }

    public StagedAliasEntry getByStaged(int staged) {
        Map<Integer, StagedAliasEntry> map = getStagedResIdMap();
        StagedAliasEntry entry = map.get(staged);
        if (entry != null) {
            if (entry.getParent() != null && staged == entry.getStaged()) {
                return entry;
            }
            map.remove(staged);
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            entry = get(i);
            if (staged == entry.getStaged()) {
                map.put(staged, entry);
                return entry;
            }
        }
        return null;
    }
    private Map<Integer, StagedAliasEntry> getStagedResIdMap() {
        Map<Integer, StagedAliasEntry> map = this.stagedResIdMap;
        if (map == null) {
            int size = size();
            map = new HashMap<>(size);
            this.stagedResIdMap = map;
            for (int i = 0; i < size; i++) {
                StagedAliasEntry entry = get(i);
                map.putIfAbsent(entry.getStaged(), entry);
            }
        }
        return map;
    }

    @Override
    public void fromJson(JSONItem json) {
        if (json == null) {
            return;
        }
        if (this.isEmpty()) {
            super.fromJson(json);
            return;
        }
        JSONArray jsonArray = (JSONArray) json;
        int length = jsonArray.length();
        BlockList<StagedAliasEntry> itemList = itemList();
        for (int i = 0; i < length; i++) {
            StagedAliasInfo info = StagedAliasEntry.of(jsonArray.optJSONObject(i));
            if (info != null && !itemList.contains(info)) {
                itemList.createNext().merge(info);
            }
        }
    }

    @Override
    public JSONArray toJson() {
        return (JSONArray) super.toJson();
    }

    @Override
    public boolean canMerge(StagedAliasEntry item) {
        return !itemList().contains(item);
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        XMLUtil.ensureStartTag(parser);
        while (parser.getEventType() != XmlPullParser.END_TAG
                && parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            createNext().parse(parser);
            XMLUtil.ensureTag(parser);
        }
        XMLUtil.skipEndTag(parser);
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).serialize(serializer);
        }
    }

    public static final String FILE_NAME_XML = ObjectsUtil.of("staged-alias.xml");
    public static final String FILE_NAME_JSON = ObjectsUtil.of("staged-alias.json");

    public static final Creator<StagedAlias> CREATOR = StagedAlias::new;
}
