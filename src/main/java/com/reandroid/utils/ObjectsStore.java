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

import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;
import com.reandroid.utils.collection.InstanceIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.util.*;
import java.util.function.Predicate;

/**
 * A utility class to hold few non-null objects, the main purpose is to minimize new List class creation.
 *    <br />If no entries, the container will be null.
 *    <br />If one entry, the container will become entry itself.
 *    <br />If more than one entry, a {@link LinkedHashSet} will be created and becomes the container.
 * */
public class ObjectsStore {

    public static boolean isEmpty(Object container) {
        if (container == null) {
            return true;
        }
        if (container instanceof LinkedHashSet) {
            return ((LinkedHashSet<?>) container).isEmpty();
        }
        return false;
    }
    public static int size(Object container) {
        if (container == null) {
            return 0;
        }
        if (container instanceof LinkedHashSet) {
            return ((LinkedHashSet<?>) container).size();
        }
        return 1;
    }
    public static boolean contains(Object container, Object item) {
        if (container == null) {
            return false;
        }
        if (container instanceof LinkedHashSet) {
            return ((LinkedHashSet<?>) container).contains(item);
        }
        return container.equals(item);
    }
    public static<T> boolean containsIf(Object container, Predicate<T> predicate) {
        return iteratorIf(container, predicate).hasNext();
    }
    public static Object clear(Object container) {
        if (container instanceof LinkedHashSet) {
            ((LinkedHashSet<?>) container).clear();
        }
        return null;
    }
    public static void sort(Object container, Comparator<?> comparator) throws ClassCastException {
        if (container instanceof LinkedHashSet) {
            LinkedHashSet<Object> set = ObjectsUtil.cast(container);
            if (set.size() < 2) {
                return;
            }
            ArrayList<Object> list = new ArrayList<>(set);
            list.sort(ObjectsUtil.cast(comparator));
            set.clear();
            set.addAll(list);
        }
    }
    public static<T> Iterator<T> iterator(Object container) throws ClassCastException {
        Iterator<?> iterator;
        if (container == null) {
            iterator = EmptyIterator.of();
        } else if (container instanceof LinkedHashSet) {
            iterator = ((LinkedHashSet<?>) container).iterator();
        } else {
            iterator = SingleIterator.of(container);
        }
        return ObjectsUtil.cast(iterator);
    }
    public static<T> Iterator<T> iterator(Object container, Class<T> instance) throws ClassCastException {
        Iterator<?> iterator;
        if (container == null) {
            iterator = EmptyIterator.of();
        } else if (container instanceof LinkedHashSet) {
            iterator = InstanceIterator.of(((LinkedHashSet<?>) container).iterator(), instance);
        } else if (instance.isInstance(container)) {
            iterator = SingleIterator.of(container);
        } else {
            iterator = EmptyIterator.of();
        }
        return ObjectsUtil.cast(iterator);
    }
    public static<T> Iterator<T> clonedIterator(Object container) throws ClassCastException {
        Iterator<?> iterator;
        if (container == null) {
            iterator = EmptyIterator.of();
        } else if (container instanceof LinkedHashSet) {
            iterator = new ArrayList<>((LinkedHashSet<?>) container).iterator();
        } else {
            iterator = SingleIterator.of(container);
        }
        return ObjectsUtil.cast(iterator);
    }
    public static<T> Iterator<T> iteratorIf(Object container, Predicate<T> predicate) throws ClassCastException {
        return FilterIterator.of(iterator(container), predicate);
    }
    public static Object remove(Object container, Object item) {
        if (item == null || container == null || item == container) {
            return null;
        }
        if (!(container instanceof LinkedHashSet)) {
            if (container.equals(item)) {
                container = null;
            }
            return container;
        }
        LinkedHashSet<Object> set = ObjectsUtil.cast(container);
        set.remove(item);
        int size = set.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return set.iterator().next();
        }
        return set;
    }
    public static Object add(Object container, Object item) {
        if (item == null || item == container) {
            return container;
        }
        if (container == null) {
            return item;
        }
        LinkedHashSet<Object> set;
        if (container instanceof LinkedHashSet) {
            set = ObjectsUtil.cast(container);
        } else {
            set = new LinkedHashSet<>();
            set.add(container);
        }
        set.add(item);
        return set;
    }
    public static Object addAll(Object container, Iterator<?> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return container;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return add(container, first);
        }
        LinkedHashSet<Object> set;
        if (container instanceof LinkedHashSet) {
            set = ObjectsUtil.cast(container);
        } else {
            set = new LinkedHashSet<>();
            if (container != null) {
                set.add(container);
            }
        }
        set.add(first);
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        int size = set.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return set.iterator().next();
        }
        return set;
    }
    public static Object addAll(Object container, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return container;
        }
        LinkedHashSet<Object> set;
        if (container instanceof LinkedHashSet) {
            set = ObjectsUtil.cast(container);
        } else {
            set = new LinkedHashSet<>();
            if (container != null) {
                set.add(container);
            }
        }
        set.addAll(collection);
        int size = set.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return set.iterator().next();
        }
        return set;
    }
    public static Object addAll(Object container, Object[] itemsArray) {
        if (itemsArray == null || itemsArray.length == 0) {
            return container;
        }
        LinkedHashSet<Object> set;
        if (container instanceof LinkedHashSet) {
            set = ObjectsUtil.cast(container);
        } else {
            set = new LinkedHashSet<>();
            if (container != null) {
                set.add(container);
            }
        }
        set.addAll(Arrays.asList(itemsArray));
        int size = set.size();
        if (size == 0) {
            return null;
        }
        if (size == 1) {
            return set.iterator().next();
        }
        return set;
    }
    public static<T> T get(Object container, int i) throws ClassCastException {
        Object item = null;
        if (container != null) {
            if (container instanceof LinkedHashSet) {
                int index = 0;
                for (Object obj : (LinkedHashSet<?>) container) {
                    if (index == i) {
                        item = obj;
                        break;
                    }
                    index++;
                }
            } else if (i == 0) {
                item = container;
            }
        }
        return ObjectsUtil.cast(item);
    }
    public static void collect(Object container, Object[] array) {
        if (container == null || array == null || array.length == 0) {
            return;
        }
        if (container instanceof LinkedHashSet) {
            int i = 0;
            for (Object obj : (LinkedHashSet<?>) container) {
                if (i >= array.length) {
                    break;
                }
                array[i++] = obj;
            }
        } else {
            array[0] = container;
        }
    }
    public static Object create(Object[] array) {
        if (array == null) {
            return null;
        }
        int length = array.length;
        if (length == 0) {
            return null;
        }
        if (length == 1) {
            return array[0];
        }
        return new LinkedHashSet<>(Arrays.asList(array));
    }
    public static Object create(Iterator<?> iterator) {
        if (iterator == null || !iterator.hasNext()) {
            return null;
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first;
        }
        LinkedHashSet<Object> set = new LinkedHashSet<>();
        set.add(first);
        while (iterator.hasNext()) {
            set.add(iterator.next());
        }
        return set;
    }
}
