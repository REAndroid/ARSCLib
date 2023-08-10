package com.reandroid.dex.value;

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.item.AnnotationElement;
import com.reandroid.dex.reader.DexReader;
import com.reandroid.dex.reader.ReaderPool;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.List;

public class AnnotationValue extends DexValue<AnnotationValue.AnnotationArray>{
    public AnnotationValue(){
        super(new AnnotationArray());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        AnnotationArray array = getValue();
        for(AnnotationElement element:array.getArray()){
            element.append(writer);
        }
    }

    public static class AnnotationArray extends FixedBlockContainer{
        private final Ule128Item typeIndex;
        private final Ule128Item elementCount;
        private final ElementArray elementArray;
        public AnnotationArray() {
            super(3);
            this.typeIndex = new Ule128Item();
            this.elementCount = new Ule128Item();
            this.elementArray = new ElementArray(elementCount);
            addChild(0, typeIndex);
            addChild(1, elementCount);
            addChild(2, elementArray);
        }
        public List<AnnotationElement> getArray(){
            return elementArray.getChildren();
        }
    }
    private static class ElementArray extends BlockList<AnnotationElement> implements Iterable<AnnotationElement>{
        private final IntegerReference itemCount;
        public ElementArray(IntegerReference itemCount){
            super();
            this.itemCount = itemCount;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            DexReader dexReader = (DexReader) reader;
            ReaderPool<AnnotationElement> pool = dexReader.getAnnotationPool();
            int count = itemCount.get();
            for(int i = 0; i < count; i++){
                AnnotationElement element = new AnnotationElement();
                element.setParent(this);
                element.getOffsetReference().set(reader.getPosition());
                element = pool.getOrRead(reader, element);
                add(element);
            }
        }
    }
}
