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

import com.reandroid.arsc.array.PackageArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.util.*;

public class TableIdentifier{
    private final List<PackageIdentifier> mPackages;
    private final Map<String, PackageIdentifier> mNameMap;
    public TableIdentifier() {
        this.mPackages = new ArrayList<>();
        this.mNameMap = new HashMap<>();
    }

    public void initialize(TableBlock tableBlock){
        initialize(tableBlock, true);
    }
    public void initialize(TableBlock tableBlock, boolean initialize_ids){
        PackageArray packageArray = tableBlock.getPackageArray();
        for(PackageIdentifier pi : getPackages()){
            PackageBlock packageBlock = packageArray.createNext();
            pi.initialize(packageBlock, initialize_ids);
        }
    }

    public void load(TableBlock tableBlock){
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            load(packageBlock);
        }
    }
    public PackageIdentifier load(PackageBlock packageBlock){
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.load(packageBlock);
        add(packageIdentifier);
        mNameMap.put(packageIdentifier.getName(), packageIdentifier);
        return packageIdentifier;
    }
    public void loadPublicXml(Collection<File> pubXmlFileList) throws IOException {
        for(File file : pubXmlFileList){
            try {
                loadPublicXml(file);
            } catch (XmlPullParserException ex) {
                throw new IOException(ex);
            }
        }
    }
    public PackageIdentifier loadPublicXml(File file) throws IOException, XmlPullParserException {
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.loadPublicXml(file);
        add(packageIdentifier);
        packageIdentifier.setTag(file);
        return packageIdentifier;
    }
    public PackageIdentifier loadPublicXml(InputStream inputStream) throws IOException, XmlPullParserException {
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.loadPublicXml(inputStream);
        add(packageIdentifier);
        return packageIdentifier;
    }
    public PackageIdentifier loadPublicXml(Reader reader) throws IOException, XmlPullParserException {PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.loadPublicXml(reader);
        add(packageIdentifier);
        return packageIdentifier;
    }
    public PackageIdentifier loadPublicXml(XmlPullParser parser) throws IOException, XmlPullParserException {
        PackageIdentifier packageIdentifier = new PackageIdentifier();
        packageIdentifier.loadPublicXml(parser);
        add(packageIdentifier);
        return packageIdentifier;
    }
    public ResourceIdentifier get(String packageName, String type, String name){
        PackageIdentifier packageIdentifier = mNameMap.get(packageName);
        if(packageIdentifier != null){
            ResourceIdentifier ri = packageIdentifier.getResourceIdentifier(type, name);
            if(ri != null){
                return ri;
            }
        }
        for(PackageIdentifier pi : getPackages()){
            if(Objects.equals(packageName, pi.getName())){
                ResourceIdentifier ri = pi.getResourceIdentifier(type, name);
                if(ri != null){
                    return ri;
                }
            }
        }
        return null;
    }
    public ResourceIdentifier get(String type, String name){
        for(PackageIdentifier pi : getPackages()){
            ResourceIdentifier ri = pi.getResourceIdentifier(type, name);
            if(ri != null){
                return ri;
            }
        }
        return null;
    }
    public int countPackages(){
        return getPackages().size();
    }
    public void add(PackageIdentifier packageIdentifier){
        if(packageIdentifier != null){
            mPackages.add(packageIdentifier);
        }
    }
    public List<PackageIdentifier> getPackages() {
        return mPackages;
    }
    public PackageIdentifier getByTag(Object tag){
        for(PackageIdentifier pi : getPackages()){
            if(Objects.equals(tag, pi.getTag())){
                return pi;
            }
        }
        return null;
    }
    public PackageIdentifier getByPackage(PackageBlock packageBlock){
        for(PackageIdentifier pi : getPackages()){
            if(packageBlock == pi.getPackageBlock()){
                return pi;
            }
        }
        return null;
    }
    public void clear(){
        for(PackageIdentifier identifier : getPackages()){
            identifier.clear();
        }
        mPackages.clear();
        mNameMap.clear();
    }

    @Override
    public String toString(){
        return getClass().getSimpleName()
                + ": packages = "
                + countPackages();
    }
}
