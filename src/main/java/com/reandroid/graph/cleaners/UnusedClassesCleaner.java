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
package com.reandroid.graph.cleaners;

import com.reandroid.apk.ApkModule;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.graph.ApkBuildOption;
import com.reandroid.graph.RequiredClassesScanner;

import java.util.Iterator;
import java.util.Set;

public class UnusedClassesCleaner extends UnusedCleaner<DexClass> {

    public UnusedClassesCleaner(ApkBuildOption buildOption, ApkModule apkModule,
                                DexClassRepository classRepository) {
        super(buildOption, apkModule, classRepository);
    }

    @Override
    public void apply() {
        if(!getBuildOption().isMinifyClasses()) {
            debug("Skip");
            return;
        }
        Set<TypeKey> requiredClasses = scanRequiredClasses();
        verbose("Required classes: " + requiredClasses.size());
        debugReportClassesToRemove(requiredClasses);
        cleanUnusedClasses(requiredClasses);
    }
    private void cleanUnusedClasses(Set<TypeKey> requiredClasses) {
        verbose("Cleaning ...");
        DexClassRepository repository = getClassRepository();
        int previousCount = repository.getDexClassesCount();
        repository.removeClassesWithKeys(typeKey -> !requiredClasses.contains(typeKey));
        int removed = previousCount - repository.getDexClassesCount();
        setCount(removed);
        if(removed != 0) {
            repository.shrink();
        }
        verbose("Cleaned: " + removed);
    }
    private void debugReportClassesToRemove(Set<TypeKey> requiredClasses) {
        if(!isDebugEnabled()) {
            return;
        }
        DexClassRepository repository = getClassRepository();
        Iterator<DexClass> iterator = repository.getDexClasses(typeKey -> !requiredClasses.contains(typeKey));
        while (iterator.hasNext()) {
            debug(iterator.next().getKey().toString());
        }
    }
    private Set<TypeKey> scanRequiredClasses() {
        RequiredClassesScanner scanner = new RequiredClassesScanner(
                getApkModule(),
                getClassRepository());
        scanner.setReporter(getReporter());
        scanner.setLookInStrings(getBuildOption().isProcessClassNamesOnStrings());

        processUserKeep(scanner);
        processOtherKeep(scanner);

        scanner.apply();
        return scanner.getResults();
    }
    private void processUserKeep(RequiredClassesScanner scanner) {
        scanner.keepClasses(getBuildOption().getKeepClasses());
    }
    private void processOtherKeep(RequiredClassesScanner scanner) {
        // FIXME: this is mainly to keep Landroidx/work/impl/WorkDatabase_Impl;
        // TODO: find universal rule
        scanner.keepClasses(typeKey -> typeKey.getTypeName().endsWith("_Impl;"));
    }
    @Override
    protected boolean isEnabled() {
        return getBuildOption().isMinifyClasses();
    }
}
