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
package com.reandroid.arsc.decoder;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.apk.FrameworkApk;
import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.chunk.MainChunk;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.value.AttributeValue;
import com.reandroid.arsc.value.Value;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.EntryStore;

import java.io.IOException;

public class Decoder {
    private final EntryStore entryStore;
    private int currentPackageId;
    private ApkFile mApkFile;
    public Decoder(EntryStore entryStore, int currentPackageId){
        this.entryStore = entryStore;
        this.currentPackageId = currentPackageId;
    }
    public String decodeResourceName(int resourceId){
        return decodeResourceName(resourceId, true);
    }
    public String decodeResourceName(int resourceId, boolean defaultHex){
        if(resourceId == 0){
            return null;
        }
        EntryGroup entryGroup = getEntryStore().getEntryGroup(resourceId);
        if(entryGroup!=null){
            return entryGroup.getSpecName();
        }
        if(defaultHex){
            return hexResourceName(resourceId);
        }
        return null;
    }
    private String hexResourceName(int resourceId){
        return HexUtil.toHex8("@0x", resourceId);
    }
    public String decodeValue(Value value){
        if(value==null){
            return null;
        }
        ValueType valueType = value.getValueType();
        if(valueType == ValueType.STRING){
            return value.getValueAsString();
        }
        return ValueDecoder.decode(getEntryStore(), getCurrentPackageId(), value);
    }
    public String decodeAttributeValue(AttributeValue attributeValue){
        return decodeAttributeValue(attributeValue, true);
    }
    public String decodeAttributeValue(AttributeValue attributeValue, boolean escapeStartChar){
        if(attributeValue == null){
            return null;
        }
        String result = ValueDecoder.decode(getEntryStore(), getCurrentPackageId(), attributeValue);
        if(!escapeStartChar || result == null || result.length() == 0
                || attributeValue.getValueType() != ValueType.STRING){
            return result;
        }
        return ValueDecoder.escapeSpecialCharacter(result);
    }
    private EntryStore getEntryStore() {
        return entryStore;
    }
    public int getCurrentPackageId() {
        return currentPackageId;
    }
    public void setCurrentPackageId(int currentPackageId) {
        this.currentPackageId = currentPackageId;
    }
    public ApkFile getApkFile(){
        return mApkFile;
    }
    public void setApkFile(ApkFile apkFile) {
        this.mApkFile = apkFile;
    }
    public boolean isNullDecoder(){
        return false;
    }

    public static Decoder create(ResXmlDocument resXmlDocument){
        MainChunk mainChunk = resXmlDocument.getMainChunk();
        if(mainChunk == null){
            return getNullEntryStoreDecoder();
        }
        ApkFile apkFile = mainChunk.getApkFile();
        if(apkFile == null){
            return getNullEntryStoreDecoder();
        }
        TableBlock tableBlock = apkFile.getTableBlock();
        if(tableBlock == null){
            return getNullEntryStoreDecoder();
        }
        AndroidManifestBlock manifestBlock = apkFile.getAndroidManifestBlock();
        if(manifestBlock!=null){
            int currentPackageId = manifestBlock.guessCurrentPackageId();
            if(currentPackageId!=0){
                return create(tableBlock, currentPackageId);
            }
        }
        return create(tableBlock);
    }
    public static Decoder create(TableBlock tableBlock){
        if(!tableBlock.hasFramework() && !tableBlock.isAndroid()){
            tableBlock.addFramework(getFramework());
        }
        int currentPackageId;
        PackageBlock packageBlock = tableBlock.pickOne();
        if(packageBlock!=null){
            currentPackageId = packageBlock.getId();
        }else {
            // 0x7f most common
            currentPackageId = 0x7f;
        }
        return create(tableBlock, currentPackageId);
    }
    public static Decoder create(TableBlock tableBlock, int currentPackageId){
        if(!tableBlock.hasFramework() && !tableBlock.isAndroid()){
            TableBlock framework = getFramework();
            if(framework!=null){
                PackageBlock packageBlock = framework.pickOne();
                if(packageBlock!=null && packageBlock.getId() != currentPackageId){
                    tableBlock.addFramework(framework);
                }
            }
        }
        return new Decoder(tableBlock, currentPackageId);
    }
    private static TableBlock getFramework(){
        try {
            FrameworkApk frameworkApk = AndroidFrameworks.getCurrent();
            if(frameworkApk == null){
                frameworkApk = AndroidFrameworks.getLatest();
                AndroidFrameworks.setCurrent(frameworkApk);
            }
            return frameworkApk.getTableBlock();
        } catch (IOException ignored) {
        }
        // Should not reach here but to be safe return dummy
        return new FrameworkTable();
    }

    public static Decoder getNullEntryStoreDecoder(){
        if(NULL_ENTRY_STORE_DECODER!=null){
            return NULL_ENTRY_STORE_DECODER;
        }
        synchronized (Decoder.class){
            NullEntryDecoder decoder = new NullEntryDecoder(getFramework(), 0x7f);
            NULL_ENTRY_STORE_DECODER = decoder;
            return decoder;
        }
    }
    static class NullEntryDecoder extends Decoder{
        public NullEntryDecoder(EntryStore entryStore, int currentPackageId) {
            super(entryStore, currentPackageId);
        }
        @Override
        public boolean isNullDecoder(){
            return true;
        }
    }
    private static NullEntryDecoder NULL_ENTRY_STORE_DECODER;
}
