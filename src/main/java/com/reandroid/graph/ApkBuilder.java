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
package com.reandroid.graph;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.model.ResourceName;
import com.reandroid.arsc.refactor.ResourceBuilder;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.utils.collection.CollectionUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ApkBuilder extends BaseApkModuleProcessor {

    private ApkBuildOption buildOption;

    public ApkBuilder(ApkModule sourceModule, DexClassRepository classRepository) {
        super(sourceModule, classRepository);
    }

    public void build() {
        resolveInlineIntegerFieldCalls();
        cleanUnusedFieldsAndMethods();
        cleanUnusedClasses();
        ResourceMergeOption resourceMergeOption = getBuildOption().getResourceMergeOption();
        ResourceBuilder resourceBuilder = new ResourceBuilder(resourceMergeOption,
                getApkModule().getTableBlock());

        cleanUnusedResFiles();
        initializeRequiredIds(resourceMergeOption);
        resourceBuilder.rebuild();

        ApkModule sourceModule = getApkModule();
        resourceBuilder.rebuildManifest(sourceModule);
        resourceBuilder.applyIdChanges(getClassRepository().visitIntegers());
        ApkModule resultModule = resourceBuilder.getResultModule();
        removeResFiles();

        sourceModule.setTableBlock(resultModule.getTableBlock());
        sourceModule.keepTableBlockChanges();

        resultModule.setTableBlock(null);
        sourceModule.getZipEntryMap().addAll(resultModule.getZipEntryMap());
    }
    private void cleanUnusedFieldsAndMethods() {
        if(getBuildOption().isMinifyFields()) {
            UnusedFieldsCleaner cleaner = new UnusedFieldsCleaner(getClassRepository());
            cleaner.setReporter(getReporter());
            cleaner.apply();
        }
        if(getBuildOption().isMinifyMethods()) {
            UnusedMethodsCleaner cleaner = new UnusedMethodsCleaner(getClassRepository());
            cleaner.setReporter(getReporter());
            cleaner.apply();
        }
    }
    private void resolveInlineIntegerFieldCalls() {
        if(getBuildOption().isMinifyResources()) {
            InlineFieldIntResolver resolver = new InlineFieldIntResolver(
                    getClassRepository(),
                    getApkModule().getTableBlock());
            resolver.setReporter(getReporter());
            resolver.apply();
        }
    }
    private void cleanUnusedClasses() {
        ApkBuildOption buildOption = getBuildOption();
        if(!buildOption.isMinifyClasses()) {
            return;
        }
        verbose("Removing unused classes ...");
        DexClassRepository repository = getClassRepository();
        RequiredClassesScanner scanner = new RequiredClassesScanner(getApkModule(), repository);
        scanner.setReporter(getReporter());
        scanner.setLookInStrings(buildOption.isProcessClassNamesOnStrings());

        // FIXME: this is mainly to keep Landroidx/work/impl/WorkDatabase_Impl;
        // TODO: find universal rule
        scanner.keepClasses(typeKey -> typeKey.getTypeName().endsWith("_Impl;"));
        scanner.keepClasses(buildOption.getKeepClasses());
        scanner.scan();
        Set<TypeKey> requiredClasses = scanner.getResults();
        int size = repository.getDexClassesCount();
        reportRemovedClasses(requiredClasses);
        repository.removeClassesWithKeys(typeKey -> !requiredClasses.contains(typeKey));
        repository.refresh();
        size = size - repository.getDexClassesCount();
        verbose("Removed unused classes: " + size);
    }
    private void reportRemovedClasses(Set<TypeKey> requiredClasses) {
        if(!isVerboseEnabled()) {
            return;
        }
        verbose("Removing ...");
        Iterator<DexClass> iterator = getClassRepository().getDexClasses(typeKey ->
                !requiredClasses.contains(typeKey));
        while (iterator.hasNext()) {
            debug(iterator.next().getKey().toString());
        }
    }
    private void cleanUnusedResFiles() {
        if(!getBuildOption().isMinifyResources()) {
            return;
        }
        ApkModule apkModule = getApkModule();
        RequiredEntriesScanner scanner = new RequiredEntriesScanner(apkModule, getClassRepository());
        scanner.scan();
        Set<String> requiredFiles = scanner.getRequiredFiles();
        List<ResFile> resFileList = apkModule.listResFiles();
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for(ResFile resFile : resFileList) {
            String path = resFile.getFilePath();
            if(!requiredFiles.contains(path)) {
                zipEntryMap.remove(path);
            }
        }
    }
    private void removeResFiles() {
        if(!getBuildOption().isMinifyResources()) {
            return;
        }
        ApkModule apkModule = getApkModule();
        List<ResFile> resFileList = apkModule.listResFiles();
        ZipEntryMap zipEntryMap = apkModule.getZipEntryMap();
        for(ResFile resFile : resFileList) {
            zipEntryMap.remove(resFile.getInputSource());
        }
    }
    private void initializeRequiredIds(ResourceMergeOption resourceMergeOption) {
        if(!getBuildOption().isMinifyResources()) {
            resourceMergeOption.setKeepEntries(CollectionUtil.getAcceptAll());
            return;
        }
        RequiredEntriesScanner scanner = new RequiredEntriesScanner(getApkModule(), getClassRepository());
        scanner.setReporter(getReporter());
        scanner.scan();
        Set<ResourceName> requiredResources = scanner.getRequiredResources();
        resourceMergeOption.setKeepEntries(resourceEntry ->
                requiredResources.contains(resourceEntry.toResourceName()));
    }
    public ApkBuildOption getBuildOption() {
        ApkBuildOption buildOption = this.buildOption;
        if(buildOption == null) {
            buildOption = new ApkBuildOption();
            this.buildOption = buildOption;
        }
        return buildOption;
    }
    public void setBuildOption(ApkBuildOption buildOption) {
        this.buildOption = buildOption;
    }
}
