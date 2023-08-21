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
package com.reandroid.dex.debug;

import com.reandroid.dex.ins.ExtraLine;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugLine extends DebugEmptyElement {
    private int lineNumber = -1;
    public DebugLine() {
        super(0xff);
    }
    public DebugLine(DebugElementType<?> elementType) {
        super(elementType.getFlag());
    }
    public int getLineNumber(){
        return lineNumber;
    }
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    @Override
    public boolean isEqualExtraLine(Object obj) {
        if(obj == this){
            return true;
        }
        if(!(obj instanceof DebugLine)){
            return false;
        }
        DebugLine debugLine = (DebugLine) obj;
        return getAddress() == debugLine.getAddress();
    }
    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        int lineNum = getLineNumber();
        if(lineNum == -1){
            return;
        }
        writer.append(".line ");
        writer.append(lineNum);
    }
    @Override
    public int getSortOrder() {
        return ExtraLine.ORDER_DEBUG_LINE_NUMBER;
    }
    @Override
    public String toString() {
        return ".line " + lineNumber;
    }
}
