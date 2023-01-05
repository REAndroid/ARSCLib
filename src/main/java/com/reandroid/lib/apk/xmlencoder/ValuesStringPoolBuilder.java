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

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ValuesStringPoolBuilder {
    private final Set<String> stringList;
    public ValuesStringPoolBuilder(){
        this.stringList=new HashSet<>();
    }
    public void addTo(TableStringPool stringPool){
        stringPool.addStrings(stringList);
        stringList.clear();
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
