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
package com.reandroid.dex.base;

import java.util.Iterator;

public interface UsageMarker {

    int getUsageType();
    void addUsageType(int usage);
    boolean containsUsage(int usage);
    void clearUsageType();


    static void clearUsageTypes(Iterator<?> usageMarkerIterator){
        while (usageMarkerIterator.hasNext()){
            UsageMarker usageMarker = (UsageMarker) usageMarkerIterator.next();
            usageMarker.clearUsageType();
        }
    }

    static String toUsageString(int usages) {
        if(containsUsage(usages, USAGE_NONE)){
            return "NONE";
        }
        StringBuilder builder = new StringBuilder();
        if(containsUsage(usages, USAGE_INSTRUCTION)){
            builder.append("INSTRUCTION");
        }
        if(containsUsage(usages, USAGE_ENCODED_VALUE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("ENCODED_VALUE");
        }
        if(containsUsage(usages, USAGE_ANNOTATION)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("ANNOTATION");
        }
        if(containsUsage(usages, USAGE_TYPE_NAME)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("TYPE_NAME");
        }
        if(containsUsage(usages, USAGE_FIELD_NAME)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("FIELD_NAME");
        }
        if(containsUsage(usages, USAGE_METHOD_NAME)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("METHOD_NAME");
        }
        if(containsUsage(usages, USAGE_SHORTY)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("SHORTY");
        }
        if(containsUsage(usages, USAGE_SOURCE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("SOURCE");
        }
        if(containsUsage(usages, USAGE_DEBUG)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("DEBUG");
        }
        if(containsUsage(usages, USAGE_DEFINITION)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("DEFINITION");
        }
        if(containsUsage(usages, USAGE_SUPER_CLASS)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("SUPER_CLASS");
        }
        if(containsUsage(usages, USAGE_FIELD_CLASS)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("FIELD_CLASS");
        }
        if(containsUsage(usages, USAGE_FIELD_TYPE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("FIELD_TYPE");
        }
        if(containsUsage(usages, USAGE_METHOD)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("METHOD");
        }
        if(containsUsage(usages, USAGE_PROTO)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("PROTO");
        }
        if(containsUsage(usages, USAGE_INTERFACE)){
            if(builder.length() != 0){
                builder.append('|');
            }
            builder.append("INTERFACE");
        }
        return builder.toString();
    }

    static boolean containsUsage(int usages, int usage){
        if(usage == 0){
            return usages == 0;
        }
        return (usages & usage) == usage;
    }
    int USAGE_NONE = 0x0000;

    int USAGE_INSTRUCTION = 1;
    int USAGE_ENCODED_VALUE = 1 << 1;
    int USAGE_ANNOTATION = 1 << 2;
    int USAGE_TYPE_NAME = 1 << 4;
    int USAGE_FIELD_NAME = 1 << 5;
    int USAGE_METHOD_NAME = 1 << 6;
    int USAGE_SHORTY = 4 << 7;
    int USAGE_SOURCE = 1 << 8;
    int USAGE_DEBUG = 1 << 9;
    int USAGE_DEFINITION = 1 << 10;
    int USAGE_SUPER_CLASS = 1 << 11;
    int USAGE_FIELD_CLASS = 1 << 12;
    int USAGE_FIELD_TYPE = 1 << 13;
    int USAGE_METHOD = 1 << 14;
    int USAGE_PROTO = 15;
    int USAGE_INTERFACE = 1 << 16;
    int USAGE_CALL_SITE = 1 << 17;
}
