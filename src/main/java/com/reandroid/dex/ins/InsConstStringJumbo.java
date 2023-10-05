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
package com.reandroid.dex.ins;

import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.key.StringKey;
import com.reandroid.utils.HexUtil;

public class InsConstStringJumbo extends Ins31c {

    public InsConstStringJumbo() {
        super(Opcode.CONST_STRING_JUMBO);
    }

    public String getString(){
        StringData stringData = getStringData();
        if(stringData != null){
            return stringData.getString();
        }
        return null;
    }
    public void setString(String string){
        super.setSectionItem(new StringKey(string));
    }
    public StringData getStringData(){
        StringId stringId = getSectionItem();
        if(stringId != null){
            return stringId.getStringData();
        }
        return null;
    }
    public int getRegister(){
        return super.getRegister(0);
    }
    public void setRegister(int register){
        super.setRegister(0, register);
    }
    @Override
    public StringId getSectionItem() {
        return (StringId) super.getSectionItem();
    }

    public InsConstString toConstString(){
        if(getParent() == null){
            return null;
        }
        StringId stringId = getSectionItem();
        if(stringId == null || !shouldConvertToConstString(stringId)){
            return null;
        }
        InsConstString constString = Opcode.CONST_STRING.newInstance();
        constString.setRegister(getRegister());
        this.replace(constString);
        constString.setSectionItem(stringId);
        return constString;
    }
    private boolean shouldConvertToConstString(StringId stringId){
        int index = stringId.getIndex();
        return index == (index & 0xffff);
    }
}
