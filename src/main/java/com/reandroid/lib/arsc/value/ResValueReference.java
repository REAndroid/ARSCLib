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
package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.item.ReferenceItem;

import java.util.Objects;

public class ResValueReference implements ReferenceItem {
    private final BaseResValueItem resValueItem;
    public ResValueReference(BaseResValueItem resValueItem){
        this.resValueItem=resValueItem;
    }
    public EntryBlock getEntryBlock(){
        return resValueItem.getEntryBlock();
    }
    @Override
    public void set(int val) {
        resValueItem.onSetReference(val);
    }
    @Override
    public int get() {
        return resValueItem.getData();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResValueReference that = (ResValueReference) o;
        return Objects.equals(resValueItem, that.resValueItem);
    }
    @Override
    public int hashCode() {
        return Objects.hash(resValueItem);
    }
}
