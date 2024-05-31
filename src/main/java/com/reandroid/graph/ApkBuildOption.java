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

import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.dex.key.TypeKey;
import java.util.function.Predicate;

public class ApkBuildOption {

    private boolean minifyResources = true;
    private boolean minifyClasses = true;
    private boolean minifyFields = true;
    private boolean minifyMethods = false;

    private boolean processClassNamesOnStrings = true;

    private ResourceMergeOption mMergeOption;
    private Predicate<? super TypeKey> keepClasses;

    public ApkBuildOption() {

    }

    public boolean isMinifyResources() {
        return minifyResources;
    }
    public void setMinifyResources(boolean minifyResources) {
        this.minifyResources = minifyResources;
    }
    public boolean isMinifyClasses() {
        return minifyClasses;
    }
    public void setMinifyClasses(boolean minifyClasses) {
        this.minifyClasses = minifyClasses;
    }

    public boolean isMinifyFields() {
        return minifyFields;
    }
    public void setMinifyFields(boolean minifyFields) {
        this.minifyFields = minifyFields;
    }
    public boolean isMinifyMethods() {
        return minifyMethods;
    }
    public void setMinifyMethods(boolean minifyMethods) {
        this.minifyMethods = minifyMethods;
    }

    public boolean isProcessClassNamesOnStrings() {
        return processClassNamesOnStrings;
    }
    public void setProcessClassNamesOnStrings(boolean processClassNamesOnStrings) {
        this.processClassNamesOnStrings = processClassNamesOnStrings;
    }


    public ResourceMergeOption getResourceMergeOption() {
        ResourceMergeOption mergeOption = this.mMergeOption;
        if(mergeOption == null){
            mergeOption = new ResourceMergeOption();
            this.mMergeOption = mergeOption;
        }
        return mergeOption;
    }
    public void setResourceMergeOption(ResourceMergeOption mergeOption) {
        this.mMergeOption = mergeOption;
    }

    public Predicate<? super TypeKey> getKeepClasses() {
        return keepClasses;
    }
    public void setKeepClasses(Predicate<? super TypeKey> filter) {
        this.keepClasses = filter;
    }
}
