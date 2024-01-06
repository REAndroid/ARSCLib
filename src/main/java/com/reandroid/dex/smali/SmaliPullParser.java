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
package com.reandroid.dex.smali;

import com.reandroid.dex.ins.Opcode;
import com.reandroid.utils.ObjectsUtil;

public class SmaliPullParser {

    private final SmaliReader reader;
    private int event;

    public SmaliPullParser(SmaliReader reader){
        this.reader = reader;
    }

    public int getEvent() {
        return event;
    }
    public int nextEvent(){
        this.event = computeNextEvent();
        return this.event;
    }
    private int computeNextEvent(){
        SmaliReader reader = this.reader;
        reader.skipWhitespacesOrComment();
        if(reader.finished()){
            return END;
        }
        byte b = reader.get();
        switch (b){
            case '.':
                return DIRECTIVE;
            case ':':
                return LABEL;
        }
        int prev = getEvent();
        if(prev == OPCODE || prev == REGISTERS){
            switch (b){
                case 'v':
                case 'p':
                    return REGISTERS;
                case '[':
                case '\"':
                case 'L':
                    return KEY;
            }
        }
        Opcode<?> opcode = Opcode.parseSmali(reader, false);
        if(opcode != null){
            return OPCODE;
        }
        return END;
    }

    public static final int BEGIN = ObjectsUtil.of(0);
    public static final int END = ObjectsUtil.of(1);
    public static final int DIRECTIVE = ObjectsUtil.of(2);
    public static final int OPCODE = ObjectsUtil.of(3);
    public static final int LABEL = ObjectsUtil.of(4);
    public static final int REGISTERS = ObjectsUtil.of(5);
    public static final int KEY = ObjectsUtil.of(6);
    public static final int STRING = ObjectsUtil.of(9);
}
