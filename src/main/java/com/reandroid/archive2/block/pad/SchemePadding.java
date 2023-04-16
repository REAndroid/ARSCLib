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
package com.reandroid.archive2.block.pad;

import com.reandroid.archive2.block.SignatureId;
import com.reandroid.archive2.block.SignatureInfo;
import com.reandroid.archive2.block.SignatureScheme;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;

import java.io.IOException;

public class SchemePadding extends SignatureScheme {
    private final ByteArray byteArray;
    public SchemePadding() {
        super(1, SignatureId.PADDING);
        this.byteArray = new ByteArray();
        addChild(this.byteArray);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        SignatureInfo signatureInfo = getSignatureInfo();
        int size = (int) signatureInfo.getDataSize() - 4;
        byteArray.setSize(size);
        super.onReadBytes(reader);
    }
}