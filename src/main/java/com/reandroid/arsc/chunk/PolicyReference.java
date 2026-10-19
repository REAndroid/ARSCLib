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
import com.reandroid.common.Mergeable;
import com.reandroid.arsc.coder.CoderUnknownReferenceId;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.json.JSONConvert;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class PolicyReference extends IntegerItem implements Mergeable<PolicyReference>,
        XmlSerializable, XmlReader, JSONConvert<Integer> {

    public PolicyReference() {
        super();
    }

    public ResourceEntry getResourceEntry() {
        PackageBlock packageBlock = getParentInstance(PackageBlock.class);
        if (packageBlock != null) {
            return packageBlock.getResource(get());
        }
        return null;
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        String name = parser.getAttributeValue(null, ATTR_name);
        if (StringsUtil.isEmpty(name)) {
            throw new EncodeException(parser, "Missing attribute '" + ATTR_name + "'");
        }
        String type = parser.getAttributeValue(null, ATTR_type);
        if (StringsUtil.isEmpty(type)) {
            EncodeResult encodeResult = CoderUnknownReferenceId.INS.encode(name);
            if (encodeResult != null) {
                this.set(encodeResult.value);
                XMLUtil.skipEndTag(parser);
                return;
            }
        }
        PackageBlock packageBlock = getParentInstance(PackageBlock.class);
        ResourceEntry resourceEntry = packageBlock.getResource(type, name);
        if (resourceEntry == null) {
            resourceEntry = packageBlock.getTableBlock()
                    .getResource((String) null, type, name);
        }
        if (resourceEntry == null) {
            throw new EncodeException(parser, "Unknown policy item: type = " + type + ", name = " + name + "");
        }
        this.set(resourceEntry.getResourceId());
        XMLUtil.skipEndTag(parser);
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startTag(null, TAG_item);
        ResourceEntry resourceEntry = getResourceEntry();
        if (resourceEntry != null) {
            serializer.attribute(null, ATTR_type, resourceEntry.getType());
            serializer.attribute(null, ATTR_name, resourceEntry.getName());
        } else {
            serializer.attribute(null, ATTR_type, "");
            serializer.attribute(null, ATTR_name,
                    CoderUnknownReferenceId.INS.decode(get()));
        }
        serializer.endTag(null, TAG_item);
    }

    @Override
    public Integer toJson() {
        return get();
    }
    @Override
    public void fromJson(Integer data) {
        if (data != null) {
            set(data);
        }
    }
    @Override
    public void merge(PolicyReference item) {
        if (item == null || item == this) {
            return;
        }
        set(item.get());
    }
    @Override
    public int hashCode() {
        return get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PolicyReference)) {
            return false;
        }
        PolicyReference policyReference = (PolicyReference) obj;
        return get() == policyReference.get();
    }

    @Override
    public String toString() {
        ResourceEntry resourceEntry = getResourceEntry();
        if (resourceEntry != null) {
            return resourceEntry.buildReference(getParentInstance(PackageBlock.class));
        }
        return toHex();
    }

    public static final String ATTR_name = ObjectsUtil.of("name");
    public static final String ATTR_type = ObjectsUtil.of("type");

    public static final String TAG_item = ObjectsUtil.of("item");

    public static final Creator<PolicyReference> CREATOR = PolicyReference::new;

}
