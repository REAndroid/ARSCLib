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
package com.reandroid.dex.ins;

import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.IndexIterator;
import com.reandroid.utils.collection.SizedSupplier;

import java.io.IOException;
import java.util.Iterator;

public class Registers implements SizedSupplier<Reg>, Iterable<Reg>, SmaliFormat {

    private final RegisterFactory factory;
    private final RegisterNumber registerNumber;

    public Registers(RegisterFactory factory, RegisterNumber registerNumber){
        this.factory = factory;
        this.registerNumber = registerNumber;
    }

    public Reg get(int index) {
        return new Reg(getFactory(), getRegisterNumber(), index);
    }
    @Override
    public int size() {
        return getRegisterNumber().getRegistersCount();
    }
    @Override
    public Iterator<Reg> iterator() {
        return new IndexIterator<>(this);
    }
    public RegisterNumber getRegisterNumber() {
        return registerNumber;
    }
    public RegisterFactory getFactory() {
        return factory;
    }
    String getSeparator() {
        return registerNumber.getRegisterSeparator();
    }


    @Override
    public void append(SmaliWriter writer) throws IOException {
        int size = size();
        String separator = getSeparator();
        for(int i = 0; i < size; i++){
            if(i != 0){
                writer.append(separator);
            }
            get(i).append(writer);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        int size = size();
        String separator = getSeparator();
        for(int i = 0; i < size; i++){
            if(i != 0){
                builder.append(separator);
            }
            builder.append(get(i));
        }
        return builder.toString();
    }
}
