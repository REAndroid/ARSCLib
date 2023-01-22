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
package com.reandroid.archive;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ByteInputSource extends InputSource {
    private byte[] inBytes;
    public ByteInputSource(byte[] inBytes, String name) {
        super(name);
        this.inBytes=inBytes;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        byte[] bts=getBytes();
        outputStream.write(bts);
        return bts.length;
    }
    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(getBytes());
    }
    public byte[] getBytes() {
        return inBytes;
    }
    @Override
    public void disposeInputSource(){
        inBytes=new byte[0];
    }
}
