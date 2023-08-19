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

import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.utils.HexUtil;

import java.io.IOException;

public class DebugElementType<T extends DebugElement> {

    public static final DebugElementType<?>[] VALUES;
    private static final BlockCreator<DebugSkip> SKIP_BLOCK_CREATOR;

    public static final DebugElementType<DebugStartLocal> START_LOCAL;
    public static final DebugElementType<DebugEndLocal> END_LOCAL;
    public static final DebugElementType<DebugRestartLocal> RESTART_LOCAL;
    public static final DebugElementType<DebugPrologueEnd> PROLOGUE_END;
    public static final DebugElementType<DebugEpilogueBegin> EPILOGUE_BEGIN;
    public static final DebugElementType<DebugSetSourceFile> SET_SOURCE_FILE;
    public static final DebugElementType<DebugLineNumber> LINE_NUMBER;
    public static final DebugElementType<DebugEndSequence> END_SEQUENCE;
    public static final DebugElementType<DebugAdvancePc> ADVANCE_PC;
    public static final DebugElementType<DebugAdvanceLine> ADVANCE_LINE;
    public static final DebugElementType<DebugStartLocalExtended> START_LOCAL_EXTENDED;


    static {

        VALUES = new DebugElementType[0xff + 1];
        SKIP_BLOCK_CREATOR = DebugSkip::new;

        START_LOCAL = new DebugElementType<>("START_LOCAL", ".local",
                0x03, DebugStartLocal::new);
        VALUES[START_LOCAL.flag] = START_LOCAL;

        END_LOCAL = new DebugElementType<>("END_LOCAL", ".end local",
                0x05, DebugEndLocal::new);
        VALUES[END_LOCAL.flag] = END_LOCAL;

        RESTART_LOCAL = new DebugElementType<>("RESTART_LOCAL", ".restart local",
                0x06, DebugRestartLocal::new);
        VALUES[RESTART_LOCAL.flag] = RESTART_LOCAL;

        PROLOGUE_END = new DebugElementType<>("PROLOGUE_END", ".prologue end",
                0x07, DebugPrologueEnd::new);
        VALUES[PROLOGUE_END.flag] = PROLOGUE_END;

        EPILOGUE_BEGIN = new DebugElementType<>("EPILOGUE_BEGIN", ".prologue begin",
                0x08, DebugEpilogueBegin::new);
        VALUES[EPILOGUE_BEGIN.flag] = EPILOGUE_BEGIN;

        SET_SOURCE_FILE = new DebugElementType<>("SET_SOURCE_FILE", ".set source file",
                0x09, DebugSetSourceFile::new);
        VALUES[SET_SOURCE_FILE.flag] = SET_SOURCE_FILE;

        LINE_NUMBER = new DebugElementType<>("LINE_NUMBER", ".line",
                0x0a, DebugLineNumber::new);
        VALUES[LINE_NUMBER.flag] = LINE_NUMBER;



        END_SEQUENCE = new DebugElementType<>("END_SEQUENCE",
                0x00, DebugEndSequence::new);
        VALUES[END_SEQUENCE.flag] = END_SEQUENCE;

        ADVANCE_PC = new DebugElementType<>("ADVANCE_PC",
                0x01, DebugAdvancePc::new);
        VALUES[ADVANCE_PC.flag] = ADVANCE_PC;

        ADVANCE_LINE = new DebugElementType<>("ADVANCE_LINE",
                0x02, DebugAdvanceLine::new);
        VALUES[ADVANCE_LINE.flag] = ADVANCE_LINE;

        START_LOCAL_EXTENDED = new DebugElementType<>("START_LOCAL_EXTENDED", ".local",
                0x04, DebugStartLocalExtended::new);
        VALUES[START_LOCAL_EXTENDED.flag] = START_LOCAL_EXTENDED;

    }



    private final String name;
    private final String opcode;
    private final int flag;
    private final BlockCreator<T> creator;

    private DebugElementType(String name, String opcode, int flag, BlockCreator<T> creator){
        this.name = name;
        this.opcode = opcode;
        this.flag = flag;
        this.creator = creator;
    }
    private DebugElementType(String name, int flag, BlockCreator<T> creator){
        this(name, null, flag, creator);
    }

    public String getName() {
        return name;
    }
    public String getOpcode() {
        return opcode;
    }
    public int getFlag() {
        return flag;
    }
    public BlockCreator<T> getCreator() {
        return creator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugElementType<?> debugElementType = (DebugElementType<?>) obj;
        return flag == debugElementType.flag;
    }
    @Override
    public int hashCode() {
        return flag;
    }
    @Override
    public String toString() {
        if(opcode != null){
            return opcode;
        }
        return name;
    }

    public static DebugElementType<?> readFlag(BlockReader reader) throws IOException {
        int flag = reader.read();
        reader.offset(-1);
        return fromFlag(flag);
    }

    public static DebugElementType<?> fromFlag(int flag){
        flag = flag & 0xff;
        DebugElementType<?> debugElementType = VALUES[flag];
        if(debugElementType == null){
            debugElementType = createUnknown(flag);
        }
        return debugElementType;
    }
    private static DebugElementType<?> createUnknown(int flag){
        synchronized (DebugElementType.class){
            DebugElementType<?> debugElementType = VALUES[flag];

            if(debugElementType == null){
                debugElementType = new DebugElementType<>(
                        HexUtil.toHex("UNKNOWN-0x", flag, 2),
                        flag,
                        SKIP_BLOCK_CREATOR);
                VALUES[flag] = debugElementType;
            }

            return debugElementType;
        }
    }
}
