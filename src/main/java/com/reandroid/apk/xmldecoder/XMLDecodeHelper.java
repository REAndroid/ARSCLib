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

import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.xml.*;
import com.reandroid.xml.parser.XMLSpanParser;

import java.io.IOException;

public class XMLDecodeHelper {

    public static void writeTextContent(EntryWriter<?> writer, StringItem stringItem) throws IOException {
        if(stringItem == null){
            return;
        }
        if(!stringItem.hasStyle()){
            String text = stringItem.get();
            text = ValueDecoder.escapeSpecialCharacter(text);
            text = ValueDecoder.quoteWhitespace(text);
            writer.text(text);
        }else {
            String xml = stringItem.getXml();
            XMLElement element = parseSpanSafe(xml);
            if(element != null){
                writeParsedSpannable(writer, element);
            }else {
                // TODO: throw or investigate the reason
                writer.text(xml);
            }
        }
    }
    public static void writeParsedSpannable(EntryWriter<?> writer, XMLElement spannableParent) throws IOException {
        for(XMLNode xmlNode : spannableParent.getChildNodes()){
            if(xmlNode instanceof XMLText){
                String text = ((XMLText)xmlNode).getText(true);
                writer.enableIndent(false);
                writer.text(ValueDecoder.escapeSpecialCharacter(text));
            }else if(xmlNode instanceof XMLElement){
                writeElement(writer, (XMLElement) xmlNode);
            }
        }
    }
    private static void writeElement(EntryWriter<?> writer, XMLElement element) throws IOException {
        writer.enableIndent(false);
        writer.startTag(element.getTagName());
        for(XMLAttribute xmlAttribute : element.listAttributes()){
            writer.attribute(xmlAttribute.getName(), xmlAttribute.getValue());
        }
        for(XMLNode xmlNode : element.getChildNodes()){
            if(xmlNode instanceof XMLText){
                String text = ((XMLText)xmlNode).getText(true);
                writer.text(text);
            }else if(xmlNode instanceof XMLElement){
                writeElement(writer, (XMLElement) xmlNode);
            }
        }
        writer.endTag(element.getTagName());
    }
    private static XMLElement parseSpanSafe(String spanText){
        if(spanText==null){
            return null;
        }
        try {
            XMLSpanParser spanParser = new XMLSpanParser();
            return spanParser.parse(spanText);
        } catch (XMLException ignored) {
            return null;
        }
    }

}
