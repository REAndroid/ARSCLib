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
package com.reandroid.dex.key;

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;

import java.io.IOException;
import java.util.Iterator;

public class PackageKey implements Key {

    public static final PackageKey ROOT = new PackageKey("L");

    private final String name;

    private PackageKey(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public String getSimpleName() {
        String name = getName();
        int length = name.length();
        if (length < 2) {
            return StringsUtil.EMPTY;
        }
        int end = length - 1;
        int i = name.lastIndexOf('/', end - 1);
        if (i < 0) {
            i = 1;
        } else {
            i = i + 1;
        }
        return name.substring(i, end);
    }
    public String getSourceName() {
        String name = getName();
        int length = name.length();
        if (length < 2) {
            return StringsUtil.EMPTY;
        }
        return name.substring(1, length - 1)
                .replace('/', '.');
    }
    public int depth() {
        return StringsUtil.countChar(getName(), '/');
    }
    public PackageKey getParent() {
        String name = getName();
        int length = name.length();
        if (length < 2) {
            return null;
        }
        int i = name.lastIndexOf('/', length - 2);
        if (i < 0) {
            return ROOT;
        }
        return createNew(name.substring(0, i + 1));
    }
    public Iterator<PackageKey> getParents() {
        PackageKey parent = getParent();
        if (parent == null) {
            return EmptyIterator.of();
        }
        return CombiningIterator.singleOne(parent, parent.getParents());
    }
    public PackageKey replace(PackageKey search, PackageKey replace) {
        return replace(search, replace, true);
    }
    public PackageKey replace(PackageKey search, PackageKey replace, boolean subPackage) {
        if (search == null || replace == null || search.equals(replace)) {
            return this;
        }
        if (this.isRootPackage() || search.equals(this)) {
            return replace;
        }
        if (!subPackage) {
            return this;
        }
        if (search.isRootPackage()) {
            return createNew(replace.getName() +
                    this.getName().substring(1));
        }
        String searchName = search.getName();
        String name = this.getName();
        if (name.startsWith(searchName)) {
            return createNew(replace.getName() + name.substring(searchName.length()));
        }
        return this;
    }
    public PackageKey child(String simpleName) {
        int i = 0;
        if (simpleName.charAt(i) == '/') {
            simpleName = simpleName.substring(1);
        }
        i = simpleName.length() - 1;
        if (simpleName.charAt(i) == '/') {
            simpleName = simpleName.substring(0, i);
        }
        if (simpleName.equals(getSimpleName())) {
            return this;
        }
        return createNew(this.getName() + simpleName + "/");
    }
    public TypeKey type(String simpleName) {
        int i = 0;
        if (simpleName.charAt(i) == '/') {
            simpleName = simpleName.substring(1);
        }
        i = simpleName.length() - 1;
        if (simpleName.charAt(i) == ';') {
            simpleName = simpleName.substring(0, i);
        }
        return TypeKey.create(this.getName() + simpleName + ";");
    }
    public PackageKey changeSimpleName(String simpleName) {
        if (isRootPackage()) {
            return this;
        }
        int i = 0;
        if (simpleName.charAt(i) == '/') {
            simpleName = simpleName.substring(1);
        }
        i = simpleName.length() - 1;
        if (simpleName.charAt(i) == '/') {
            simpleName = simpleName.substring(0, i);
        }
        if (simpleName.equals(getSimpleName())) {
            return this;
        }
        return createNew(getParent().getName() + simpleName + "/");
    }
    public boolean isRootPackage() {
        return "L".equals(getName());
    }
    public boolean isChildOf(PackageKey parent) {
        if (parent == null || parent == this) {
            return false;
        }
        String name = this.getName();
        String parentName = parent.getName();
        if (!name.startsWith(parentName) || name.equals(parentName)) {
            return false;
        }
        return name.lastIndexOf('/', name.length() - 2) == parentName.length();
    }
    public boolean contains(PackageKey parent) {
        if (parent == null) {
            return false;
        }
        if (parent.isRootPackage()) {
            return true;
        }
        return this.getName().startsWith(parent.getName());
    }
    public boolean equalsPackage(String packageName) {
        return this.getName().equals(packageName);
    }
    public boolean equalsPackage(TypeKey typeKey) {
        return this.getName().equals(getPackageName(typeKey));
    }
    public boolean contains(String packageName) {
        if (packageName == null) {
            return false;
        }
        return getName().startsWith(packageName);
    }
    public boolean contains(TypeKey typeKey) {
        String packageName = getPackageName(typeKey);
        if (packageName == null) {
            return false;
        }
        return contains(getPackageName(typeKey));
    }

    @Override
    public PackageKey replaceKey(Key search, Key replace) {
        if (!(search instanceof PackageKey) || !(replace instanceof PackageKey)) {
            return this;
        }
        return this.replace((PackageKey) search, (PackageKey) replace, false);
    }
    @Override
    public Iterator<PackageKey> contents() {
        return CombiningIterator.singleOne(this, getParents());
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
    }

    @Override
    public int compareTo(Object obj) {
        if (obj == this) {
            return 0;
        }
        if (!(obj instanceof PackageKey)) {
            return StringsUtil.compareToString(this, obj);
        }
        PackageKey key = (PackageKey) obj;
        return CompareUtil.compare(getName(), key.getName());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PackageKey)) {
            return false;
        }
        PackageKey key = (PackageKey) obj;
        return getName().equals(key.getName());
    }
    @Override
    public int hashCode() {
        return getName().hashCode();
    }
    @Override
    public String toString() {
        return getName();
    }

    public static PackageKey of(TypeKey typeKey) {
        return createNew(getPackageName(typeKey));
    }
    public static PackageKey of(String packageName) {
        if (packageName == null) {
            return null;
        }
        packageName = convertSourceName(removeArray(packageName));
        if ("L".equals(packageName)) {
            return ROOT;
        }
        int length = packageName.length();
        if (length < 3 || packageName.charAt(0) != 'L' || packageName.charAt(length - 1) != '/') {
            return null;
        }
        return createNew(packageName);
    }
    public static PackageKey readOrNull(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        if (reader.finished() || reader.get() != 'L') {
            return null;
        }
        int end = reader.indexOfLineEnd();
        int i = reader.indexOfWhiteSpace();
        if (i < 0 || i > end) {
            return null;
        }
        int position = reader.position();
        int length = i - position;
        char last = reader.getASCII(i - 1);
        if (last != '/') {
            if (length != 1 && last != 'L') {
                return null;
            }
        }
        String name = reader.readString(length);
        PackageKey key = of(name);
        if (key == null) {
            reader.position(position);
        }
        return key;
    }
    private static PackageKey createNew(String packageName) {
        if (packageName == null) {
            return null;
        }
        if (packageName.equals("L")) {
            return ROOT;
        }
        return new PackageKey(packageName);
    }
    private static String convertSourceName(String name) {
        if ("L".equals(name)) {
            return name;
        }
        if (name.indexOf('/') >= 0) {
            return name;
        }
        name = name.replace('.', '/');
        int length = name.length();
        StringBuilder builder = new StringBuilder(length + 2);
        builder.append('L');
        builder.append(name);
        if (builder.charAt(length) != '/'){
            builder.append('/');
        }
        return builder.toString();
    }
    private static String removeArray(String name) {
        int length = name.length();
        int i = 0;
        while (i < length && name.charAt(i) == '[') {
            i ++;
        }
        if (i != 0) {
            return name.substring(i);
        }
        return name;
    }
    public static String getPackageName(TypeKey typeKey) {
        if (typeKey == null || typeKey.isPrimitive()) {
            return null;
        }
        String packageName = removeArray(typeKey.getTypeName());
        int length = packageName.length();
        if (length < 2 || packageName.charAt(0) != 'L') {
            return null;
        }
        int i = packageName.lastIndexOf('/');
        if (i < 0) {
            return "L";
        }
        return packageName.substring(0, i + 1);
    }
}
