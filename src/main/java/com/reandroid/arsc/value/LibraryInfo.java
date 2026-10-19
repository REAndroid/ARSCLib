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
import com.reandroid.common.Mergeable;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.IntegerItem;
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


public class LibraryInfo extends FixedBlockContainer implements ResourceLibrary,
        Mergeable<LibraryInfo>, JSONConvert<JSONObject>, XmlSerializable, XmlReader {
    
    private final IntegerItem mPackageId;
    private final FixedLengthString mPackageName;

    public LibraryInfo() {
        super(2);
        
        this.mPackageId = new IntegerItem();
        this.mPackageName = new FixedLengthString(256);

        addChild(0, mPackageId);
        addChild(1, mPackageName);
    }

    @Override
    public int getId() {
        return mPackageId.get();
    }
    public void setId(int id) {
        mPackageId.set(id);
    }
    @Override
    public String getName() {
        return mPackageName.get();
    }
    public void setName(String packageName) {
        mPackageName.set(packageName);
    }
    @Override
    public String getPrefix() {
        return ResourceLibrary.toPrefix(getName());
    }
    @Override
    public String getUri() {
        if (getId() == 0x01 && ResourceLibrary.PREFIX_ANDROID.equals(getName())) {
            return ResourceLibrary.URI_ANDROID;
        }
        return ResourceLibrary.URI_RES_AUTO;
    }
    @Override
    public boolean packageNameMatches(String packageName) {
        return ResourceLibrary.packageNameMatches(this, packageName);
    }

    @Override
    public void merge(LibraryInfo info) {
        if (info == null || info == this) {
            return;
        }
        int id = this.getId();
        int infoId = info.getId();
        if (id != 0 && id != infoId) {
            throw new IllegalArgumentException("Can not add different id libraries: "
                    + id + "!=" + infoId);
        }
        setId(infoId);
        setName(info.getName());
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getId());
        jsonObject.put("name", getName());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt("id"));
        setName(json.getString("name"));
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        String id = parser.getAttributeValue(null, NAME_id);
        if (StringsUtil.isEmpty(id)) {
            throw new EncodeException(parser, "Missing attribute '" + NAME_id + "'");
        }
        String name = parser.getAttributeValue(null, NAME_name);
        if (name == null) {
            throw new EncodeException(parser, "Missing attribute '" + NAME_name + "'");
        }
        EncodeResult encodeResult = CoderHex.INS.encode(id);
        if (encodeResult == null) {
            encodeResult = CoderInteger.INS.encode(id);
        }
        if (encodeResult == null) {
            throw new EncodeException(parser, "Invalid id: '" + id + "'");
        }
        setId(encodeResult.value);
        setName(name);
        XMLUtil.skipEndTag(parser);
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_library);
        serializer.attribute(null, NAME_id, HexUtil.toHex(getId(), 2));
        serializer.attribute(null, NAME_name, getName());
        serializer.endTag(null, TAG_library);
    }

    @Override
    public int hashCode() {
        return getId() * 31 + ObjectsUtil.hash(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof LibraryInfo)) {
            return false;
        }
        LibraryInfo info = (LibraryInfo) obj;
        return ObjectsUtil.equals(getName(), info.getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LIBRARY{");
        builder.append(HexUtil.toHex(getId(), 2));
        builder.append(':');
        String name = getName();
        if (name == null) {
            name = "NULL";
        }
        builder.append(name);
        builder.append('}');
        return builder.toString();
    }

    public static final Creator<LibraryInfo> CREATOR = LibraryInfo::new;

    public static final String TAG_library = ObjectsUtil.of("library");
    public static final String NAME_id = ObjectsUtil.of("id");
    public static final String NAME_name = ObjectsUtil.of("name");
}
