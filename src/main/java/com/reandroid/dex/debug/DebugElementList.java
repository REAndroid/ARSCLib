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
package com.reandroid.dex.debug;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.FixedDexContainer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class DebugElementList extends FixedDexContainer implements Collection<DebugElement> {
    private final IntegerReference lineStart;
    private final BlockList<DebugElement> elementList;
    public DebugElementList(IntegerReference lineStart){
        super(1);
        this.lineStart = lineStart;
        this.elementList = new BlockList<>();
        addChild(0, elementList);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        DebugElement debugElement = readNext(reader);
        while (debugElement.getElementTypeFlag() != 0){
            debugElement = readNext(reader);
        }
    }
    private DebugElement readNext(BlockReader reader) throws IOException {
        DebugElementType<?> type = DebugElementType.readFlag(reader);
        DebugElement debugElement = type.getCreator().newInstance();
        this.elementList.add(debugElement);
        debugElement.readBytes(reader);
        return debugElement;
    }

    @Override
    public int size() {
        return elementList.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public boolean contains(Object o) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public Iterator<DebugElement> iterator() {
        return elementList.iterator();
    }
    @Override
    public Object[] toArray() {
        return elementList.toArray();
    }
    @Override
    public <T> T[] toArray(T[] ts) {
        return elementList.toArray(ts);
    }
    @Override
    public boolean add(DebugElement element) {
        return elementList.add(element);
    }
    @Override
    public boolean remove(Object obj) {
        return elementList.remove((DebugElement) obj);
    }
    @Override
    public boolean containsAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public boolean addAll(Collection<? extends DebugElement> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new IllegalArgumentException("Not implemented");
    }
    @Override
    public void clear() {
        elementList.clearChildes();
    }
}
