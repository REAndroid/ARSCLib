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
package com.reandroid.utils;

import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * A utility class to hold few non-null objects, the main purpose is to minimize new List class creation.
 *    <br />If no entries, the container will be null.
 *    <br />If one entry, the container will become entry itself.
 *    <br />If more than one entry, List class will be created and becomes the container.
 * */
public class ObjectsStore {

    public static boolean isEmpty(Object container) {
        if (container == null) {
            return true;
        }
        if (container.getClass() == ObjectsList.class) {
            return ((ObjectsList) container).isEmpty();
        }
        return false;
    }
    public static int size(Object container) {
        if (container == null) {
            return 0;
        }
        if (container.getClass() == ObjectsList.class) {
            return ((ObjectsList) container).size();
        }
        return 1;
    }
    public static boolean contains(Object container, Object reference) {
        if (container == null) {
            return false;
        }
        if (container.getClass() == ObjectsList.class) {
            return ((ObjectsList) container).contains(reference);
        }
        return container.equals(reference);
    }
    public static<T> boolean containsIf(Object container, Predicate<T> predicate) {
        return iteratorIf(container, predicate).hasNext();
    }
    public static Object clear(Object container) {
        if (container != null && container.getClass() == ObjectsList.class) {
            ((ObjectsList) container).clear();
        }
        return null;
    }
    public static<T> Iterator<T> iterator(Object container) throws ClassCastException {
        Iterator<?> iterator;
        if (container == null) {
            iterator = EmptyIterator.of();
        } else if (container.getClass() == ObjectsList.class) {
            iterator = ((ObjectsList) container).iterator();
        } else {
            iterator = SingleIterator.of(container);
        }
        return ObjectsUtil.cast(iterator);
    }
    public static<T> Iterator<T> clonedIterator(Object container) throws ClassCastException {
        Iterator<?> iterator;
        if (container == null) {
            iterator = EmptyIterator.of();
        } else if (container.getClass() == ObjectsList.class) {
            iterator = ((ObjectsList) container).clonedIterator();
        } else {
            iterator = SingleIterator.of(container);
        }
        return ObjectsUtil.cast(iterator);
    }
    public static<T> Iterator<T> iteratorIf(Object container, Predicate<T> predicate) throws ClassCastException {
        return FilterIterator.of(iterator(container), predicate);
    }
    public static Object remove(Object container, Object reference) {
        if (reference == null || container == null || reference == container) {
            return null;
        }
        if (container.getClass() != ObjectsList.class) {
            if (container.equals(reference)) {
                container = null;
            }
            return container;
        }
        ObjectsList list = (ObjectsList) container;
        list.remove(reference);
        int size = list.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return list.get(0);
        }
        return list;
    }
    public static Object add(Object container, Object reference) {
        if (reference == null || reference == container) {
            return container;
        }
        if (container == null) {
            return reference;
        }
        ObjectsList list;
        if (container.getClass() == ObjectsList.class) {
            list = (ObjectsList) container;
        } else {
            list = new ObjectsList();
            list.add(container);
        }
        list.add(reference);
        return list;
    }
    public static Object addAll(Object container, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return container;
        }
        ObjectsList list;
        if (container.getClass() == ObjectsList.class) {
            list = (ObjectsList) container;
        } else {
            list = new ObjectsList();
            list.add(container);
        }
        list.addAll(collection);
        int size = list.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return list.get(0);
        }
        return list;
    }

    static final class ObjectsList extends ArrayCollection<Object> {

        ObjectsList() {
            super(10);
        }

        @Override
        public boolean add(Object item) {
            if (containsExact(item) || item == null) {
                return false;
            }
            return super.add(item);
        }
    }
}
