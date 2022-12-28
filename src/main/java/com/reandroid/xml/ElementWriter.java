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
package com.reandroid.xml;

import java.io.IOException;
import java.io.Writer;

class ElementWriter extends Writer {
    private final Writer mWriter;
    private final long mMaxLen;
    private final boolean mUnlimitedLength;
    private long mCurrentLength;
    private boolean mLengthFinished;
    ElementWriter(Writer writer, long maxLen){
        mWriter=writer;
        this.mMaxLen=maxLen;
        this.mUnlimitedLength=maxLen<0;
    }
    ElementWriter(Writer writer){
        this(writer, -1);
    }
    boolean isFinished(){
        return mLengthFinished;
    }
    private boolean mInterruptedWritten;
    void writeInterrupted(){
        if(!mLengthFinished){
            return;
        }
        if(mInterruptedWritten){
            return;
        }
        mInterruptedWritten=true;
        String txt="\n      .\n      .\n      .\n   more items ...\n";
        try {
            mWriter.write(txt);
        } catch (IOException e) {
        }
    }
    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
        updateCurrentLength(i1);
        mWriter.write(chars, i, i1);
    }

    @Override
    public void flush() throws IOException {
        mWriter.flush();
    }
    @Override
    public void close() throws IOException {
        mWriter.close();
    }
    private boolean updateCurrentLength(int len){
        if(mUnlimitedLength){
            return false;
        }
        if(mLengthFinished){
            mLengthFinished=true;
            //return true;
        }
        mCurrentLength+=len;
        mLengthFinished=mCurrentLength>=mMaxLen;
        return mLengthFinished;
    }
}
