package com.reandroid.dex.item;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.index.FieldIndex;
import com.reandroid.dex.index.MethodIndex;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class MethodDef extends Def {
    private final Ule128Item codeOffset;
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
            return ".method " + AccessFlag.formatForField(getAccessFlagsValue())
                    + " " + methodIndex.toString();
        }
        return Integer.toString(getIdValue());
    }
}
