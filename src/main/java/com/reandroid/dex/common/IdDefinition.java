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

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.program.AccessibleProgram;

import java.util.Iterator;

public interface IdDefinition<T extends IdItem> extends AccessibleProgram, IdUsageIterator {

    T getId();
    boolean isRemoved();

    @Override
    default boolean uses(Key key) {
        IdItem id = getId();
        Iterator<IdItem> iterator = usedIds();
        while (iterator.hasNext()) {
            IdItem idItem = iterator.next();
            if (!id.equals(idItem) && key.equals(idItem.getKey())) {
                return true;
            }
        }
        return false;
    }
}
