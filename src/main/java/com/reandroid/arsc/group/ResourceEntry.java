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
package com.reandroid.arsc.group;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class ResourceEntry implements Iterable<Entry>, Predicate<Entry> {
    private final int resourceId;
    private final PackageBlock packageBlock;

    public ResourceEntry(PackageBlock packageBlock, int resourceId){
        this.resourceId = resourceId;
        this.packageBlock = packageBlock;
    }

    public Entry getOrCreate(String qualifiers){
        return getOrCreate(ResConfig.parse(qualifiers));
    }
    public Entry getOrCreate(ResConfig resConfig){
        int resourceId = this.getResourceId();
        byte typeId = (byte)((resourceId >> 16) & 0xff);
        short entryId = (short)(resourceId & 0xffff);
        Entry entry = packageBlock.getOrCreateEntry(typeId, entryId, resConfig);
        String name = getName();
        if(name != null && entry.getName() ==  null){
            entry.setName(name, true);
        }
        return entry;
    }
    public Entry get(String qualifiers){
        return get(ResConfig.parse(qualifiers));
    }
    public Entry get(ResConfig resConfig){
        for(Entry entry : this){
            if(resConfig.equals(entry.getResConfig())){
                return entry;
            }
        }
        return null;
    }
    public Entry getEqualsOrMoreSpecific(ResConfig resConfig){
        Entry result = null;
        for(Entry entry : this){
            if(resConfig.equals(entry.getResConfig())){
                return entry;
            }
            if(result != null){
                continue;
            }
            if(entry.getResConfig().isEqualOrMoreSpecificThan(resConfig)){
                result = entry;
            }
        }
        return result;
    }
    public Entry get(){
        Entry result = null;
        for(Entry entry : this){
            if(entry.isDefault()){
                return entry;
            }
            if(result == null){
                result = entry;
            }
        }
        return result;
    }
    public Entry any(){
        Iterator<Entry> iterator = iterator(true);
        if(iterator.hasNext()){
            return iterator.next();
        }
        return null;
    }
    public boolean isEmpty() {
        return !iterator(true).hasNext();
    }
    public int getResourceId() {
        return resourceId;
    }
    public String getPackage(){
        return packageBlock.getPackageBlock().getName();
    }
    public String getType(){
        return packageBlock.typeNameOf((getResourceId() >> 16) & 0xff);
    }
    public void setName(String name){
        boolean hasEntry = false;
        SpecString specString = null;
        for (Entry entry : this) {
            if (specString != null) {
                entry.setSpecReference(specString);
                continue;
            }
            specString = entry.setName(name);
            hasEntry = true;
        }
        if(hasEntry){
            return;
        }
        Iterator<Entry> itr = iterator(false);
        if(!itr.hasNext()){
            return;
        }
        Entry entry = itr.next();
        entry.setName(name, true);
    }
    public String getName(){
        Iterator<Entry> itr = iterator(false);
        while (itr.hasNext()) {
            Entry entry = itr.next();
            String name = entry.getName();
            if (name != null) {
                return name;
            }
        }
        return null;
    }

    @Override
    public Iterator<Entry> iterator(){
        return iterator(true);
    }
    @Override
    public boolean test(Entry entry) {
        return entry != null && !entry.isNull();
    }
    public Iterator<Entry> iterator(boolean skipNull){
        return packageBlock.getEntries(getResourceId(), skipNull);
    }
    public String getHexId(){
        return HexUtil.toHex8(getResourceId());
    }
    public int count(){
        int result = 0;
        Iterator<Entry> itr = iterator(true);
        while (itr.hasNext()){
            if(!itr.next().isNull()){
                result ++;
            }
        }
        return result;
    }

    public boolean serializePublicXml(XmlSerializer serializer) throws IOException {
        if(isEmpty()){
            return false;
        }
        serializer.text("\n  ");
        serializer.startTag(null, PackageBlock.TAG_public);
        serializer.attribute(null, "id", getHexId());
        serializer.attribute(null, "type", getType());
        serializer.attribute(null, "name", getName());
        serializer.endTag(null, PackageBlock.TAG_public);
        return true;
    }

    @Override
    public int hashCode(){
        return getResourceId();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceEntry)) {
            return false;
        }
        ResourceEntry other = (ResourceEntry) obj;
        return this.getResourceId() == other.getResourceId();
    }

    @Override
    public String toString(){
        return getHexId() + " @" + getPackage()
                + ":" + getType() + "/" + getName();
    }

}
