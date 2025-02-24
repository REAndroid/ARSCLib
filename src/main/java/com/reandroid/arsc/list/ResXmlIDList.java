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
package com.reandroid.arsc.list;

import com.reandroid.arsc.chunk.xml.ResXmlDocumentChunk;
import com.reandroid.arsc.container.CountedBlockList;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ResXmlID;
import com.reandroid.arsc.item.StringItem;
import com.reandroid.utils.CompareUtil;

import java.util.Comparator;
import java.util.Iterator;

public class ResXmlIDList extends CountedBlockList<ResXmlID> {

    public ResXmlIDList(IntegerReference countReference) {
        super(ResXmlID.CREATOR, countReference);
    }

    public ResXmlID getOrCreate(int resourceId) {
        ResXmlID id = get(resourceId);
        if (id == null) {
            id = createNext();
            id.set(resourceId);
        }
        return id;
    }
    public ResXmlID getId(int resourceId) {
        int size = size();
        for (int i = 0; i < size; i++) {
            ResXmlID id = get(i);
            if (resourceId == id.get()) {
                return id;
            }
        }
        return null;
    }
    @Override
    public void setSize(int size, boolean notify) {
        super.setSize(size, true);
    }
    @Override
    public int countBytes() {
        int count = size();
        if (count != 0) {
            count = count * get(0).countBytes();
        }
        return count;
    }
    public void clear() {
        super.clearChildes();
    }

    @Override
    protected boolean hasSimilarEntries() {
        return true;
    }

    public boolean sort() {
        return sort(CompareUtil.getComparableComparator());
    }
    @Override
    public boolean sort(Comparator<? super ResXmlID> comparator) {
        boolean sorted = super.sort(comparator);
        if (adjustIndexes()) {
            if (super.sort(comparator)) {
                sorted = true;
            }
        }
        trimLastIf(ResXmlID::isEmpty);
        return sorted;
    }
    private boolean adjustIndexes() {
        Iterator<ResXmlID> iterator = clonedIterator();
        boolean adjusted = false;
        while (iterator.hasNext()) {
            ResXmlID xmlID = iterator.next();
            StringItem stringItem = xmlID.getResXmlString();
            if (stringItem != null) {
                int index = stringItem.getIndex();
                if(index != xmlID.getIndex()) {
                    moveTo(xmlID, index);
                    adjusted = true;
                }
            }
        }
        if (adjusted) {
            getParentInstance(ResXmlDocumentChunk.class)
                    .getStringPool().linkResXmlIDMapInternal();
        }
        return adjusted;
    }
}
