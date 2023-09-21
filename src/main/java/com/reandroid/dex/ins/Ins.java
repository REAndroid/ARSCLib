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

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.NumberIntegerReference;
import com.reandroid.dex.item.DexContainerItem;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.EmptyList;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Ins extends DexContainerItem implements SmaliFormat {
    private final Opcode<?> opcode;
    private List<ExtraLine> extraLines;
    private final IntegerReference address;
    Ins(int childesCount, Opcode<?> opcode) {
        super(childesCount);
        this.opcode = opcode;
        this.extraLines = EmptyList.of();
        this.address = new NumberIntegerReference();
    }
    Ins(Opcode<?> opcode) {
        this(1, opcode);
    }
    public Opcode<?> getOpcode() {
        return opcode;
    }

    public int getCodeUnits(){
        return countBytes() / 2;
    }
    public IntegerReference getAddressReference() {
        return address;
    }
    public int getAddress() {
        return address.get();
    }
    public void setAddress(int address) {
        this.address.set(address);
    }

    private boolean containsExtraLine(ExtraLine extraLine){
        Iterator<ExtraLine> iterator = getExtraLines();
        while (iterator.hasNext()){
            if(extraLine.isEqualExtraLine(iterator.next())){
                return true;
            }
        }
        return false;
    }
    public void addExtraLine(ExtraLine extraLine){
        if(extraLines.isEmpty()){
            extraLines = new ArrayList<>();
        }else if(containsExtraLine(extraLine)){
            return;
        }
        extraLines.add(extraLine);
    }
    public Iterator<ExtraLine> getExtraLines(){
        return this.extraLines.iterator();
    }
    public Iterator<ExtraLine> getExtraLinesSorted(){
        if(extraLines.isEmpty()){
            return EmptyIterator.of();
        }
        extraLines.sort(ExtraLine.COMPARATOR);
        return this.extraLines.iterator();
    }
    public void sortExtraLines() {
        if(extraLines.isEmpty()){
            return;
        }
        extraLines.sort(ExtraLine.COMPARATOR);
    }
    public void clearExtraLines() {
        if(!extraLines.isEmpty()){
            extraLines = EmptyList.of();
        }
    }
    public boolean hasExtraLines() {
        return !extraLines.isEmpty();
    }
    private void appendExtraLines(SmaliWriter writer) throws IOException {
        Iterator<ExtraLine> iterator = getExtraLinesSorted();
        ExtraLine extraLine = null;
        boolean hasHandler = false;
        while (iterator.hasNext()){
            writer.newLine();
            extraLine = iterator.next();
            extraLine.appendExtra(writer);
            if(!hasHandler){
                hasHandler = extraLine.getSortOrder() == ExtraLine.ORDER_EXCEPTION_HANDLER;
            }
        }
        if(hasHandler && extraLine.getSortOrder() >= ExtraLine.ORDER_EXCEPTION_HANDLER){
            writer.newLine();
        }
    }

    @Override
    protected void onRefreshed() {
        super.onRefreshed();
        clearExtraLines();
    }

    @Override
    public final void append(SmaliWriter writer) throws IOException {
        appendExtraLines(writer);
        appendCode(writer);
    }
    void appendCode(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(getOpcode().getName());
        writer.append(' ');
    }
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        SmaliWriter smaliWriter = new SmaliWriter(writer);
        try {
            append(smaliWriter);
            smaliWriter.close();
        } catch (IOException exception) {
            return exception.toString();
        }
        return writer.toString();
    }
}
