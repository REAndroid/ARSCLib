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
package com.reandroid.archive2.writer;

import com.reandroid.archive.InputSource;
import com.reandroid.archive2.block.DataDescriptor;
import com.reandroid.archive2.block.LocalFileHeader;

import java.util.zip.ZipEntry;

public class ZipAligner {
    private long mTotalPadding;
    private long mCurrentOffset;
    private boolean enableDataDescriptor = true;
    public ZipAligner(){
    }
    public void align(InputSource inputSource, LocalFileHeader lfh){
        int padding;
        if(inputSource.getMethod() != ZipEntry.STORED){
            padding = 0;
            createDataDescriptor(lfh);
        }else {
            int alignment = getAlignment(inputSource);
            long newOffset = mCurrentOffset + mTotalPadding;
            padding = (int) ((alignment - (newOffset % alignment)) % alignment);
            mTotalPadding += padding;
        }
        lfh.setExtra(new byte[padding]);
        mCurrentOffset += lfh.getDataSize() + lfh.countBytes();
        DataDescriptor dataDescriptor = lfh.getDataDescriptor();
        if(dataDescriptor!=null){
            mCurrentOffset += dataDescriptor.countBytes();
        }
    }
    private void createDataDescriptor(LocalFileHeader lfh){
        DataDescriptor dataDescriptor;
        if(enableDataDescriptor){
            dataDescriptor = DataDescriptor.fromLocalFile(lfh);
        }else {
            dataDescriptor = null;
        }
        lfh.setDataDescriptor(dataDescriptor);
    }
    public void reset(){
        mTotalPadding = 0;
        mCurrentOffset = 0;
    }
    public void setEnableDataDescriptor(boolean enableDataDescriptor) {
        this.enableDataDescriptor = enableDataDescriptor;
    }

    private int getAlignment(InputSource inputSource){
        String name = inputSource.getAlias();
        if(name.startsWith("lib/") && name.endsWith(".so")){
            return ALIGNMENT_PAGE;
        }else {
            return ALIGNMENT_4;
        }
    }

    private static final int ALIGNMENT_4 = 4;
    private static final int ALIGNMENT_PAGE = 4096;
}
