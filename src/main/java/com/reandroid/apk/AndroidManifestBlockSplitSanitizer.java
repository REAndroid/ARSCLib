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
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.xml.XMLPath;

import java.util.Set;

public class AndroidManifestBlockSplitSanitizer {

    private boolean mEnabled;
    private final Set<XMLPath> removeAttributeList;
    private final Set<XMLPath> removeElementList;

    public AndroidManifestBlockSplitSanitizer() {
        this.mEnabled = true;

        XMLPath manifest = XMLPath.newElement(AndroidManifest.TAG_manifest);
        XMLPath application = manifest.element(AndroidManifest.TAG_application);
        XMLPath appMetaData = application.element(AndroidManifest.TAG_meta_data);
        XMLPath appMetaDataName = appMetaData.attribute(AndroidManifest.ID_name);

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

        this.removeElementList = CollectionUtil.asHashSet(
                manifest.element(AndroidManifest.TAG_uses_split),
                appMetaDataName.value("com.android.vending.splits.required"),
                appMetaDataName.value("com.android.vending.splits"),
                appMetaDataName.value("com.android.stamp.source"),
                appMetaDataName.value("com.android.stamp.type")
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
