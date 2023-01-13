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
package com.reandroid.lib.apk.xmldecoder;

import com.reandroid.lib.arsc.chunk.xml.*;

import java.util.ArrayList;
import java.util.List;

public class XMLNamespaceValidator {
    private static final String URI_ANDROID="http://schemas.android.com/apk/res/android";
    private static final String URI_APP="http://schemas.android.com/apk/res-auto";
    private static final String PREFIX_ANDROID ="android";
    private static final String PREFIX_APP="app";
    private final ResXmlBlock xmlBlock;
    private List<ResXmlAttribute> mAttributeList;
    private ResXmlElement mRootElement;
    private ResXmlStartNamespace mNsAndroid;
    private ResXmlStartNamespace mNsApp;
    public XMLNamespaceValidator(ResXmlBlock xmlBlock){
        this.xmlBlock=xmlBlock;
    }
    public void validate(){
        if(getRootElement()==null){
            return;
        }
        for(ResXmlAttribute attribute:getAttributes()){
            validate(attribute);
        }
    }
    private void validate(ResXmlAttribute attribute){
        int resourceId=attribute.getNameResourceID();
        if(resourceId==0){
            removeNamespace(attribute);
            return;
        }
        int pkgId=toPackageId(resourceId);
        if(isAndroid(pkgId)){
            setAndroidNamespace(attribute);
        }else {
            setAppNamespace(attribute);
        }
    }
    private void removeNamespace(ResXmlAttribute attribute){
        attribute.setNamespaceReference(-1);
    }
    private void setAppNamespace(ResXmlAttribute attribute){
        if(isValidAppNamespace(attribute)){
            return;
        }
        ResXmlStartNamespace ns = getOrCreateApp();
        attribute.setNamespaceReference(ns.getUriReference());
    }
    private boolean isValidAppNamespace(ResXmlAttribute attribute){
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
    private void setAndroidNamespace(ResXmlAttribute attribute){
        if(URI_ANDROID.equals(attribute.getUri())
                && PREFIX_ANDROID.equals(attribute.getNamePrefix())){
            return;
        }
        ResXmlStartNamespace ns = getOrCreateAndroid();
        attribute.setNamespaceReference(ns.getUriReference());
    }
    private ResXmlStartNamespace getOrCreateApp(){
        if(mNsApp !=null){
            return mNsApp;
        }
        ResXmlElement root=getRootElement();
        ResXmlStartNamespace ns = root.getOrCreateNamespace(URI_APP, PREFIX_APP);
        String prefix=ns.getPrefix();
        if(PREFIX_ANDROID.equals(prefix) || isEmpty(prefix)){
            ns.setPrefix(PREFIX_APP);
        }
        mNsApp = ns;
        return mNsApp;
    }
    private ResXmlStartNamespace getOrCreateAndroid(){
        if(mNsAndroid !=null){
            return mNsAndroid;
        }
        ResXmlElement root=getRootElement();
        ResXmlStartNamespace ns = root.getOrCreateNamespace(URI_ANDROID, PREFIX_ANDROID);
        if(!PREFIX_ANDROID.equals(ns.getPrefix())){
            ns.setPrefix(PREFIX_ANDROID);
        }
        mNsAndroid =ns;
        return mNsAndroid;
    }
    private ResXmlElement getRootElement() {
        if(mRootElement==null){
            mRootElement = xmlBlock.getResXmlElement();
        }
        return mRootElement;
    }
    private List<ResXmlAttribute> getAttributes(){
        if(mAttributeList==null){
            mAttributeList=listAttributes(xmlBlock.getResXmlElement());
        }
        return mAttributeList;
    }
    private List<ResXmlAttribute> listAttributes(ResXmlElement element){
        List<ResXmlAttribute> results = new ArrayList<>(element.listAttributes());
        for(ResXmlElement child:element.listElements()){
            results.addAll(listAttributes(child));
        }
        return results;
    }
    private boolean isAndroid(int pkgId){
        return pkgId==1;
    }
    private int toPackageId(int resId){
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
