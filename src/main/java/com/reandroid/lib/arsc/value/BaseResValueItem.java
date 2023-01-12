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

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;

public abstract class BaseResValueItem extends BaseResValue implements ResValueItem {

    private ReferenceItem mReferenceItem;
    BaseResValueItem(int bytesLength) {
        super(bytesLength);
    }
    String getString(int ref){
        TableString tableString=getTableString(ref);
        if(tableString==null){
            return null;
        }
        return tableString.getHtml();
    }
    TableString getTableString(int ref){
        TableStringPool stringPool=getTableStringPool();
        if(stringPool==null){
            return null;
        }
        return stringPool.get(ref);
    }
    TableStringPool getTableStringPool(){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        PackageBlock packageBlock=entryBlock.getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock!=null){
            return tableBlock.getTableStringPool();
        }
        return null;
    }
    public ReferenceItem getTableStringReference(){
        if(getValueType()!=ValueType.STRING || getEntryBlock()==null){
            return null;
        }
        if(mReferenceItem==null){
            mReferenceItem=new ResValueReference(this);
        }
        return mReferenceItem;
    }
    boolean hasTableReference(){
        return mReferenceItem!=null;
    }
    boolean removeTableReference(){
        ReferenceItem ref=mReferenceItem;
        if(ref==null){
            return false;
        }
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        mReferenceItem=null;
        return entryBlock.removeTableReference(ref);
    }

    public ValueType getValueType(){
        return ValueType.valueOf(getType());
    }
    @Override
    public void setType(ValueType valueType){
        byte type=0;
        if(valueType!=null){
            type=valueType.getByte();
        }
        setType(type);
    }
}
