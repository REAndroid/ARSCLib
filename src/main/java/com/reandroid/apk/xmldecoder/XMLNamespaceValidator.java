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

import com.reandroid.arsc.chunk.xml.*;

import java.util.Collection;

public class XMLNamespaceValidator {
    private static final String URI_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String URI_APP = "http://schemas.android.com/apk/res-auto";
    private static final String PREFIX_ANDROID = "android";
    private static final String PREFIX_APP = "app";
    private final ResXmlDocument xmlBlock;
    public XMLNamespaceValidator(ResXmlDocument xmlBlock){
        this.xmlBlock=xmlBlock;
    }
    public void validate(){
        validateNamespaces(xmlBlock);
    }

    public static boolean isValid(ResXmlAttribute attribute){
        int resourceId = attribute.getNameResourceID();
        if(resourceId == 0){
            return attribute.getUri() == null;
        }
        if(isAndroid(toPackageId(resourceId))){
            return isValidAndroidNamespace(attribute);
        }else {
            return isValidAppNamespace(attribute);
        }
    }
    public static void validateNamespaces(ResXmlDocument resXmlDocument){
        validateNamespaces(resXmlDocument.getResXmlElement());
    }
    public static void validateNamespaces(ResXmlElement element){
        validateNamespaces(element.listAttributes());
        for(ResXmlElement child : element.listElements()){
            validateNamespaces(child);
        }
    }

    private static void validateNamespaces(Collection<ResXmlAttribute> attributeList){
        for(ResXmlAttribute attribute : attributeList){
            validateNamespace(attribute);
        }
    }
    private static void validateNamespace(ResXmlAttribute attribute){
        int resourceId = attribute.getNameResourceID();
        if(resourceId == 0){
            attribute.setNamespaceReference(-1);
            return;
        }
        if(isAndroid(toPackageId(resourceId))){
            if(!isValidAndroidNamespace(attribute)){
                attribute.setNamespace(URI_ANDROID, PREFIX_ANDROID);
            }
        }else {
            if(!isValidAppNamespace(attribute)){
                attribute.setNamespace(URI_APP, PREFIX_APP);
            }
        }
    }

    private static boolean isValidAppNamespace(ResXmlAttribute attribute){
        String uri = attribute.getUri();
        String prefix = attribute.getNamePrefix();
        if(URI_ANDROID.equals(uri) || PREFIX_ANDROID.equals(prefix)){
            return false;
        }
        if(isEmpty(uri) || isEmpty(prefix)){
            return false;
        }
        return true;
    }
    private static boolean isValidAndroidNamespace(ResXmlAttribute attribute){
        return URI_ANDROID.equals(attribute.getUri())
                && PREFIX_ANDROID.equals(attribute.getNamePrefix());
    }

    private static boolean isAndroid(int pkgId){
        return pkgId==1;
    }
    private static int toPackageId(int resId){
        return (resId >> 24 & 0xFF);
    }
    private static boolean isEmpty(String str){
        if(str==null){
            return true;
        }
        str=str.trim();
        return str.length()==0;
    }
}
