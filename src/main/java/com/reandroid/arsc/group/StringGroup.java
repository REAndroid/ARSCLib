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
package com.reandroid.arsc.group;

import com.reandroid.arsc.base.BlockArrayCreator;
import com.reandroid.arsc.item.StringItem;

import java.util.Comparator;

public class StringGroup<T extends StringItem> extends ItemGroup<T>
        implements Comparator<StringItem>{
    public StringGroup(BlockArrayCreator<T> blockArrayCreator, String name){
        super(blockArrayCreator, name);
    }
    public StringGroup(BlockArrayCreator<T> blockArrayCreator, String name, T firstItem){
        super(blockArrayCreator, name, firstItem);
    }

    public int clearDuplicates(){
        if(!isDuplicate()){
            return 0;
        }
        T[] stringItems = getItems();
        StringItem first = stringItems[0];
        int length = stringItems.length;
        for(int i = 1; i < length; i++){
            StringItem item = stringItems[i];
            first.transferReferences(item);
        }
        return length - 1;
    }
    public boolean isDuplicate(){
        T[] stringItems = getItems();
        int length = stringItems.length;
        if(length < 2){
            return false;
        }
        int end = length - 1;
        for(int i = 0; i < end; i++){
            for(int j = i + 1; j < length; j++){
                StringItem left = stringItems[i];
                StringItem right = stringItems[j];
                if(left == right){
                    continue;
                }
                if(compare(left, right) == 0){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int compare(StringItem stringItem1, StringItem stringItem2) {
        if(stringItem1 == stringItem2){
            return 0;
        }
        if(stringItem1 == null){
            return 1;
        }
        return stringItem1.compareTo(stringItem2);
    }
}
