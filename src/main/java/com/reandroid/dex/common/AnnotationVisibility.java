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

import com.reandroid.utils.StringsUtil;

public class AnnotationVisibility {
    private static final AnnotationVisibility[] VALUES;
    public static final AnnotationVisibility BUILD;
    public static final AnnotationVisibility RUNTIME;
    public static final AnnotationVisibility SYSTEM;

    static {
        VALUES = new AnnotationVisibility[3];
        BUILD = new AnnotationVisibility("build", 0);
        VALUES[0] = BUILD;
        RUNTIME = new AnnotationVisibility("runtime", 1);
        VALUES[1] = RUNTIME;
        SYSTEM = new AnnotationVisibility("system", 2);
        VALUES[2] = SYSTEM;
    }

    private final String name;
    private final int value;
    private AnnotationVisibility(String name, int value){
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return name;
    }
    public int getValue() {
        return value;
    }
    @Override
    public String toString() {
        return getName();
    }

    public static AnnotationVisibility valueOf(int visibility) {
        if (visibility < 0 || visibility >= VALUES.length) {
            return null;
        }
        return VALUES[visibility];
    }

    public static AnnotationVisibility getVisibility(String visibility) {
        visibility = StringsUtil.toLowercase(visibility);
        if (visibility.equals("build")) {
            return BUILD;
        }
        if (visibility.equals("runtime")) {
            return RUNTIME;
        }
        if (visibility.equals("system")) {
            return SYSTEM;
        }
        return null;
    }

}
