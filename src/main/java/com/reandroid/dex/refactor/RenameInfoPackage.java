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
package com.reandroid.dex.refactor;

import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.EmptyList;

import java.util.List;

public class RenameInfoPackage extends RenameInfoClass {

    public RenameInfoPackage(String search, String replace) {
        super(search, replace);
    }

    @Override
    public void apply(SectionList sectionList){
    }
    @Override
    SectionType<StringId> getSectionType() {
        return null;
    }
    @Override
    public boolean lookString(StringId stringId){
        if(!stringId.containsUsage(UsageMarker.USAGE_TYPE_NAME)){
            return false;
        }
        String text = stringId.getString();
        String search = getSearch();
        if(!text.startsWith(search)){
            return false;
        }
        text = text.replace(search, getReplace());
        stringId.setString(text);
        addRenameCount();
        return true;
    }
    @Override
    public boolean looksStrings(){
        return true;
    }
    @Override
    void addClassAnnotation(List<RenameInfo<?>> results) {
    }
    @Override
    void addClassInner(List<RenameInfo<?>> results) {
    }
    @Override
    void addJava(List<RenameInfo<?>> results) {
        results.add(new RenameInfoPackageJava(this));
    }

    static class RenameInfoPackageJava extends RenameInfoClass.RenameInfoClassJava {
        public RenameInfoPackageJava(RenameInfo<?> parent) {
            super(parent);
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            return EmptyList.of();
        }
    }
}
