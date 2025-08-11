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
package com.reandroid.dex.base;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.utils.ObjectsUtil;


public class ParallelIntegerPair implements IntegerPair, BlockRefresh {

    private final ParallelReference first;
    private final ParallelReference second;

    public ParallelIntegerPair(IntegerPair integerPair) {
        this.first = new ParallelReference(integerPair.getFirst());
        this.second = new ParallelReference(integerPair.getSecond());
    }

    public void add(IntegerPair pair) {
        getFirst().add(pair.getFirst());
        getSecond().add(pair.getSecond());
    }
    public void remove(IntegerPair pair) {
        getFirst().remove(pair.getFirst());
        getSecond().remove(pair.getSecond());
    }
    public boolean contains(IntegerPair pair) {
        return getFirst().contains(pair.getFirst()) &&
                getSecond().contains(pair.getSecond());
    }
    @Override
    public ParallelReference getFirst() {
        return first;
    }
    @Override
    public ParallelReference getSecond() {
        return second;
    }
    @Override
    public void refresh() {
        getFirst().refresh();
        getSecond().refresh();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParallelIntegerPair pair = (ParallelIntegerPair) obj;
        return ObjectsUtil.equals(first, pair.first) &&
                ObjectsUtil.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public static IntegerPair combine(IntegerPair pair1, IntegerPair pair2) {
        if (pair1 == null) {
            return pair2;
        }
        if (pair2 == null) {
            return pair1;
        }
        if (pair1 instanceof ParallelIntegerPair) {
            ParallelIntegerPair p = (ParallelIntegerPair) pair1;
            p.add(pair2);
            return p;
        }
        ParallelIntegerPair p = new ParallelIntegerPair(pair1);
        p.add(pair2);
        return p;
    }
}
