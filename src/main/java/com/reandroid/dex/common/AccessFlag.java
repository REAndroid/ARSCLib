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

// originally copied from JesusFreke/smali
package com.reandroid.dex.common;

import java.util.HashMap;

public enum AccessFlag
{
    PUBLIC(0x1, "public", true, true, true),
    PRIVATE(0x2, "private", true, true, true),
    PROTECTED(0x4, "protected", true, true, true),
    STATIC(0x8, "static", true, true, true),
    FINAL(0x10, "final", true, true, true),
    SYNCHRONIZED(0x20, "synchronized", false, true, false),
    VOLATILE(0x40, "volatile", false, false, true),
    BRIDGE(0x40, "bridge", false, true, false),
    TRANSIENT(0x80, "transient", false, false, true),
    VARARGS(0x80, "varargs", false, true, false),
    NATIVE(0x100, "native", false, true, false),
    INTERFACE(0x200, "interface", true, false, false),
    ABSTRACT(0x400, "abstract", true, true, false),
    STRICTFP(0x800, "strictfp", false, true, false),
    SYNTHETIC(0x1000, "synthetic", true, true, true),
    ANNOTATION(0x2000, "annotation", true, false, false),
    ENUM(0x4000, "enum", true, false, true),
    CONSTRUCTOR(0x10000, "constructor", false, true, false),
    DECLARED_SYNCHRONIZED(0x20000, "declared-synchronized", false, true, false);

    private final int value;
    private final String accessFlagName;
    private final boolean validForClass;
    private final boolean validForMethod;
    private final boolean validForField;

    //cache the array of all AccessFlag, because .values() allocates a new array for every call
    private final static AccessFlag[] allFlags;

    private static final HashMap<String, AccessFlag> accessFlagsByName;

    static {
        allFlags = AccessFlag.values();

        accessFlagsByName = new HashMap<>();
        for (AccessFlag accessFlag : allFlags) {
            accessFlagsByName.put(accessFlag.accessFlagName, accessFlag);
        }
    }

    AccessFlag(int value, String accessFlagName, boolean validForClass, boolean validForMethod,
               boolean validForField) {
        this.value = value;
        this.accessFlagName = accessFlagName;
        this.validForClass = validForClass;
        this.validForMethod = validForMethod;
        this.validForField = validForField;
    }

    public boolean isSet(int accessFlags) {
        return (this.value & accessFlags) != 0;
    }

    public int getValue() {
        return value;
    }

    public String toString() {
        return accessFlagName;
    }

    public static AccessFlag[] getAccessFlagsForClass(int accessFlagValue) {
        int size = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForClass && (accessFlagValue & accessFlag.value) != 0) {
                size++;
            }
        }

        AccessFlag[] accessFlags = new AccessFlag[size];
        int accessFlagsPosition = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForClass && (accessFlagValue & accessFlag.value) != 0) {
                accessFlags[accessFlagsPosition++] = accessFlag;
            }
        }
        return accessFlags;
    }

    public static String format(AccessFlag[] accessFlags) {
        int size = 0;
        for (AccessFlag accessFlag: accessFlags) {
            size += accessFlag.toString().length() + 1;
        }

        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < accessFlags.length; i++) {
            if(i != 0){
                builder.append(' ');
            }
            builder.append(accessFlags[i].toString());
        }
        return builder.toString();
    }

    public static String formatForClass(int accessFlagValue) {
        return format(getAccessFlagsForClass(accessFlagValue));
    }

    public static AccessFlag[] getForMethod(int accessFlagValue) {
        int size = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForMethod && (accessFlagValue & accessFlag.value) != 0) {
                size++;
            }
        }

        AccessFlag[] accessFlags = new AccessFlag[size];
        int accessFlagsPosition = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForMethod && (accessFlagValue & accessFlag.value) != 0) {
                accessFlags[accessFlagsPosition++] = accessFlag;
            }
        }
        return accessFlags;
    }

    public static String formatForMethod(int accessFlagValue) {
        return format(getForMethod(accessFlagValue));
    }

    public static AccessFlag[] getForField(int accessFlagValue) {
        int size = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForField && (accessFlagValue & accessFlag.value) != 0) {
                size++;
            }
        }

        AccessFlag[] accessFlags = new AccessFlag[size];
        int accessFlagPosition = 0;
        for (AccessFlag accessFlag: allFlags) {
            if (accessFlag.validForField && (accessFlagValue & accessFlag.value) != 0) {
                accessFlags[accessFlagPosition++] = accessFlag;
            }
        }
        return accessFlags;
    }

    public static String formatForField(int accessFlagValue) {
        return format(getForField(accessFlagValue));
    }

    public static AccessFlag getAccessFlag(String accessFlag) {
        return accessFlagsByName.get(accessFlag);
    }

}
