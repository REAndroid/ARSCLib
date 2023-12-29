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
import com.reandroid.utils.collection.CombiningIterator;

import java.util.Iterator;
import java.util.Objects;

public class MethodHandleKey implements Key{

    private final MethodKey id;
    private final MethodKey member;

    public MethodHandleKey(MethodKey id, MethodKey member){
        this.id = id;
        this.member = member;
    }

    public MethodKey getId() {
        return id;
    }
    public MethodKey getMember() {
        return member;
    }

    @Override
    public TypeKey getDeclaring() {
        return getId().getDeclaring();
    }

    @Override
    public Iterator<? extends Key> mentionedKeys() {
        return CombiningIterator.two(getId().mentionedKeys(),
                getMember().mentionedKeys());
    }

    @Override
    public Key replaceKey(Key search, Key replace) {
        if(search.equals(this)){
            return replace;
        }
        MethodKey id = (MethodKey) getId().replaceKey(search, replace);
        MethodKey member = (MethodKey) getMember().replaceKey(search, replace);
        if(id != getId() || member != getMember()){
            return new MethodHandleKey(id, member);
        }
        return this;
    }

    @Override
    public int compareTo(Object obj) {
        if(obj == null){
            return -1;
        }
        MethodHandleKey other = (MethodHandleKey) obj;
        int i = CompareUtil.compare(getId(), other.getId());
        if(i != 0){
            return i;
        }
        return CompareUtil.compare(getMember(), other.getMember());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodHandleKey)) {
            return false;
        }
        MethodHandleKey other = (MethodHandleKey) obj;
        return Objects.equals(getId(), other.getId()) &&
                Objects.equals(getMember(), other.getMember());
    }
    @Override
    public int hashCode() {
        return getId().hashCode() + getMember().hashCode() * 31 ;
    }

    @Override
    public String toString() {
        return getId() + ", " + getMember();
    }
}
