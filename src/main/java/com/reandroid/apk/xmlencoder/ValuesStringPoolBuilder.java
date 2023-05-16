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
package com.reandroid.apk.xmlencoder;

import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLSpanInfo;
import com.reandroid.xml.XMLSpannable;

import java.io.File;
import java.util.*;

public class ValuesStringPoolBuilder {
    private final Set<String> stringList;
    private final Set<String> styleList;
    public ValuesStringPoolBuilder(){
        this.stringList=new HashSet<>();
        this.styleList=new HashSet<>();
    }
    public void addTo(TableStringPool stringPool){
        if(stringPool.getStringsArray().childesCount()==0){
            buildWithStyles(stringPool);
        }
        stringPool.addStrings(stringList);
        stringList.clear();
        styleList.clear();
        stringPool.refresh();
    }
    private void buildWithStyles(TableStringPool stringPool){
        List<XMLSpannable> spannableList = buildSpannable();
        if(spannableList.size()==0){
            return;
        }
        StringArray<TableString> stringsArray = stringPool.getStringsArray();
        StyleArray styleArray = stringPool.getStyleArray();

        int stylesCount = spannableList.size();
        stringsArray.setChildesCount(stylesCount);
        styleArray.setChildesCount(stylesCount);

        List<String> tagList =
                new ArrayList<>(XMLSpannable.tagList(spannableList));
        EncodeUtil.sortStrings(tagList);
        Map<String, TableString> tagsMap =
                stringPool.insertStrings(tagList);

        List<String> textList = XMLSpannable.toTextList(spannableList);

        for(int i=0;i<stylesCount;i++){
            XMLSpannable spannable = spannableList.get(i);
            TableString tableString = stringsArray.get(i);
            StyleItem styleItem = styleArray.get(i);

            tableString.set(textList.get(i));

            for(XMLSpanInfo spanInfo:spannable.getSpanInfoList()){
                TableString tag = tagsMap.get(spanInfo.tag);
                int tagRef=tag.getIndex();
                styleItem.addStylePiece(tagRef, spanInfo.start, spanInfo.end);
            }
        }
        stringPool.refreshUniqueIdMap();
    }
    private List<XMLSpannable> buildSpannable(){
        List<XMLSpannable> results=new ArrayList<>();
        Set<String> removeList=new HashSet<>();
        for(String text:styleList){
            XMLSpannable spannable=XMLSpannable.parse(text);
            if(spannable!=null){
                results.add(spannable);
                removeList.add(text);
            }else {
                stringList.add(text);
            }
        }
        stringList.removeAll(removeList);
        XMLSpannable.sort(results);
        return results;
    }
    public void scanValuesDirectory(File dir){
        addStringsFile(new File(dir, "strings.xml"));
        addBagsFile(new File(dir, "plurals.xml"));
    }
    public int size(){
        return stringList.size();
    }
    private void addStringsFile(File file) {
        if(file==null||!file.isFile()){
            return;
        }
        try {
            XMLDocument xmlDocument = XMLDocument.load(file);
            addStrings(xmlDocument);
        } catch (Exception ignored) {
        }
    }
    private void addBagsFile(File file) {
        if(file==null||!file.isFile()){
            return;
        }
        try {
            XMLDocument xmlDocument = XMLDocument.load(file);
            addBagStrings(xmlDocument);
        } catch (Exception ignored) {
        }
    }
    private void addBagStrings(XMLDocument xmlDocument){
        if(xmlDocument == null){
            return;
        }
        XMLElement documentElement = xmlDocument.getDocumentElement();
        if(documentElement==null){
            return;
        }
        int count = documentElement.getChildesCount();
        for(int i=0;i<count;i++){
            XMLElement child=documentElement.getChildAt(i);
            int childCount=child.getChildesCount();
            for(int j=0;j<childCount;j++){
                addStrings(child.getChildAt(i));
            }
        }
    }
    private void addStrings(XMLDocument xmlDocument){
        if(xmlDocument == null){
            return;
        }
        XMLElement documentElement = xmlDocument.getDocumentElement();
        if(documentElement==null){
            return;
        }
        int count = documentElement.getChildesCount();
        for(int i=0;i<count;i++){
            addStrings(documentElement.getChildAt(i));
        }
    }
    private void addStrings(XMLElement element){
        if(element.hasChildElements()){
            addStyleElement(element);
        }else {
            String text = ValueDecoder
                    .unQuoteWhitespace(element.getTextContent());
            text = ValueDecoder
                    .unEscapeSpecialCharacter(text);
            addString(text);
        }
    }
    private void addString(String text){
        if(text!=null && text.length()>0 && text.charAt(0)!='@'){
            stringList.add(text);
        }
    }
    private void addStyleElement(XMLElement element){
        styleList.add(element.buildTextContent(false));
    }

}
