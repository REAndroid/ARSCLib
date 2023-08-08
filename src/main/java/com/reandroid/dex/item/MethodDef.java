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
package com.reandroid.dex.item;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.MethodIndex;
import com.reandroid.dex.reader.DexReader;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class MethodDef extends Def {
    private final Ule128Item codeOffset;
    private CodeItem codeItem;
    public MethodDef() {
        super(1);
        this.codeOffset = new Ule128Item();
        addChild(2, codeOffset);
    }
    public MethodIndex getMethodIndex(){
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile != null){
            return dexFile.getMethodSection().get(getDefIndexId());
        }
        return null;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        CodeItem codeItem = new CodeItem(this.codeOffset);
        codeItem.setParent(this);
        DexReader dexReader = (DexReader) reader;
        codeItem = dexReader.getCodePool().getOrRead(reader, codeItem, true);
        this.codeItem = codeItem;
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.newLine();
        writer.append(".method ");
        AccessFlag[] accessFlags = AccessFlag.getForMethod(getAccessFlagsValue());
        for(AccessFlag af:accessFlags){
            writer.append(af.toString());
            writer.append(' ');
        }
        MethodIndex methodIndex = getMethodIndex();
        writer.append(methodIndex.getNameString().getString());
        writer.append('(');
        methodIndex.getProto().append(writer);
        writer.append(')');
        methodIndex.getProto().getReturnTypeIndex().append(writer);
        writer.indentPlus();
        writer.newLine();
        for(AnnotationGroup group: methodIndex.getAnnotations()){
            group.append(writer);
        }
        writer.newLine();
        writer.append(".locals 0");
        writer.newLine();
        writer.appendComment("TODO: finish instructions");
        writer.indentMinus();
        writer.newLine();
        writer.append(".end method");
    }
    @Override
    public String toString() {
        MethodIndex methodIndex = getMethodIndex();
        if(methodIndex != null){
            return ".method " + AccessFlag.formatForMethod(getAccessFlagsValue())
                    + " " + methodIndex.toString();
        }
        return Integer.toString(getIdValue());
    }
}
