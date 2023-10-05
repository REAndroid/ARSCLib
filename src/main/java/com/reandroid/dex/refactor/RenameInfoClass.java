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

import com.reandroid.arsc.group.ItemGroup;
import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.EmptyList;

import java.util.ArrayList;
import java.util.List;

public class RenameInfoClass extends RenameInfo<StringData> {

    public RenameInfoClass(String search, String replace) {
        super(search, replace);
    }

    @Override
    SectionType<StringData> getSectionType() {
        return SectionType.STRING_DATA;
    }
    @Override
    void apply(ItemGroup<StringData> group){
        String replace = getReplace();
        for(StringData stringData : group){
            if(!stringData.containsUsage(StringId.USAGE_TYPE_NAME)){
                continue;
            }
            stringData.setString(replace);
        }
    }
    @Override
    public TypeKey getKey(){
        return new TypeKey(getSearch());
    }
    @Override
    List<RenameInfo<?>> createChildRenames() {
        List<RenameInfo<?>> results = new ArrayList<>(6);
        addClassAnnotation(results);
        addClassInner(results);
        addArrays(results);
        addJava(results);
        return results;
    }
    void addClassAnnotation(List<RenameInfo<?>> results) {
        results.add(new RenameInfoClassAnnotation(this));
    }
    void addClassInner(List<RenameInfo<?>> results) {
        results.add(new RenameInfoClassInner(this));
    }
    void addArrays(List<RenameInfo<?>> results) {
        results.add(new RenameInfoClassArray(this, 1));
        results.add(new RenameInfoClassArray(this, 2));
        results.add(new RenameInfoClassArray(this, 3));
    }
    void addJava(List<RenameInfo<?>> results) {
        results.add(new RenameInfoClassJava(this));
    }

    static class RenameInfoClassAnnotation extends RenameInfoClass{
        private final RenameInfoClass parent;

        public RenameInfoClassAnnotation(RenameInfoClass parent) {
            super(null, null);
            this.parent = parent;
        }

        @Override
        public String getSearch() {
            return getParent().getSearch().replace(';', '<');
        }
        @Override
        public String getReplace() {
            return getParent().getReplace().replace(';', '<');
        }
        @Override
        public RenameInfoClass getParent() {
            return parent;
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            return EmptyList.of();
        }
        @Override
        void addJava(List<RenameInfo<?>> results) {
        }
    }

    static class RenameInfoClassInner extends RenameInfoClass{
        private final RenameInfoClass parent;

        public RenameInfoClassInner(RenameInfoClass parent) {
            super(null, null);
            this.parent = parent;
        }
        @Override
        public boolean lookString(StringData stringData){
            if(stringData.getUsageType() != StringId.USAGE_TYPE_NAME){
                return false;
            }
            String text = stringData.getString();
            String search = getSearch();
            if(!text.startsWith(search)){
                return false;
            }
            text = text.replace(search, getReplace());
            stringData.setString(text);
            addRenameCount();
            return true;
        }
        @Override
        void apply(ItemGroup<StringData> group){
        }
        @Override
        public String getSearch() {
            return getParent().getSearch().replace(';', '$');
        }
        @Override
        public String getReplace() {
            return getParent().getReplace().replace(';', '$');
        }
        @Override
        public RenameInfoClass getParent() {
            return parent;
        }
        @Override
        public boolean looksStrings(){
            return true;
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            return EmptyList.of();
        }
    }

    static class RenameInfoClassArray extends RenameInfoClass{
        private final RenameInfoClass parent;
        private final int dimension;

        public RenameInfoClassArray(RenameInfoClass parent, int dimension) {
            super(null, null);
            this.parent = parent;
            this.dimension = dimension;
        }

        @Override
        public String getSearch() {
            return appendArray(getParent().getSearch());
        }
        @Override
        public String getReplace() {
            return appendArray(getParent().getReplace());
        }
        @Override
        public RenameInfoClass getParent() {
            return parent;
        }
        private String appendArray(String name){
            int dimension = this.dimension;
            StringBuilder builder = new StringBuilder(dimension + name.length());
            for(int i = 0; i < dimension; i++){
                builder.append('[');
            }
            builder.append(name);
            return builder.toString();
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            return super.createChildRenames();
        }
        @Override
        void addArrays(List<RenameInfo<?>> results) {
        }
        @Override
        void addJava(List<RenameInfo<?>> results) {
        }
    }

    static class RenameInfoClassJava extends RenameInfoClass{
        private final RenameInfo<?> parent;

        public RenameInfoClassJava(RenameInfo<?> parent) {
            super(null, null);
            this.parent = parent;
        }

        @Override
        void apply(ItemGroup<StringData> group){
            String replace = getReplace();
            for(StringData stringData : group){
                if(stringData.getUsageType() != StringId.USAGE_INSTRUCTION){
                    continue;
                }
                stringData.setString(replace);
            }
        }
        @Override
        public boolean lookString(StringData stringData){
            if(stringData.getUsageType() != StringId.USAGE_INSTRUCTION){
                return false;
            }
            String text = stringData.getString();
            String search = getSearch();
            if(!text.startsWith(search)){
                return false;
            }
            text = text.replace(search, getReplace());
            stringData.setString(text);
            addRenameCount();
            return true;
        }

        @Override
        public String getSearch() {
            return DexUtils.toJavaName(getParent().getSearch());
        }
        @Override
        public String getReplace() {
            return DexUtils.toJavaName(getParent().getReplace());
        }
        @Override
        public RenameInfo<?> getParent() {
            return parent;
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            List<RenameInfo<?>> results = new ArrayList<>(1);
            results.add(new RenameInfoClassJavaInner(this));
            return results;
        }
        @Override
        void addArrays(List<RenameInfo<?>> results) {
        }
        @Override
        void addJava(List<RenameInfo<?>> results) {
        }
    }

    static class RenameInfoClassJavaInner extends RenameInfoClassJava{

        public RenameInfoClassJavaInner(RenameInfo<?> parent) {
            super(parent);
        }

        @Override
        public boolean looksStrings(){
            return true;
        }
        @Override
        public String getSearch() {
            return getParent().getSearch() + '.';
        }
        @Override
        public String getReplace() {
            return getParent().getReplace() + '.';
        }
        @Override
        List<RenameInfo<?>> createChildRenames() {
            return EmptyList.of();
        }
        @Override
        void addArrays(List<RenameInfo<?>> results) {
        }
        @Override
        void addJava(List<RenameInfo<?>> results) {
        }
    }

}
