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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class ZipAligner {
    private final Map<Pattern, Integer> alignmentMap;
    private int defaultAlignment;
    private boolean enableDataDescriptor;
    private long mCurrentOffset;

    public ZipAligner(){
        alignmentMap = new HashMap<>();
    }

    public void setFileAlignment(Pattern patternFileName, int alignment){
        if(patternFileName == null){
            return;
        }
        alignmentMap.remove(patternFileName);
        if(alignment > 1){
            alignmentMap.put(patternFileName, alignment);
        }
    }
    public void clearFileAlignment(){
        alignmentMap.clear();
    }
    public void setDefaultAlignment(int defaultAlignment) {
        if(defaultAlignment <= 0){
            defaultAlignment = 1;
        }
        this.defaultAlignment = defaultAlignment;
    }
    public void setEnableDataDescriptor(boolean enableDataDescriptor) {
        this.enableDataDescriptor = enableDataDescriptor;
    }

    void reset(){
        mCurrentOffset = 0;
    }
    void align(InputSource inputSource, LocalFileHeader lfh){
        lfh.setExtra(null);
        int padding;
        if(inputSource.getMethod() != ZipEntry.STORED){
            padding = 0;
            createDataDescriptor(lfh);
        }else {
            int alignment = getAlignment(inputSource.getAlias());
            long dataOffset = mCurrentOffset + lfh.countBytes();
            padding = (int) ((alignment - (dataOffset % alignment)) % alignment);
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
    private int getAlignment(String name){
        for(Map.Entry<Pattern, Integer> entry:alignmentMap.entrySet()){
            Matcher matcher = entry.getKey().matcher(name);
            if(matcher.matches()){
                return entry.getValue();
            }
        }
        return defaultAlignment;
    }

    public static ZipAligner apkAligner(){
        ZipAligner zipAligner = new ZipAligner();
        zipAligner.setDefaultAlignment(ALIGNMENT_4);
        Pattern patternNativeLib = Pattern.compile("^lib/.+\\.so$");
        zipAligner.setFileAlignment(patternNativeLib, ALIGNMENT_PAGE);
        zipAligner.setEnableDataDescriptor(true);
        return zipAligner;
    }

    private static final int ALIGNMENT_4 = 4;
    private static final int ALIGNMENT_PAGE = 4096;
}
