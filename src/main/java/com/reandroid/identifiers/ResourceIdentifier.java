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
package com.reandroid.identifiers;

import com.reandroid.arsc.util.HexUtil;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public class ResourceIdentifier extends Identifier{
    public ResourceIdentifier(int id, String name){
        super(id, name);
    }
    public ResourceIdentifier(){
        this(0, null);
    }


    public void write(XmlSerializer serializer) throws IOException {
        serializer.text("\n  ");
        serializer.startTag(null, XML_TAG_PUBLIC);
        serializer.attribute(null, XML_ATTRIBUTE_ID, getHexId());
        serializer.attribute(null, TypeIdentifier.XML_ATTRIBUTE_TYPE, getTypeName());
        serializer.attribute(null, XML_ATTRIBUTE_NAME, getName());
        serializer.endTag(null, XML_TAG_PUBLIC);
    }
    public TypeIdentifier getTypeIdentifier() {
        return (TypeIdentifier) getParent();
    }
    public void setTypeIdentifier(TypeIdentifier typeIdentifier) {
        setParent(typeIdentifier);
    }
    public PackageIdentifier getPackageIdentifier(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageIdentifier();
        }
        return null;
    }
    public String getTypeName(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getName();
        }
        return null;
    }
    public String getPackageName(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageName();
        }
        return null;
    }
    public int getTypeId(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getId();
        }
        return 0;
    }
    public int getPackageId(){
        TypeIdentifier typeIdentifier = getTypeIdentifier();
        if(typeIdentifier != null){
            return typeIdentifier.getPackageId();
        }
        return 0;
    }
    public int getResourceId(){
        int resourceId = getPackageId() << 24;
        resourceId |= getTypeId() << 16;
        resourceId |= getId();
        return resourceId;
    }
    @Override
    public void setId(int id) {
        super.setId(id & 0xffff);
    }
    @Override
    public String getHexId(){
        return HexUtil.toHex8(getResourceId());
    }
    public String getResourceName(){
        return getResourceName(null);
    }
    public String getResourceName(PackageIdentifier context){
        boolean appendPackage = context != getPackageIdentifier();
        return getResourceName('@', appendPackage, true);
    }
    public String getResourceName(char prefix, boolean appendPackage, boolean appendType){
        String packageName = appendPackage ? getPackageName() : null;
        String typeName = appendType ? getTypeName() : null;
        return buildResourceName(prefix, packageName, typeName, getName());
    }
    @Override
    long getUniqueId(){
        return 0x00000000ffffffffL & this.getResourceId();
    }
    @Override
    public void setTag(Object tag){
        TypeIdentifier ti = getTypeIdentifier();
        if(ti == null){
            super.setTag(tag);
            return;
        }
        Object exist = getTag();
        if(exist != null){
            ti.removeTag(exist);
        }
        ti.addTag(tag, this);
        super.setTag(tag);
    }
    @Override
    public String toString(){
        return getHexId() + " " + getResourceName();
    }

    public static String buildResourceName(char prefix, String packageName, String type, String entry){
        StringBuilder builder = new StringBuilder();
        if(prefix != 0){
            builder.append(prefix);
        }
        if(packageName != null){
            builder.append(packageName);
            builder.append(':');
        }
        if(type != null){
            builder.append(type);
            builder.append('/');
        }
        builder.append(entry);
        return builder.toString();
    }

}
