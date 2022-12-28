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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameSpaceItem {
    private String prefix;
    private String namespaceUri;
    public NameSpaceItem(String prefix, String nsUri){
        this.prefix=prefix;
        this.namespaceUri=nsUri;
        validate();
    }
    public String toAttributeName(){
        return ATTR_PREFIX+":"+prefix;
    }
    public SchemaAttr toSchemaAttribute(){
        return new SchemaAttr(getPrefix(), getNamespaceUri());
    }
    public boolean isPrefixEqual(String p){
        if(XMLUtil.isEmpty(prefix)){
            return false;
        }
        return prefix.equals(p);
    }
    public boolean isUriEqual(String nsUri){
        if(XMLUtil.isEmpty(namespaceUri)){
            return false;
        }
        return namespaceUri.equals(nsUri);
    }
    public boolean isValid(){
        return isPrefixValid() && isUriValid();
    }
    private boolean validate(){
        boolean preOk=isPrefixValid();
        boolean uriOk=isUriValid();
        if(preOk && uriOk){
            if(!NAME_ANDROID.equals(prefix) && URI_ANDROID.equals(namespaceUri)){
                namespaceUri= URI_APP;
            }
            return true;
        }
        if(!preOk && !uriOk){
            return false;
        }
        if(!preOk){
            if(URI_ANDROID.equals(namespaceUri)){
                prefix= NAME_ANDROID;
            }else {
                prefix= NAME_APP;
            }
        }
        if(!uriOk){
            if(NAME_ANDROID.equals(prefix)){
                namespaceUri= URI_ANDROID;
            }else {
                namespaceUri= URI_APP;
            }
        }
        return true;
    }
    private boolean isPrefixValid(){
        return !XMLUtil.isEmpty(prefix);
    }
    private boolean isUriValid(){
        if(XMLUtil.isEmpty(namespaceUri)){
            return false;
        }
        Matcher matcher=PATTERN_URI.matcher(namespaceUri);
        return matcher.find();
    }
    public String getNamespaceUri() {
        return namespaceUri;
    }
    public String getPrefix() {
        return prefix;
    }
    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
        validate();
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        validate();
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof NameSpaceItem){
            return isUriEqual(((NameSpaceItem)o).namespaceUri);
        }
        return false;
    }
    @Override
    public int hashCode(){
        String u=namespaceUri;
        if(u==null){
            u="";
        }
        return u.hashCode();
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        boolean appendOnce=false;
        if(namespaceUri!=null){
            builder.append(namespaceUri);
            appendOnce=true;
        }
        if(prefix!=null){
            if(appendOnce){
                builder.append(':');
            }
            builder.append(prefix);
        }
        return builder.toString();
    }
    private static NameSpaceItem ns_android;
    private static NameSpaceItem ns_app;
    public static NameSpaceItem getAndroid(){
        if(ns_android==null){
            ns_android=new NameSpaceItem(NAME_ANDROID, URI_ANDROID);
        }
        return ns_android;
    }
    public static NameSpaceItem getApp(){
        if(ns_app==null){
            ns_app=new NameSpaceItem(NAME_APP, URI_APP);
        }
        return ns_app;
    }
    private static final Pattern PATTERN_URI=Pattern.compile("^https?://[^\\s/]+/[^\\s]+$");


    private static final String ATTR_PREFIX = "xmlns";
    private static final String URI_ANDROID = "http://schemas.android.com/apk/res/android";
    private static final String URI_APP = "http://schemas.android.com/apk/res-auto";
    private static final String NAME_ANDROID = "android";
    private static final String NAME_APP = "app";

}
