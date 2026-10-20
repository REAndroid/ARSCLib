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

import com.reandroid.arsc.coder.CoderHex;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.xml.XmlEncodeException;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueHeader;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PublicXmlParser implements XmlReader {

    private final PackageBlock packageBlock;
    private final Set<Integer> uniqueIds;

    public PublicXmlParser(PackageBlock packageBlock) {
        this.packageBlock = packageBlock;
        this.uniqueIds = new HashSet<>();
    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parseResourcesAttributes(parser);
        while (XMLUtil.ensureStartTag(parser) == XmlPullParser.START_TAG) {
            parsePublic(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG) {
            parser.next();
        }
        IOUtil.close(parser);
    }

    private void parseResourcesAttributes(XmlPullParser parser) throws IOException, XmlPullParserException {
        int event = parser.getEventType();
        boolean documentStarted = false;
        if (event == XmlPullParser.START_DOCUMENT) {
            documentStarted = true;
            parser.next();
        }
        event = XMLUtil.ensureStartTag(parser);
        if (event != XmlPullParser.START_TAG) {
            throw new XmlEncodeException(parser, "Expecting xml state START_TAG but found: "
                    + XMLUtil.toEventName(parser.getEventType()));
        }
        if (!PackageBlock.TAG_resources.equals(parser.getName())) {
            if (documentStarted) {
                throw new XmlEncodeException(parser,
                        "Expecting <resources> tag but found: " + parser.getName());
            }
            return;
        }

        PackageBlock packageBlock = this.packageBlock;
        String packageName = parser.getAttributeValue(null, PackageBlock.ATTR_package);
        if (!StringsUtil.isBlank(packageName)
                && !PackageBlock.EMPTY_PACKAGE_NAME.equals(packageName)) {
            packageBlock.setName(packageName);
        }
        String id = parser.getAttributeValue(null, PackageBlock.ATTR_id);
        if (!StringsUtil.isWhiteSpace(id) && packageBlock.getId() == 0) {
            EncodeResult encodeResult = ValueCoder
                    .encodeHexOrInteger(id);
            if (encodeResult == null || !PackageBlock.isPackageId(encodeResult.value)) {
                throw new XmlEncodeException(parser, "Invalid id value: '" + id + "'");
            }
            packageBlock.setId(encodeResult.value);
        }
        if ("true".equalsIgnoreCase(parser.getAttributeValue(
                null, TableBlock.ATTR_null_table))) {
            packageBlock.getTableBlock().setNull(true);
        }
        parser.next();
    }

    private void parsePublic(XmlPullParser parser) throws IOException, XmlPullParserException {
        if (XMLUtil.ensureStartTag(parser) != XmlPullParser.START_TAG) {
            return;
        }
        if (!PackageBlock.TAG_public.equals(parser.getName())) {
            XMLUtil.skipEndTag(parser);
            return;
        }
        PublicInfo info = parseNext(parser);
        PackageBlock packageBlock = this.packageBlock;
        int i = packageBlock.getId();
        int pkgId = info.getPackageId();
        if (i == 0) {
            packageBlock.setId(pkgId);
        } else if (i != pkgId) {
            throw new XmlEncodeException(parser, "Different package id, expecting: "
                    + HexUtil.toHex2(i) + " " + info);
        }
        TypeString typeString = packageBlock.getOrCreateTypeString(
                info.getTypeId(), info.type);
        ResourceEntry resourceEntry = packageBlock.getResource(typeString.getId(),
                info.getEntryId());
        if (resourceEntry != null && !resourceEntry.isEmpty()) {
            resourceEntry.setName(info.name);
        } else {
            TypeBlock typeBlock = packageBlock.getOrCreateTypeBlock(typeString.getId(), "");
            Entry entry = typeBlock.getOrCreateEntry(info.getEntryId());
            entry.setName(info.name, true);
            if (typeBlock.isTypeId()) {
                entry.setDataAsBoolean(false);
                ValueHeader header = entry.getHeader();
                header.setPublic(true);
                header.setWeak(true);
            }
            resourceEntry = entry.getResourceEntry();
        }
        int spec = info.spec;
        if (spec != 0) {
            resourceEntry.getOrCreateSpecFlag().set(spec);
        }
    }


    public PublicInfo parseNext(XmlPullParser parser) throws IOException, XmlPullParserException {
        String id = parser.getAttributeValue(null, PackageBlock.ATTR_id);
        if (StringsUtil.isEmpty(id)) {
            throw new XmlPullParserException("Missing id", parser, null);
        }
        String type = parser.getAttributeValue(null, PackageBlock.ATTR_type);
        if (StringsUtil.isEmpty(type)) {
            throw new XmlPullParserException("Missing type", parser, null);
        }
        String name = parser.getAttributeValue(null, PackageBlock.ATTR_name);
        if (StringsUtil.isEmpty(name)) {
            throw new XmlPullParserException("Missing name", parser, null);
        }
        EncodeResult resourceId = CoderHex.INS.encode(id);
        if (resourceId == null) {
            throw new XmlPullParserException("Invalid id for: "
                    + type + "/" + name, parser, null);
        }
        int resId = resourceId.value;
        if (!PackageBlock.isResourceId(resId)) {
            throw new XmlEncodeException(parser, "Invalid id format: "
                    + type + "/" + name);
        }
        if (!uniqueIds.add(resId)) {
            throw new XmlEncodeException(parser, "Duplicate id: " + id);
        }
        String spec = parser.getAttributeValue(null, PackageBlock.ATTR_spec);
        int specId = 0;
        if (!StringsUtil.isEmpty(spec)) {
            EncodeResult specValue = CoderHex.INS.encode(spec);
            if (specValue == null) {
                throw new XmlPullParserException("Invalid spec for: "
                        + type + "/" + name, parser, null);
            }
            specId = specValue.value;
        }
        XMLUtil.skipEndTag(parser);
        return new PublicInfo(resId, type, name, specId);
    }

    static class PublicInfo {

        private final int id;
        public final String type;
        public final String name;
        public final int spec;

        public PublicInfo(int id, String type, String name, int spec) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.spec = spec;
        }

        public int getPackageId() {
            return (id >> 24) & 0xff;
        }
        public int getTypeId() {
            return (id >> 16) & 0xff;
        }
        public int getEntryId() {
            return id & 0xffff;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("<public id=\"");
            builder.append(HexUtil.toHex8(id));
            builder.append("\" type=\"");
            builder.append(type);
            builder.append("\" name=\"");
            builder.append(name);
            builder.append("\"");
            int spec = this.spec;
            if (spec != 0) {
                builder.append(" spec=\"");
                builder.append(HexUtil.toHex8(spec));
                builder.append("\"");
            }
            builder.append(" />");
            return builder.toString();
        }
    }
}
