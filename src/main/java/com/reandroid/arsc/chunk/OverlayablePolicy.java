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
import com.reandroid.arsc.header.OverlayablePolicyHeader;
import com.reandroid.json.JSONItem;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class OverlayablePolicy extends EntriesChunk<PolicyReference> implements
        XmlSerializable, XmlReader, Iterable<PolicyReference> {

    public OverlayablePolicy() {
        super(new OverlayablePolicyHeader(), PolicyReference.CREATOR);
        itemList().setHasSameByteSizeEntries(true);
    }

    @Override
    public OverlayablePolicyHeader getHeaderBlock() {
        return (OverlayablePolicyHeader) super.getHeaderBlock();
    }

    public int getFlags() {
        return getHeaderBlock().flags().get();
    }
    public void setFlags(int flags) {
        getHeaderBlock().flags().set(flags);
    }
    public void setFlags(PolicyFlag ... policyFlags) {
        setFlags(PolicyFlag.sum(policyFlags));
    }
    public void addFlag(PolicyFlag flag) {
        setFlags(getFlags() | (flag == null ? 0 : flag.flag()));
    }
    public PolicyFlag[] getPolicyFlags() {
        return PolicyFlag.valuesOf(getFlags());
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new XmlPullParserException("Parser not in START_TAG event: " + parser.getEventType());
        }
        if (!TAG_policy.equals(parser.getName())) {
            throw new XmlPullParserException("Expecting tag '" + TAG_policy +
                    "', but found '" + parser.getName() + "'");
        }
        setFlags(PolicyFlag.parse(parser));
        parser.next();
        XMLUtil.ensureTag(parser);
        while (parser.getEventType() != XmlPullParser.END_TAG &&
                parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            createNext().parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
        }
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_policy);
        int flags = getFlags();
        if (flags != 0) {
            serializer.attribute(null, "type", PolicyFlag.toString(flags));
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).serialize(serializer);
        }
        serializer.endTag(null, TAG_policy);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_flags, getFlags());
        if (!isEmpty()) {
            jsonObject.put(NAME_references, super.toJson());
        }
        return jsonObject;
    }

    @Override
    public void fromJson(JSONItem jsonItem) {
        JSONObject json = (JSONObject) jsonItem;
        setFlags(json.getInt(NAME_flags));
        super.fromJson(json.optJSONArray(NAME_references));
    }

    @Override
    public boolean canMerge(PolicyReference item) {
        return !itemList().contains(item);
    }

    @Override
    public void merge(EntriesChunk<? extends PolicyReference> entriesChunk) {
        if (entriesChunk == null || entriesChunk == this) {
            return;
        }
        OverlayablePolicy policy = (OverlayablePolicy) entriesChunk;
        setFlags(this.getFlags() | policy.getFlags());
        super.merge(entriesChunk);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                ": flags=" + PolicyFlag.toString(getFlags())
                +", count=" + size();
    }

    public static final Creator<OverlayablePolicy> CREATOR = OverlayablePolicy::new;

    public static final String NAME_flags = ObjectsUtil.of("flags");
    public static final String NAME_references = ObjectsUtil.of("references");

    public static final String TAG_policy = ObjectsUtil.of("policy");
}
