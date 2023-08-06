package com.reandroid.dex.value;

import com.reandroid.arsc.base.BlockArray;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.item.AnnotationElement;
import com.reandroid.dex.item.AnnotationItemList;

import java.io.IOException;

public class AnnotationValue extends DexValue<AnnotationValue.AnnotationArray>{
    public AnnotationValue(){
        super(new AnnotationArray());
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
        public BlockArray<AnnotationElement> getArray(){
            return elementArray;
        }
    }
    private static class ElementArray extends BlockArray<AnnotationElement>{
        private final IntegerReference itemCount;
        public ElementArray(Ule128Item itemCount){
            super();
            this.itemCount = itemCount;
        }

        @Override
        public AnnotationElement[] newInstance(int length) {
            return new AnnotationElement[length];
        }
        @Override
        public AnnotationElement newInstance() {
            return new AnnotationElement();
        }
        @Override
        protected void onRefreshed() {
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
            setChildesCount(itemCount.get());
            super.onReadBytes(reader);
        }
    }
}
