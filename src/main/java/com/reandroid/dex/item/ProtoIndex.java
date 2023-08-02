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
package com.reandroid.dex.item;

import com.reandroid.arsc.io.BlockReader;
import java.io.IOException;

public class ProtoIndex extends BaseItem {
    private TypeList typeList;
    public ProtoIndex() {
        super(SIZE);
    }

    public TypeIndex getReturnType(){
        return getTypeIndex(getReturnTypeIndex());
    }

    public int getShortyIndex(){
        return getInteger(getBytesInternal(), OFFSET_SHORTY);
    }
    public void setShortyIndex(int index){
        putInteger(getBytesInternal(), OFFSET_RETURN_TYPE, index);
    }
    public int getReturnTypeIndex(){
        return getShort(getBytesInternal(), OFFSET_RETURN_TYPE) & 0xffff;
    }
    public void setReturnTypeIndex(int index){
        putShort(getBytesInternal(), OFFSET_RETURN_TYPE, (short) index);
    }
    public int getParametersOffset(){
        return getInteger(getBytesInternal(), OFFSET_PARAMETERS);
    }
    public void setParametersOffset(int index){
        putInteger(getBytesInternal(), OFFSET_PARAMETERS, index);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        typeList = TypeList.read(reader, getParametersOffset());
    }
    public int[] getParametersIndexes(){
        TypeList typeList = this.typeList;
        if(typeList != null){
            return typeList.toArray();
        }
        return null;
    }
    public TypeIndex[] getParameters(){
        TypeList typeList = this.typeList;
        if(typeList != null){
            return typeList.toTypes(getTypeSection());
        }
        return null;
    }

    public String buildMethodParameters(){
        TypeIndex[] parameters = getParameters();
        if(parameters == null){
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for(TypeIndex typeIndex:parameters){
            builder.append(typeIndex.getString().getString());
        }
        return builder.toString();
    }
    @Override
    public String toString() {
        return "(" + buildMethodParameters() +")" + getReturnType().toString();
    }

    private static final int OFFSET_SHORTY = 0;
    private static final int OFFSET_RETURN_TYPE = 4;
    private static final int OFFSET_PARAMETERS = 8;
    public static final int SIZE = 12;
}
