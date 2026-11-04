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
package com.reandroid.common;

import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;

public interface Namespace {

    String getPrefix();
    String getUri();

    class Util {
        public static String toPrefix(String name) {
            if (name == null) {
                return null;
            }
            int i = name.lastIndexOf('.');
            if (i > 0) {
                name = name.substring(i + 1);
            }
            return name;
        }
        public static String createUriForPrefix(String prefix) {
            if (prefix == null) {
                return URI_RES_AUTO;
            }
            return "http://schemas.android.com/apk/res/" + prefix;
        }
        public static String uriForResourceId(int resourceId) {
            if (resourceId == 0) {
                return null;
            }
            int packageId = (resourceId >> 24) & 0xff;
            if (packageId == 0x1) {
                return URI_ANDROID;
            } else if (packageId != 0) {
                return URI_RES_AUTO;
            } else {
                return null;
            }
        }

        public static boolean isValidNamespace(String uri, String prefix) {
            return isValidUri(uri) && isValidPrefix(prefix);
        }
        public static boolean isValidUri(String uri, int resourceId) {
            int packageId = (resourceId >> 24 ) & 0xff;
            if (packageId == 0) {
                if (StringsUtil.isEmpty(uri)) {
                    return true;
                }
                return isExternalUri(uri);
            }
            if (packageId == 0x1) {
                return URI_ANDROID.equals(uri);
            }
            if (URI_ANDROID.equals(uri) || !isValidUri(uri)) {
                return false;
            }
            return !isExternalUri(uri);
        }
        public static boolean isValidUri(String uri, String packageName) {
            if (PREFIX_ANDROID.equals(packageName)) {
                return URI_ANDROID.equals(uri);
            }
            if (URI_ANDROID.equals(uri)) {
                return false;
            }
            return isValidUri(uri);
        }
        public static boolean isValidUri(String uri) {
            if (uri == null || uri.length() < 3) {
                return false;
            }
            // TODO: Properly check for valid uri
            return uri.contains("://");
        }
        public static boolean isExternalUri(String uri) {
            return uri != null && !uri.contains("schemas.android.com");
        }
        public static boolean isValidPrefix(String prefix, String packageName) {
            if (PREFIX_ANDROID.equals(packageName)) {
                return PREFIX_ANDROID.equals(prefix);
            }
            if (PREFIX_ANDROID.equals(prefix)) {
                return false;
            }
            return isValidPrefix(prefix);
        }
        public static boolean isValidPrefix(String prefix) {
            if (prefix == null) {
                return false;
            }
            int length = prefix.length();
            if (length == 0) {
                return false;
            }
            if (!isValidPrefixChar(prefix.charAt(0))) {
                return false;
            }
            for (int i = 1; i < length; i++) {
                char c = prefix.charAt(i);
                if (isValidPrefixChar(c) || isValidPrefixSymbol(c)) {
                    continue;
                }
                return false;
            }
            return true;
        }
        private static boolean isValidPrefixChar(char ch) {
            return (ch <= 'Z' && ch >= 'A')
                    || (ch <= 'z' && ch >= 'a');
        }
        private static boolean isValidPrefixSymbol(char ch) {
            return (ch <= '9' && ch >= '0')
                    || ch == '_';
        }
        public static String prefixForResourceId(int resourceId) {
            if (resourceId == 0) {
                return null;
            }
            int packageId = (resourceId >> 24) & 0xff;
            if (packageId == 0x1) {
                return PREFIX_ANDROID;
            } else if (packageId != 0) {
                return PREFIX_APP;
            } else {
                return null;
            }
        }
        public static final String URI_ANDROID = ObjectsUtil.of("http://schemas.android.com/apk/res/android");
        public static final String URI_RES_AUTO = ObjectsUtil.of("http://schemas.android.com/apk/res-auto");
        public static final String PREFIX_ANDROID = ObjectsUtil.of("android");
        public static final String PREFIX_APP = ObjectsUtil.of("app");
    }
}
