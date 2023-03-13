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
package com.reandroid.common;

import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.arsc.util.FrameworkTable;

import java.io.IOException;
import java.io.InputStream;

/**Use {@link AndroidFrameworks} */
@Deprecated
public class Frameworks {
    private static FrameworkTable android_table;
    private static boolean load_once;
    @Deprecated
    public static FrameworkTable getAndroid(){
        if(android_table!=null || load_once){
            return android_table;
        }
        load_once=true;
        FrameworkTable frameworkTable=null;
        try {
            frameworkTable = AndroidFrameworks.getLatest().getTableBlock();
        } catch (IOException e) {
        }
        android_table=frameworkTable;
        return android_table;
    }
}
