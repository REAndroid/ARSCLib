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
package com.reandroid.dex.program;

import com.reandroid.utils.StringsUtil;

public class ProgramType {

    public static final ProgramType DEX = new ProgramType("DEX");
    public static final ProgramType SMALI = new ProgramType("SMALI");
    public static final ProgramType JAR = new ProgramType("JAR");
    public static final ProgramType KEY = new ProgramType("KEY");
    public static final ProgramType REFLECTION = new ProgramType("REFLECTION");
    public static final ProgramType UNKNOWN = new ProgramType("UNKNOWN");

    private final String name;

    private ProgramType(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    public static ProgramType valueOf(String name) {
        name = StringsUtil.toUpperCase(name);
        if ("DEX".equals(name)) {
            return DEX;
        }
        if ("SMALI".equals(name)) {
            return SMALI;
        }
        if ("JAR".equals(name)) {
            return JAR;
        }
        if ("KEY".equals(name)) {
            return KEY;
        }
        if ("REFLECTION".equals(name)) {
            return REFLECTION;
        }
        return UNKNOWN;
    }
}
