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
import com.reandroid.arsc.value.LibraryInfo;
import com.reandroid.json.JSONArray;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.XmlReader;
import com.reandroid.xml.base.XmlSerializable;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class LibraryBlock extends EntriesChunk<LibraryInfo> implements XmlSerializable, XmlReader {

    public LibraryBlock() {
        super(ChunkType.LIBRARY, LibraryInfo.CREATOR);
    }

    public LibraryInfo get(String packageName) {
        int size = size();
        for (int i = 0; i < size; i++) {
            LibraryInfo info = get(i);
            if (info.packageNameMatches(packageName)) {
                return info;
            }
        }
        return null;
    }
    public boolean contains(String packageName) {
        return get(packageName) != null;
    }

    @Override
    public JSONArray toJson() {
        return (JSONArray) super.toJson();
    }
    @Override
    public boolean canMerge(LibraryInfo item) {
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
    }
    @Override
    public void serialize(XmlSerializer serializer) throws IOException {
        int size = size();
        for (int i = 0; i < size; i++) {
            get(i).serialize(serializer);
        }
    }

    public static final Creator<LibraryBlock> CREATOR = LibraryBlock::new;

    public static final String FILE_NAME_XML = ObjectsUtil.of("library.xml");
    public static final String FILE_NAME_JSON = ObjectsUtil.of("library.json");
}
