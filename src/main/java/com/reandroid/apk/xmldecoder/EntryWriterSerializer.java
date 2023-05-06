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
package com.reandroid.apk.xmldecoder;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class EntryWriterSerializer implements EntryWriter<XmlSerializer> {
    private final XmlSerializer xmlSerializer;
    public EntryWriterSerializer(XmlSerializer xmlSerializer){
        this.xmlSerializer = xmlSerializer;
    }

    public XmlSerializer getXmlSerializer() {
        return xmlSerializer;
    }

    @Override
    public void setFeature(String name, Object value) {
        if(value == null){
            value = false;
        }else if(!(value instanceof Boolean)){
            return;
        }
        xmlSerializer.setFeature(name, (Boolean)value);
    }
    @Override
    public XmlSerializer startTag(String name) throws IOException {
        return xmlSerializer.startTag(null, name);
    }
    @Override
    public XmlSerializer endTag(String name) throws IOException {
        return xmlSerializer.endTag(null, name);
    }
    @Override
    public XmlSerializer attribute(String name, String value) throws IOException {
        return xmlSerializer.attribute(null, name, value);
    }
    @Override
    public XmlSerializer text(String text) throws IOException {
        return xmlSerializer.text(text);
    }
    @Override
    public void comment(String comment) throws IOException {
        xmlSerializer.comment(comment);
    }
    @Override
    public void flush() throws IOException {
        xmlSerializer.flush();
    }
    @Override
    public void enableIndent(boolean enable){
        setFeature(FEATURE_INDENT, enable);
    }

    private static final String FEATURE_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output";
}
