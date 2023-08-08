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
package com.reandroid.dex.reader;

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.DexFile;
import com.reandroid.dex.item.AnnotationElement;
import com.reandroid.dex.item.CodeItem;

public class DexReader extends BlockReader {
    private final DexFile dexFile;
    private final ReaderPool<AnnotationElement> annotationElementPool;
    private final ReaderPool<CodeItem> codePool;

    public DexReader(DexFile dexFile, byte[] bytes) {
        super(bytes);
        this.dexFile = dexFile;
        this.codePool = new ReaderPool<>();
        this.annotationElementPool = new ReaderPool<>();
    }
    public DexReader(DexFile dexFile, BlockReader reader) {
        this(dexFile, reader.getBuffer());
        reader.close();
    }
    public ReaderPool<AnnotationElement> getAnnotationPool(){
        return annotationElementPool;
    }
    public ReaderPool<CodeItem> getCodePool() {
        return codePool;
    }

    public DexFile getDexFile() {
        return dexFile;
    }
    public static DexReader create(DexFile dexFile, BlockReader reader){
        if(reader instanceof DexReader){
            DexReader dexReader = (DexReader) reader;
            if(dexFile == dexReader.getDexFile()){
                return dexReader;
            }
        }
        return new DexReader(dexFile, reader);
    }
}
