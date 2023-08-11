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

import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.MethodId;
import com.reandroid.dex.sections.SectionType;
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
    public MethodId getMethodIndex(){
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile != null){
            return dexFile.getSectionList().get(SectionType.METHOD_ID, getDefIndexId());
        }
        return null;
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
        MethodId methodId = getMethodIndex();
        writer.append(methodId.getNameString().getString());
        writer.append('(');
        methodId.getProto().append(writer);
        writer.append(')');
        methodId.getProto().getReturnTypeId().append(writer);
        writer.indentPlus();
        writer.newLine();
        for(AnnotationSet group: methodId.getAnnotations()){
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
        MethodId methodId = getMethodIndex();
        if(methodId != null){
            return ".method " + AccessFlag.formatForMethod(getAccessFlagsValue())
                    + " " + methodId.toString();
        }
        return ".method " + AccessFlag.formatForMethod(getAccessFlagsValue())
                + " " + getIdValue();
    }
}
