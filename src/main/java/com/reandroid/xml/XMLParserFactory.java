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
package com.reandroid.xml;

import com.android.org.kxml2.io.KXmlParser;
import com.reandroid.common.FileChannelInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class XMLParserFactory {

    public static XmlPullParser newPullParser(File file) throws XmlPullParserException {
        XmlPullParser parser = newPullParser();
        try {
            parser.setInput(new FileChannelInputStream(file), StandardCharsets.UTF_8.name());
        } catch (IOException ex) {
            throw new XmlPullParserException(ex.getMessage() + ", file = " + file);
        }
        return parser;
    }
    public static XmlPullParser newPullParser(InputStream inputStream) throws XmlPullParserException {
        XmlPullParser parser = newPullParser();
        parser.setInput(inputStream, StandardCharsets.UTF_8.name());
        return parser;
    }
    public static XmlPullParser newPullParser(){
        return new KXmlParser();
    }
}
