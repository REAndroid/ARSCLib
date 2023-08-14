package com.reandroid.dex.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationElement extends DexItem
        implements BlockLoad, SmaliFormat {
    private final Ule128Item nameIndex;
    private final SingleBlockContainer<DexValue<?>> valueContainer;

    public AnnotationElement() {
        super(2);
        this.nameIndex = new Ule128Item();
        this.valueContainer = new SingleBlockContainer<>();

        addChild(0, nameIndex);
        addChild(1, valueContainer);

        nameIndex.setBlockLoad(this);
    }
    public DexValue<?> getValue(){
        return valueContainer.getItem();
    }
    public void setValue(DexValue<?> dexValue){
        valueContainer.setItem(dexValue);
    }
    public DexValueType getValueType(){
        DexValue<?> value = getValue();
        if(value != null){
            return value.getValueType();
        }
        return null;
    }

    public StringData getName(){
        int i = nameIndex.get();
        if(i == 0){
            return null;
        }
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile == null){
            return null;
        }
        return dexFile.getSectionList().get(SectionType.STRING_DATA, i);
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == this.nameIndex){
            DexValueType valueType = DexValueType.fromFlag(reader.read());
            reader.offset(-1);
            setValue(DexValue.createFor(valueType));
        }
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName().getString());
        writer.append(" = ");
        getValue().append(writer);
    }
    @Override
    public String toString() {
        return  getName() + " = " + getValue();
    }

    public static final Creator<AnnotationElement> CREATOR = new Creator<AnnotationElement>() {
        @Override
        public AnnotationElement[] newInstance(int length) {
            if(length == 0){
                return EMPTY;
            }
            return new AnnotationElement[length];
        }
        @Override
        public AnnotationElement newInstance() {
            return new AnnotationElement();
        }
    };
    private static final AnnotationElement[] EMPTY = new AnnotationElement[0];
}
