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

import com.reandroid.dex.sections.DexLayout;
import com.reandroid.utils.Alder32OutputStream;
import com.reandroid.utils.HexUtil;

import java.io.IOException;
import java.io.OutputStream;

public class DexChecksum extends HeaderPiece {

    public DexChecksum(){
        super(4);
    }

    public int getValue(){
        return getInteger(0);
    }
    public void setValue(long checksum){
        setSize(4);
        putInteger(0, (int)checksum);
    }
    public boolean update() {
        DexLayout dexLayout = getParentInstance(DexLayout.class);
        if (dexLayout == null) {
            return false;
        }
        int previous = getValue();
        Alder32OutputStream outputStream = new Alder32OutputStream();
        try {
            dexLayout.writeBytes(outputStream);
        } catch (IOException exception) {
            // will not reach here
            throw new RuntimeException(exception);
        }
        setValue(outputStream.getValue());
        return previous != getValue();
    }

    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if (stream instanceof Alder32OutputStream) {
            ((Alder32OutputStream) stream).reset();
            return 0;
        }
        return super.onWriteBytes(stream);
    }

    @Override
    public String toString(){
        return HexUtil.toHex8(getValue());
    }
}
