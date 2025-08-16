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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Iterator;

public class ParallelReference implements IntegerReference, BlockRefresh {

    private final IntegerReference reference1;
    private IntegerReference reference2;

    public ParallelReference(IntegerReference reference1, IntegerReference reference2) {
        this.reference1 = reference1;
        this.reference2 = reference2;
    }
    public ParallelReference(IntegerReference reference1) {
        this(reference1, null);
    }

    public IntegerReference getReference2() {
        return reference2;
    }

    public boolean contains(IntegerReference reference) {
        if (reference == this) {
            return true;
        }
        if (!(reference instanceof ParallelReference)) {
            return CollectionUtil.contains(getRootReferences(), reference);
        }
        Iterator<IntegerReference> iterator = ((ParallelReference) reference).getRootReferences();
        while (iterator.hasNext()) {
            if (!contains(iterator.next())) {
                return false;
            }
        }
        return true;
    }
    public Iterator<IntegerReference> getRootReferences() {
        return CombiningIterator.two(
                getRootReferences(reference1),
                getRootReferences(reference2));
    }
    public void remove(IntegerReference ref) {
        Iterator<IntegerReference> iterator = getRootReferences(ref);
        while (iterator.hasNext()) {
            removeRoot(iterator.next());
        }
    }
    private void removeRoot(IntegerReference ref) {
        if (ref == reference2) {
            this.reference2 = null;
        } else if (reference2 instanceof ParallelReference) {
            ParallelReference p = (ParallelReference) reference2;
            if (p.reference1 == ref) {
                if (p.reference2 == null) {
                    this.reference2 = null;
                } else {
                    p = new ParallelReference(p.reference2);
                    this.reference2 = p;
                }
            }
            p.removeRoot(ref);
        }
    }
    public void add(IntegerReference ref) {
        Iterator<IntegerReference> iterator = getRootReferences(ref);
        while (iterator.hasNext()) {
            addRoot(iterator.next());
        }
    }
    private void addRoot(IntegerReference ref) {
        if (ref == null || contains(ref)) {
            return;
        }
        if (putOnNull(ref)) {
            return;
        }
        this.reference2 = new ParallelReference(this.reference2, ref);
    }
    private boolean putOnNull(IntegerReference ref) {
        IntegerReference current = this.reference1;
        if (current instanceof ParallelReference) {
            ParallelReference p = (ParallelReference) current;
            if (p.putOnNull(ref)) {
                return true;
            }
        }
        current = this.reference2;
        if (current == null) {
            this.reference2 = ref;
            return true;
        }
        if (current instanceof ParallelReference) {
            return ((ParallelReference) current).putOnNull(ref);
        }
        return false;
    }
    public void setReference2(IntegerReference reference2) {
        if (reference2 == null) {
            return;
        }
        if (contains(reference2)) {
            return;
        }
        IntegerReference current = this.reference2;
        if (current instanceof ParallelReference) {
            ((ParallelReference) current).setReference2(reference2);
        } else if (reference2 != this) {
            this.reference2 = reference2;
        }
    }

    @Override
    public void set(int value) {
        this.reference1.set(value);
        if (reference2 != null) {
            reference2.set(value);
        }
    }
    @Override
    public int get() {
        return reference1.get();
    }
    @Override
    public void refresh() {
        set(get());
    }
    public int get2() {
        IntegerReference ref2 = this.reference2;
        if (ref2 != null) {
            return ref2.get();
        }
        return reference1.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ParallelReference ref = (ParallelReference) obj;
        return this.contains(ref) && ref.contains(this);
    }

    @Override
    public int hashCode() {
        return ObjectsUtil.hash(reference1);
    }

    @Override
    public String toString() {
        int i1 = reference1.get();
        if (reference2 == null) {
            return Integer.toString(i1);
        }
        int i2 = reference2.get();
        if (i1 == i2) {
            return Integer.toString(i1);
        }
        return "r1=" + reference1 + ", r2=" + reference2;
    }

    public static IntegerReference combine(IntegerReference reference1, IntegerReference reference2) {
        if (reference1 == null) {
            return reference2;
        }
        if (reference2 == null) {
            return reference1;
        }
        if (reference1 instanceof ParallelReference) {
            ParallelReference ref = (ParallelReference) reference1;
            if (ref.getReference2() == null) {
                ref.setReference2(reference2);
                return ref;
            }
        }
        if (reference2 instanceof ParallelReference) {
            ParallelReference ref = (ParallelReference) reference2;
            if (ref.getReference2() == null) {
                ref.setReference2(reference1);
                return ref;
            }
        }
        if (reference1.equals(reference2)) {
            return reference1;
        }
        return new ParallelReference(reference1, reference2);
    }

    private static Iterator<IntegerReference> getRootReferences(IntegerReference ref) {
        if (ref == null) {
            return EmptyIterator.of();
        }
        if (ref instanceof ParallelReference) {
            return ((ParallelReference) ref).getRootReferences();
        }
        return SingleIterator.of(ref);
    }
}
