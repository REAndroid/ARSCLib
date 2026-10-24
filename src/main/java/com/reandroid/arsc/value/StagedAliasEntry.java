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
package com.reandroid.arsc.value;

import com.reandroid.apk.xmlencoder.EncodeException;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.coder.CoderHex;
import com.reandroid.arsc.coder.CoderInteger;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.common.Mergeable;
import com.reandroid.utils.HexUtil;
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

public class StagedAliasEntry extends BlockItem implements StagedAliasInfo,
        Mergeable<StagedAliasInfo>, JSONConvert<JSONObject>, XmlSerializable, XmlReader {

    public StagedAliasEntry() {
        super(8);
    }

    @Override
    public int getStaged() {
        return getInteger(bytes(), 0);
    }
    public void setStagedResId(int id) {
        putInteger(bytes(), 0, id);
    }
    @Override
    public int getFinalized() {
        return getInteger(bytes(), 4);
    }
    public void setFinalizedResId(int id) {
        putInteger(bytes(),4, id);
    }
    public void set(int stagedId, int finalizedId) {
        setStagedResId(stagedId);
        setFinalizedResId(finalizedId);
    }

    @Override
    public void merge(StagedAliasInfo info) {
        if (info != null && info != this) {
            set(info.getStaged(), info.getFinalized());
        }
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(NAME_staged, getStaged());
        jsonObject.put(NAME_finalized, getFinalized());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setStagedResId(json.getInt(NAME_staged));
        setFinalizedResId(json.getInt(NAME_finalized));
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        XMLUtil.ensureStartTag(parser);
        if (!TAG_staged_alias.equals(parser.getName())) {
            throw new XmlPullParserException("Expecting tag '" + TAG_staged_alias +
                    "', but found '" + parser.getName() + "'", parser, null);
        }
        String staged = parser.getAttributeValue(null, NAME_staged);
        if (StringsUtil.isEmpty(staged)) {
            throw new EncodeException(parser, "Missing attribute '" + NAME_staged + "'");
        }
        String finalized = parser.getAttributeValue(null, NAME_finalized);
        if (finalized == null) {
            throw new EncodeException(parser, "Missing attribute '" + NAME_finalized + "'");
        }
        EncodeResult encodeStaged = CoderHex.INS.encode(staged);
        if (encodeStaged == null) {
            encodeStaged = CoderInteger.INS.encode(staged);
        }
        if (encodeStaged == null) {
            throw new EncodeException(parser, "Invalid id: '" + staged + "'");
        }
        EncodeResult encodeFinalized = CoderHex.INS.encode(finalized);
        if (encodeFinalized == null) {
            encodeFinalized = CoderInteger.INS.encode(finalized);
        }
        if (encodeFinalized == null) {
            throw new EncodeException(parser, "Invalid id: '" + finalized + "'");
        }
        set(encodeStaged.value, encodeFinalized.value);
        XMLUtil.skipEndTag(parser);
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_staged_alias);
        serializer.attribute(null, NAME_staged, HexUtil.toHex8(getStaged()));
        serializer.attribute(null, NAME_finalized, HexUtil.toHex8(getFinalized()));
        serializer.endTag(null, TAG_staged_alias);
    }

    @Override
    public int hashCode() {
        return getStaged() * 31 + getFinalized();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StagedAliasInfo)) {
            return false;
        }
        StagedAliasInfo info = (StagedAliasInfo) obj;
        return getStaged() == info.getStaged() &&
                getFinalized() == info.getFinalized();
    }

    @Override
    public String toString() {
        return "staged=" + HexUtil.toHex8(getStaged())
                +", finalized=" + HexUtil.toHex8(getFinalized());
    }

    public static StagedAliasInfo of(JSONObject json) {
        if (json == null) {
            return null;
        }
        return new StagedAliasInfo() {
            @Override
            public int getStaged() {
                return json.getInt(NAME_staged);
            }

            @Override
            public int getFinalized() {
                return json.getInt(NAME_finalized);
            }
            @Override
            public int hashCode() {
                return getStaged() * 31 + getFinalized();
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof StagedAliasInfo)) {
                    return false;
                }
                StagedAliasInfo info = (StagedAliasInfo) obj;
                return getStaged() == info.getStaged() &&
                        getFinalized() == info.getFinalized();
            }

            @Override
            public String toString() {
                return "staged=" + HexUtil.toHex8(getStaged())
                        +", finalized=" + HexUtil.toHex8(getFinalized());
            }
        };
    }
    public static final String NAME_staged = ObjectsUtil.of("staged");
    public static final String NAME_finalized = ObjectsUtil.of("finalized");
    public static final String TAG_staged_alias = ObjectsUtil.of("staged-alias");

    public static final Creator<StagedAliasEntry> CREATOR = StagedAliasEntry::new;
}
