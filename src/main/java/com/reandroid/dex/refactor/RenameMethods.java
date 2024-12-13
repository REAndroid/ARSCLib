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

import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.id.MethodId;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.*;

public class RenameMethods extends Rename<MethodKey, MethodKey> {

    public RenameMethods() {
        super();
    }

    public void add(DexClassRepository classRepository, MethodKey search, String replace) {
        add(classRepository, search, search.changeName(replace));
    }
    public void add(DexClassRepository classRepository, MethodKey search, MethodKey replace) {
        KeyPair<MethodKey, MethodKey> start = new KeyPair<>(search, replace);
        if (!start.isValid() || isLocked(start)) {
            return;
        }
        if (classRepository.getMethods(replace).hasNext()) {
            lock(start);
            return;
        }
        List<KeyPair<MethodKey, MethodKey>> list = new ArrayCollection<>();
        list.add(start);
        Iterator<MethodKey> iterator = classRepository.findEquivalentMethods(search);
        while (iterator.hasNext()) {
            MethodKey equivalent = iterator.next();
            KeyPair<MethodKey, MethodKey> pair = new KeyPair<>(equivalent,
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
        List<KeyPair<MethodKey, MethodKey>> list = toList();
        int count = applyToMethodIds(classRepository, list);
        count += applyToAnnotations(classRepository, list);
        return count;
    }
    private int applyToMethodIds(DexClassRepository classRepository,
                                 List<KeyPair<MethodKey, MethodKey>> keyPairList) {
        int count = 0;
        for (KeyPair<MethodKey, MethodKey> pair : keyPairList) {
            MethodKey search = pair.getFirst();
            MethodKey replace = pair.getSecond();
            Iterator<MethodId> iterator = classRepository.getItems(SectionType.METHOD_ID, search);
            while (iterator.hasNext()) {
                MethodId methodId = iterator.next();
                methodId.setKey(replace);
                count ++;
            }
        }
        return count;
    }
    private int applyToAnnotations(DexClassRepository classRepository,
                                 List<KeyPair<MethodKey, MethodKey>> list) {
        Map<TypeKey, Set<KeyPair<MethodKey, MethodKey>>> map = mapDeclaring(list);
        int count = 0;
        Iterator<AnnotationItem> iterator = classRepository.getItems(SectionType.ANNOTATION_ITEM);
        while (iterator.hasNext()) {
            AnnotationItem annotationItem = iterator.next();
            count += renameAnnotation(map, annotationItem);
        }
        return count;
    }
    private int renameAnnotation(Map<TypeKey, Set<KeyPair<MethodKey, MethodKey>>> map, AnnotationItem annotationItem) {
        TypeKey typeKey = annotationItem.getType();
        Set<KeyPair<MethodKey, MethodKey>> set = map.get(typeKey);
        if (set == null) {
            return 0;
        }
        int result = 0;
        for (KeyPair<MethodKey, MethodKey> pair : set) {
            MethodKey search = pair.getFirst();
            MethodKey replace = pair.getSecond();
            AnnotationItemKey key = annotationItem.getKey();
            AnnotationItemKey update = key.replaceKey(search, replace);
            if (key != update) {
                annotationItem.setKey(update);
                result ++;
            }
        }
        return result;
    }

    private Map<TypeKey, Set<KeyPair<MethodKey, MethodKey>>> mapDeclaring(
            List<KeyPair<MethodKey, MethodKey>> list) {

        Map<TypeKey, Set<KeyPair<MethodKey, MethodKey>>> map = new HashMap<>();

        for (KeyPair<MethodKey, MethodKey> pair : list) {
            TypeKey typeKey = pair.getFirst().getDeclaring();
            Set<KeyPair<MethodKey, MethodKey>> set = map.get(typeKey);
            if (set == null) {
                set = new HashSet<>();
                set.add(pair);
                map.put(typeKey, set);
            } else {
                set.add(pair);
            }
            set.add(pair);
        }
        return map;
    }
}
