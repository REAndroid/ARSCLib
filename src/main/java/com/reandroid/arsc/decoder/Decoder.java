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
import com.reandroid.arsc.value.AttributeValue;
import com.reandroid.arsc.value.Value;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.common.EntryStore;

import java.io.IOException;

public class Decoder {
    private final EntryStore entryStore;
    private int currentPackageId;
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
        return String.format("@0x%08x", resourceId);
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
        if(attributeValue == null){
            return null;
        }
        return ValueDecoder.decode(getEntryStore(), getCurrentPackageId(), attributeValue);
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
        if(tableBlock.getFrameWorks().size()==0){
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
        if(tableBlock.getFrameWorks().size()==0){
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
        return null;
    }

    public static Decoder getNullEntryStoreDecoder(){
        if(NULL_ENTRY_STORE_DECODER!=null){
            return NULL_ENTRY_STORE_DECODER;
        }
        synchronized (Decoder.class){
            TableBlock tableBlock = new TableBlock();
            Decoder decoder = new Decoder(tableBlock, 0x7f);
            NULL_ENTRY_STORE_DECODER = decoder;
            return decoder;
        }
    }
    private static Decoder NULL_ENTRY_STORE_DECODER;
}
