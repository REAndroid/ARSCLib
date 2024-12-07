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

import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;

import java.util.*;

public abstract class Rename<T extends Key, R extends Key> {

    private final Map<KeyPair<?, ?>, KeyPair<T, R>> keyPairMap;
    private final Map<KeyPair<?, ?>, KeyPair<T, R>> flippedKeyMap;
    private final Set<KeyPair<?, ?>> badKeys;

    public Rename() {
        this.keyPairMap = new HashMap<>();
        this.flippedKeyMap = new HashMap<>();
        this.badKeys = new HashSet<>();
    }

    public void add(T search, R replace) {
        add(new KeyPair<>(search, replace));
    }
    public void add(KeyPair<T, R> keyPair){
        addToSet(keyPair);
    }
    public void addAll(Collection<KeyPair<T, R>> keyPairs){
        this.addAll(keyPairs.iterator());
    }
    public void addAll(Iterator<KeyPair<T, R>> iterator){
        while (iterator.hasNext()){
            addToSet(iterator.next());
        }
    }
    private void addToSet(KeyPair<T, R> keyPair) {
        if (keyPair == null || !keyPair.isValid()) {
            return;
        }
        boolean bad = false;
        if (badKeys.contains(keyPair)) {
            bad = true;
        }
        KeyPair<R, T> flip = keyPair.flip();
        if (badKeys.contains(flip)) {
            if (bad) {
                return;
            }
            bad = true;
        }
        if (!bad) {
            KeyPair<T, R> exist = keyPairMap.get(keyPair);
            if (exist == null) {
                exist = flippedKeyMap.get(flip);
                if (exist != null) {
                    bad = true;
                }
            } else {
                if (keyPair.equalsBoth(exist)) {
                    return;
                }
                bad = true;
            }
        }
        if (bad) {
            addBadKey(keyPair, flip);
        } else {
            keyPairMap.put(keyPair, keyPair);
            flippedKeyMap.put(flip, keyPair);
        }
    }
    private void addBadKey(KeyPair<T, R> keyPair, KeyPair<R, T> flip) {
        badKeys.add(keyPair);
        badKeys.add(flip);
        KeyPair<T, R> p1 = keyPairMap.remove(keyPair);
        if (p1 == null) {
            p1 = keyPairMap.remove(flip);
        }
        KeyPair<T, R> p2 = flippedKeyMap.remove(flip);
        if (p2 == null) {
            p2 = flippedKeyMap.remove(keyPair);
        }
        if (p1 != null && !p1.equalsBoth(keyPair)) {
            addBadKey(p1, p1.flip());
        }
        if (p2 != null && !p2.equalsBoth(keyPair)) {
            addBadKey(p2, p2.flip());
        }
    }
    public int size() {
        return keyPairMap.size();
    }
    public List<KeyPair<T, R>> toList() {
        return toList(CompareUtil.getComparableComparator());
    }
    public List<KeyPair<T, R>> toList(Comparator<KeyPair<? super T, ? super R>> comparator) {
        List<KeyPair<T, R>> results = new ArrayCollection<>(getKeyPairSet());
        if (comparator != null) {
            results.sort(comparator);
        }
        return results;
    }

    public abstract int apply(DexClassRepository classRepository);

    public Set<KeyPair<T, R>> getKeyPairSet() {
        return ObjectsUtil.cast(keyPairMap.keySet());
    }

    @Override
    public String toString() {
        return StringsUtil.join(toList(), '\n');
    }
}
