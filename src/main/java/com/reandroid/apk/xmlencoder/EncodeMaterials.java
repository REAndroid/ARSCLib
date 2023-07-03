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
import com.reandroid.arsc.coder.*;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.model.FrameworkTable;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.value.Entry;

import java.util.HashSet;
import java.util.Set;

public class EncodeMaterials {
    private PackageBlock currentPackage;
    private final Set<FrameworkTable> frameworkTables = new HashSet<>();
    private APKLogger apkLogger;
    private boolean mForceCreateNamespaces = true;
    private Set<String> mFrameworkPackageNames;
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
        int i = refString.lastIndexOf(':');
        if(i>=0){
            packageName=refString.substring(0, i);
            name=refString.substring(i+1);
        }
        if(!EncodeUtil.isEmpty(packageName) &&  !isFrameworkPackageName(packageName)){
            Entry entry = getLocalEntry(packageName, type, name);
            if(entry != null){
                return entry;
            }
        }
        if(EncodeUtil.isEmpty(packageName)
                || packageName.equals(getCurrentPackageName())
                || !isFrameworkPackageName(packageName)){

            return getLocalEntry(type, name);
        }
        return getFrameworkEntry(type, name);
    }

    public EncodeResult encodeReference(String value){
        if(value == null || value.length() < 3){
            return null;
        }
        EncodeResult encodeResult = ValueCoder.encodeUnknownResourceId(value);
        if(encodeResult != null){
            return encodeResult;
        }
        ReferenceString referenceString = ReferenceString.parseReference(value);
        if(referenceString != null){
            int resourceId = resolveReference(referenceString);
            return new EncodeResult(referenceString.getValueType(), resourceId);
        }
        return null;
    }
    public int resolveReference(String refString){
        ReferenceString referenceString = ReferenceString.parseReference(refString);
        if(referenceString == null){
            EncodeResult ref = ValueCoder.encodeUnknownResourceId(refString);
            if(ref != null){
                return ref.value;
            }
            throw new EncodeException(
                    "Not proper reference string: '"+refString+"'");
        }
        return resolveReference(referenceString);
    }

    public int resolveReference(ReferenceString referenceString){
        String packageName = referenceString.packageName;
        String type = referenceString.type;
        String name = referenceString.name;
        if(isLocalPackageName(packageName)){
            return resolveLocalResourceId(packageName, type, name);
        }
        if(packageName != null
                && (packageName.equals(getCurrentPackageName())
                || !isFrameworkPackageName(packageName))){
            return resolveLocalResourceId(packageName, type, name);
        }
        if(packageName == null){
            return resolveLocalResourceId(type, name);
        }
        return resolveFrameworkResourceId(packageName, type, name);
    }
    private int resolveLocalResourceId(String packageName, String type, String name){
        PackageBlock packageBlock = getCurrentPackage();
        if(packageBlock != null){
            int id = packageBlock.getTableBlock().resolveResourceId(packageName, type, name);
            if(id != 0){
                return id;
            }
        }
        throw new EncodeException("Local entry not found: " +
                "@" + packageName + ":" + type + "/" + name
                + ", " + getFrameworkInfo());
    }
    public int resolveLocalResourceId(String type, String name){
        PackageBlock current = getCurrentPackage();
        if(current != null){
            int id = current.resolveResourceId(type, name);
            if(id != 0){
                return id;
            }
            for(PackageBlock packageBlock : current.getTableBlock().listPackages()){
                if(packageBlock == current){
                    continue;
                }
                id = packageBlock.resolveResourceId(type, name);
                if(id != 0){
                    return id;
                }
            }
        }
        throw new EncodeException("Local entry not found: " +
                "@" + type + "/" + name + ", " + getFrameworkInfo());
    }
    public int resolveFrameworkResourceId(String packageName, String type, String name){
        for(FrameworkTable frameworkTable : frameworkTables){
            int id = frameworkTable.resolveResourceId(packageName, type, name);
            if(id != 0){
                return id;
            }
        }
        throw new EncodeException("Framework entry not found: " +
                "@" + packageName + ":" + type + "/" + name
                + ", " + getFrameworkInfo());
    }
    public Entry getLocalEntry(String packageName, String type, String name){
        if(currentPackage == null){
            return null;
        }
        TableBlock tableBlock = currentPackage.getTableBlock();
        ResourceEntry resourceEntry = tableBlock.getResource(packageName, type, name);
        if(resourceEntry != null){
            return resourceEntry.get();
        }
        return null;
    }
    public Entry getLocalEntry(String type, String name){
        if(currentPackage == null){
            return null;
        }
        Entry entry = currentPackage.getEntry(type, name);
        if(entry != null){
            return entry;
        }
        TableBlock tableBlock = currentPackage.getTableBlock();
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            if(packageBlock == currentPackage){
                continue;
            }
            entry = packageBlock.getEntry(type, name);
            if(entry != null){
                return entry;
            }
        }
        return null;
    }
    public ResourceEntry getLocalResourceEntry(String type, String name){
        if(currentPackage == null){
            return null;
        }
        ResourceEntry entry = currentPackage.getResource(type, name);
        if(entry != null){
            return entry;
        }
        TableBlock tableBlock = currentPackage.getTableBlock();
        for(PackageBlock packageBlock : tableBlock.listPackages()){
            if(packageBlock == currentPackage){
                continue;
            }
            entry = packageBlock.getResource(type, name);
            if(entry != null){
                return entry;
            }
        }
        return null;
    }
    public Entry getFrameworkEntry(String type, String name){
        for(FrameworkTable table:frameworkTables){
            Entry entry = table.getEntry(null, type, name);
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
    public EncodeMaterials setForceCreateNamespaces(boolean force) {
        this.mForceCreateNamespaces = force;
        return this;
    }
    public EncodeMaterials setCurrentPackage(PackageBlock currentPackage) {
        this.currentPackage = currentPackage;
        return this;
    }
    private boolean isLocalPackageName(String packageName){
        if(packageName == null || currentPackage == null){
            return false;
        }
        TableBlock tableBlock = currentPackage.getTableBlock();
        return tableBlock.getPackages(packageName).hasNext();
    }
    private String getFrameworkInfo(){
        if(frameworkTables.size() == 0){
            return "Frameworks = No frameworks found";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("Frameworks = ").append(frameworkTables.size());
        builder.append(" [");
        boolean appendOnce = false;
        for(FrameworkTable frameworkTable : frameworkTables){
            if(appendOnce){
                builder.append(", ");
            }
            builder.append(frameworkTable.getVersionCode());
            builder.append('(')
                    .append(frameworkTable.getFrameworkName())
                    .append(')');
            appendOnce = true;
        }
        builder.append(']');
        return builder.toString();
    }
    public EncodeMaterials addFramework(FrameworkApk frameworkApk) {
        if(frameworkApk!=null){
            addFramework(frameworkApk.getTableBlock());
        }
        return this;
    }
    public EncodeMaterials addFramework(FrameworkTable frameworkTable) {
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
        encodeMaterials.setCurrentPackage(packageBlock);

        for(TableBlock frameworkTable:tableBlock.getFrameWorks()){
            if(frameworkTable instanceof FrameworkTable){
                encodeMaterials.addFramework((FrameworkTable) frameworkTable);
            }
        }
        return encodeMaterials;
    }

}
