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
package com.reandroid.dex.value;

import com.reandroid.dex.data.EncodedArray;
import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.collection.IterableIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class ArrayValue extends DexValueBlock<EncodedArray>
        implements Iterable<DexValueBlock<?>> {

    public ArrayValue() {
        super(new EncodedArray(), DexValueType.ARRAY);
    }

    public DexValueBlock<?> get(int i){
        return getValueContainer().get(i);
    }
    public int size() {
        return getValueContainer().size();
    }
    public void add(DexValueBlock<?> value){
        getValueContainer().add(value);
    }
    public void remove(DexValueBlock<?> value){
        getValueContainer().remove(value);
    }
    public<T1 extends DexValueBlock<?>> T1 createNext(DexValueType<T1> valueType){
        T1 item = valueType.newInstance();
        add(item);
        return item;
    }
    @Override
    public Iterator<DexValueBlock<?>> iterator() {
        return getValueContainer().iterator();
    }
    public<T1 extends DexValueBlock<?>> Iterator<T1> iterator(Class<T1> instance) {
        return getValueContainer().iterator(instance);
    }
    public<T1 extends DexValueBlock<?>> Iterator<T1> iterator(Class<T1> instance, Predicate<? super T1> filter){
        return getValueContainer().iterator(instance, filter);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ARRAY;
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return new IterableIterator<DexValueBlock<?>, IdItem>(iterator()) {
            @Override
            public Iterator<IdItem> iterator(DexValueBlock<?> element) {
                return element.usedIds();
            }
        };
    }
    @Override
    public void replaceKeys(Key search, Key replace) {
        for(DexValueBlock<?> valueBlock : this){
            valueBlock.replaceKeys(search, replace);
        }
    }

    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
        ArrayValue coming = (ArrayValue) valueBlock;
        getValueContainer().merge(coming.getValueContainer());
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append('{');
        writer.indentPlus();
        int count = size();
        for(int i = 0; i < count; i++){
            if(i != 0){
                writer.append(',');
            }
            writer.newLine();
            get(i).append(writer);
        }
        writer.indentMinus();
        if(count > 0){
            writer.newLine();
        }
        writer.append('}');
    }
}
