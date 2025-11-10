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
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.xml.XMLPath;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AndroidManifestBlockMerger {

    private boolean mEnabled;
    private boolean mBuildFusedModules;
    private AndroidManifestBlockSplitSanitizer mSplitSanitizer;

    private final List<String> fusedModuleNameList;
    private final Set<XMLPath> excludePaths;

    private AndroidManifestBlock baseManifest;

    public AndroidManifestBlockMerger() {
        this.mEnabled = true;
        this.mBuildFusedModules = true;
        this.mSplitSanitizer = new AndroidManifestBlockSplitSanitizer();

        this.fusedModuleNameList = new ArrayCollection<>();
        this.excludePaths = CollectionUtil.asHashSet(
                XMLPath.compile("/manifest/uses-split"),
                XMLPath.compile("/manifest/uses-split")
        );
    }

    public AndroidManifestBlockMerger initializeBase(AndroidManifestBlock base) {
        if (this.baseManifest != null && this.baseManifest != base) {
            throw new IllegalArgumentException("Base manifest already initialized");
        }
        this.baseManifest = base;
        addFusedModuleName("base");
        this.addFusedModules(base);
        AndroidManifestBlockSplitSanitizer sanitizer = getSplitSanitizer();
        if (sanitizer != null) {
            sanitizer.sanitize(base);
        }
        return this;
    }

    public AndroidManifestBlockMerger setEnabled(boolean enabled) {
        this.mEnabled = enabled;
        return this;
    }
    public AndroidManifestBlockMerger setBuildFusedModules(boolean buildFusedModules) {
        this.mBuildFusedModules = buildFusedModules;
        return this;
    }
    public AndroidManifestBlockMerger exclude(XMLPath xmlPath) {
        this.excludePaths.add(xmlPath);
        return this;
    }
    public AndroidManifestBlockMerger setSplitSanitizer(AndroidManifestBlockSplitSanitizer sanitizer) {
        this.mSplitSanitizer = sanitizer;
        return this;
    }
    public AndroidManifestBlockMerger reset() {
        this.baseManifest = null;
        this.fusedModuleNameList.clear();
        return this;
    }

    public boolean isEnabled() {
        return mEnabled;
    }
    public boolean isBuildFusedModules() {
        return mBuildFusedModules;
    }
    public AndroidManifestBlock getBaseManifestBlock() {
        return baseManifest;
    }
    public AndroidManifestBlockSplitSanitizer getSplitSanitizer() {
        return mSplitSanitizer;
    }

    public boolean merge(AndroidManifestBlock split) {
        if (!isEnabled() || split == null || split.getManifestElement() == null) {
            return false;
        }
        AndroidManifestBlock base = requireBaseManifestInitialized();
        if (split == base) {
            return false;
        }
        boolean fusedUpdated = addFusedModules(split);
        boolean result = mergeManifestElement(split.getManifestElement());
        if (fusedUpdated) {
            commitFusedModules();
        }
        AndroidManifestBlockSplitSanitizer sanitizer = getSplitSanitizer();
        if (sanitizer != null) {
            result = sanitizer.sanitize(base) || result;
        }
        if (result) {
            base.refresh();
        }
        return result;
    }
    private boolean mergeManifestElement(ResXmlElement manifest) {
        boolean result = false;
        Iterator<ResXmlElement> iterator = manifest.getElements();
        while (iterator.hasNext()) {
            ResXmlElement element = iterator.next();
            if (containsNamedManifestChild(element.getName())) {
                result = mergeNamedChild(element) || result;
            } else if (element.hasAttribute(AndroidManifest.ID_name)) {
                result = addNamedElement(element) || result;
            }
        }
        return result;
    }
    private boolean mergeNamedChild(ResXmlElement application) {
        boolean result = false;
        Iterator<ResXmlElement> iterator = application.getElements();
        while (iterator.hasNext()) {
            ResXmlElement element = iterator.next();
            if (element.hasAttribute(AndroidManifest.ID_name)) {
                result = addNamedElement(element) || result;
            } else {

            }
        }
        return result;
    }
    private boolean addNamedElement(ResXmlElement element) {
        if (isExcluded(element)) {
            return false;
        }
        String nameValue = AndroidManifestBlock.getAndroidNameValue(element);
        if (StringsUtil.isEmpty(nameValue)) {
            return false;
        }
        XMLPath xmlPath = XMLPath.of(element);
        AndroidManifestBlock base = getBaseManifestBlock();
        if (base.getNamedElement(xmlPath, nameValue) != null) {
            return false;
        }
        ResXmlElement result = base.newChildElement(xmlPath);
        result.merge(element);
        moveAboveApplication(result);
        return true;
    }
    private boolean isExcluded(ResXmlElement element) {
        for (XMLPath xmlPath : this.excludePaths) {
            if (xmlPath.test(element)) {
                return true;
            }
        }
        return false;
    }
    private void moveAboveApplication(ResXmlElement element) {
        if (element.getDepth() != 2) {
            return;
        }
        ResXmlElement manifest = element.getParentElement();
        if (!AndroidManifest.TAG_manifest.equals(manifest.getName())) {
            return;
        }
        ResXmlElement application = manifest.getElement(AndroidManifest.TAG_application);
        if (application == null || application.getIndex() > element.getIndex()) {
            return;
        }
        manifest.moveTo(element, application.getIndex());
    }
    private AndroidManifestBlock requireBaseManifestInitialized() {
        AndroidManifestBlock baseManifest = this.getBaseManifestBlock();
        if (baseManifest == null) {
            throw new IllegalArgumentException("Base manifest not initialized");
        }
        return baseManifest;
    }
    private void commitFusedModules() {
        if (!isBuildFusedModules()) {
            return;
        }
        List<String> fusedModuleNameList = this.fusedModuleNameList;
        int size = fusedModuleNameList.size();
        if (size == 0 || (size == 1 && fusedModuleNameList.contains("base"))) {
            return;
        }
        ResXmlElement element = getBaseManifestBlock()
                .getOrCreateNamedElement(XMLPath.compile("/manifest/application/meta-data"),
                AndroidManifest.VALUE_com_android_dynamic_apk_fused_modules);
        ResXmlAttribute attribute = element.getOrCreateAndroidAttribute(
                AndroidManifest.NAME_value,
                AndroidManifest.ID_value);
        attribute.setValueAsString(StringsUtil.join(fusedModuleNameList, ","));
    }
    private boolean addFusedModules(AndroidManifestBlock manifestBlock) {
        boolean result = false;
        if (isBuildFusedModules()) {
            result = addFusedModuleName(manifestBlock.getSplit());
            result = addFusedModuleNames(manifestBlock.getFusedModuleNames()) || result;
        }
        return result;
    }
    private boolean addFusedModuleNames(String[] names) {
        boolean result = false;
        if (names != null) {
            for (String name : names) {
                result = addFusedModuleName(name) || result;
            }
        }
        return result;
    }
    private boolean addFusedModuleName(String name) {
        if (StringsUtil.isEmpty(name)) {
            return false;
        }
        if (name.indexOf(',') >= 0) {
            return addFusedModuleNames(StringsUtil.split(name, ','));
        } else if (!fusedModuleNameList.contains(name)) {
            fusedModuleNameList.add(name);
            return true;
        }
        return false;
    }
    private static boolean containsNamedManifestChild(String tag) {
        return AndroidManifest.TAG_application.equals(tag) ||
                AndroidManifest.TAG_queries.equals(tag);
    }
}
