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
package com.reandroid.dex.instruction;

import com.reandroid.arsc.base.BlockCreator;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.sections.SectionType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Opcode<T extends Instruction> implements BlockCreator<T> {

    public static final Opcode<?>[] VALUES;
    public static final Opcode<?>[] PAYLOADS;
    public static final Opcode<?>[] VALUES_2;
    public static final Opcode<?>[] VALUES_3;
    public static final Map<String, Opcode<?>> NAME_MAP;


    public static final Opcode<Ins10x> NOP;
    public static final Opcode<Ins12x> MOVE;
    public static final Opcode<Ins22x> MOVE_FROM16;
    public static final Opcode<Ins32x> MOVE_16;
    public static final Opcode<Ins12x> MOVE_WIDE;
    public static final Opcode<Ins22x> MOVE_WIDE_FROM16;
    public static final Opcode<Ins32x> MOVE_WIDE_16;
    public static final Opcode<Ins12x> MOVE_OBJECT;
    public static final Opcode<Ins22x> MOVE_OBJECT_FROM16;
    public static final Opcode<Ins32x> MOVE_OBJECT_16;
    public static final Opcode<Ins11x> MOVE_RESULT;
    public static final Opcode<Ins11x> MOVE_RESULT_WIDE;
    public static final Opcode<Ins11x> MOVE_RESULT_OBJECT;
    public static final Opcode<Ins11x> MOVE_EXCEPTION;
    public static final Opcode<Ins10x> RETURN_VOID;
    public static final Opcode<Ins11x> RETURN;
    public static final Opcode<Ins11x> RETURN_WIDE;
    public static final Opcode<Ins11x> RETURN_OBJECT;
    public static final Opcode<Ins11n> CONST_4;
    public static final Opcode<Ins21s> CONST_16;
    public static final Opcode<Ins31i> CONST;
    public static final Opcode<Ins21ih> CONST_HIGH16;
    public static final Opcode<Ins21s> CONST_WIDE_16;
    public static final Opcode<Ins31i> CONST_WIDE_32;
    public static final Opcode<Ins51l> CONST_WIDE;
    public static final Opcode<Ins21lh> CONST_WIDE_HIGH16;
    public static final Opcode<Ins21c> CONST_STRING;
    public static final Opcode<Ins31c> CONST_STRING_JUMBO;
    public static final Opcode<Ins21c> CONST_CLASS;
    public static final Opcode<Ins11x> MONITOR_ENTER;
    public static final Opcode<Ins11x> MONITOR_EXIT;
    public static final Opcode<Ins21c> CHECK_CAST;
    public static final Opcode<Ins22c> INSTANCE_OF;
    public static final Opcode<Ins12x> ARRAY_LENGTH;
    public static final Opcode<Ins21c> NEW_INSTANCE;
    public static final Opcode<Ins22c> NEW_ARRAY;
    public static final Opcode<Ins35c> FILLED_NEW_ARRAY;
    public static final Opcode<Ins3rc> FILLED_NEW_ARRAY_RANGE;
    public static final Opcode<Ins31t> FILL_ARRAY_DATA;
    public static final Opcode<Ins11x> THROW;
    public static final Opcode<Ins10t> GOTO;
    public static final Opcode<Ins20t> GOTO_16;
    public static final Opcode<Ins30t> GOTO_32;
    public static final Opcode<Ins31t> PACKED_SWITCH;
    public static final Opcode<Ins31t> SPARSE_SWITCH;
    public static final Opcode<Ins23x> CMPL_FLOAT;
    public static final Opcode<Ins23x> CMPG_FLOAT;
    public static final Opcode<Ins23x> CMPL_DOUBLE;
    public static final Opcode<Ins23x> CMPG_DOUBLE;
    public static final Opcode<Ins23x> CMP_LONG;
    public static final Opcode<Ins22t> IF_EQ;
    public static final Opcode<Ins22t> IF_NE;
    public static final Opcode<Ins22t> IF_LT;
    public static final Opcode<Ins22t> IF_GE;
    public static final Opcode<Ins22t> IF_GT;
    public static final Opcode<Ins22t> IF_LE;
    public static final Opcode<Ins21t> IF_EQZ;
    public static final Opcode<Ins21t> IF_NEZ;
    public static final Opcode<Ins21t> IF_LTZ;
    public static final Opcode<Ins21t> IF_GEZ;
    public static final Opcode<Ins21t> IF_GTZ;
    public static final Opcode<Ins21t> IF_LEZ;
    public static final Opcode<Ins23x> AGET;
    public static final Opcode<Ins23x> AGET_WIDE;
    public static final Opcode<Ins23x> AGET_OBJECT;
    public static final Opcode<Ins23x> AGET_BOOLEAN;
    public static final Opcode<Ins23x> AGET_BYTE;
    public static final Opcode<Ins23x> AGET_CHAR;
    public static final Opcode<Ins23x> AGET_SHORT;
    public static final Opcode<Ins23x> APUT;
    public static final Opcode<Ins23x> APUT_WIDE;
    public static final Opcode<Ins23x> APUT_OBJECT;
    public static final Opcode<Ins23x> APUT_BOOLEAN;
    public static final Opcode<Ins23x> APUT_BYTE;
    public static final Opcode<Ins23x> APUT_CHAR;
    public static final Opcode<Ins23x> APUT_SHORT;
    public static final Opcode<Ins22c> IGET;
    public static final Opcode<Ins22c> IGET_WIDE;
    public static final Opcode<Ins22c> IGET_OBJECT;
    public static final Opcode<Ins22c> IGET_BOOLEAN;
    public static final Opcode<Ins22c> IGET_BYTE;
    public static final Opcode<Ins22c> IGET_CHAR;
    public static final Opcode<Ins22c> IGET_SHORT;
    public static final Opcode<Ins22c> IPUT;
    public static final Opcode<Ins22c> IPUT_WIDE;
    public static final Opcode<Ins22c> IPUT_OBJECT;
    public static final Opcode<Ins22c> IPUT_BOOLEAN;
    public static final Opcode<Ins22c> IPUT_BYTE;
    public static final Opcode<Ins22c> IPUT_CHAR;
    public static final Opcode<Ins22c> IPUT_SHORT;
    public static final Opcode<Ins21c> SGET;
    public static final Opcode<Ins21c> SGET_WIDE;
    public static final Opcode<Ins21c> SGET_OBJECT;
    public static final Opcode<Ins21c> SGET_BOOLEAN;
    public static final Opcode<Ins21c> SGET_BYTE;
    public static final Opcode<Ins21c> SGET_CHAR;
    public static final Opcode<Ins21c> SGET_SHORT;
    public static final Opcode<Ins21c> SPUT;
    public static final Opcode<Ins21c> SPUT_WIDE;
    public static final Opcode<Ins21c> SPUT_OBJECT;
    public static final Opcode<Ins21c> SPUT_BOOLEAN;
    public static final Opcode<Ins21c> SPUT_BYTE;
    public static final Opcode<Ins21c> SPUT_CHAR;
    public static final Opcode<Ins21c> SPUT_SHORT;
    public static final Opcode<Ins35c> INVOKE_VIRTUAL;
    public static final Opcode<Ins35c> INVOKE_SUPER;
    public static final Opcode<Ins35c> INVOKE_DIRECT;
    public static final Opcode<Ins35c> INVOKE_STATIC;
    public static final Opcode<Ins35c> INVOKE_INTERFACE;
    public static final Opcode<Ins10x> RETURN_VOID_NO_BARRIER;
    public static final Opcode<Ins3rc> INVOKE_VIRTUAL_RANGE;
    public static final Opcode<Ins3rc> INVOKE_SUPER_RANGE;
    public static final Opcode<Ins3rc> INVOKE_DIRECT_RANGE;
    public static final Opcode<Ins3rc> INVOKE_STATIC_RANGE;
    public static final Opcode<Ins3rc> INVOKE_INTERFACE_RANGE;
    public static final Opcode<Ins12x> NEG_INT;
    public static final Opcode<Ins12x> NOT_INT;
    public static final Opcode<Ins12x> NEG_LONG;
    public static final Opcode<Ins12x> NOT_LONG;
    public static final Opcode<Ins12x> NEG_FLOAT;
    public static final Opcode<Ins12x> NEG_DOUBLE;
    public static final Opcode<Ins12x> INT_TO_LONG;
    public static final Opcode<Ins12x> INT_TO_FLOAT;
    public static final Opcode<Ins12x> INT_TO_DOUBLE;
    public static final Opcode<Ins12x> LONG_TO_INT;
    public static final Opcode<Ins12x> LONG_TO_FLOAT;
    public static final Opcode<Ins12x> LONG_TO_DOUBLE;
    public static final Opcode<Ins12x> FLOAT_TO_INT;
    public static final Opcode<Ins12x> FLOAT_TO_LONG;
    public static final Opcode<Ins12x> FLOAT_TO_DOUBLE;
    public static final Opcode<Ins12x> DOUBLE_TO_INT;
    public static final Opcode<Ins12x> DOUBLE_TO_LONG;
    public static final Opcode<Ins12x> DOUBLE_TO_FLOAT;
    public static final Opcode<Ins12x> INT_TO_BYTE;
    public static final Opcode<Ins12x> INT_TO_CHAR;
    public static final Opcode<Ins12x> INT_TO_SHORT;
    public static final Opcode<Ins23x> ADD_INT;
    public static final Opcode<Ins23x> SUB_INT;
    public static final Opcode<Ins23x> MUL_INT;
    public static final Opcode<Ins23x> DIV_INT;
    public static final Opcode<Ins23x> REM_INT;
    public static final Opcode<Ins23x> AND_INT;
    public static final Opcode<Ins23x> OR_INT;
    public static final Opcode<Ins23x> XOR_INT;
    public static final Opcode<Ins23x> SHL_INT;
    public static final Opcode<Ins23x> SHR_INT;
    public static final Opcode<Ins23x> USHR_INT;
    public static final Opcode<Ins23x> ADD_LONG;
    public static final Opcode<Ins23x> SUB_LONG;
    public static final Opcode<Ins23x> MUL_LONG;
    public static final Opcode<Ins23x> DIV_LONG;
    public static final Opcode<Ins23x> REM_LONG;
    public static final Opcode<Ins23x> AND_LONG;
    public static final Opcode<Ins23x> OR_LONG;
    public static final Opcode<Ins23x> XOR_LONG;
    public static final Opcode<Ins23x> SHL_LONG;
    public static final Opcode<Ins23x> SHR_LONG;
    public static final Opcode<Ins23x> USHR_LONG;
    public static final Opcode<Ins23x> ADD_FLOAT;
    public static final Opcode<Ins23x> SUB_FLOAT;
    public static final Opcode<Ins23x> MUL_FLOAT;
    public static final Opcode<Ins23x> DIV_FLOAT;
    public static final Opcode<Ins23x> REM_FLOAT;
    public static final Opcode<Ins23x> ADD_DOUBLE;
    public static final Opcode<Ins23x> SUB_DOUBLE;
    public static final Opcode<Ins23x> MUL_DOUBLE;
    public static final Opcode<Ins23x> DIV_DOUBLE;
    public static final Opcode<Ins23x> REM_DOUBLE;
    public static final Opcode<Ins12x> ADD_INT_2ADDR;
    public static final Opcode<Ins12x> SUB_INT_2ADDR;
    public static final Opcode<Ins12x> MUL_INT_2ADDR;
    public static final Opcode<Ins12x> DIV_INT_2ADDR;
    public static final Opcode<Ins12x> REM_INT_2ADDR;
    public static final Opcode<Ins12x> AND_INT_2ADDR;
    public static final Opcode<Ins12x> OR_INT_2ADDR;
    public static final Opcode<Ins12x> XOR_INT_2ADDR;
    public static final Opcode<Ins12x> SHL_INT_2ADDR;
    public static final Opcode<Ins12x> SHR_INT_2ADDR;
    public static final Opcode<Ins12x> USHR_INT_2ADDR;
    public static final Opcode<Ins12x> ADD_LONG_2ADDR;
    public static final Opcode<Ins12x> SUB_LONG_2ADDR;
    public static final Opcode<Ins12x> MUL_LONG_2ADDR;
    public static final Opcode<Ins12x> DIV_LONG_2ADDR;
    public static final Opcode<Ins12x> REM_LONG_2ADDR;
    public static final Opcode<Ins12x> AND_LONG_2ADDR;
    public static final Opcode<Ins12x> OR_LONG_2ADDR;
    public static final Opcode<Ins12x> XOR_LONG_2ADDR;
    public static final Opcode<Ins12x> SHL_LONG_2ADDR;
    public static final Opcode<Ins12x> SHR_LONG_2ADDR;
    public static final Opcode<Ins12x> USHR_LONG_2ADDR;
    public static final Opcode<Ins12x> ADD_FLOAT_2ADDR;
    public static final Opcode<Ins12x> SUB_FLOAT_2ADDR;
    public static final Opcode<Ins12x> MUL_FLOAT_2ADDR;
    public static final Opcode<Ins12x> DIV_FLOAT_2ADDR;
    public static final Opcode<Ins12x> REM_FLOAT_2ADDR;
    public static final Opcode<Ins12x> ADD_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> SUB_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> MUL_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> DIV_DOUBLE_2ADDR;
    public static final Opcode<Ins12x> REM_DOUBLE_2ADDR;
    public static final Opcode<Ins22s> ADD_INT_LIT16;
    public static final Opcode<Ins22s> RSUB_INT;
    public static final Opcode<Ins22s> MUL_INT_LIT16;
    public static final Opcode<Ins22s> DIV_INT_LIT16;
    public static final Opcode<Ins22s> REM_INT_LIT16;
    public static final Opcode<Ins22s> AND_INT_LIT16;
    public static final Opcode<Ins22s> OR_INT_LIT16;
    public static final Opcode<Ins22s> XOR_INT_LIT16;
    public static final Opcode<Ins22b> ADD_INT_LIT8;
    public static final Opcode<Ins22b> RSUB_INT_LIT8;
    public static final Opcode<Ins22b> MUL_INT_LIT8;
    public static final Opcode<Ins22b> DIV_INT_LIT8;
    public static final Opcode<Ins22b> REM_INT_LIT8;
    public static final Opcode<Ins22b> AND_INT_LIT8;
    public static final Opcode<Ins22b> OR_INT_LIT8;
    public static final Opcode<Ins22b> XOR_INT_LIT8;
    public static final Opcode<Ins22b> SHL_INT_LIT8;
    public static final Opcode<Ins22b> SHR_INT_LIT8;
    public static final Opcode<Ins22b> USHR_INT_LIT8;
    public static final Opcode<Ins22c> IGET_VOLATILE;
    public static final Opcode<Ins22c> IPUT_VOLATILE;
    public static final Opcode<Ins21c> SGET_VOLATILE;
    public static final Opcode<Ins21c> SPUT_VOLATILE;
    public static final Opcode<Ins22c> IGET_OBJECT_VOLATILE;
    public static final Opcode<Ins22c> IGET_WIDE_VOLATILE;
    public static final Opcode<Ins22c> IPUT_WIDE_VOLATILE;
    public static final Opcode<Ins21c> SGET_WIDE_VOLATILE;
    public static final Opcode<Ins21c> SPUT_WIDE_VOLATILE;
    public static final Opcode<Ins22cs> IPUT_BYTE_QUICK;
    public static final Opcode<Ins20bc> THROW_VERIFICATION_ERROR;
    public static final Opcode<Ins35mi> EXECUTE_INLINE;
    public static final Opcode<Ins3rmi> EXECUTE_INLINE_RANGE;
    public static final Opcode<Ins35c> INVOKE_DIRECT_EMPTY;
    public static final Opcode<Ins10x> RETURN_VOID_BARRIER;
    public static final Opcode<Ins22cs> IGET_QUICK;
    public static final Opcode<Ins22cs> IGET_WIDE_QUICK;
    public static final Opcode<Ins22cs> IGET_OBJECT_QUICK;
    public static final Opcode<Ins22cs> IPUT_QUICK;
    public static final Opcode<Ins22cs> IPUT_WIDE_QUICK;
    public static final Opcode<Ins22cs> IPUT_OBJECT_QUICK;
    public static final Opcode<Ins35ms> INVOKE_VIRTUAL_QUICK;
    public static final Opcode<Ins3rms> INVOKE_VIRTUAL_QUICK_RANGE;
    public static final Opcode<Ins35ms> INVOKE_SUPER_QUICK;
    public static final Opcode<Ins3rms> INVOKE_SUPER_QUICK_RANGE;
    public static final Opcode<Ins22c> IPUT_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> SGET_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> SPUT_OBJECT_VOLATILE;
    public static final Opcode<Ins21c> CONST_METHOD_TYPE;

    public static final Opcode<InsPacked> PACKED_SWITCH_PAYLOAD;
    public static final Opcode<InsSparse> SPARSE_SWITCH_PAYLOAD;
    public static final Opcode<InsArray> ARRAY_PAYLOAD;

    public static final Opcode<Ins22cs> IPUT_BOOLEAN_QUICK;
    public static final Opcode<Ins22cs> IPUT_CHAR_QUICK;
    public static final Opcode<Ins22cs> IPUT_SHORT_QUICK;
    public static final Opcode<Ins22cs> IGET_BOOLEAN_QUICK;
    public static final Opcode<Ins3rc> INVOKE_OBJECT_INIT_RANGE;
    public static final Opcode<Ins22cs> IGET_CHAR_QUICK;
    public static final Opcode<Ins22cs> IGET_SHORT_QUICK;
    public static final Opcode<Ins45cc> INVOKE_POLYMORPHIC;
    public static final Opcode<Ins4rcc> INVOKE_POLYMORPHIC_RANGE;
    public static final Opcode<Ins35c> INVOKE_CUSTOM;
    public static final Opcode<Ins3rc> INVOKE_CUSTOM_RANGE;
    public static final Opcode<Ins21c> CONST_METHOD_HANDLE;

    public static final Opcode<Ins22cs> IGET_BYTE_QUICK;

    static {
        VALUES = new Opcode[0xff + 1];
        PAYLOADS = new Opcode[3];
        VALUES_2 = new Opcode[12];
        VALUES_3 = new Opcode[1];
        Map<String, Opcode<?>> map = new HashMap<>();
        NAME_MAP = map;

        NOP = new Opcode<>(0x00, 2, "nop", new BlockCreator<Ins10x>() {
            @Override
            public Ins10x newInstance() {
                return new Ins10x(NOP);
            }
        });
        VALUES[0x00] = NOP;
        MOVE = new Opcode<>(0x01, 2, "move", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MOVE);
            }
        });
        VALUES[0x01] = MOVE;
        MOVE_FROM16 = new Opcode<>(0x02, 4, "move/from16", new BlockCreator<Ins22x>() {
            @Override
            public Ins22x newInstance() {
                return new Ins22x(MOVE_FROM16);
            }
        });
        VALUES[0x02] = MOVE_FROM16;
        MOVE_16 = new Opcode<>(0x03, 6, "move/16", new BlockCreator<Ins32x>() {
            @Override
            public Ins32x newInstance() {
                return new Ins32x(MOVE_16);
            }
        });
        VALUES[0x03] = MOVE_16;
        MOVE_WIDE = new Opcode<>(0x04, 2, "move-wide", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MOVE_WIDE);
            }
        });
        VALUES[0x04] = MOVE_WIDE;
        MOVE_WIDE_FROM16 = new Opcode<>(0x05, 4, "move-wide/from16", new BlockCreator<Ins22x>() {
            @Override
            public Ins22x newInstance() {
                return new Ins22x(MOVE_WIDE_FROM16);
            }
        });
        VALUES[0x05] = MOVE_WIDE_FROM16;
        MOVE_WIDE_16 = new Opcode<>(0x06, 6, "move-wide/16", new BlockCreator<Ins32x>() {
            @Override
            public Ins32x newInstance() {
                return new Ins32x(MOVE_WIDE_16);
            }
        });
        VALUES[0x06] = MOVE_WIDE_16;
        MOVE_OBJECT = new Opcode<>(0x07, 2, "move-object", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MOVE_OBJECT);
            }
        });
        VALUES[0x07] = MOVE_OBJECT;
        MOVE_OBJECT_FROM16 = new Opcode<>(0x08, 4, "move-object/from16", new BlockCreator<Ins22x>() {
            @Override
            public Ins22x newInstance() {
                return new Ins22x(MOVE_OBJECT_FROM16);
            }
        });
        VALUES[0x08] = MOVE_OBJECT_FROM16;
        MOVE_OBJECT_16 = new Opcode<>(0x09, 6, "move-object/16", new BlockCreator<Ins32x>() {
            @Override
            public Ins32x newInstance() {
                return new Ins32x(MOVE_OBJECT_16);
            }
        });
        VALUES[0x09] = MOVE_OBJECT_16;
        MOVE_RESULT = new Opcode<>(0x0a, 2, "move-result", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MOVE_RESULT);
            }
        });
        VALUES[0x0a] = MOVE_RESULT;
        MOVE_RESULT_WIDE = new Opcode<>(0x0b, 2, "move-result-wide", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MOVE_RESULT_WIDE);
            }
        });
        VALUES[0x0b] = MOVE_RESULT_WIDE;
        MOVE_RESULT_OBJECT = new Opcode<>(0x0c, 2, "move-result-object", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MOVE_RESULT_OBJECT);
            }
        });
        VALUES[0x0c] = MOVE_RESULT_OBJECT;
        MOVE_EXCEPTION = new Opcode<>(0x0d, 2, "move-exception", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MOVE_EXCEPTION);
            }
        });
        VALUES[0x0d] = MOVE_EXCEPTION;
        RETURN_VOID = new Opcode<>(0x0e, 2, "return-void", new BlockCreator<Ins10x>() {
            @Override
            public Ins10x newInstance() {
                return new Ins10x(RETURN_VOID);
            }
        });
        VALUES[0x0e] = RETURN_VOID;
        RETURN = new Opcode<>(0x0f, 2, "return", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(RETURN);
            }
        });
        VALUES[0x0f] = RETURN;
        RETURN_WIDE = new Opcode<>(0x10, 2, "return-wide", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(RETURN_WIDE);
            }
        });
        VALUES[0x10] = RETURN_WIDE;
        RETURN_OBJECT = new Opcode<>(0x11, 2, "return-object", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(RETURN_OBJECT);
            }
        });
        VALUES[0x11] = RETURN_OBJECT;
        CONST_4 = new Opcode<>(0x12, 2, "const/4", new BlockCreator<Ins11n>() {
            @Override
            public Ins11n newInstance() {
                return new Ins11n(CONST_4);
            }
        });
        VALUES[0x12] = CONST_4;
        CONST_16 = new Opcode<>(0x13, 4, "const/16", new BlockCreator<Ins21s>() {
            @Override
            public Ins21s newInstance() {
                return new Ins21s(CONST_16);
            }
        });
        VALUES[0x13] = CONST_16;
        CONST = new Opcode<>(0x14, 6, "const", new BlockCreator<Ins31i>() {
            @Override
            public Ins31i newInstance() {
                return new Ins31i(CONST);
            }
        });
        VALUES[0x14] = CONST;
        CONST_HIGH16 = new Opcode<>(0x15, 4, "const/high16", new BlockCreator<Ins21ih>() {
            @Override
            public Ins21ih newInstance() {
                return new Ins21ih(CONST_HIGH16);
            }
        });
        VALUES[0x15] = CONST_HIGH16;
        CONST_WIDE_16 = new Opcode<>(0x16, 4, "const-wide/16", new BlockCreator<Ins21s>() {
            @Override
            public Ins21s newInstance() {
                return new Ins21s(CONST_WIDE_16);
            }
        });
        VALUES[0x16] = CONST_WIDE_16;
        CONST_WIDE_32 = new Opcode<>(0x17, 6, "const-wide/32", new BlockCreator<Ins31i>() {
            @Override
            public Ins31i newInstance() {
                return new Ins31i(CONST_WIDE_32);
            }
        });
        VALUES[0x17] = CONST_WIDE_32;
        CONST_WIDE = new Opcode<>(0x18, 10, "const-wide", new BlockCreator<Ins51l>() {
            @Override
            public Ins51l newInstance() {
                return new Ins51l(CONST_WIDE);
            }
        });
        VALUES[0x18] = CONST_WIDE;
        CONST_WIDE_HIGH16 = new Opcode<>(0x19, 4, "const-wide/high16", new BlockCreator<Ins21lh>() {
            @Override
            public Ins21lh newInstance() {
                return new Ins21lh(CONST_WIDE_HIGH16);
            }
        });
        VALUES[0x19] = CONST_WIDE_HIGH16;
        CONST_STRING = new Opcode<>(0x1a, 4, "const-string", SectionType.STRING_DATA, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(CONST_STRING);
            }
        });
        VALUES[0x1a] = CONST_STRING;
        CONST_STRING_JUMBO = new Opcode<>(0x1b, 6, "const-string/jumbo", new BlockCreator<Ins31c>() {
            @Override
            public Ins31c newInstance() {
                return new Ins31c(CONST_STRING_JUMBO);
            }
        });
        VALUES[0x1b] = CONST_STRING_JUMBO;
        CONST_CLASS = new Opcode<>(0x1c, 4, "const-class", SectionType.TYPE_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(CONST_CLASS);
            }
        });
        VALUES[0x1c] = CONST_CLASS;
        MONITOR_ENTER = new Opcode<>(0x1d, 2, "monitor-enter", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MONITOR_ENTER);
            }
        });
        VALUES[0x1d] = MONITOR_ENTER;
        MONITOR_EXIT = new Opcode<>(0x1e, 2, "monitor-exit", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(MONITOR_EXIT);
            }
        });
        VALUES[0x1e] = MONITOR_EXIT;
        CHECK_CAST = new Opcode<>(0x1f, 4, "check-cast", SectionType.TYPE_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(CHECK_CAST);
            }
        });
        VALUES[0x1f] = CHECK_CAST;
        INSTANCE_OF = new Opcode<>(0x20, 4, "instance-of", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(INSTANCE_OF);
            }
        });
        VALUES[0x20] = INSTANCE_OF;
        ARRAY_LENGTH = new Opcode<>(0x21, 2, "array-length", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(ARRAY_LENGTH);
            }
        });
        VALUES[0x21] = ARRAY_LENGTH;
        NEW_INSTANCE = new Opcode<>(0x22, 4, "new-instance", SectionType.TYPE_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(NEW_INSTANCE);
            }
        });
        VALUES[0x22] = NEW_INSTANCE;
        NEW_ARRAY = new Opcode<>(0x23, 4, "new-array", SectionType.TYPE_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(NEW_ARRAY);
            }
        });
        VALUES[0x23] = NEW_ARRAY;
        FILLED_NEW_ARRAY = new Opcode<>(0x24, 6, "filled-new-array", new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(FILLED_NEW_ARRAY);
            }
        });
        VALUES[0x24] = FILLED_NEW_ARRAY;
        FILLED_NEW_ARRAY_RANGE = new Opcode<>(0x25, 6, "filled-new-array/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(FILLED_NEW_ARRAY_RANGE);
            }
        });
        VALUES[0x25] = FILLED_NEW_ARRAY_RANGE;
        FILL_ARRAY_DATA = new Opcode<>(0x26, 6, "fill-array-data", new BlockCreator<Ins31t>() {
            @Override
            public Ins31t newInstance() {
                return new Ins31t(FILL_ARRAY_DATA);
            }
        });
        VALUES[0x26] = FILL_ARRAY_DATA;
        THROW = new Opcode<>(0x27, 2, "throw", new BlockCreator<Ins11x>() {
            @Override
            public Ins11x newInstance() {
                return new Ins11x(THROW);
            }
        });
        VALUES[0x27] = THROW;
        GOTO = new Opcode<>(0x28, 2, "goto", new BlockCreator<Ins10t>() {
            @Override
            public Ins10t newInstance() {
                return new Ins10t(GOTO);
            }
        });
        VALUES[0x28] = GOTO;
        GOTO_16 = new Opcode<>(0x29, 4, "goto/16", new BlockCreator<Ins20t>() {
            @Override
            public Ins20t newInstance() {
                return new Ins20t(GOTO_16);
            }
        });
        VALUES[0x29] = GOTO_16;
        GOTO_32 = new Opcode<>(0x2a, 6, "goto/32", new BlockCreator<Ins30t>() {
            @Override
            public Ins30t newInstance() {
                return new Ins30t(GOTO_32);
            }
        });
        VALUES[0x2a] = GOTO_32;
        PACKED_SWITCH = new Opcode<>(0x2b, 6, "packed-switch", new BlockCreator<Ins31t>() {
            @Override
            public Ins31t newInstance() {
                return new Ins31t(PACKED_SWITCH);
            }
        });
        VALUES[0x2b] = PACKED_SWITCH;
        SPARSE_SWITCH = new Opcode<>(0x2c, 6, "sparse-switch", new BlockCreator<Ins31t>() {
            @Override
            public Ins31t newInstance() {
                return new Ins31t(SPARSE_SWITCH);
            }
        });
        VALUES[0x2c] = SPARSE_SWITCH;
        CMPL_FLOAT = new Opcode<>(0x2d, 4, "cmpl-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(CMPL_FLOAT);
            }
        });
        VALUES[0x2d] = CMPL_FLOAT;
        CMPG_FLOAT = new Opcode<>(0x2e, 4, "cmpg-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(CMPG_FLOAT);
            }
        });
        VALUES[0x2e] = CMPG_FLOAT;
        CMPL_DOUBLE = new Opcode<>(0x2f, 4, "cmpl-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(CMPL_DOUBLE);
            }
        });
        VALUES[0x2f] = CMPL_DOUBLE;
        CMPG_DOUBLE = new Opcode<>(0x30, 4, "cmpg-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(CMPG_DOUBLE);
            }
        });
        VALUES[0x30] = CMPG_DOUBLE;
        CMP_LONG = new Opcode<>(0x31, 4, "cmp-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(CMP_LONG);
            }
        });
        VALUES[0x31] = CMP_LONG;
        IF_EQ = new Opcode<>(0x32, 4, "if-eq", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_EQ);
            }
        });
        VALUES[0x32] = IF_EQ;
        IF_NE = new Opcode<>(0x33, 4, "if-ne", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_NE);
            }
        });
        VALUES[0x33] = IF_NE;
        IF_LT = new Opcode<>(0x34, 4, "if-lt", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_LT);
            }
        });
        VALUES[0x34] = IF_LT;
        IF_GE = new Opcode<>(0x35, 4, "if-ge", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_GE);
            }
        });
        VALUES[0x35] = IF_GE;
        IF_GT = new Opcode<>(0x36, 4, "if-gt", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_GT);
            }
        });
        VALUES[0x36] = IF_GT;
        IF_LE = new Opcode<>(0x37, 4, "if-le", new BlockCreator<Ins22t>() {
            @Override
            public Ins22t newInstance() {
                return new Ins22t(IF_LE);
            }
        });
        VALUES[0x37] = IF_LE;
        IF_EQZ = new Opcode<>(0x38, 4, "if-eqz", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_EQZ);
            }
        });
        VALUES[0x38] = IF_EQZ;
        IF_NEZ = new Opcode<>(0x39, 4, "if-nez", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_NEZ);
            }
        });
        VALUES[0x39] = IF_NEZ;
        IF_LTZ = new Opcode<>(0x3a, 4, "if-ltz", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_LTZ);
            }
        });
        VALUES[0x3a] = IF_LTZ;
        IF_GEZ = new Opcode<>(0x3b, 4, "if-gez", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_GEZ);
            }
        });
        VALUES[0x3b] = IF_GEZ;
        IF_GTZ = new Opcode<>(0x3c, 4, "if-gtz", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_GTZ);
            }
        });
        VALUES[0x3c] = IF_GTZ;
        IF_LEZ = new Opcode<>(0x3d, 4, "if-lez", new BlockCreator<Ins21t>() {
            @Override
            public Ins21t newInstance() {
                return new Ins21t(IF_LEZ);
            }
        });
        VALUES[0x3d] = IF_LEZ;
        AGET = new Opcode<>(0x44, 4, "aget", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET);
            }
        });
        VALUES[0x44] = AGET;
        AGET_WIDE = new Opcode<>(0x45, 4, "aget-wide", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_WIDE);
            }
        });
        VALUES[0x45] = AGET_WIDE;
        AGET_OBJECT = new Opcode<>(0x46, 4, "aget-object", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_OBJECT);
            }
        });
        VALUES[0x46] = AGET_OBJECT;
        AGET_BOOLEAN = new Opcode<>(0x47, 4, "aget-boolean", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_BOOLEAN);
            }
        });
        VALUES[0x47] = AGET_BOOLEAN;
        AGET_BYTE = new Opcode<>(0x48, 4, "aget-byte", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_BYTE);
            }
        });
        VALUES[0x48] = AGET_BYTE;
        AGET_CHAR = new Opcode<>(0x49, 4, "aget-char", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_CHAR);
            }
        });
        VALUES[0x49] = AGET_CHAR;
        AGET_SHORT = new Opcode<>(0x4a, 4, "aget-short", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AGET_SHORT);
            }
        });
        VALUES[0x4a] = AGET_SHORT;
        APUT = new Opcode<>(0x4b, 4, "aput", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT);
            }
        });
        VALUES[0x4b] = APUT;
        APUT_WIDE = new Opcode<>(0x4c, 4, "aput-wide", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_WIDE);
            }
        });
        VALUES[0x4c] = APUT_WIDE;
        APUT_OBJECT = new Opcode<>(0x4d, 4, "aput-object", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_OBJECT);
            }
        });
        VALUES[0x4d] = APUT_OBJECT;
        APUT_BOOLEAN = new Opcode<>(0x4e, 4, "aput-boolean", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_BOOLEAN);
            }
        });
        VALUES[0x4e] = APUT_BOOLEAN;
        APUT_BYTE = new Opcode<>(0x4f, 4, "aput-byte", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_BYTE);
            }
        });
        VALUES[0x4f] = APUT_BYTE;
        APUT_CHAR = new Opcode<>(0x50, 4, "aput-char", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_CHAR);
            }
        });
        VALUES[0x50] = APUT_CHAR;
        APUT_SHORT = new Opcode<>(0x51, 4, "aput-short", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(APUT_SHORT);
            }
        });
        VALUES[0x51] = APUT_SHORT;
        IGET = new Opcode<>(0x52, 4, "iget", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET);
            }
        });
        VALUES[0x52] = IGET;
        IGET_WIDE = new Opcode<>(0x53, 4, "iget-wide", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_WIDE);
            }
        });
        VALUES[0x53] = IGET_WIDE;
        IGET_OBJECT = new Opcode<>(0x54, 4, "iget-object", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_OBJECT);
            }
        });
        VALUES[0x54] = IGET_OBJECT;
        IGET_BOOLEAN = new Opcode<>(0x55, 4, "iget-boolean", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_BOOLEAN);
            }
        });
        VALUES[0x55] = IGET_BOOLEAN;
        IGET_BYTE = new Opcode<>(0x56, 4, "iget-byte", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_BYTE);
            }
        });
        VALUES[0x56] = IGET_BYTE;
        IGET_CHAR = new Opcode<>(0x57, 4, "iget-char", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_CHAR);
            }
        });
        VALUES[0x57] = IGET_CHAR;
        IGET_SHORT = new Opcode<>(0x58, 4, "iget-short", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_SHORT);
            }
        });
        VALUES[0x58] = IGET_SHORT;
        IPUT = new Opcode<>(0x59, 4, "iput", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT);
            }
        });
        VALUES[0x59] = IPUT;
        IPUT_WIDE = new Opcode<>(0x5a, 4, "iput-wide", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_WIDE);
            }
        });
        VALUES[0x5a] = IPUT_WIDE;
        IPUT_OBJECT = new Opcode<>(0x5b, 4, "iput-object", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_OBJECT);
            }
        });
        VALUES[0x5b] = IPUT_OBJECT;
        IPUT_BOOLEAN = new Opcode<>(0x5c, 4, "iput-boolean", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_BOOLEAN);
            }
        });
        VALUES[0x5c] = IPUT_BOOLEAN;
        IPUT_BYTE = new Opcode<>(0x5d, 4, "iput-byte", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_BYTE);
            }
        });
        VALUES[0x5d] = IPUT_BYTE;
        IPUT_CHAR = new Opcode<>(0x5e, 4, "iput-char", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_CHAR);
            }
        });
        VALUES[0x5e] = IPUT_CHAR;
        IPUT_SHORT = new Opcode<>(0x5f, 4, "iput-short", SectionType.FIELD_ID, new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_SHORT);
            }
        });
        VALUES[0x5f] = IPUT_SHORT;
        SGET = new Opcode<>(0x60, 4, "sget", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET);
            }
        });
        VALUES[0x60] = SGET;
        SGET_WIDE = new Opcode<>(0x61, 4, "sget-wide", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_WIDE);
            }
        });
        VALUES[0x61] = SGET_WIDE;
        SGET_OBJECT = new Opcode<>(0x62, 4, "sget-object", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_OBJECT);
            }
        });
        VALUES[0x62] = SGET_OBJECT;
        SGET_BOOLEAN = new Opcode<>(0x63, 4, "sget-boolean", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_BOOLEAN);
            }
        });
        VALUES[0x63] = SGET_BOOLEAN;
        SGET_BYTE = new Opcode<>(0x64, 4, "sget-byte", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_BYTE);
            }
        });
        VALUES[0x64] = SGET_BYTE;
        SGET_CHAR = new Opcode<>(0x65, 4, "sget-char", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_CHAR);
            }
        });
        VALUES[0x65] = SGET_CHAR;
        SGET_SHORT = new Opcode<>(0x66, 4, "sget-short", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_SHORT);
            }
        });
        VALUES[0x66] = SGET_SHORT;
        SPUT = new Opcode<>(0x67, 4, "sput", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT);
            }
        });
        VALUES[0x67] = SPUT;
        SPUT_WIDE = new Opcode<>(0x68, 4, "sput-wide", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_WIDE);
            }
        });
        VALUES[0x68] = SPUT_WIDE;
        SPUT_OBJECT = new Opcode<>(0x69, 4, "sput-object", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_OBJECT);
            }
        });
        VALUES[0x69] = SPUT_OBJECT;
        SPUT_BOOLEAN = new Opcode<>(0x6a, 4, "sput-boolean", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_BOOLEAN);
            }
        });
        VALUES[0x6a] = SPUT_BOOLEAN;
        SPUT_BYTE = new Opcode<>(0x6b, 4, "sput-byte", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_BYTE);
            }
        });
        VALUES[0x6b] = SPUT_BYTE;
        SPUT_CHAR = new Opcode<>(0x6c, 4, "sput-char", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_CHAR);
            }
        });
        VALUES[0x6c] = SPUT_CHAR;
        SPUT_SHORT = new Opcode<>(0x6d, 4, "sput-short", SectionType.FIELD_ID, new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_SHORT);
            }
        });
        VALUES[0x6d] = SPUT_SHORT;
        INVOKE_VIRTUAL = new Opcode<>(0x6e, 6, "invoke-virtual", SectionType.METHOD_ID, new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_VIRTUAL);
            }
        });
        VALUES[0x6e] = INVOKE_VIRTUAL;
        INVOKE_SUPER = new Opcode<>(0x6f, 6, "invoke-super", SectionType.METHOD_ID, new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_SUPER);
            }
        });
        VALUES[0x6f] = INVOKE_SUPER;
        INVOKE_DIRECT = new Opcode<>(0x70, 6, "invoke-direct", SectionType.METHOD_ID, new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_DIRECT);
            }
        });
        VALUES[0x70] = INVOKE_DIRECT;
        INVOKE_STATIC = new Opcode<>(0x71, 6, "invoke-static", SectionType.METHOD_ID, new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_STATIC);
            }
        });
        VALUES[0x71] = INVOKE_STATIC;
        INVOKE_INTERFACE = new Opcode<>(0x72, 6, "invoke-interface", SectionType.METHOD_ID, new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_INTERFACE);
            }
        });
        VALUES[0x72] = INVOKE_INTERFACE;
        RETURN_VOID_NO_BARRIER = new Opcode<>(0x73, 2, "return-void-no-barrier", new BlockCreator<Ins10x>() {
            @Override
            public Ins10x newInstance() {
                return new Ins10x(RETURN_VOID_NO_BARRIER);
            }
        });
        VALUES[0x73] = RETURN_VOID_NO_BARRIER;
        INVOKE_VIRTUAL_RANGE = new Opcode<>(0x74, 6, "invoke-virtual/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_VIRTUAL_RANGE);
            }
        });
        VALUES[0x74] = INVOKE_VIRTUAL_RANGE;
        INVOKE_SUPER_RANGE = new Opcode<>(0x75, 6, "invoke-super/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_SUPER_RANGE);
            }
        });
        VALUES[0x75] = INVOKE_SUPER_RANGE;
        INVOKE_DIRECT_RANGE = new Opcode<>(0x76, 6, "invoke-direct/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_DIRECT_RANGE);
            }
        });
        VALUES[0x76] = INVOKE_DIRECT_RANGE;
        INVOKE_STATIC_RANGE = new Opcode<>(0x77, 6, "invoke-static/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_STATIC_RANGE);
            }
        });
        VALUES[0x77] = INVOKE_STATIC_RANGE;
        INVOKE_INTERFACE_RANGE = new Opcode<>(0x78, 6, "invoke-interface/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_INTERFACE_RANGE);
            }
        });
        VALUES[0x78] = INVOKE_INTERFACE_RANGE;
        NEG_INT = new Opcode<>(0x7b, 2, "neg-int", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NEG_INT);
            }
        });
        VALUES[0x7b] = NEG_INT;
        NOT_INT = new Opcode<>(0x7c, 2, "not-int", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NOT_INT);
            }
        });
        VALUES[0x7c] = NOT_INT;
        NEG_LONG = new Opcode<>(0x7d, 2, "neg-long", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NEG_LONG);
            }
        });
        VALUES[0x7d] = NEG_LONG;
        NOT_LONG = new Opcode<>(0x7e, 2, "not-long", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NOT_LONG);
            }
        });
        VALUES[0x7e] = NOT_LONG;
        NEG_FLOAT = new Opcode<>(0x7f, 2, "neg-float", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NEG_FLOAT);
            }
        });
        VALUES[0x7f] = NEG_FLOAT;
        NEG_DOUBLE = new Opcode<>(0x80, 2, "neg-double", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(NEG_DOUBLE);
            }
        });
        VALUES[0x80] = NEG_DOUBLE;
        INT_TO_LONG = new Opcode<>(0x81, 2, "int-to-long", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_LONG);
            }
        });
        VALUES[0x81] = INT_TO_LONG;
        INT_TO_FLOAT = new Opcode<>(0x82, 2, "int-to-float", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_FLOAT);
            }
        });
        VALUES[0x82] = INT_TO_FLOAT;
        INT_TO_DOUBLE = new Opcode<>(0x83, 2, "int-to-double", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_DOUBLE);
            }
        });
        VALUES[0x83] = INT_TO_DOUBLE;
        LONG_TO_INT = new Opcode<>(0x84, 2, "long-to-int", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(LONG_TO_INT);
            }
        });
        VALUES[0x84] = LONG_TO_INT;
        LONG_TO_FLOAT = new Opcode<>(0x85, 2, "long-to-float", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(LONG_TO_FLOAT);
            }
        });
        VALUES[0x85] = LONG_TO_FLOAT;
        LONG_TO_DOUBLE = new Opcode<>(0x86, 2, "long-to-double", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(LONG_TO_DOUBLE);
            }
        });
        VALUES[0x86] = LONG_TO_DOUBLE;
        FLOAT_TO_INT = new Opcode<>(0x87, 2, "float-to-int", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(FLOAT_TO_INT);
            }
        });
        VALUES[0x87] = FLOAT_TO_INT;
        FLOAT_TO_LONG = new Opcode<>(0x88, 2, "float-to-long", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(FLOAT_TO_LONG);
            }
        });
        VALUES[0x88] = FLOAT_TO_LONG;
        FLOAT_TO_DOUBLE = new Opcode<>(0x89, 2, "float-to-double", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(FLOAT_TO_DOUBLE);
            }
        });
        VALUES[0x89] = FLOAT_TO_DOUBLE;
        DOUBLE_TO_INT = new Opcode<>(0x8a, 2, "double-to-int", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DOUBLE_TO_INT);
            }
        });
        VALUES[0x8a] = DOUBLE_TO_INT;
        DOUBLE_TO_LONG = new Opcode<>(0x8b, 2, "double-to-long", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DOUBLE_TO_LONG);
            }
        });
        VALUES[0x8b] = DOUBLE_TO_LONG;
        DOUBLE_TO_FLOAT = new Opcode<>(0x8c, 2, "double-to-float", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DOUBLE_TO_FLOAT);
            }
        });
        VALUES[0x8c] = DOUBLE_TO_FLOAT;
        INT_TO_BYTE = new Opcode<>(0x8d, 2, "int-to-byte", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_BYTE);
            }
        });
        VALUES[0x8d] = INT_TO_BYTE;
        INT_TO_CHAR = new Opcode<>(0x8e, 2, "int-to-char", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_CHAR);
            }
        });
        VALUES[0x8e] = INT_TO_CHAR;
        INT_TO_SHORT = new Opcode<>(0x8f, 2, "int-to-short", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(INT_TO_SHORT);
            }
        });
        VALUES[0x8f] = INT_TO_SHORT;
        ADD_INT = new Opcode<>(0x90, 4, "add-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(ADD_INT);
            }
        });
        VALUES[0x90] = ADD_INT;
        SUB_INT = new Opcode<>(0x91, 4, "sub-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SUB_INT);
            }
        });
        VALUES[0x91] = SUB_INT;
        MUL_INT = new Opcode<>(0x92, 4, "mul-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(MUL_INT);
            }
        });
        VALUES[0x92] = MUL_INT;
        DIV_INT = new Opcode<>(0x93, 4, "div-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(DIV_INT);
            }
        });
        VALUES[0x93] = DIV_INT;
        REM_INT = new Opcode<>(0x94, 4, "rem-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(REM_INT);
            }
        });
        VALUES[0x94] = REM_INT;
        AND_INT = new Opcode<>(0x95, 4, "and-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AND_INT);
            }
        });
        VALUES[0x95] = AND_INT;
        OR_INT = new Opcode<>(0x96, 4, "or-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(OR_INT);
            }
        });
        VALUES[0x96] = OR_INT;
        XOR_INT = new Opcode<>(0x97, 4, "xor-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(XOR_INT);
            }
        });
        VALUES[0x97] = XOR_INT;
        SHL_INT = new Opcode<>(0x98, 4, "shl-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SHL_INT);
            }
        });
        VALUES[0x98] = SHL_INT;
        SHR_INT = new Opcode<>(0x99, 4, "shr-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SHR_INT);
            }
        });
        VALUES[0x99] = SHR_INT;
        USHR_INT = new Opcode<>(0x9a, 4, "ushr-int", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(USHR_INT);
            }
        });
        VALUES[0x9a] = USHR_INT;
        ADD_LONG = new Opcode<>(0x9b, 4, "add-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(ADD_LONG);
            }
        });
        VALUES[0x9b] = ADD_LONG;
        SUB_LONG = new Opcode<>(0x9c, 4, "sub-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SUB_LONG);
            }
        });
        VALUES[0x9c] = SUB_LONG;
        MUL_LONG = new Opcode<>(0x9d, 4, "mul-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(MUL_LONG);
            }
        });
        VALUES[0x9d] = MUL_LONG;
        DIV_LONG = new Opcode<>(0x9e, 4, "div-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(DIV_LONG);
            }
        });
        VALUES[0x9e] = DIV_LONG;
        REM_LONG = new Opcode<>(0x9f, 4, "rem-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(REM_LONG);
            }
        });
        VALUES[0x9f] = REM_LONG;
        AND_LONG = new Opcode<>(0xa0, 4, "and-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(AND_LONG);
            }
        });
        VALUES[0xa0] = AND_LONG;
        OR_LONG = new Opcode<>(0xa1, 4, "or-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(OR_LONG);
            }
        });
        VALUES[0xa1] = OR_LONG;
        XOR_LONG = new Opcode<>(0xa2, 4, "xor-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(XOR_LONG);
            }
        });
        VALUES[0xa2] = XOR_LONG;
        SHL_LONG = new Opcode<>(0xa3, 4, "shl-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SHL_LONG);
            }
        });
        VALUES[0xa3] = SHL_LONG;
        SHR_LONG = new Opcode<>(0xa4, 4, "shr-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SHR_LONG);
            }
        });
        VALUES[0xa4] = SHR_LONG;
        USHR_LONG = new Opcode<>(0xa5, 4, "ushr-long", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(USHR_LONG);
            }
        });
        VALUES[0xa5] = USHR_LONG;
        ADD_FLOAT = new Opcode<>(0xa6, 4, "add-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(ADD_FLOAT);
            }
        });
        VALUES[0xa6] = ADD_FLOAT;
        SUB_FLOAT = new Opcode<>(0xa7, 4, "sub-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SUB_FLOAT);
            }
        });
        VALUES[0xa7] = SUB_FLOAT;
        MUL_FLOAT = new Opcode<>(0xa8, 4, "mul-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(MUL_FLOAT);
            }
        });
        VALUES[0xa8] = MUL_FLOAT;
        DIV_FLOAT = new Opcode<>(0xa9, 4, "div-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(DIV_FLOAT);
            }
        });
        VALUES[0xa9] = DIV_FLOAT;
        REM_FLOAT = new Opcode<>(0xaa, 4, "rem-float", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(REM_FLOAT);
            }
        });
        VALUES[0xaa] = REM_FLOAT;
        ADD_DOUBLE = new Opcode<>(0xab, 4, "add-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(ADD_DOUBLE);
            }
        });
        VALUES[0xab] = ADD_DOUBLE;
        SUB_DOUBLE = new Opcode<>(0xac, 4, "sub-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(SUB_DOUBLE);
            }
        });
        VALUES[0xac] = SUB_DOUBLE;
        MUL_DOUBLE = new Opcode<>(0xad, 4, "mul-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(MUL_DOUBLE);
            }
        });
        VALUES[0xad] = MUL_DOUBLE;
        DIV_DOUBLE = new Opcode<>(0xae, 4, "div-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(DIV_DOUBLE);
            }
        });
        VALUES[0xae] = DIV_DOUBLE;
        REM_DOUBLE = new Opcode<>(0xaf, 4, "rem-double", new BlockCreator<Ins23x>() {
            @Override
            public Ins23x newInstance() {
                return new Ins23x(REM_DOUBLE);
            }
        });
        VALUES[0xaf] = REM_DOUBLE;
        ADD_INT_2ADDR = new Opcode<>(0xb0, 2, "add-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(ADD_INT_2ADDR);
            }
        });
        VALUES[0xb0] = ADD_INT_2ADDR;
        SUB_INT_2ADDR = new Opcode<>(0xb1, 2, "sub-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SUB_INT_2ADDR);
            }
        });
        VALUES[0xb1] = SUB_INT_2ADDR;
        MUL_INT_2ADDR = new Opcode<>(0xb2, 2, "mul-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MUL_INT_2ADDR);
            }
        });
        VALUES[0xb2] = MUL_INT_2ADDR;
        DIV_INT_2ADDR = new Opcode<>(0xb3, 2, "div-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DIV_INT_2ADDR);
            }
        });
        VALUES[0xb3] = DIV_INT_2ADDR;
        REM_INT_2ADDR = new Opcode<>(0xb4, 2, "rem-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(REM_INT_2ADDR);
            }
        });
        VALUES[0xb4] = REM_INT_2ADDR;
        AND_INT_2ADDR = new Opcode<>(0xb5, 2, "and-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(AND_INT_2ADDR);
            }
        });
        VALUES[0xb5] = AND_INT_2ADDR;
        OR_INT_2ADDR = new Opcode<>(0xb6, 2, "or-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(OR_INT_2ADDR);
            }
        });
        VALUES[0xb6] = OR_INT_2ADDR;
        XOR_INT_2ADDR = new Opcode<>(0xb7, 2, "xor-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(XOR_INT_2ADDR);
            }
        });
        VALUES[0xb7] = XOR_INT_2ADDR;
        SHL_INT_2ADDR = new Opcode<>(0xb8, 2, "shl-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SHL_INT_2ADDR);
            }
        });
        VALUES[0xb8] = SHL_INT_2ADDR;
        SHR_INT_2ADDR = new Opcode<>(0xb9, 2, "shr-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SHR_INT_2ADDR);
            }
        });
        VALUES[0xb9] = SHR_INT_2ADDR;
        USHR_INT_2ADDR = new Opcode<>(0xba, 2, "ushr-int/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(USHR_INT_2ADDR);
            }
        });
        VALUES[0xba] = USHR_INT_2ADDR;
        ADD_LONG_2ADDR = new Opcode<>(0xbb, 2, "add-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(ADD_LONG_2ADDR);
            }
        });
        VALUES[0xbb] = ADD_LONG_2ADDR;
        SUB_LONG_2ADDR = new Opcode<>(0xbc, 2, "sub-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SUB_LONG_2ADDR);
            }
        });
        VALUES[0xbc] = SUB_LONG_2ADDR;
        MUL_LONG_2ADDR = new Opcode<>(0xbd, 2, "mul-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MUL_LONG_2ADDR);
            }
        });
        VALUES[0xbd] = MUL_LONG_2ADDR;
        DIV_LONG_2ADDR = new Opcode<>(0xbe, 2, "div-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DIV_LONG_2ADDR);
            }
        });
        VALUES[0xbe] = DIV_LONG_2ADDR;
        REM_LONG_2ADDR = new Opcode<>(0xbf, 2, "rem-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(REM_LONG_2ADDR);
            }
        });
        VALUES[0xbf] = REM_LONG_2ADDR;
        AND_LONG_2ADDR = new Opcode<>(0xc0, 2, "and-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(AND_LONG_2ADDR);
            }
        });
        VALUES[0xc0] = AND_LONG_2ADDR;
        OR_LONG_2ADDR = new Opcode<>(0xc1, 2, "or-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(OR_LONG_2ADDR);
            }
        });
        VALUES[0xc1] = OR_LONG_2ADDR;
        XOR_LONG_2ADDR = new Opcode<>(0xc2, 2, "xor-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(XOR_LONG_2ADDR);
            }
        });
        VALUES[0xc2] = XOR_LONG_2ADDR;
        SHL_LONG_2ADDR = new Opcode<>(0xc3, 2, "shl-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SHL_LONG_2ADDR);
            }
        });
        VALUES[0xc3] = SHL_LONG_2ADDR;
        SHR_LONG_2ADDR = new Opcode<>(0xc4, 2, "shr-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SHR_LONG_2ADDR);
            }
        });
        VALUES[0xc4] = SHR_LONG_2ADDR;
        USHR_LONG_2ADDR = new Opcode<>(0xc5, 2, "ushr-long/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(USHR_LONG_2ADDR);
            }
        });
        VALUES[0xc5] = USHR_LONG_2ADDR;
        ADD_FLOAT_2ADDR = new Opcode<>(0xc6, 2, "add-float/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(ADD_FLOAT_2ADDR);
            }
        });
        VALUES[0xc6] = ADD_FLOAT_2ADDR;
        SUB_FLOAT_2ADDR = new Opcode<>(0xc7, 2, "sub-float/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SUB_FLOAT_2ADDR);
            }
        });
        VALUES[0xc7] = SUB_FLOAT_2ADDR;
        MUL_FLOAT_2ADDR = new Opcode<>(0xc8, 2, "mul-float/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MUL_FLOAT_2ADDR);
            }
        });
        VALUES[0xc8] = MUL_FLOAT_2ADDR;
        DIV_FLOAT_2ADDR = new Opcode<>(0xc9, 2, "div-float/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DIV_FLOAT_2ADDR);
            }
        });
        VALUES[0xc9] = DIV_FLOAT_2ADDR;
        REM_FLOAT_2ADDR = new Opcode<>(0xca, 2, "rem-float/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(REM_FLOAT_2ADDR);
            }
        });
        VALUES[0xca] = REM_FLOAT_2ADDR;
        ADD_DOUBLE_2ADDR = new Opcode<>(0xcb, 2, "add-double/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(ADD_DOUBLE_2ADDR);
            }
        });
        VALUES[0xcb] = ADD_DOUBLE_2ADDR;
        SUB_DOUBLE_2ADDR = new Opcode<>(0xcc, 2, "sub-double/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(SUB_DOUBLE_2ADDR);
            }
        });
        VALUES[0xcc] = SUB_DOUBLE_2ADDR;
        MUL_DOUBLE_2ADDR = new Opcode<>(0xcd, 2, "mul-double/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(MUL_DOUBLE_2ADDR);
            }
        });
        VALUES[0xcd] = MUL_DOUBLE_2ADDR;
        DIV_DOUBLE_2ADDR = new Opcode<>(0xce, 2, "div-double/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(DIV_DOUBLE_2ADDR);
            }
        });
        VALUES[0xce] = DIV_DOUBLE_2ADDR;
        REM_DOUBLE_2ADDR = new Opcode<>(0xcf, 2, "rem-double/2addr", new BlockCreator<Ins12x>() {
            @Override
            public Ins12x newInstance() {
                return new Ins12x(REM_DOUBLE_2ADDR);
            }
        });
        VALUES[0xcf] = REM_DOUBLE_2ADDR;
        ADD_INT_LIT16 = new Opcode<>(0xd0, 4, "add-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(ADD_INT_LIT16);
            }
        });
        VALUES[0xd0] = ADD_INT_LIT16;
        RSUB_INT = new Opcode<>(0xd1, 4, "rsub-int", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(RSUB_INT);
            }
        });
        VALUES[0xd1] = RSUB_INT;
        MUL_INT_LIT16 = new Opcode<>(0xd2, 4, "mul-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(MUL_INT_LIT16);
            }
        });
        VALUES[0xd2] = MUL_INT_LIT16;
        DIV_INT_LIT16 = new Opcode<>(0xd3, 4, "div-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(DIV_INT_LIT16);
            }
        });
        VALUES[0xd3] = DIV_INT_LIT16;
        REM_INT_LIT16 = new Opcode<>(0xd4, 4, "rem-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(REM_INT_LIT16);
            }
        });
        VALUES[0xd4] = REM_INT_LIT16;
        AND_INT_LIT16 = new Opcode<>(0xd5, 4, "and-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(AND_INT_LIT16);
            }
        });
        VALUES[0xd5] = AND_INT_LIT16;
        OR_INT_LIT16 = new Opcode<>(0xd6, 4, "or-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(OR_INT_LIT16);
            }
        });
        VALUES[0xd6] = OR_INT_LIT16;
        XOR_INT_LIT16 = new Opcode<>(0xd7, 4, "xor-int/lit16", new BlockCreator<Ins22s>() {
            @Override
            public Ins22s newInstance() {
                return new Ins22s(XOR_INT_LIT16);
            }
        });
        VALUES[0xd7] = XOR_INT_LIT16;
        ADD_INT_LIT8 = new Opcode<>(0xd8, 4, "add-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(ADD_INT_LIT8);
            }
        });
        VALUES[0xd8] = ADD_INT_LIT8;
        RSUB_INT_LIT8 = new Opcode<>(0xd9, 4, "rsub-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(RSUB_INT_LIT8);
            }
        });
        VALUES[0xd9] = RSUB_INT_LIT8;
        MUL_INT_LIT8 = new Opcode<>(0xda, 4, "mul-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(MUL_INT_LIT8);
            }
        });
        VALUES[0xda] = MUL_INT_LIT8;
        DIV_INT_LIT8 = new Opcode<>(0xdb, 4, "div-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(DIV_INT_LIT8);
            }
        });
        VALUES[0xdb] = DIV_INT_LIT8;
        REM_INT_LIT8 = new Opcode<>(0xdc, 4, "rem-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(REM_INT_LIT8);
            }
        });
        VALUES[0xdc] = REM_INT_LIT8;
        AND_INT_LIT8 = new Opcode<>(0xdd, 4, "and-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(AND_INT_LIT8);
            }
        });
        VALUES[0xdd] = AND_INT_LIT8;
        OR_INT_LIT8 = new Opcode<>(0xde, 4, "or-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(OR_INT_LIT8);
            }
        });
        VALUES[0xde] = OR_INT_LIT8;
        XOR_INT_LIT8 = new Opcode<>(0xdf, 4, "xor-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(XOR_INT_LIT8);
            }
        });
        VALUES[0xdf] = XOR_INT_LIT8;
        SHL_INT_LIT8 = new Opcode<>(0xe0, 4, "shl-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(SHL_INT_LIT8);
            }
        });
        VALUES[0xe0] = SHL_INT_LIT8;
        SHR_INT_LIT8 = new Opcode<>(0xe1, 4, "shr-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(SHR_INT_LIT8);
            }
        });
        VALUES[0xe1] = SHR_INT_LIT8;
        USHR_INT_LIT8 = new Opcode<>(0xe2, 4, "ushr-int/lit8", new BlockCreator<Ins22b>() {
            @Override
            public Ins22b newInstance() {
                return new Ins22b(USHR_INT_LIT8);
            }
        });
        VALUES[0xe2] = USHR_INT_LIT8;
        IGET_VOLATILE = new Opcode<>(0xe3, 4, "iget-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_VOLATILE);
            }
        });
        VALUES[0xe3] = IGET_VOLATILE;
        IPUT_VOLATILE = new Opcode<>(0xe4, 4, "iput-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_VOLATILE);
            }
        });
        VALUES[0xe4] = IPUT_VOLATILE;
        SGET_VOLATILE = new Opcode<>(0xe5, 4, "sget-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_VOLATILE);
            }
        });
        VALUES[0xe5] = SGET_VOLATILE;
        SPUT_VOLATILE = new Opcode<>(0xe6, 4, "sput-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_VOLATILE);
            }
        });
        VALUES[0xe6] = SPUT_VOLATILE;
        IGET_OBJECT_VOLATILE = new Opcode<>(0xe7, 4, "iget-object-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_OBJECT_VOLATILE);
            }
        });
        VALUES[0xe7] = IGET_OBJECT_VOLATILE;
        IGET_WIDE_VOLATILE = new Opcode<>(0xe8, 4, "iget-wide-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IGET_WIDE_VOLATILE);
            }
        });
        VALUES[0xe8] = IGET_WIDE_VOLATILE;
        IPUT_WIDE_VOLATILE = new Opcode<>(0xe9, 4, "iput-wide-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_WIDE_VOLATILE);
            }
        });
        VALUES[0xe9] = IPUT_WIDE_VOLATILE;
        SGET_WIDE_VOLATILE = new Opcode<>(0xea, 4, "sget-wide-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_WIDE_VOLATILE);
            }
        });
        VALUES[0xea] = SGET_WIDE_VOLATILE;
        SPUT_WIDE_VOLATILE = new Opcode<>(0xeb, 4, "sput-wide-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_WIDE_VOLATILE);
            }
        });
        VALUES[0xeb] = SPUT_WIDE_VOLATILE;
        IPUT_BYTE_QUICK = new Opcode<>(0xec, 4, "iput-byte-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_BYTE_QUICK);
            }
        });
        VALUES[0xec] = IPUT_BYTE_QUICK;
        THROW_VERIFICATION_ERROR = new Opcode<>(0xed, 4, "throw-verification-error", new BlockCreator<Ins20bc>() {
            @Override
            public Ins20bc newInstance() {
                return new Ins20bc(THROW_VERIFICATION_ERROR);
            }
        });
        VALUES[0xed] = THROW_VERIFICATION_ERROR;
        EXECUTE_INLINE = new Opcode<>(0xee, 6, "execute-inline", new BlockCreator<Ins35mi>() {
            @Override
            public Ins35mi newInstance() {
                return new Ins35mi(EXECUTE_INLINE);
            }
        });
        VALUES[0xee] = EXECUTE_INLINE;
        EXECUTE_INLINE_RANGE = new Opcode<>(0xef, 6, "execute-inline/range", new BlockCreator<Ins3rmi>() {
            @Override
            public Ins3rmi newInstance() {
                return new Ins3rmi(EXECUTE_INLINE_RANGE);
            }
        });
        VALUES[0xef] = EXECUTE_INLINE_RANGE;
        INVOKE_DIRECT_EMPTY = new Opcode<>(0xf0, 6, "invoke-direct-empty", new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_DIRECT_EMPTY);
            }
        });
        VALUES[0xf0] = INVOKE_DIRECT_EMPTY;
        RETURN_VOID_BARRIER = new Opcode<>(0xf1, 2, "return-void-barrier", new BlockCreator<Ins10x>() {
            @Override
            public Ins10x newInstance() {
                return new Ins10x(RETURN_VOID_BARRIER);
            }
        });
        VALUES[0xf1] = RETURN_VOID_BARRIER;
        IGET_QUICK = new Opcode<>(0xf2, 4, "iget-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_QUICK);
            }
        });
        VALUES[0xf2] = IGET_QUICK;
        IGET_WIDE_QUICK = new Opcode<>(0xf3, 4, "iget-wide-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_WIDE_QUICK);
            }
        });
        VALUES[0xf3] = IGET_WIDE_QUICK;
        IGET_OBJECT_QUICK = new Opcode<>(0xf4, 4, "iget-object-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_OBJECT_QUICK);
            }
        });
        VALUES[0xf4] = IGET_OBJECT_QUICK;
        IPUT_QUICK = new Opcode<>(0xf5, 4, "iput-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_QUICK);
            }
        });
        VALUES[0xf5] = IPUT_QUICK;
        IPUT_WIDE_QUICK = new Opcode<>(0xf6, 4, "iput-wide-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_WIDE_QUICK);
            }
        });
        VALUES[0xf6] = IPUT_WIDE_QUICK;
        IPUT_OBJECT_QUICK = new Opcode<>(0xf7, 4, "iput-object-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_OBJECT_QUICK);
            }
        });
        VALUES[0xf7] = IPUT_OBJECT_QUICK;
        INVOKE_VIRTUAL_QUICK = new Opcode<>(0xf8, 6, "invoke-virtual-quick", new BlockCreator<Ins35ms>() {
            @Override
            public Ins35ms newInstance() {
                return new Ins35ms(INVOKE_VIRTUAL_QUICK);
            }
        });
        VALUES[0xf8] = INVOKE_VIRTUAL_QUICK;
        INVOKE_VIRTUAL_QUICK_RANGE = new Opcode<>(0xf9, 6, "invoke-virtual-quick/range", new BlockCreator<Ins3rms>() {
            @Override
            public Ins3rms newInstance() {
                return new Ins3rms(INVOKE_VIRTUAL_QUICK_RANGE);
            }
        });
        VALUES[0xf9] = INVOKE_VIRTUAL_QUICK_RANGE;
        INVOKE_SUPER_QUICK = new Opcode<>(0xfa, 6, "invoke-super-quick", new BlockCreator<Ins35ms>() {
            @Override
            public Ins35ms newInstance() {
                return new Ins35ms(INVOKE_SUPER_QUICK);
            }
        });
        VALUES[0xfa] = INVOKE_SUPER_QUICK;
        INVOKE_SUPER_QUICK_RANGE = new Opcode<>(0xfb, 6, "invoke-super-quick/range", new BlockCreator<Ins3rms>() {
            @Override
            public Ins3rms newInstance() {
                return new Ins3rms(INVOKE_SUPER_QUICK_RANGE);
            }
        });
        VALUES[0xfb] = INVOKE_SUPER_QUICK_RANGE;
        IPUT_OBJECT_VOLATILE = new Opcode<>(0xfc, 4, "iput-object-volatile", new BlockCreator<Ins22c>() {
            @Override
            public Ins22c newInstance() {
                return new Ins22c(IPUT_OBJECT_VOLATILE);
            }
        });
        VALUES[0xfc] = IPUT_OBJECT_VOLATILE;
        SGET_OBJECT_VOLATILE = new Opcode<>(0xfd, 4, "sget-object-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SGET_OBJECT_VOLATILE);
            }
        });
        VALUES[0xfd] = SGET_OBJECT_VOLATILE;
        SPUT_OBJECT_VOLATILE = new Opcode<>(0xfe, 4, "sput-object-volatile", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(SPUT_OBJECT_VOLATILE);
            }
        });
        VALUES[0xfe] = SPUT_OBJECT_VOLATILE;
        CONST_METHOD_TYPE = new Opcode<>(0xff, 4, "const-method-type", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(CONST_METHOD_TYPE);
            }
        });
        VALUES[0xff] = CONST_METHOD_TYPE;


        PACKED_SWITCH_PAYLOAD = new Opcode<>(0x100, -1, "packed-switch-payload", new BlockCreator<InsPacked>() {
            @Override
            public InsPacked newInstance() {
                return new InsPacked();
            }
        });
        PAYLOADS[0] = PACKED_SWITCH_PAYLOAD;
        SPARSE_SWITCH_PAYLOAD = new Opcode<>(0x200, -1, "sparse-switch-payload", new BlockCreator<InsSparse>() {
            @Override
            public InsSparse newInstance() {
                return new InsSparse();
            }
        });
        PAYLOADS[1] = SPARSE_SWITCH_PAYLOAD;
        ARRAY_PAYLOAD = new Opcode<>(0x300, -1, "array-payload", new BlockCreator<InsArray>() {
            @Override
            public InsArray newInstance() {
                return new InsArray();
            }
        });
        PAYLOADS[2] = ARRAY_PAYLOAD;


        IPUT_BOOLEAN_QUICK = new Opcode<>(0xeb, 4, "iput-boolean-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_BOOLEAN_QUICK);
            }
        });
        VALUES_2[0] = IPUT_BOOLEAN_QUICK;
        IPUT_CHAR_QUICK = new Opcode<>(0xed, 4, "iput-char-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_CHAR_QUICK);
            }
        });
        VALUES_2[1] = IPUT_CHAR_QUICK;
        IPUT_SHORT_QUICK = new Opcode<>(0xee, 4, "iput-short-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IPUT_SHORT_QUICK);
            }
        });
        VALUES_2[2] = IPUT_SHORT_QUICK;
        IGET_BOOLEAN_QUICK = new Opcode<>(0xef, 4, "iget-boolean-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_BOOLEAN_QUICK);
            }
        });
        VALUES_2[3] = IGET_BOOLEAN_QUICK;
        INVOKE_OBJECT_INIT_RANGE = new Opcode<>(0xf0, 6, "invoke-object-init/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_OBJECT_INIT_RANGE);
            }
        });
        VALUES_2[4] = INVOKE_OBJECT_INIT_RANGE;
        IGET_CHAR_QUICK = new Opcode<>(0xf1, 4, "iget-char-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_CHAR_QUICK);
            }
        });
        VALUES_2[5] = IGET_CHAR_QUICK;
        IGET_SHORT_QUICK = new Opcode<>(0xf2, 4, "iget-short-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_SHORT_QUICK);
            }
        });
        VALUES_2[6] = IGET_SHORT_QUICK;
        INVOKE_POLYMORPHIC = new Opcode<>(0xfa, 8, "invoke-polymorphic", new BlockCreator<Ins45cc>() {
            @Override
            public Ins45cc newInstance() {
                return new Ins45cc(INVOKE_POLYMORPHIC);
            }
        });
        VALUES_2[7] = INVOKE_POLYMORPHIC;
        INVOKE_POLYMORPHIC_RANGE = new Opcode<>(0xfb, 8, "invoke-polymorphic/range", new BlockCreator<Ins4rcc>() {
            @Override
            public Ins4rcc newInstance() {
                return new Ins4rcc(INVOKE_POLYMORPHIC_RANGE);
            }
        });
        VALUES_2[8] = INVOKE_POLYMORPHIC_RANGE;
        INVOKE_CUSTOM = new Opcode<>(0xfc, 6, "invoke-custom", new BlockCreator<Ins35c>() {
            @Override
            public Ins35c newInstance() {
                return new Ins35c(INVOKE_CUSTOM);
            }
        });
        VALUES_2[9] = INVOKE_CUSTOM;
        INVOKE_CUSTOM_RANGE = new Opcode<>(0xfd, 6, "invoke-custom/range", new BlockCreator<Ins3rc>() {
            @Override
            public Ins3rc newInstance() {
                return new Ins3rc(INVOKE_CUSTOM_RANGE);
            }
        });
        VALUES_2[10] = INVOKE_CUSTOM_RANGE;
        CONST_METHOD_HANDLE = new Opcode<>(0xfe, 4, "const-method-handle", new BlockCreator<Ins21c>() {
            @Override
            public Ins21c newInstance() {
                return new Ins21c(CONST_METHOD_HANDLE);
            }
        });
        VALUES_2[11] = CONST_METHOD_HANDLE;


        IGET_BYTE_QUICK = new Opcode<>(0xf0, 4, "iget-byte-quick", new BlockCreator<Ins22cs>() {
            @Override
            public Ins22cs newInstance() {
                return new Ins22cs(IGET_BYTE_QUICK);
            }
        });
        VALUES_3[0] = IGET_BYTE_QUICK;

        for(Opcode<?> opcode : VALUES){
            if(opcode == null){
                continue;
            }
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : PAYLOADS){
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : VALUES_2){
            map.put(opcode.name, opcode);
        }

        for(Opcode<?> opcode : VALUES_3){
            map.put(opcode.name, opcode);
        }


    }

    private final int value;
    private final int size;
    private final String name;
    private final BlockCreator<T> creator;
    private final SectionType<?> sectionType;

    private final int width;

    private Opcode(int value, int size, String name, SectionType<?> sectionType, BlockCreator<T> creator){
        this.value = value;
        this.size = size;
        this.name = name;
        this.sectionType = sectionType;
        this.creator = creator;
        int width;
        if((value & 0xff00) != 0){
            width = 2;
        }else {
            width = 1;
        }
        this.width = width;
    }
    private Opcode(int value, int size, String name, BlockCreator<T> creator){
        this(value, size, name, null, creator);
    }


    public int getValue() {
        return value;
    }
    public int size() {
        return size;
    }
    public String getName() {
        return name;
    }

    public int getWidth() {
        return width;
    }
    public SectionType<?> getSectionType(){
        return sectionType;
    }
    @Override
    public T newInstance(){
        return creator.newInstance();
    }
    @Override
    public String toString() {
        return getName();
    }

    public static Opcode<?> valueOf(int value){
        if(value <= 0xff){
            return VALUES[value];
        }
        for (Opcode<?> opcode : PAYLOADS){
            if(value == opcode.value){
                return opcode;
            }
        }
        return null;
    }
    public static Opcode<?> valueOf(String name){
        return NAME_MAP.get(name);
    }

    public static Opcode<?> read(BlockReader reader) throws IOException {
        int value = reader.read();
        if(value == 0){
            value = reader.read() << 8;
            reader.offset(-1);
        }
        reader.offset(-1);
        return valueOf(value);
    }

}