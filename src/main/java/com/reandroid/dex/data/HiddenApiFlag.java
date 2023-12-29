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
package com.reandroid.dex.data;

import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HiddenApiFlag implements SmaliFormat {

    public static final HiddenApiFlag WHITELIST;
    public static final HiddenApiFlag GREYLIST;
    public static final HiddenApiFlag BLACKLIST;
    public static final HiddenApiFlag GREYLIST_MAX_O;
    public static final HiddenApiFlag GREYLIST_MAX_P;
    public static final HiddenApiFlag GREYLIST_MAX_Q;
    public static final HiddenApiFlag GREYLIST_MAX_R;
    public static final HiddenApiFlag CORE_PLATFORM_API;

    public static final int NO_RESTRICTION;

    public static final HiddenApiFlag TEST_API;
    private static final HiddenApiFlag[] VALUES;
    private static final HiddenApiFlag[] DOMAIN_VALUES;

    private static final Map<String, HiddenApiFlag> NAME_MAP;

    static {

        WHITELIST = new HiddenApiFlag(0, "whitelist");
        GREYLIST = new HiddenApiFlag(1, "greylist");
        BLACKLIST = new HiddenApiFlag(2, "blacklist");
        GREYLIST_MAX_O = new HiddenApiFlag(3, "greylist-max-o");
        GREYLIST_MAX_P = new HiddenApiFlag(4, "greylist-max-p");
        GREYLIST_MAX_Q = new HiddenApiFlag(5, "greylist-max-q");
        GREYLIST_MAX_R = new HiddenApiFlag(6, "greylist-max-r");

        CORE_PLATFORM_API = new HiddenApiFlag(8, "core-platform-api", true);
        TEST_API = new HiddenApiFlag(16, "test-api", true);

        NO_RESTRICTION = ObjectsUtil.of(0x7);

        VALUES = new HiddenApiFlag[]{
                WHITELIST,
                GREYLIST,
                BLACKLIST,
                GREYLIST_MAX_O,
                GREYLIST_MAX_P,
                GREYLIST_MAX_Q,
                GREYLIST_MAX_R
        };
        DOMAIN_VALUES = new HiddenApiFlag[]{
                CORE_PLATFORM_API,
                TEST_API
        };

        Map<String, HiddenApiFlag> map = new HashMap<>();
        for(HiddenApiFlag apiFlag : VALUES){
            map.put(apiFlag.name, apiFlag);
        }
        for(HiddenApiFlag apiFlag : DOMAIN_VALUES){
            map.put(apiFlag.name, apiFlag);
        }
        NAME_MAP = map;
    }

    private final int flag;
    private final String name;
    private final boolean domainFlag;

    private HiddenApiFlag(int flag, String name, boolean domainFlag){
        this.flag = flag;
        this.name = name;
        this.domainFlag = domainFlag;
    }
    private HiddenApiFlag(int flag, String name){
        this(flag, name, false);
    }

    public String getName() {
        return name;
    }
    public int getFlag() {
        return flag;
    }
    public boolean isDomainFlag() {
        return domainFlag;
    }
    public boolean isSet(int value) {
        if (domainFlag) {
            return (value & this.flag) == this.flag;
        } else {
            return (value & NO_RESTRICTION) == this.flag;
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(' ');
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
        return getName();
    }

    public static HiddenApiFlag valueOf(String name){
        return NAME_MAP.get(name);
    }

    public static HiddenApiFlag[] getAllFlags(int value) {
        if((value & NO_RESTRICTION) == NO_RESTRICTION){
            return null;
        }
        HiddenApiFlag normalRestriction = VALUES[value & NO_RESTRICTION];

        int domainSpecificPart = (value & ~NO_RESTRICTION);
        if (domainSpecificPart == 0) {
            return new HiddenApiFlag[]{normalRestriction};
        }
        int count = 1;
        for (HiddenApiFlag flag : DOMAIN_VALUES) {
            if (flag.isSet(value)) {
                count ++;
            }
        }
        HiddenApiFlag[] results = new HiddenApiFlag[count];
        count = 0;
        results[count] = normalRestriction;
        count ++;
        for (HiddenApiFlag domainFlag : DOMAIN_VALUES) {
            if (domainFlag.isSet(value)) {
                results[count] = domainFlag;
                count ++;
            }
        }
        return results;
    }

    public static void append(SmaliWriter writer, HiddenApiFlag[] flags) throws IOException {
        if(flags == null){
            return;
        }
        for(HiddenApiFlag apiFlag : flags){
            apiFlag.append(writer);
        }
    }
    public static String toString(HiddenApiFlag[] flags) {
        if(flags == null){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(HiddenApiFlag apiFlag : flags){
            builder.append(apiFlag.getName());
            builder.append(' ');
        }
        return builder.toString();
    }
}
