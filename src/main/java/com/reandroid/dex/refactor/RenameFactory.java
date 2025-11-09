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
package com.reandroid.dex.refactor;

import com.reandroid.dex.smali.SmaliDirective;

public interface RenameFactory {
    Rename<?, ?> createRename(SmaliDirective directive);

    RenameFactory DEFAULT_FACTORY = directive -> {
        if (directive == SmaliDirective.CLASS) {
            return new RenameTypes();
        }
        if (directive == SmaliDirective.FIELD) {
            return new RenameFields();
        }
        if (directive == SmaliDirective.METHOD) {
            return new RenameMethods();
        }
        return null;
    };
    RenameFactory RENAME_INNER_CLASSES_FACTORY = directive -> {
        if (directive == SmaliDirective.CLASS) {
            RenameTypes renameTypes = new RenameTypes();
            renameTypes.setRenameInnerClasses(true);
            return renameTypes;
        }
        if (directive == SmaliDirective.FIELD) {
            return new RenameFields();
        }
        if (directive == SmaliDirective.METHOD) {
            return new RenameMethods();
        }
        return null;
    };
}
