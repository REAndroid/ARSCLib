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

import com.reandroid.utils.CompareUtil;

import java.util.Objects;

public class KeyPair<T1 extends Key, T2 extends Key> implements Comparable<KeyPair<Key, Key>>{

    private T1 first;
    private T2 second;

    public KeyPair(T1 first, T2 second){
        this.first = first;
        this.second = second;
    }
    public KeyPair(T1 first){
        this(first, null);
    }
    public KeyPair(){
        this(null, null);
    }

    public T1 getFirst() {
        return first;
    }
    public void setFirst(T1 first) {
        this.first = first;
    }
    public T2 getSecond() {
        return second;
    }
    public void setSecond(T2 second) {
        this.second = second;
    }


    @Override
    public int compareTo(KeyPair<Key, Key> pair) {
        if(pair == null){
            return -1;
        }
        return CompareUtil.compare(getFirst(), pair.getFirst());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyPair)) {
            return false;
        }
        KeyPair<?, ?> keyPair = (KeyPair<?, ?>) obj;
        return Objects.equals(getFirst(), keyPair.getFirst());
    }

    @Override
    public int hashCode() {
        T1 first = getFirst();
        if(first != null){
            return first.hashCode();
        }
        return 0;
    }
    @Override
    public String toString() {
        return getFirst() + "=" + getSecond();
    }
}
