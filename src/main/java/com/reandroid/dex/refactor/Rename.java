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

import com.reandroid.utils.collection.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

public class Rename implements Iterable<RenameInfo<?>>{
    private final List<RenameInfo<?>> renameInfoList;

    public Rename(){
        this.renameInfoList = new ArrayCollection<>();
    }

    public Iterator<RenameInfo<?>> getAll(){
        return new MergingIterator<>(ComputeIterator.of(iterator(),
                RenameInfo::iterator));
    }

    public void add(RenameInfo<?> renameInfo){
        if(renameInfo == null || contains(renameInfo)){
            return;
        }
        this.renameInfoList.add(renameInfo);
    }
    public boolean contains(RenameInfo<?> renameInfo){
        if(renameInfo == null){
            return false;
        }
        if(this.renameInfoList.contains(renameInfo)){
            return true;
        }
        for(RenameInfo<?> info : this){
            if(info.contains(renameInfo)){
                return true;
            }
        }
        return false;
    }
    @Override
    public Iterator<RenameInfo<?>> iterator(){
        return renameInfoList.iterator();
    }
    public void write(Writer writer) throws IOException {
        for(RenameInfo<?> info : this){
            info.write(writer, true);
        }
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            write(writer);
            writer.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
