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

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.header.OverlayableHeader;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class Overlayable extends Chunk<OverlayableHeader> implements
        JSONConvert<JSONObject>, XmlSerializable, XmlReader {

    private final BlockList<OverlayablePolicy> policyList;

    public Overlayable() {
        super(new OverlayableHeader(), 1);

        this.policyList = new BlockList<>(OverlayablePolicy.CREATOR);
        
        addChild(this.policyList);
    }

    public Iterator<OverlayablePolicy> policies() {
        return policyList.iterator();
    }

    public BlockList<OverlayablePolicy> getPolicyList() {
        return policyList;
    }
    public OverlayablePolicy createOverlayablePolicy() {
        return getPolicyList().createNext();
    }

    public OverlayablePolicy getByFlag(int flags) {
        BlockList<OverlayablePolicy> policyList = getPolicyList();
        int size =  policyList.size();
        for (int i = 0; i < size; i++) {
            OverlayablePolicy policy = policyList.get(i);
            if (flags == policy.getFlags()) {
                return policy;
            }
        }
        return null;
    }

    public String getName() {
        return getHeaderBlock().name().get();
    }
    public void setName(String str) {
        getHeaderBlock().name().set(str);
    }
    public String getActor() {
        return getHeaderBlock().actor().get();
    }
    public void setActor(String str) {
        getHeaderBlock().actor().set(str);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        checkInvalidChunk(headerBlock);

        int size = headerBlock.getChunkSize();
        BlockReader chunkReader = reader.create(size);
        headerBlock = getHeaderBlock();
        headerBlock.readBytes(chunkReader);

        readOverlayablePolicies(chunkReader);

        reader.offset(size);
        chunkReader.close();
        onChunkLoaded();
    }
    private void readOverlayablePolicies(BlockReader reader) throws IOException {
        HeaderBlock headerBlock = reader.readHeaderBlock();
        BlockList<OverlayablePolicy> policyList = this.policyList;
        while (headerBlock != null && headerBlock.getChunkType() == ChunkType.OVERLAYABLE_POLICY) {
            policyList.createNext().readBytes(reader);
            headerBlock = reader.readHeaderBlock();
        }
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        XMLUtil.ensureStartTag(parser);
        if (!TAG_overlayable.equals(parser.getName())) {
            throw new XmlPullParserException("Expecting tag '" + TAG_overlayable +
                    "', but found '" + parser.getName() + "'", parser, null);
        }
        setName(parser.getAttributeValue(null, NAME_name));
        setActor(parser.getAttributeValue(null, NAME_actor));
        parser.next();
        XMLUtil.ensureTag(parser);
        while (parser.getEventType() != XmlPullParser.END_TAG
                && parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            getPolicyList().createNext().parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
            XMLUtil.ensureTag(parser);
        }
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_overlayable);
        String name = getName();
        if (!StringsUtil.isEmpty(name)) {
            serializer.attribute(null, NAME_name, name);
        }
        String actor = getActor();
        if (!StringsUtil.isEmpty(actor)) {
            serializer.attribute(null, NAME_actor, actor);
        }
        BlockList<OverlayablePolicy> policyList = getPolicyList();
        int size =  policyList.size();
        for (int i = 0; i < size; i++) {
            policyList.get(i).serialize(serializer);
        }
        serializer.endTag(null, TAG_overlayable);
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_name, getName());
        jsonObject.put(NAME_actor, getActor());
        JSONArray jsonArray = BlockList.toJsonArray(policyList);
        if (jsonArray != null) {
            jsonObject.put(NAME_policies, jsonArray);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setName(json.optString(NAME_name, null));
        setActor(json.optString(NAME_actor, null));
        BlockList.fromJsonArray(policyList, json.optJSONArray(NAME_policies));
    }
    public void merge(Overlayable overlayable) {
        if (overlayable == null || overlayable == this) {
            return;
        }
        setName(overlayable.getName());
        setActor(overlayable.getActor());
        BlockList<OverlayablePolicy> policyList = overlayable.getPolicyList();
        int size =  policyList.size();
        for (int i = 0; i < size; i++) {
            OverlayablePolicy policy = policyList.get(i);
            OverlayablePolicy exist = getByFlag(policy.getFlags());
            if (exist == null) {
                exist = getPolicyList().createNext();
            }
            exist.merge(policy);
        }
    }

    @Override
    public String toString() {
        return "name='" + getName()
                +"', actor='" + getActor()
                +"', policies=" + policyList.size();
    }

    public static final String NAME_name = ObjectsUtil.of("name");
    public static final String NAME_actor = ObjectsUtil.of("actor");
    public static final String NAME_policies = ObjectsUtil.of("policies");
    public static final String TAG_overlayable = ObjectsUtil.of("overlayable");
    public static final String FILE_NAME_XML = ObjectsUtil.of("overlayable.xml");
    public static final String FILE_NAME_JSON = ObjectsUtil.of("overlayable.json");

    public static final Creator<Overlayable> CREATOR = Overlayable::new;
}
