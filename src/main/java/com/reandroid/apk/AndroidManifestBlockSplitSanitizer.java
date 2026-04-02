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
package com.reandroid.apk;

import com.reandroid.app.AndroidManifest;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.xml.XMLPath;

import java.util.Set;

public class AndroidManifestBlockSplitSanitizer {

    private boolean mEnabled;
    private final Set<XMLPath> removeAttributeList;
    private final Set<XMLPath> removeElementList;
    private final XMLPath splitsPath;

    public AndroidManifestBlockSplitSanitizer() {
        this.mEnabled = true;

        this.removeAttributeList = CollectionUtil.asHashSet(
                XMLPath.newElement(XMLPath.ANY_ELEMENT_PATH)
                        .attribute(AndroidManifest.ID_isFeatureSplit)
                        .alternate(AndroidManifest.ID_splitTypes)
                        .alternate(AndroidManifest.ID_requiredSplitTypes)
                        .alternate(AndroidManifest.ID_isSplitRequired)
                        .alternate(AndroidManifest.ID_isolatedSplits)
                        .alternate(AndroidManifest.NAME_split)
                        .alternate(AndroidManifest.NAME_isFeatureSplit)
                        .alternate(AndroidManifest.NAME_splitTypes)
                        .alternate(AndroidManifest.NAME_requiredSplitTypes)
                        .alternate(AndroidManifest.NAME_isolatedSplits)
                        .alternate(AndroidManifest.NAME_isSplitRequired)
        );

        XMLPath appMetaDataName = AndroidManifest.PATH_APPLICATION_META_DATA_NAME;

        this.splitsPath = appMetaDataName.value("com.android.vending.splits");

        this.removeElementList = CollectionUtil.asHashSet(
                AndroidManifest.PATH_MANIFEST.element(AndroidManifest.TAG_uses_split),
                appMetaDataName.alternateValue("com.android.vending.splits.required"),
                appMetaDataName.alternateValue("com.android.stamp.source"),
                appMetaDataName.alternateValue("com.android.stamp.type"),
                appMetaDataName.alternateValue("com.android.vending.derived.apk.id")
        );
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public AndroidManifestBlockSplitSanitizer addAttribute(XMLPath xmlPath) {
        removeAttributeList.add(xmlPath);
        return this;
    }
    public AndroidManifestBlockSplitSanitizer addElement(XMLPath xmlPath) {
        removeAttributeList.add(xmlPath);
        return this;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public boolean sanitize(ApkModule apkModule) {
        if (!isEnabled()) {
            return false;
        }
        AndroidManifestBlock manifestBlock = apkModule.getAndroidManifest();
        if (manifestBlock == null) {
            return false;
        }
        boolean result = sanitize(manifestBlock);
        result = deleteSplitsXml(apkModule, manifestBlock) || result;
        apkModule.setExtractNativeLibs(manifestBlock.isExtractNativeLibs());
        return result;
    }
    public boolean sanitize(AndroidManifestBlock manifestBlock) {
        if (!isEnabled()) {
            return false;
        }
        if (manifestBlock == null || manifestBlock.getManifestElement() == null) {
            return false;
        }
        boolean result;
        result = removeAttributes(manifestBlock);
        result = removeElements(manifestBlock) || result;
        return result;
    }
    private boolean deleteSplitsXml(ApkModule apkModule, AndroidManifestBlock manifestBlock) {
        ResXmlElement element = manifestBlock.getNamedElement(AndroidManifest.PATH_APPLICATION_META_DATA,
                "com.android.vending.splits");
        if (element == null) {
            return false;
        }
        ResXmlAttribute attribute = element.searchAttributeByResourceId(AndroidManifest.ID_resource);
        if (attribute == null) {
            attribute = element.searchAttributeByResourceId(AndroidManifest.ID_value);
        }
        if (attribute == null || attribute.getValueType() != ValueType.REFERENCE) {
            return false;
        }
        TableBlock tableBlock = apkModule.getTableBlock();
        if (tableBlock == null) {
            return false;
        }
        ResourceEntry resourceEntry = tableBlock.getResource(attribute.getData());
        if (resourceEntry == null) {
            return false;
        }
        element.removeSelf();
        String path = CollectionUtil.getFirst(ComputeIterator.of(
                resourceEntry.iterator(true), Entry::getValueAsString));
        if (path != null) {
            return apkModule.removeResFile(path, false);
        }
        return true;
    }
    private boolean removeAttributes(AndroidManifestBlock manifestBlock) {
        boolean results = false;
        for (XMLPath path : removeAttributeList) {
            results = manifestBlock.removeAttributes(path) || results;
        }
        return results;
    }
    private boolean removeElements(AndroidManifestBlock manifestBlock) {
        boolean results = false;
        for (XMLPath path : removeElementList) {
            results = manifestBlock.removeElements(path) || results;
        }
        return results;
    }
}
