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
package com.reandroid.arsc.list;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.common.Mergeable;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public abstract class ExtraChunkList<T extends Chunk<?>> extends BlockList<T> implements Mergeable<ExtraChunkList<T>>,
        JSONConvert<JSONArray>, XmlSerializable, XmlReader {

    public ExtraChunkList(Creator<? extends T> creator) {
        super(creator);
    }

    public boolean isEmpty() {
        return size() == 0;
    }
    public boolean isBlank() {
        return isEmpty();
    }

    @Override
    public void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() == XmlPullParser.START_DOCUMENT) {
            parser.next();
        }
        XMLUtil.ensureStartTag(parser);
        if (PackageBlock.TAG_resources.equals(parser.getName())) {
            parser.next();
            XMLUtil.ensureStartTag(parser);
        }
        while (parser.getEventType() != XmlPullParser.END_TAG &&
                parser.getEventType() != XmlPullParser.END_DOCUMENT) {
            ((XmlReader) createNext()).parse(parser);
            XMLUtil.ensureTag(parser);
        }
        if (parser.getEventType() == XmlPullParser.END_TAG
                && PackageBlock.TAG_resources.equals(parser.getName())) {
            parser.next();
            XMLUtil.ensureTag(parser);
        }
    }

    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        serializer.startDocument("utf-8", null);
        serializer.startTag(null, PackageBlock.TAG_resources);
        int size = size();
        for (int i = 0; i < size; i++) {
            ((XmlSerializable) get(i)).serialize(serializer);
        }
        serializer.endTag(null, PackageBlock.TAG_resources);
        serializer.endDocument();
    }

    public boolean matchesFileName(String name) {
        if (name == null) {
            return false;
        }
        return name.equalsIgnoreCase(fileNameXml())
                || name.equalsIgnoreCase(fileNameJson());
    }

    public abstract String fileNameJson();
    public abstract String fileNameXml();
}
