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

import com.reandroid.arsc.coder.XmlSanitizer;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.xml.*;

import java.io.IOException;
import java.util.Iterator;

public class XMLDecodeHelper {

    public static boolean writeTextContent(EntryWriter<?> writer, StringItem stringItem) throws IOException {
        if(stringItem == null){
            return false;
        }
        if(!stringItem.hasStyle()){
            String text = stringItem.get();
            text = XmlSanitizer.escapeSpecialCharacter(text);
            text = XmlSanitizer.quoteWhitespace(text);
            writer.text(text);
            return false;
        }else {
            TableString tableString= (TableString) stringItem;
            StyleDocument styleDocument = tableString.getStyleDocument();
            String xml = stringItem.getXml();
            if(styleDocument != null){
                writeParsedSpannable(writer, styleDocument);
            }else {
                // TODO: throw or investigate the reason
                writer.text(xml);
            }
            return true;
        }
    }
    public static void writeParsedSpannable(EntryWriter<?> writer, StyleDocument spannableParent) throws IOException {

        Iterator<XMLNode> iterator = spannableParent.iterator();
        while (iterator.hasNext()){
            Object xmlNode = iterator.next();
            if(xmlNode instanceof XMLText){
                String text = ((XMLText)xmlNode).getText();
                writer.enableIndent(false);
                writer.text(XmlSanitizer.escapeSpecialCharacter(text));
            }else if(xmlNode instanceof XMLElement){
                writeElement(writer, (XMLElement) xmlNode);
            }
        }
    }
    private static void writeElement(EntryWriter<?> writer, XMLElement element) throws IOException {
        writer.enableIndent(false);
        writer.startTag(element.getName());
        for(XMLAttribute xmlAttribute : element.listAttributes()){
            writer.attribute(xmlAttribute.getName(), xmlAttribute.getValue());
        }
        Iterator<XMLNode> iterator = element.iterator();
        while (iterator.hasNext()){
            Object xmlNode = iterator.next();
            if(xmlNode instanceof XMLText){
                String text = ((XMLText)xmlNode).getText();
                writer.text(text);
            }else if(xmlNode instanceof XMLElement){
                writeElement(writer, (XMLElement) xmlNode);
            }
        }
        writer.endTag(element.getName());
    }

}
