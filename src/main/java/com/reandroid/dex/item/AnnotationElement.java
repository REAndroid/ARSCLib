package com.reandroid.dex.item;

import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.container.SingleBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.StringIndex;
import com.reandroid.dex.value.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationElement extends FixedBlockContainer implements SmaliFormat {
    private final Ule128Item nameIndex;
    private final ByteItem valueType;
    private final SingleBlockContainer<DexValue<?>> valueContainer;

    public AnnotationElement() {
        super(3);
        this.nameIndex = new Ule128Item();
        this.valueType = new ByteItem();
        this.valueContainer = new SingleBlockContainer<>();

        addChild(0, nameIndex);
        addChild(1, valueType);
        addChild(2, valueContainer);
    }
    public DexValue<?> getValue(){
        return valueContainer.getItem();
    }
    public void setValue(DexValue<?> dexValue){
        valueContainer.setItem(dexValue);
    }
    public DexValueType getValueType(){
        return DexValueType.fromFlag(valueType.unsignedInt());
    }
    public int getValueTypeSize(){
        return DexValueType.decodeSize(valueType.unsignedInt()) + 1;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        nameIndex.readBytes(reader);
        valueType.readBytes(reader);
        reader.offset(-1);
        setValue(DexValue.createFor(getValueType()));
        valueContainer.readBytes(reader);
    }
    public StringIndex getName(){
        int i = nameIndex.get();
        if(i == 0){
            return null;
        }
        DexFile dexFile = getParentInstance(DexFile.class);
        if(dexFile == null){
            return null;
        }
        return dexFile.getStringPool().get(i);
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
}
