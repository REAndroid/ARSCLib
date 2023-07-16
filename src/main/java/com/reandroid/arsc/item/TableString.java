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
package com.reandroid.arsc.item;

import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.arsc.value.Entry;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleText;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class TableString extends StringItem implements Comparable<TableString> {
    public TableString(boolean utf8) {
        super(utf8);
    }

    public StyleDocument getStyleDocument(){
        StyleItem styleItem = getStyle();
        if(styleItem == null){
            return null;
        }
        return styleItem.build(get());
    }
    public Iterator<Entry> getEntries(boolean complex) {
        return super.getUsers(Entry.class, new Predicate<Entry>() {
            @Override
            public boolean test(Entry item) {
                if(complex){
                    return item.isComplex();
                }
                return item.isScalar();
            }
        });
    }
    public Iterator<Entry> getEntries(Predicate<Entry> tester) {
        return super.getUsers(Entry.class, tester);
    }

    public List<Entry> listReferencedResValueEntries(){
        return CollectionUtil.toList(getEntries(false));
    }

    @Override
    public boolean hasReference(){
        if(!super.hasReference()){
            return false;
        }
        Collection<?> references = getReferencedList();
        if(references.size() > 1){
            return true;
        }
        Object referenceItem = references.iterator().next();
        return !(referenceItem instanceof StyleItem.StyleIndexReference);
    }
    @Override
    public int compareTo(TableString tableString) {
        if(tableString == null){
            return -1;
        }
        boolean hasStyle1 = this.hasStyle();
        boolean hasStyle2 = tableString.hasStyle();
        if(hasStyle1 && !hasStyle2){
            return -1;
        }
        if(!hasStyle1 && hasStyle2){
            return 1;
        }
        return StringsUtil.compareStrings(this.getXml(), tableString.getXml());
    }
}
