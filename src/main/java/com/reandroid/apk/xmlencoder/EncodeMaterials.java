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
 import com.reandroid.apk.ResourceIds;
 import com.reandroid.arsc.chunk.PackageBlock;
 import com.reandroid.arsc.chunk.TableBlock;
 import com.reandroid.arsc.chunk.TypeBlock;
 import com.reandroid.arsc.container.SpecTypePair;
 import com.reandroid.arsc.decoder.ValueDecoder;
 import com.reandroid.arsc.group.EntryGroup;
 import com.reandroid.arsc.item.SpecString;
 import com.reandroid.arsc.util.FrameworkTable;
 import com.reandroid.arsc.util.ResNameMap;
 import com.reandroid.arsc.value.EntryBlock;
 import com.reandroid.common.Frameworks;
 import com.reandroid.common.ResourceResolver;

 import java.util.HashSet;
 import java.util.Set;
 import java.util.regex.Matcher;

 public class EncodeMaterials implements ResourceResolver {
     private final Set<ResourceIds.Table.Package> packageIdSet = new HashSet<>();
     private PackageBlock currentPackage;
     private final Set<FrameworkTable> frameworkTables = new HashSet<>();
     private APKLogger apkLogger;
     private boolean mForceCreateNamespaces = true;
     private Set<String> mFrameworkPackageNames;
     private final ResNameMap<EntryBlock> mLocalResNameMap = new ResNameMap<>();
     public EncodeMaterials(){
     }
     public SpecString getSpecString(String name){
         return currentPackage.getSpecStringPool()
                 .get(name)
                 .get(0);
     }
     public EntryBlock getAttributeBlock(String refString){
         String packageName = null;
         String type = "attr";
         String name = refString;
         int i=refString.lastIndexOf(':');
         if(i>=0){
             packageName=refString.substring(0, i);
             name=refString.substring(i+1);
         }
         if(EncodeUtil.isEmpty(packageName)
                 || packageName.equals(getCurrentPackageName())
                 || !isFrameworkPackageName(packageName)){

             return getLocalEntryBlock(type, name);
         }
         return getFrameworkEntry(type, name);
     }
     public int resolveReference(String refString){
         if("@null".equals(refString)){
             return 0;
         }
         Matcher matcher = ValueDecoder.PATTERN_REFERENCE.matcher(refString);
         if(!matcher.find()){
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
         if(EncodeUtil.isEmpty(packageName)
                 || packageName.equals(getCurrentPackageName())
                 || !isFrameworkPackageName(packageName)){
             return resolveLocalResourceId(type, name);
         }
         return resolveFrameworkResourceId(packageName, type, name);
     }
     public int resolveLocalResourceId(String type, String name){
         for(ResourceIds.Table.Package pkg:packageIdSet){
             Integer resId = pkg.getResourceId(type, name);
             if(resId!=null){
                 return resId;
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
         EntryBlock entryBlock = getFrameworkEntry(packageName, type, name);
         if(entryBlock!=null){
             return entryBlock.getResourceId();
         }
         throw new EncodeException("Framework entry not found: " +
                 "package="+packageName+
                 ", type="+type+
                 ", name="+name);
     }
     public int resolveFrameworkResourceId(int packageId, String type, String name){
         EntryBlock entryBlock = getFrameworkEntry(packageId, type, name);
         if(entryBlock!=null){
             return entryBlock.getResourceId();
         }
         throw new EncodeException("Framework entry not found: " +
                 "packageId="+String.format("0x%02x", packageId)+
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
     public EntryBlock getLocalEntryBlock(String type, String name){
         EntryBlock entryBlock=mLocalResNameMap.get(type, name);
         if(entryBlock!=null){
             return entryBlock;
         }
         loadLocalEntryBlockMap(type);
         entryBlock=mLocalResNameMap.get(type, name);
         if(entryBlock!=null){
             return entryBlock;
         }
         entryBlock= searchLocalEntryBlock(type, name);
         if(entryBlock!=null){
             mLocalResNameMap.add(type, name, entryBlock);
         }
         return entryBlock;
     }
     private EntryBlock searchLocalEntryBlock(String type, String name){
         for(EntryGroup entryGroup : currentPackage.listEntryGroup()){
             if(type.equals(entryGroup.getTypeName()) &&
                     name.equals(entryGroup.getSpecName())){
                 return entryGroup.pickOne();
             }
         }
         SpecTypePair specTypePair=currentPackage.searchByTypeName(type);
         if(specTypePair!=null){
             for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                 for(EntryBlock entryBlock:typeBlock.listEntries(true)){
                     if(name.equals(entryBlock.getName())){
                         return entryBlock;
                     }
                 }
                 break;
             }
         }
         for(PackageBlock packageBlock:currentPackage.getTableBlock().listPackages()){
             if(packageBlock==currentPackage){
                 continue;
             }
             specTypePair=packageBlock.searchByTypeName(type);
             if(specTypePair!=null){
                 for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                     for(EntryBlock entryBlock:typeBlock.listEntries(true)){
                         if(name.equals(entryBlock.getName())){
                             return entryBlock;
                         }
                     }
                     break;
                 }
             }
         }
         return null;
     }
     private void loadLocalEntryBlockMap(String type){
         ResNameMap<EntryBlock> localMap = mLocalResNameMap;
         for(PackageBlock packageBlock:currentPackage.getTableBlock().listPackages()){
             SpecTypePair specTypePair=packageBlock.searchByTypeName(type);
             if(specTypePair!=null){
                 for(TypeBlock typeBlock:specTypePair.listTypeBlocks()){
                     for(EntryBlock entryBlock:typeBlock.listEntries(true)){
                         localMap.add(entryBlock.getTypeName(),
                                 entryBlock.getName(), entryBlock);
                     }
                 }
             }
         }
     }
     public EntryBlock getFrameworkEntry(String type, String name){
         for(FrameworkTable table:frameworkTables){
             EntryBlock entryBlock = table.searchEntryBlock(type, name);
             if(entryBlock!=null){
                 return entryBlock;
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
     public EntryBlock getFrameworkEntry(String packageName, String type, String name){
         for(FrameworkTable table:frameworkTables){
             for(PackageBlock packageBlock:table.listPackages()){
                 if(packageName.equals(packageBlock.getName())){
                     EntryBlock entryBlock = table.searchEntryBlock(type, name);
                     if(entryBlock!=null){
                         return entryBlock;
                     }
                 }
             }
         }
         return null;
     }
     public EntryBlock getFrameworkEntry(int packageId, String type, String name){
         for(FrameworkTable table:frameworkTables){
             for(PackageBlock packageBlock:table.listPackages()){
                 if(packageId==packageBlock.getId()){
                     EntryBlock entryBlock = table.searchEntryBlock(type, name);
                     if(entryBlock!=null){
                         return entryBlock;
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
     public EncodeMaterials addPackageIds(ResourceIds.Table.Package packageIds) {
         packageIds.loadEntryMap();
         this.packageIdSet.add(packageIds);
         return this;
     }
     public EncodeMaterials setCurrentPackage(PackageBlock currentPackage) {
         this.currentPackage = currentPackage;
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

     @Override
     public int resolveResourceId(String packageName, String type, String name) {
         if(!isFrameworkPackageName(packageName)){
             return resolveLocalResourceId(type, name);
         }
         return resolveFrameworkResourceId(packageName, type, name);
     }
     @Override
     public int resolveResourceId(int packageId, String type, String name) {
         if(packageId==getCurrentPackageId()){
             return resolveLocalResourceId(type, name);
         }
         return resolveFrameworkResourceId(packageId, type, name);
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
         ResourceIds resourceIds = new ResourceIds();
         resourceIds.loadPackageBlock(packageBlock);
         ResourceIds.Table.Package packageId = resourceIds.getTable().listPackages().get(0);
         return new EncodeMaterials()
                 .addPackageIds(packageId)
                 .setCurrentPackage(packageBlock)
                 .addFramework(Frameworks.getAndroid());
     }

 }
