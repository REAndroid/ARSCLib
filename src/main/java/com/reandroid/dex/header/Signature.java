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
package com.reandroid.dex.header;

import com.reandroid.dex.sections.DexLayoutBlock;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.Sha1OutputStream;

import java.io.IOException;
import java.io.OutputStream;

public class Signature extends HeaderPiece {

    public Signature(){
        super(20);
    }

    public void update() {
        DexLayoutBlock dexLayoutBlock = getParentInstance(DexLayoutBlock.class);
        if (dexLayoutBlock == null) {
            return;
        }
        Sha1OutputStream outputStream = new Sha1OutputStream();
        try {
            dexLayoutBlock.writeBytes(outputStream);
        } catch (IOException exception) {
            // will not reach here
            throw new RuntimeException(exception);
        }
        outputStream.digest(getBytesInternal(), 0);
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if (stream instanceof Sha1OutputStream) {
            ((Sha1OutputStream) stream).reset();
            return 0;
        }
        return super.onWriteBytes(stream);
    }
    public String getHex() {
        return HexUtil.toHexString(getBytesInternal());
    }
    @Override
    public String toString() {
        return getHex();
    }
}
