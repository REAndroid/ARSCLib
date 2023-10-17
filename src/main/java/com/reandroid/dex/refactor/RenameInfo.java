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

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.KeyItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.model.DexDirectory;
import com.reandroid.dex.pool.DexSectionPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionList;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

public abstract class RenameInfo<T extends Block> implements KeyItem {
    private final String search;
    private final String replace;
    private List<RenameInfo<?>> childRenames;
    private int renameCount;

    public RenameInfo(String search, String replace){
        this.search = search;
        this.replace = replace;
    }

    public void apply(DexDirectory dexDirectory){

    }
    public void apply(SectionList sectionList){
        apply(sectionList.get(getSectionType()));
        Iterator<RenameInfo<?>> iterator = getChildRenames();
        while (iterator.hasNext()){
            RenameInfo<?> renameInfo = iterator.next();
            renameInfo.apply(sectionList);
        }
    }
    private void apply(Section<T> section){
        if(section == null){
            return;
        }
        DexSectionPool<T> pool = section.getPool();
        Key key = getKey();
        Iterable<T> group = pool.getGroup(key);
        if(group == null){
            return;
        }
        apply(group);
        pool.update(key);
        addRenameCount();
    }

    public boolean contains(RenameInfo<?> renameInfo){
        if(renameInfo == null){
            return false;
        }
        List<RenameInfo<?>> childRenames = this.childRenames;
        if(childRenames == null){
            return false;
        }
        if(childRenames.contains(renameInfo)){
            return true;
        }
        for (RenameInfo<?> info : childRenames){
            if(info.contains(renameInfo)){
                return true;
            }
        }
        return false;
    }
    public boolean lookString(StringId stringData){
        return false;
    }
    public boolean looksStrings(){
        return false;
    }
    public Iterator<RenameInfo<?>> iterator(){
        return CombiningIterator.of(SingleIterator.of(this),
                ComputeIterator.of(getChildRenames(), RenameInfo::iterator));
    }

    public String getSearch() {
        return search;
    }
    public String getReplace() {
        return replace;
    }
    Iterator<RenameInfo<?>> getChildRenames(){
        return listChildRenames().iterator();
    }
    public void add(RenameInfo<?> renameInfo){
        if(renameInfo == null || renameInfo == this){
            return;
        }
        List<RenameInfo<?>> renameInfoList = listChildRenames();
        if(renameInfoList == null || renameInfoList.isEmpty()){
            renameInfoList = new ArrayList<>();
        }
        renameInfoList.add(renameInfo);
    }
    public List<RenameInfo<?>> listChildRenames() {
        List<RenameInfo<?>> childRenames = this.childRenames;
        if(childRenames == null){
            childRenames = createChildRenames();
            this.childRenames = childRenames;
        }
        return childRenames;
    }
    public RenameInfo<?> getParent(){
        return null;
    }

    abstract SectionType<T> getSectionType();
    abstract void apply(Iterable<T> group);
    abstract List<RenameInfo<?>> createChildRenames();
    void addRenameCount(){
        renameCount ++;
    }
    public int getRenameCount() {
        return renameCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RenameInfo<?> renameInfo = (RenameInfo<?>) obj;
        return Objects.equals(getKey(), renameInfo.getKey());
    }
    @Override
    public int hashCode() {
        return Objects.hash(getKey());
    }

    private int getDepth(){
        int result = 0;
        RenameInfo<?> renameInfo = getParent();
        while (renameInfo != null){
            result ++;
            renameInfo = renameInfo.getParent();
        }
        return result;
    }
    public void write(Writer writer, boolean appendCount) throws IOException {
        append(writer, appendCount);
        Iterator<RenameInfo<?>> iterator = getChildRenames();
        while (iterator.hasNext()){
            iterator.next().write(writer, appendCount);
        }
    }
    void append(Writer writer, boolean appendCount) throws IOException {
        int count = getRenameCount();
        if(appendCount && count == 0){
            return;
        }
        appendIndent(writer);
        writer.write(getKey().toString());
        writer.write("=");
        writer.write(getReplace());
        if(appendCount){
            writer.append("  // count=");
            writer.write(Integer.toString(getRenameCount()));
        }
        writer.write("\n");
    }
    void appendIndent(Writer writer) throws IOException {
        int depth = getDepth() * 2;
        for(int i = 0; i < depth; i++){
            writer.append(' ');
        }
    }
    private String getLogTag(){
        return "[" + getClass().getSimpleName() + "] ";
    }
    void log(Object msg){
        System.err.println(getLogTag() + msg);
    }
    @Override
    public String toString() {
        return getKey() + "=" + getReplace();
    }
}
