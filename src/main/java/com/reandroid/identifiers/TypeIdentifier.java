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

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TypeIdentifier extends IdentifierMap<ResourceIdentifier> {
    private final Map<Object, ResourceIdentifier> tagMap;
    public TypeIdentifier(int id, String name){
        super(id, name);
        this.tagMap = new HashMap<>();
    }
    public TypeIdentifier(){
        this(0, null);
    }


    public void write(XmlSerializer serializer) throws IOException {
        for(ResourceIdentifier resourceIdentifier : list()){
            resourceIdentifier.write(serializer);
        }
    }
    public PackageIdentifier getPackageIdentifier() {
        return (PackageIdentifier) getParent();
    }
    public void setPackageIdentifier(PackageIdentifier packageIdentifier) {
        setParent(packageIdentifier);
    }
    public String getPackageName(){
        PackageIdentifier packageIdentifier = getPackageIdentifier();
        if(packageIdentifier != null){
            return packageIdentifier.getName();
        }
        return null;
    }
    public int getPackageId(){
        PackageIdentifier packageIdentifier = getPackageIdentifier();
        if(packageIdentifier != null){
            return packageIdentifier.getId();
        }
        return 0;
    }

    @Override
    public ResourceIdentifier getByTag(Object tag){
        ResourceIdentifier ri = this.tagMap.get(tag);
        if(ri != null){
            return ri;
        }
        return super.getByTag(tag);
    }
    @Override
    public void clear(){
        tagMap.clear();
        super.clear();
    }
    @Override
    long getUniqueId(){
        int uniqueId = getPackageId() << 8;
        uniqueId |= getId();
        return uniqueId;
    }
    void addTag(Object tag, ResourceIdentifier ri){
        if(tag != null){
            tagMap.put(tag, ri);
        }
    }
    void removeTag(Object tag){
        tagMap.remove(tag);
    }

}
