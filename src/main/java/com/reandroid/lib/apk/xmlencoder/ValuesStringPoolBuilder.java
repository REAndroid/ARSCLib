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
package com.reandroid.lib.apk.xmlencoder;

import com.reandroid.lib.arsc.array.StyleArray;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.item.StyleItem;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLSpanInfo;
import com.reandroid.xml.XMLSpannable;

import java.io.File;
import java.util.*;

 public class ValuesStringPoolBuilder {
    private final Set<String> stringList;
    public ValuesStringPoolBuilder(){
        this.stringList=new HashSet<>();
    }
    public void addTo(TableStringPool stringPool){
        if(stringPool.getStringsArray().childesCount()==0){
            buildWithStyles(stringPool);
        }
        stringPool.addStrings(stringList);
        stringList.clear();
        stringPool.refresh();
    }
    private void buildWithStyles(TableStringPool stringPool){
        List<XMLSpannable> spannableList = buildSpannable();
        if(spannableList.size()==0){
            return;
        }

        Map<String, TableString> stringsMap = stringPool
                .insertStrings(XMLSpannable.toTextList(spannableList));

        List<String> tagList =
                new ArrayList<>(XMLSpannable.tagList(spannableList));
        EncodeUtil.sortStrings(tagList);
        Map<String, TableString> tagsMap =
                stringPool.insertStrings(tagList);

        StyleArray styleArray = stringPool.getStyleArray();
        styleArray.setChildesCount(stringsMap.size());

        for(XMLSpannable spannable:spannableList){

            TableString tableString=stringsMap.get(spannable.getText());
            StyleItem styleItem = styleArray.get(tableString.getIndex());

            for(XMLSpanInfo spanInfo:spannable.getSpanInfoList()){
                int tagRef=tagsMap.get(spanInfo.tag).getIndex();
                styleItem.addStylePiece(tagRef, spanInfo.start, spanInfo.end);
            }
        }
        stringPool.refreshUniqueIdMap();
    }
    private List<XMLSpannable> buildSpannable(){
        List<XMLSpannable> results=new ArrayList<>();
        Set<String> removeList=new HashSet<>();
        for(String text:stringList){
            XMLSpannable spannable=XMLSpannable.parse(text);
            if(spannable!=null){
                results.add(spannable);
                removeList.add(text);
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
        String text = ValueDecoder
                .unEscapeSpecialCharacter(element.getTextContent());
        addString(text);
    }
    private void addString(String text){
        if(text!=null && text.length()>0 && text.charAt(0)!='@'){
            stringList.add(text);
        }
    }

}
