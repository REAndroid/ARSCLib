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
package com.reandroid.dex.common;

public class RegisterFormat {

    public static final RegisterFormat NONE;
    
    public static final RegisterFormat READ8;
    public static final RegisterFormat READ8W;
    public static final RegisterFormat READ16;
    public static final RegisterFormat WRITE4;
    public static final RegisterFormat WRITE8;
    public static final RegisterFormat WRITE8W;
    
    public static final RegisterFormat READ4_READ4;
    public static final RegisterFormat READ4W_READ4;
    
    public static final RegisterFormat READ8_READ8_READ8;
    public static final RegisterFormat READ8W_READ8_READ8;

    public static final RegisterFormat RW4_READ4;
    public static final RegisterFormat RW4W_READ4;
    public static final RegisterFormat RW4W_READ4W;

    public static final RegisterFormat WRITE4_READ4;
    public static final RegisterFormat WRITE4_READ4W;
    public static final RegisterFormat WRITE4W_READ4;
    public static final RegisterFormat WRITE4W_READ4W;

    public static final RegisterFormat WRITE8_READ8;
    public static final RegisterFormat WRITE8_READ16;
    public static final RegisterFormat WRITE8W_READ16W;
    public static final RegisterFormat WRITE16_READ16;
    public static final RegisterFormat WRITE16W_READ16W;

    public static final RegisterFormat WRITE8_READ8_READ8;
    public static final RegisterFormat WRITE8_READ8W_READ8W;
    public static final RegisterFormat WRITE8W_READ8_READ8;
    public static final RegisterFormat WRITE8W_READ8W_READ8;
    public static final RegisterFormat WRITE8W_READ8W_READ8W;
    
    public static final RegisterFormat OUT;
    public static final RegisterFormat OUT_RANGE;

    static {

        RegisterType[] read = new RegisterType[]{RegisterType.READ};
        RegisterType[] readRead = new RegisterType[]{RegisterType.READ, RegisterType.READ};
        RegisterType[] readReadRead = new RegisterType[]{RegisterType.READ, RegisterType.READ, RegisterType.READ};

        RegisterType[] rwRead = new RegisterType[]{RegisterType.RW, RegisterType.READ};

        RegisterType[] write = new RegisterType[]{ RegisterType.WRITE};
        RegisterType[] writeRead = new RegisterType[]{RegisterType.WRITE, RegisterType.READ};
        RegisterType[] writeReadRead = new RegisterType[]{RegisterType.WRITE, RegisterType.READ, RegisterType.READ};
        
        int[] limits_f = new int[]{0x0f};
        int[] limits_ff = new int[]{0xff};
        int[] limits_f_f = new int[]{0x0f, 0x0f};
        int[] limits_ff_ff = new int[]{0xff, 0xff};
        int[] limits_ff_ffff = new int[]{0xff, 0xffff};
        int[] limits_ffff_ffff = new int[]{0xffff, 0xffff};
        int[] limits_ff_ff_ff = new int[]{0xff, 0xff, 0xff};

        boolean[] wide_t = new boolean[] {true};
        boolean[] wide_t_f = new boolean[] {true, false};
        boolean[] wide_f_t = new boolean[] {false, true};
        boolean[] wide_t_t = new boolean[] {true, true};
        boolean[] wide_f_t_t = new boolean[] {false, true, true};
        boolean[] wide_t_f_f = new boolean[] {true, false, false};
        boolean[] wide_t_t_f = new boolean[] {true, true, false};
        boolean[] wide_t_t_t = new boolean[] {true, true, true};
        
        NONE = new RegisterFormat("NONE", null, null);
        READ8 = new RegisterFormat("READ8", read, limits_ff);
        READ8W = new RegisterFormat("READ8W", read, limits_ff, wide_t);
        READ16 = new RegisterFormat("READ16", read, new int[]{0xffff});
        WRITE4 = new RegisterFormat("WRITE4", write, limits_f);
        WRITE8 = new RegisterFormat("WRITE8", write, limits_ff);
        WRITE8W = new RegisterFormat("WRITE8W", write, limits_ff, wide_t);
        
        READ4_READ4 = new RegisterFormat("READ4_READ4", readRead, limits_f_f);
        READ4W_READ4 = new RegisterFormat("READ4W_READ4", readRead, limits_f_f, wide_t_f);
        READ8_READ8_READ8 = new RegisterFormat("READ8_READ8_READ8", readReadRead, limits_ff_ff_ff);
        READ8W_READ8_READ8 = new RegisterFormat("READ8W_READ8_READ8",
                readReadRead, limits_ff_ff_ff, wide_t_f_f);

        RW4_READ4 = new RegisterFormat("RW4_READ4", rwRead, limits_f_f);
        RW4W_READ4 = new RegisterFormat("RW4W_READ4", rwRead, limits_f_f, wide_t_f);
        RW4W_READ4W = new RegisterFormat("RW4W_READ4W", rwRead, limits_f_f, wide_t_t);

        WRITE4_READ4 = new RegisterFormat("WRITE4_READ4", writeRead, limits_f_f);
        WRITE4_READ4W = new RegisterFormat("WRITE4_READ4W", writeRead, limits_f_f, wide_f_t);
        WRITE4W_READ4 = new RegisterFormat("WRITE4W_READ4", writeRead, limits_f_f, wide_t_f);
        WRITE4W_READ4W = new RegisterFormat("WRITE4W_READ4W", writeRead, limits_f_f, wide_t_t);
        
        WRITE8_READ8 = new RegisterFormat("WRITE8_READ8", writeRead, limits_ff_ff);
        WRITE8_READ16 = new RegisterFormat("WRITE8_READ16", writeRead, limits_ff_ffff);
        WRITE8W_READ16W = new RegisterFormat("WRITE8W_READ16W",
                writeRead, limits_ff_ffff, wide_t_t);
        WRITE16_READ16 = new RegisterFormat("WRITE16_READ16", writeRead, limits_ffff_ffff);
        WRITE16W_READ16W = new RegisterFormat("WRITE16W_READ16W",
                writeRead, limits_ffff_ffff, wide_t_t);

        WRITE8_READ8_READ8 = new RegisterFormat("WRITE8_READ8_READ8", writeReadRead, limits_ff_ff_ff);
        WRITE8_READ8W_READ8W = new RegisterFormat("WRITE8_READ8W_READ8W",
                writeReadRead, limits_ff_ff_ff, wide_f_t_t);
        WRITE8W_READ8_READ8 = new RegisterFormat("WRITE8W_READ8_READ8",
                writeReadRead, limits_ff_ff_ff, wide_t_f_f);
        WRITE8W_READ8W_READ8 = new RegisterFormat("WRITE8W_READ8W_READ8",
                writeReadRead, limits_ff_ff_ff, wide_t_t_f);
        WRITE8W_READ8W_READ8W = new RegisterFormat("WRITE8W_READ8W_READ8W",
                writeReadRead, limits_ff_ff_ff, wide_t_t_t);

        OUT = new RegisterFormat("OUT", null, null) {
            @Override
            public RegisterType get(int i) {
                return RegisterType.READ;
            }
            @Override
            public boolean isOut() {
                return true;
            }
            @Override
            public int limit(int i) {
                return 0xf;
            }
            @Override
            public boolean isWide(int i) {
                return false;
            }
        };
        OUT_RANGE = new RegisterFormat("OUT_RANGE", null, null) {
            @Override
            public RegisterType get(int i) {
                return RegisterType.READ;
            }
            @Override
            public boolean isOut() {
                return true;
            }
            @Override
            public boolean isRange() {
                return true;
            }
            @Override
            public int limit(int i) {
                return 0xffff;
            }
            @Override
            public boolean isWide(int i) {
                return false;
            }
        };
    }

    private final String name;
    private final RegisterType[] types;
    private final int[] limits;
    private final boolean[] wide;

    RegisterFormat(String name, RegisterType[] types, int[] limits) {
        this(name, types, limits, null);
    }
    RegisterFormat(String name, RegisterType[] types, int[] limits, boolean[] wide) {
        this.name = name;
        this.types = types;
        this.limits = limits;
        this.wide = wide;
    }

    public RegisterType get(int i) {
        RegisterType[] types = this.types;
        if (types != null && i >= 0 && i < types.length) {
            return types[i];
        }
        return null;
    }
    public int size() {
        RegisterType[] types = this.types;
        if (types != null) {
            return types.length;
        }
        return 0;
    }
    public boolean isOut() {
        return false;
    }
    public boolean isRange() {
        return false;
    }
    public int limit(int i) {
        int[] limits = this.limits;
        if (limits != null && i >= 0 && i <= limits.length) {
            return limits[i];
        }
        return 0;
    }
    public boolean isWide(int i) {
        boolean[] wide = this.wide;
        if (wide != null && i >= 0 && i <= wide.length) {
            return wide[i];
        }
        return false;
    }
    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
    @Override
    public int hashCode() {
        int h = 31;
        RegisterType[] types = this.types;
        if (types != null) {
            h = h * 31 + types.length;
        }
        h = h * 31 + name.hashCode();
        return h;
    }

    @Override
    public String toString() {
        return name;
    }
}
