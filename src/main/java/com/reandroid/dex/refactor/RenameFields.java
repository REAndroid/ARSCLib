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

import com.reandroid.dex.id.FieldId;
import com.reandroid.dex.key.FieldKey;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.Iterator;
import java.util.List;

public class RenameFields extends Rename<FieldKey> {

    public RenameFields() {
        super();
    }

    public void add(DexClassRepository classRepository, FieldKey search, String replace) {
        add(classRepository, search, search.changeName(replace));
    }
    @Override
    public void add(DexClassRepository classRepository, KeyPair<FieldKey, FieldKey> start) {
        if (!start.isValid() || isLocked(start)) {
            return;
        }
        FieldKey search = start.getFirst();
        FieldKey replace = start.getSecond();
        if (!search.getType().equals(replace.getType())) {
            return;
        }
        if (containsDeclaration(classRepository, replace)) {
            lock(start);
            return;
        }
        List<KeyPair<FieldKey, FieldKey>> list = new ArrayCollection<>();
        list.add(start);
        Iterator<FieldKey> iterator = classRepository.findEquivalentFields(search);
        while (iterator.hasNext()) {
            FieldKey equivalent = iterator.next();
            KeyPair<FieldKey, FieldKey> pair = new KeyPair<>(equivalent,
                    equivalent.changeName(replace.getName()));
            if (isLocked(pair)) {
                lockAll(list);
                list.clear();
                break;
            }
            list.add(pair);
        }
        addAll(list);
    }

    @Override
    public int apply(DexClassRepository classRepository) {
        if (isEmpty()) {
            return 0;
        }
        List<KeyPair<FieldKey, FieldKey>> list = toList();
        return applyToFieldIds(classRepository, list);
    }
    private int applyToFieldIds(DexClassRepository classRepository,
                                List<KeyPair<FieldKey, FieldKey>> keyPairList) {
        int count = 0;
        for (KeyPair<FieldKey, FieldKey> pair : keyPairList) {
            FieldKey search = pair.getFirst();
            FieldKey replace = pair.getSecond();
            Iterator<FieldId> iterator = classRepository.getItems(SectionType.FIELD_ID, search);
            while (iterator.hasNext()) {
                FieldId fieldId = iterator.next();
                fieldId.setKey(replace);
                count ++;
            }
        }
        return count;
    }

    @Override
    protected boolean containsDeclaration(DexClassRepository classRepository, FieldKey replaceKey) {
        return classRepository.getDeclaredField(replaceKey, true) != null;
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.FIELD;
    }

}
