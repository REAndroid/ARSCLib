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

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.FrameworkApk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.TypeBlock;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.decoder.ValueDecoder;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.item.SpecString;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.util.ResNameMap;
import com.reandroid.arsc.value.Entry;
import com.reandroid.identifiers.PackageIdentifier;
import com.reandroid.identifiers.ResourceIdentifier;
import com.reandroid.identifiers.TableIdentifier;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class EncodeMaterials {
    private PackageBlock currentPackage;
    private final Set<FrameworkTable> frameworkTables = new HashSet<>();
    private APKLogger apkLogger;
    private boolean mForceCreateNamespaces = true;
    private Set<String> mFrameworkPackageNames;
    private final ResNameMap<Entry> mLocalResNameMap = new ResNameMap<>();
    private final TableIdentifier tableIdentifier = new TableIdentifier();
    private PackageIdentifier currentPackageIdentifier;
    private Integer mMainPackageId;
    public EncodeMaterials(){
    }
    public void setMainPackageId(Integer mainPackageId){
        this.mMainPackageId = mainPackageId;
    }
    public PackageBlock pickMainPackageBlock(TableBlock tableBlock){
        if(mMainPackageId != null){
            return tableBlock.pickOne(mMainPackageId);
        }
        return tableBlock.pickOne();
    }
    public TableIdentifier getTableIdentifier(){
        return tableIdentifier;
    }
    public void setEntryName(Entry entry, String name){
        PackageBlock packageBlock = entry.getPackageBlock();
        SpecString specString = packageBlock
                .getSpecStringPool().getOrCreate(name);
        entry.setSpecReference(specString);
    }
    public SpecString getSpecString(String name){
        return currentPackage.getSpecStringPool()
                .get(name)
                .get(0);
    }
    public Entry getAttributeBlock(String refString){
        String type = "attr";
        Entry entry = getAttributeBlock(type, refString);
        if(entry == null){
            type = "^attr-private";
            entry = getAttributeBlock(type, refString);
        }
        return entry;
    }
    private Entry getAttributeBlock(String type, String refString){
        String packageName = null;
        String name = refString;
        int i=refString.lastIndexOf(':');
        if(i>=0){
            packageName=refString.substring(0, i);
            name=refString.substring(i+1);
        }
        if(EncodeUtil.isEmpty(packageName)
                || packageName.equals(getCurrentPackageName())
                || !isFrameworkPackageName(packageName)){

            return getLocalEntry(type, name);
        }
        return getFrameworkEntry(type, name);
    }
    public int resolveReference(String refString){
        if("@null".equals(refString)){
            return 0;
        }
        Matcher matcher = ValueDecoder.PATTERN_REFERENCE.matcher(refString);
        if(!matcher.find()){
            ValueDecoder.EncodeResult ref = ValueDecoder.encodeHexReference(refString);
            if(ref!=null){
                return ref.value;
            }
            ref = ValueDecoder.encodeNullReference(refString);
            if(ref!=null){
                return ref.value;
            }
            throw new EncodeException(
                    "Not proper reference string: '"+refString+"'");
        }
        String prefix=matcher.group(1);
        String packageName = matcher.group(2);
        if(packageName!=null && packageName.endsWith(":")){
            packageName=packageName.substring(0, packageName.length()-1);
        }
        String type = matcher.group(4);
        String name = matcher.group(5);
        if(isLocalPackageName(packageName)){
            return resolveLocalResourceId(packageName, type, name);
        }

        if(EncodeUtil.isEmpty(packageName)
                || packageName.equals(getCurrentPackageName())
                || !isFrameworkPackageName(packageName)){
            return resolveLocalResourceId(type, name);
        }
        return resolveFrameworkResourceId(packageName, type, name);
    }
    private int resolveLocalResourceId(String packageName, String type, String name){
        ResourceIdentifier ri = tableIdentifier.get(packageName, type, name);
        if(ri != null){
            return ri.getResourceId();
        }
        EntryGroup entryGroup=getLocalEntryGroup(type, name);
        if(entryGroup!=null){
            return entryGroup.getResourceId();
        }
        throw new EncodeException("Local entry not found: " +
                "package=" + packageName +
                ", type=" + type +
                ", name=" + name);
    }
    public int resolveLocalResourceId(String type, String name){
        PackageIdentifier pi = this.currentPackageIdentifier;
        if(pi != null){
            ResourceIdentifier ri = pi.getResourceIdentifier(type, name);
            if(ri != null){
                return ri.getResourceId();
            }
        }
        EntryGroup entryGroup=getLocalEntryGroup(type, name);
        if(entryGroup!=null){
            return entryGroup.getResourceId();
        }
        throw new EncodeException("Local entry not found: " +
                "type="+type+
                ", name="+name);
    }
    public int resolveFrameworkResourceId(String packageName, String type, String name){
        Entry entry = getFrameworkEntry(packageName, type, name);
        if(entry !=null){
            return entry.getResourceId();
        }
        throw new EncodeException("Framework entry not found: " +
                "package="+packageName+
                ", type="+type+
                ", name="+name);
    }
    public int resolveFrameworkResourceId(int packageId, String type, String name){
        Entry entry = getFrameworkEntry(packageId, type, name);
        if(entry !=null){
            return entry.getResourceId();
        }
        throw new EncodeException("Framework entry not found: " +
                "packageId=" + HexUtil.toHex2((byte) packageId)+
                ", type="+type+
                ", name="+name);
    }
    public EntryGroup getLocalEntryGroup(String type, String name){
        for(EntryGroup entryGroup : currentPackage.listEntryGroup()){
            if(type.equals(entryGroup.getTypeName()) &&
                    name.equals(entryGroup.getSpecName())){
                return entryGroup;
            }
        }
        for(PackageBlock packageBlock:currentPackage.getTableBlock().listPackages()){
            for(EntryGroup entryGroup : packageBlock.listEntryGroup()){
                if(type.equals(entryGroup.getTypeName()) &&
                        name.equals(entryGroup.getSpecName())){
                    return entryGroup;
                }
            }
        }
        return null;
    }
    public Entry getLocalEntry(String type, String name){
        Entry entry =mLocalResNameMap.get(type, name);
        if(entry !=null){
            return entry;
        }
        loadLocalEntryMap(type);
        entry =mLocalResNameMap.get(type, name);
        if(entry !=null){
            return entry;
        }
        entry = searchLocalEntry(type, name);
        if(entry !=null){
            mLocalResNameMap.add(type, name, entry);
        }
        return entry;
    }
    private Entry searchLocalEntry(String type, String name){
        for(EntryGroup entryGroup : currentPackage.listEntryGroup()){
            if(type.equals(entryGroup.getTypeName()) &&
                    name.equals(entryGroup.getSpecName())){
                return entryGroup.pickOne();
            }
        }
        SpecTypePair specTypePair=currentPackage.getSpecTypePair(type);
        if(specTypePair!=null){
            for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                for(Entry entry :typeBlock.listEntries(true)){
                    if(name.equals(entry.getName())){
                        return entry;
                    }
                }
                break;
            }
        }
        for(PackageBlock packageBlock:currentPackage.getTableBlock().listPackages()){
            if(packageBlock==currentPackage){
                continue;
            }
            specTypePair=packageBlock.getSpecTypePair(type);
            if(specTypePair!=null){
                for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                    for(Entry entry :typeBlock.listEntries(true)){
                        if(name.equals(entry.getName())){
                            return entry;
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
    private void loadLocalEntryMap(String type){
        ResNameMap<Entry> localMap = mLocalResNameMap;
        for(PackageBlock packageBlock:currentPackage.getTableBlock().listPackages()){
            SpecTypePair specTypePair=packageBlock.getSpecTypePair(type);
            if(specTypePair!=null){
                for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                    for(Entry entry :typeBlock.listEntries(true)){
                        localMap.add(entry.getTypeName(),
                                entry.getName(), entry);
                    }
                }
            }
        }
    }
    public Entry getFrameworkEntry(String type, String name){
        for(FrameworkTable table:frameworkTables){
            Entry entry = table.searchEntry(type, name);
            if(entry !=null){
                return entry;
            }
        }
        return null;
    }
    private boolean isFrameworkPackageName(String packageName){
        return getFrameworkPackageNames().contains(packageName);
    }
    private Set<String> getFrameworkPackageNames(){
        if(mFrameworkPackageNames!=null){
            return mFrameworkPackageNames;
        }
        Set<String> results=new HashSet<>();
        for(FrameworkTable table:frameworkTables){
            for(PackageBlock packageBlock:table.listPackages()){
                results.add(packageBlock.getName());
            }
        }
        mFrameworkPackageNames=results;
        return results;
    }
    public Entry getFrameworkEntry(String packageName, String type, String name){
        for(FrameworkTable table:frameworkTables){
            for(PackageBlock packageBlock:table.listPackages()){
                if(packageName.equals(packageBlock.getName())){
                    Entry entry = table.searchEntry(type, name);
                    if(entry !=null){
                        return entry;
                    }
                }
            }
        }
        return null;
    }
    public Entry getFrameworkEntry(int packageId, String type, String name){
        for(FrameworkTable table:frameworkTables){
            for(PackageBlock packageBlock:table.listPackages()){
                if(packageId==packageBlock.getId()){
                    Entry entry = table.searchEntry(type, name);
                    if(entry !=null){
                        return entry;
                    }
                }
            }
        }
        return null;
    }
    public EncodeMaterials setForceCreateNamespaces(boolean force) {
        this.mForceCreateNamespaces = force;
        return this;
    }
    public EncodeMaterials setCurrentPackage(PackageBlock currentPackage) {
        this.currentPackage = currentPackage;
        onCurrentPackageChanged(currentPackage);
        return this;
    }
    public EncodeMaterials setCurrentLocalPackage(PackageIdentifier packageIdentifier) {
        this.currentPackageIdentifier = packageIdentifier;
        return this;
    }
    private void onCurrentPackageChanged(PackageBlock currentPackage){
        if(currentPackage == null){
            return;
        }
        PackageIdentifier pi = tableIdentifier.getByPackage(currentPackage);
        if(pi != null){
            this.currentPackageIdentifier = pi;
        }
    }
    private boolean isLocalPackageName(String packageName){
        if(packageName == null){
            return false;
        }
        for(PackageIdentifier pi : tableIdentifier.getPackages()){
            if(packageName.equals(pi.getName())){
                return true;
            }
        }
        return false;
    }
    private boolean isUniquePackageNames(){
        Set<String> names = new HashSet<>();
        for(PackageIdentifier pi : tableIdentifier.getPackages()){
            names.add(pi.getName());
        }
        return names.size() == tableIdentifier.getPackages().size();
    }
    private boolean isUniquePackageIds(){
        Set<Integer> ids = new HashSet<>();
        for(PackageIdentifier pi : tableIdentifier.getPackages()){
            ids.add(pi.getId());
        }
        return ids.size() == tableIdentifier.getPackages().size();
    }
    public EncodeMaterials addFramework(FrameworkApk frameworkApk) {
        if(frameworkApk!=null){
            addFramework(frameworkApk.getTableBlock());
        }
        return this;
    }
    public EncodeMaterials addFramework(FrameworkTable frameworkTable) {
        frameworkTable.loadResourceNameMap();
        this.frameworkTables.add(frameworkTable);
        this.mFrameworkPackageNames=null;
        return this;
    }
    public EncodeMaterials setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
        return this;
    }

    public PackageBlock getCurrentPackage() {
        return currentPackage;
    }
    public boolean isForceCreateNamespaces() {
        return mForceCreateNamespaces;
    }

    public String getCurrentPackageName(){
        return currentPackage.getName();
    }
    public int getCurrentPackageId(){
        return currentPackage.getId();
    }

    public void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    public void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    public void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
    public static EncodeMaterials create(TableBlock tableBlock){
        PackageBlock packageBlock = tableBlock.pickOne();
        if(packageBlock==null){
            throw new EncodeException("No packages found on table block");
        }
        return create(packageBlock);
    }
    public static EncodeMaterials create(PackageBlock packageBlock){
        EncodeMaterials encodeMaterials = new EncodeMaterials();

        TableBlock tableBlock = packageBlock.getTableBlock();
        encodeMaterials.getTableIdentifier().load(tableBlock);
        encodeMaterials.setCurrentPackage(packageBlock);

        for(TableBlock frameworkTable:tableBlock.getFrameWorks()){
            if(frameworkTable instanceof FrameworkTable){
                encodeMaterials.addFramework((FrameworkTable) frameworkTable);
            }
        }
        return encodeMaterials;
    }

}
