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
package com.reandroid.arsc.container;

import com.reandroid.arsc.base.BlockContainer;
import com.reandroid.arsc.value.ValueItem;

public class ResValueContainer extends BlockContainer<ValueItem> {
    private final ValueItem[] mChildes;
    public ResValueContainer(){
        super();
        mChildes=new ValueItem[1];
    }
    @Override
    protected void onRefreshed(){
    }
    @Override
    public int childesCount() {
        return mChildes.length;
    }
    @Override
    public ValueItem[] getChildes() {
        return mChildes;
    }
    public void setResValue(ValueItem resValue){
        ValueItem old=getResValue();
        if(old!=null){
            old.setIndex(-1);
            old.setParent(null);
        }
        mChildes[0]=resValue;
        if(resValue==null){
            return;
        }
        resValue.setIndex(0);
        resValue.setParent(this);
    }
    public ValueItem getResValue(){
        if(mChildes.length==0){
            return null;
        }
        return mChildes[0];
    }
}
