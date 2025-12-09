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
package com.reandroid.dex.smali.formatters;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public interface ResourceIdComment extends SmaliComment {

    void writeComment(SmaliWriter writer, int id) throws IOException;

    static ResourceIdComment of(PackageBlock packageBlock, Locale locale) {
        return new ResourceTableComment(packageBlock, locale);
    }
    static ResourceIdComment of(PackageBlock packageBlock) {
        return new ResourceTableComment(packageBlock);
    }

    class ResourceTableComment implements ResourceIdComment {

        private final PackageBlock packageBlock;
        private final TableBlock tableBlock;
        private final ResConfig localeConfig;
        private final Map<Integer, Object> mCachedComment;
        private final Object mNoComment;

        public ResourceTableComment(PackageBlock packageBlock, Locale locale) {
            this.packageBlock = packageBlock;
            this.tableBlock = packageBlock.getTableBlock();
            if (locale == null) {
                locale = new Locale("en");
            }
            ResConfig resConfig = new ResConfig();
            resConfig.setLanguage(locale.getLanguage());
            resConfig.setRegion(locale.getLanguage());
            this.localeConfig = resConfig;
            this.mCachedComment = new HashMap<>();
            this.mNoComment = new Object();
        }
        public ResourceTableComment(PackageBlock packageBlock) {
            this(packageBlock, Locale.getDefault());
        }

        @Override
        public void writeComment(SmaliWriter writer, int resourceId) {
            if (!PackageBlock.isResourceId(resourceId)) {
                return;
            }
            String comment = getComment(resourceId);
            if (comment != null) {
                writer.appendComment(comment);
            }
        }
        private synchronized String getComment(int resourceId) {
            Integer id = resourceId;
            Map<Integer, Object> map = this.mCachedComment;
            Object obj = map.get(id);
            if (obj == mNoComment) {
                return null;
            }
            if (obj != null) {
                return (String) obj;
            }
            String comment = buildComment(resourceId);
            if (comment == null) {
                obj = mNoComment;
            } else {
                obj = comment;
            }
            map.put(id, obj);
            return comment;
        }
        private String buildComment(int resourceId) {
            ResourceEntry resourceEntry = tableBlock.getResource(resourceId);
            if (resourceEntry == null || !resourceEntry.isDeclared()) {
                return null;
            }
            String ref = resourceEntry
                    .buildReference(packageBlock, ValueType.REFERENCE);

            if (!resourceEntry.isContext(tableBlock)) {
                return ref;
            }
            if ("id".equals(resourceEntry.getType())) {
                return ref;
            }
            Entry entry = resourceEntry.getMatchingOrAny(this.localeConfig);
            if (entry == null) {
                return ref;
            }
            ResValue resValue = entry.getResValue();
            if (resValue == null) {
                return ref;
            }
            String decoded = resValue.decodeValue();
            if (decoded == null) {
                return ref;
            }
            if (decoded.length() > 100) {
                decoded = decoded.substring(0, 100) + " ...";
            }
            return ref + " '" + decoded + "'";
        }
    }
}
