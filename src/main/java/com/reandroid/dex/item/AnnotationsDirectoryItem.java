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

import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.IntegerList;

import java.io.IOException;

public class AnnotationsDirectoryItem extends FixedBlockContainer {
    private final Header header;
    private final BlockList<AnnotationItem> classAnnotations;
    private final BlockList<AnnotationItem> fieldAnnotations;
    private final BlockList<AnnotationItem> methodAnnotations;
    public AnnotationsDirectoryItem() {
        super(4);
        this.header = new Header();
        this.classAnnotations = new BlockList<>();
        this.fieldAnnotations = new BlockList<>();
        this.methodAnnotations = new BlockList<>();
        addChild(0, header);
        addChild(1, classAnnotations);
        addChild(2, fieldAnnotations);
        addChild(3, methodAnnotations);
    }
    public BlockList<AnnotationItem> getClassAnnotations() {
        return classAnnotations;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        Header itemValue = this.header;
        itemValue.readBytes(reader);
        int position = reader.getPosition();
        int offset = itemValue.getClassOffset();
        if(offset <= 0){
            return;
        }
        reader.seek(offset);
        IntegerList integerList = new IntegerList();
        integerList.readBytes(reader);
        if(integerList.size() == 0){
            reader.seek(position);
            return;
        }
        int[] offsetsArray = integerList.toArray();
        for(int annotationOffset : offsetsArray){
            if(annotationOffset == 0){
                continue;
            }
            reader.seek(annotationOffset);
            AnnotationItem adi = new AnnotationItem();
            classAnnotations.add(adi);
            adi.readBytes(reader);
        }

        reader.seek(position);
        //TODO: read field, method annotation, data ...
    }

    @Override
    public String toString() {
        return header.toString();
    }

    public static class Header extends BaseItem{
        public Header() {
            super(SIZE);
        }
        public int getClassOffset(){
            return getInteger(getBytesInternal(), OFFSET_CLASS_OFFSET);
        }
        public int getFieldCount(){
            return getInteger(getBytesInternal(), OFFSET_FIELD_COUNT);
        }
        public int getMethodCount(){
            return getInteger(getBytesInternal(), OFFSET_METHOD_COUNT);
        }
        public int getParameterCount(){
            return getInteger(getBytesInternal(), OFFSET_PARAMETER_COUNT);
        }
        public int getAnnotationsStart(){
            return getInteger(getBytesInternal(), OFFSET_ANNOTATIONS_START);
        }


        @Override
        public String toString() {
            return "class_offset=" + getClassOffset()
                    + ", fields=" + getFieldCount()
                    + ", methods=" + getMethodCount()
                    + ", parameter=" + getParameterCount()
                    + ", annotations start=" + getAnnotationsStart();
        }

        private static final int OFFSET_CLASS_OFFSET = 0;
        private static final int OFFSET_FIELD_COUNT = 4;
        private static final int OFFSET_METHOD_COUNT = 8;
        private static final int OFFSET_PARAMETER_COUNT = 12;
        private static final int OFFSET_ANNOTATIONS_START = 16;
        private static final int SIZE = 20;
    }
}
