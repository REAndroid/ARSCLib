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
package com.reandroid.dex.smali;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;

import java.io.File;
import java.io.IOException;

public class SmaliFileNameFactory {

    public static final boolean WINDOWS_OS;
    public static final int DEFAULT_MAX_PATH_LENGTH;
    public static final int DEFAULT_MAX_PATH_SEGMENTS;
    public static final String DEFAULT_RENAMED_PREFIX;
    public static final SmaliFileNameFactory INSTANCE;

    static {
        boolean win = false;
        try {
            String name = System.getProperty("os.name", "").toLowerCase();
            win = name.contains("windows");
        } catch (Throwable ignored) {}

        WINDOWS_OS = win;
        DEFAULT_MAX_PATH_LENGTH = ObjectsUtil.of(200);
        DEFAULT_MAX_PATH_SEGMENTS = ObjectsUtil.of(25);
        DEFAULT_RENAMED_PREFIX = ObjectsUtil.of("renamed_");
        INSTANCE = new SmaliFileNameFactory(WINDOWS_OS, DEFAULT_MAX_PATH_LENGTH,
                DEFAULT_MAX_PATH_SEGMENTS, DEFAULT_RENAMED_PREFIX);
    }

    private boolean caseSensitive;
    private int maxFilePathLength;
    private int maxPathSegments;
    private String renamedPrefix;

    private SmaliFileNameFactory(boolean caseSensitive, int maxFilePathLength,
                                 int maxPathSegments, String renamedPrefix) {
        this.caseSensitive = caseSensitive;
        this.maxFilePathLength = maxFilePathLength;
        this.maxPathSegments = maxPathSegments;
        this.renamedPrefix = renamedPrefix;
    }
    public SmaliFileNameFactory() {
        this(WINDOWS_OS, DEFAULT_MAX_PATH_LENGTH, DEFAULT_MAX_PATH_SEGMENTS, DEFAULT_RENAMED_PREFIX);
    }

    public File toFile(File rootDir, TypeKey typeKey) {
        File file = new File(rootDir, toPath(typeKey));
        if (shouldGenerateUniqueFile(file, typeKey)) {
            file = generateUniqueFile(rootDir, typeKey);
        }
        return file;
    }
    public String toPath(TypeKey typeKey) {
        return toRawPath(typeKey) + ".smali";
    }
    private File generateUniqueFile(File rootDir, TypeKey typeKey) {
        String rawPath = toRawPath(typeKey);
        int i = 0;
        int limit = 50000;
        File file = rootDir;
        while (i < limit) {
            file = toFileWithSuffix(rootDir, rawPath, i);
            if (!file.exists()) {
                break;
            }
            i ++;
        }
        return file;
    }
    private String toRawPath(TypeKey typeKey) {
        String path = typeKey.getTypeName();
        path = path.substring(1, path.length() - 1);
        path = path.replace('/', File.separatorChar);
        path = fixPathLength(path);
        return path;
    }
    private File toFileWithSuffix(File rootDir, String rawPath, int suffix) {
        String path = rawPath + "_" + suffix + ".smali";
        return new File(rootDir, path);
    }
    private boolean shouldGenerateUniqueFile(File file, TypeKey typeKey) {
        if (!isCaseSensitive()) {
            return false;
        }
        if (!file.exists()) {
            return false;
        }
        TypeKey existing = readClassType(file);
        return existing == null || !existing.equals(typeKey);
    }
    private String fixPathLength(String rawPath) {
        int length = rawPath.length();
        int maxLength = getMaxFilePathLength();
        int maxSegments = getMaxPathSegments();
        int segments = StringsUtil.countChar(rawPath, File.separatorChar);
        if (length <= maxLength && segments <= maxSegments) {
            return rawPath;
        }
        String dir;
        String str;
        int i = rawPath.lastIndexOf(File.separatorChar);
        if (i > maxLength / 3 || i < 0 || segments > maxSegments) {
            str = rawPath;
            dir = "";
        } else {
            dir = rawPath.substring(0, i + 1);
            str = rawPath;
        }
        return dir + createHashString(str);
    }
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public int getMaxFilePathLength() {
        return maxFilePathLength;
    }
    public void setMaxFilePathLength(int maxFilePathLength) {
        if (maxFilePathLength < 2) {
            throw new IllegalArgumentException("Maximum file path length too small: "
                    + maxFilePathLength);
        }
        this.maxFilePathLength = maxFilePathLength;
    }
    public int getMaxPathSegments() {
        return maxPathSegments;
    }
    public void setMaxPathSegments(int maxPathSegments) {
        if (maxPathSegments < 2) {
            throw new IllegalArgumentException("Maximum file path segments too small: "
                    + maxPathSegments);
        }
        this.maxPathSegments = maxPathSegments;
    }

    public String getRenamedPrefix() {
        String prefix = this.renamedPrefix;
        if (prefix == null) {
            prefix = "";
            this.renamedPrefix = prefix;
        }
        return prefix;
    }

    public void setRenamedPrefix(String renamedPrefix) {
        this.renamedPrefix = renamedPrefix;
    }

    private String createHashString(String str) {
        long hash = longHashCode(str);
        String hex = Long.toHexString(hash);
        String prefix = getRenamedPrefix();
        StringBuilder builder = new StringBuilder(prefix.length() + 16);
        builder.append(prefix);
        if (getMaxFilePathLength() > 32) {
            int pad = 16 - hex.length();
            for (int i = 0; i < pad; i++) {
                builder.append('0');
            }
        }
        builder.append(hex);
        return builder.toString();
    }
    private static long longHashCode(String str) {
        long l = 1;
        int length = str.length();
        for (int i = 0; i < length; i++) {
            l = l * 0x1f + str.charAt(i);
        }
        if (l < 0) {
            l = l & 0x7effffffffffffffL;
        }
        return l;
    }
    private static TypeKey readClassType(File file) {
        try {
            SmaliReader reader = SmaliReader.of(file);
            reader.skipWhitespacesOrComment();
            SmaliParseException.expect(reader, SmaliDirective.CLASS);
            AccessFlag.parse(reader);
            reader.skipWhitespacesOrComment();
            return TypeKey.read(reader);
        } catch (IOException ignored) {
            return null;
        }
    }

    public static SmaliFileNameFactory newInstance() {
        SmaliFileNameFactory instance = INSTANCE;
        return new SmaliFileNameFactory(instance.isCaseSensitive(),
                instance.getMaxFilePathLength(), instance.getMaxPathSegments(),
                instance.getRenamedPrefix());
    }
}
