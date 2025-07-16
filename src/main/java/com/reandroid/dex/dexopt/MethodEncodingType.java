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
package com.reandroid.dex.dexopt;

public class MethodEncodingType {

    public static final MethodEncodingType HOT;
    public static final MethodEncodingType STARTUP;
    public static final MethodEncodingType POST_STARTUP;
    public static final MethodEncodingType INLINE_CACHE_MISSING_TYPES_ENCODING;
    public static final MethodEncodingType INLINE_CACHE_MEGAMORPHIC_ENCODING;

    private static final MethodEncodingType[] VALUES;

    static {

        HOT = new MethodEncodingType(1, "HOT");
        STARTUP = new MethodEncodingType(1 << 1, "STARTUP");
        POST_STARTUP = new MethodEncodingType(1 << 2, "POST_STARTUP");
        INLINE_CACHE_MISSING_TYPES_ENCODING = new MethodEncodingType(1 << 6,
                "INLINE_CACHE_MISSING_TYPES_ENCODING");
        INLINE_CACHE_MEGAMORPHIC_ENCODING = new MethodEncodingType(1 << 7,
                "INLINE_CACHE_MEGAMORPHIC_ENCODING");

        VALUES = new MethodEncodingType[]{
                HOT,
                STARTUP,
                POST_STARTUP,
                INLINE_CACHE_MISSING_TYPES_ENCODING,
                INLINE_CACHE_MEGAMORPHIC_ENCODING
        };
    }

    private final int flag;
    private final String name;

    private MethodEncodingType(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public int flag() {
        return flag;
    }

    public String name() {
        return name;
    }

    public boolean isPcMapSize() {
        MethodEncodingType type = this;
        return type == HOT ||
                type == STARTUP ||
                type == POST_STARTUP;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public int hashCode() {
        return flag;
    }

    @Override
    public String toString() {
        return name;
    }

    public static MethodEncodingType valueOf(int flag) {
        for (MethodEncodingType type : VALUES) {
            if (type.flag == flag) {
                return type;
            }
        }
        return null;
    }

    public static MethodEncodingType valueOf(String name) {
        for (MethodEncodingType type : VALUES) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isPcMapSize(int value) {
        return isPcMapSize(valueOf(value));
    }

    public static boolean isPcMapSize(MethodEncodingType type) {
        return type == null || type.isPcMapSize();
    }
}
