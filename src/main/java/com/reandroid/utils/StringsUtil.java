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
package com.reandroid.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class StringsUtil {

    public static String toString(Collection<?> collection){
        return toString(collection, MAX_STRING_APPEND);
    }
    public static String toString(Collection<?> collection, int max){
        if(collection == null){
            return "null";
        }
        if(max < 0){
            max = collection.size();
        }
        StringBuilder builder = new StringBuilder();
        builder.append("size=");
        builder.append(collection.size());
        builder.append(" [");
        int count = 0;
        Iterator<?> iterator = collection.iterator();
        while (iterator.hasNext() && count < max){
            if(count != 0){
                builder.append(", ");
            }
            builder.append(iterator.next());
            count ++;
        }
        if(count < collection.size()){
            builder.append(" ... ");
        }
        builder.append(']');
        return builder.toString();
    }
    public static String toString(Object[] elements){
        return toString(elements, MAX_STRING_APPEND);
    }
    public static String toString(Object[] elements, int max){
        if(elements == null){
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("length=");
        builder.append(elements.length);
        builder.append(" [");
        if(max < 0 || max > elements.length){
            max = elements.length;
        }
        int count;
        for (count = 0; count < max; count++){
            if(count != 0){
                builder.append(", ");
            }
            builder.append(elements[count]);
        }
        if(count < elements.length){
            builder.append(" ... ");
        }
        builder.append(']');
        return builder.toString();
    }
    public static String emptyToNull(String text){
        if(isEmpty(text)){
            return null;
        }
        return text;
    }
    public static boolean isEmpty(String text){
        return text == null || text.length() == 0;
    }
    public static String toUpperCase(String str){
        if(str == null || str.length() == 0){
            return str;
        }
        char[] chars = str.toCharArray();
        boolean changed = false;
        for(int i = 0; i < chars.length; i++){
            char ch = chars[i];
            char lower = toUpperCase(ch);
            if(ch == lower){
                continue;
            }
            chars[i] = lower;
            changed = true;
        }
        if(!changed){
            return str;
        }
        return new String(chars);
    }
    public static char toUpperCase(char ch){
        if(ch > 'z' || ch < 'a'){
            return ch;
        }
        int i = ch - 'a';
        return (char) (i + 'A');
    }
    public static String toLowercase(String str){
        char[] chars = str.toCharArray();
        boolean changed = false;
        for(int i = 0; i < chars.length; i++){
            char ch = chars[i];
            char lower = toLowercase(ch);
            if(ch == lower){
                continue;
            }
            chars[i] = lower;
            changed = true;
        }
        if(!changed){
            return str;
        }
        return new String(chars);
    }
    public static char toLowercase(char ch){
        if(ch > 'Z' || ch < 'A'){
            return ch;
        }
        int i = ch - 'A';
        return (char) (i + 'a');
    }
    public static void toStringSort(List<?> itemList){
        if(itemList == null || itemList.size() < 2){
            return;
        }
        Comparator<Object> cmp = new Comparator<Object>() {
            @Override
            public int compare(Object obj1, Object obj2) {
                return String.valueOf(obj1).compareTo(String.valueOf(obj2));
            }
        };
        itemList.sort(cmp);
    }
    public static String formatNumber(long number, long maximumValue){
        int minLength = Long.toString(maximumValue).length();
        return trailZeros(number, minLength);
    }
    public static String trailZeros(long number, int minLength){
        boolean negative = false;
        if(number < 0){
            negative = true;
            number = -number;
        }
        String text = Long.toString(number);
        int count = minLength - text.length();
        text = append(text, '0', count, true);
        if(negative){
            text = "-" + text;
        }
        return text;
    }

    public static String appendPostfix(String text, char ch, int count){
        return append(text, ch, count, false);
    }
    public static String append(String text, char ch, int count, boolean prefix){
        StringBuilder builder = new StringBuilder();
        if(!prefix){
            builder.append(text);
        }
        for(int i = 0; i < count; i++){
            builder.append(ch);
        }
        if(prefix){
            builder.append(text);
        }
        return builder.toString();
    }
    public static int compareToString(Object obj1, Object obj2) {
        return compareStrings(obj1 == null ? null : obj1.toString(),
                obj2 == null ? null : obj2.toString());
    }
    public static int compareStrings(String s1, String s2) {
        if(s1 == null && s2 == null){
            return 0;
        }
        if(s1 == null){
            return 1;
        }
        if(s2 == null){
            return -1;
        }
        int i = s1.compareTo(s2);
        if(i == 0){
            return 0;
        }
        if(i > 0){
            return 1;
        }
        return -1;
    }

    private static final int MAX_STRING_APPEND = 5;
}
