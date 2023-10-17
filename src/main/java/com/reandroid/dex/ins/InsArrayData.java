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

import com.reandroid.arsc.item.*;
import com.reandroid.dex.base.DexBlockAlign;
import com.reandroid.dex.base.NumberArray;
import com.reandroid.dex.data.InstructionList;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.InstanceIterator;

import java.io.IOException;
import java.util.Iterator;

public class InsArrayData extends PayloadData implements VisitableInteger {

    private final NumberArray numberArray;

    public InsArrayData() {
        super(4, Opcode.ARRAY_PAYLOAD);
        ShortItem elementWidth = new ShortItem();
        IntegerItem elementCount = new IntegerItem();
        this.numberArray = new NumberArray(elementWidth, elementCount);

        DexBlockAlign blockAlign = new DexBlockAlign(this.numberArray);
        blockAlign.setAlignment(2);

        addChild(1, elementWidth);
        addChild(2, elementCount);
        addChild(3, this.numberArray);
        addChild(4, blockAlign);
    }

    public Iterator<InsFillArrayData> getInsFillArrayData(){
        InstructionList instructionList = getInstructionList();
        if(instructionList == null){
            return EmptyIterator.of();
        }
        instructionList.buildExtraLines();
        return InstanceIterator.of(getExtraLines(), InsFillArrayData.class);
    }
    @Override
    public void visitIntegers(IntegerVisitor visitor) {
        if(getWidth() != 4) {
            return;
        }
        int size = size();
        for(int i = 0; i < size; i++){
            visitor.visit(this, getReference(i));
        }
    }
    public int size(){
        return getNumberArray().size();
    }
    public int getWidth(){
        return getNumberArray().getWidth();
    }
    public void setWidth(int width){
        getNumberArray().setWidth(width);
    }
    public IntegerReference getReference(int i){
        return getNumberArray().getReference(i);
    }
    public int getAsInteger(int index){
        return getNumberArray().getAsInteger(index);
    }
    public void put(int index, int value){
        getNumberArray().put(index, value);
    }
    public void putLong(int index, long value){
        getNumberArray().putLong(index, value);
    }
    public NumberArray getNumberArray() {
        return numberArray;
    }

    @Override
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append('.');
        writer.append(getOpcode().getName());
        writer.append(' ');
        writer.append(getWidth());
        writer.indentPlus();
        appendData(writer);
        writer.indentMinus();
        writer.newLine();
        writer.append(".end ");
        writer.append(getOpcode().getName());
    }
    private void appendData(SmaliWriter writer) throws IOException {
        NumberArray numberArray = getNumberArray();
        int width = numberArray.getWidth();
        if(width < 5){
            buildHex(writer, numberArray.getAsIntegers());
        }else {
            buildHex(writer, numberArray.getLongArray());
        }
    }
    private void buildHex(SmaliWriter builder, int[] numbers) throws IOException {
        for(int num : numbers){
            builder.newLine();
            builder.append(HexUtil.toHex(num, 1));
        }
    }
    private void buildHex(SmaliWriter builder, long[] numbers) throws IOException {
        for(long num : numbers){
            builder.newLine();
            builder.append(HexUtil.toHex(num, 1));
        }
    }
}