package com.reandroid.dex.item;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.index.StringId;
import com.reandroid.dex.key.AnnotationKey;
import com.reandroid.dex.value.DexValueBlock;
import com.reandroid.dex.value.DexValueType;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationElement extends DataSectionEntry implements SmaliFormat {

    private final StringReferenceUle128 elementName;

    public AnnotationElement() {
        super(2);
        this.elementName = new StringReferenceUle128(StringId.USAGE_METHOD_NAME);
        addChild(0, elementName);
    }

    @Override
    public AnnotationKey getKey(){
        AnnotationItem parentItem = getParent(AnnotationItem.class);
        if(parentItem != null){
            return new AnnotationKey(parentItem.getTypeId().getName(), getName(), null);
        }
        return null;
    }
    public DexValueBlock<?> getValue(){
        return (DexValueBlock<?>) getChildes()[1];
    }
    public void setValue(DexValueBlock<?> dexValue){
        addChild(1, dexValue);
    }
    public DexValueType<?> getValueType(){
        DexValueBlock<?> value = getValue();
        if(value != null){
            return value.getValueType();
        }
        return null;
    }
    public String getName(){
        return elementName.getString();
    }
    public void setName(String name){
        elementName.setString(name);
    }
    public StringData getNameStringData(){
        return elementName.getItem();
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        this.elementName.onReadBytes(reader);
        DexValueBlock<?> value = DexValueType.create(reader);
        setValue(value);
        value.onReadBytes(reader);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(" = ");
        getValue().append(writer);
    }
    @Override
    public String toString() {
        return getName() + " = " + getValue();
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
