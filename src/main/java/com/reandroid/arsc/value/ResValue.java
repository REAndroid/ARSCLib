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
package com.reandroid.arsc.value;

import com.reandroid.arsc.base.Block;

 public class ResValue extends ValueItem  {
    public ResValue() {
        super(8);
    }
    public Entry getEntry(){
        Block parent = getParent();
        while (parent!=null){
            if(parent instanceof Entry){
                return (Entry) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    int getSizeOffset(){
        return OFFSET_SIZE;
    }


    private static final int OFFSET_SIZE = 0;

}
