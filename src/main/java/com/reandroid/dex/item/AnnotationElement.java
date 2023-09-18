package com.reandroid.dex.item;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.pool.DexIdPool;
import com.reandroid.dex.sections.Section;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.value.*;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class AnnotationElement extends DataItemEntry
        implements BlockLoad, SmaliFormat {

    private final StringReferenceUle128 elementName;

    public AnnotationElement() {
        super(2);
        this.elementName = new StringReferenceUle128();
        addChild(0, elementName);
        elementName.setBlockLoad(this);
    }

    @Override
    public String getKey(){
        StringBuilder builder = new StringBuilder();
        AnnotationItem parentItem = getParent(AnnotationItem.class);
        if(parentItem != null){
            builder.append(parentItem.getTypeId().getKey());
            builder.append("->");
        }
        builder.append(getName());
        builder.append("()");
        return builder.toString();
    }
    public DexValue<?> getValue(){
        return (DexValue<?>) getChildes()[1];
    }
    public void setValue(DexValue<?> dexValue){
        addChild(1, dexValue);
    }
    public DexValueType<?> getValueType(){
        DexValue<?> value = getValue();
        if(value != null){
            return value.getValueType();
        }
        return null;
    }
    public String getName(){
        StringData stringData = getNameStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setName(String name){
        Section<StringData> section = getSection(SectionType.STRING_DATA);
        DexIdPool<StringData> pool = section.getPool();
        StringData stringData = pool.getOrCreate(name);
        setName(stringData);
    }
    public void setName(StringData name){
        elementName.setItem(name);
        linkStringUsageName();
    }
    public StringData getNameStringData(){
        return elementName.getItem();
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == this.elementName){
            setValue(DexValueType.create(reader));
        }
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        linkStringUsage();
    }
    private void linkStringUsage(){
        linkStringUsageName();
    }
    private void linkStringUsageName(){
        StringData stringData = this.elementName.getItem();
        if(stringData != null){
            stringData.addStringUsage(StringData.USAGE_METHOD);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getNameStringData().getString());
        writer.append(" = ");
        getValue().append(writer);
    }
    @Override
    public String toString() {
        return getNameStringData() + " = " + getValue();
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
