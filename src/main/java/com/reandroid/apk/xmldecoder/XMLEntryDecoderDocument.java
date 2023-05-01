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

import com.reandroid.apk.XmlHelper;
import com.reandroid.arsc.value.Entry;
import com.reandroid.common.EntryStore;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

import java.io.IOException;
import java.util.Collection;

public class XMLEntryDecoderDocument extends XMLEntryDecoder<XMLElement>{
    private final EntryWriterElement entryWriterElement;
    public XMLEntryDecoderDocument(EntryStore entryStore) {
        super(entryStore);
        this.entryWriterElement = new EntryWriterElement();
    }

    public XMLElement decode(Entry entry) throws IOException {
        return super.decode(this.entryWriterElement, entry);
    }

    public XMLDocument decode(XMLDocument xmlDocument, Collection<Entry> entryList)
            throws IOException {

        if(xmlDocument == null){
            xmlDocument = new XMLDocument(XmlHelper.RESOURCES_TAG);
        }
        XMLElement docElement = xmlDocument.getDocumentElement();

        if(docElement == null){
            docElement = new XMLElement(XmlHelper.RESOURCES_TAG);
            xmlDocument.setDocumentElement(docElement);
        }
        for(Entry entry : entryList){
            docElement.addChild(decode(entry));
        }
        return xmlDocument;
    }
}
