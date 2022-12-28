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
package com.reandroid.lib.common;

import com.reandroid.lib.arsc.util.FrameworkTable;

import java.io.IOException;
import java.io.InputStream;

public class Frameworks {
    private static FrameworkTable android_table;
    private static boolean load_once;
    public static FrameworkTable getAndroid(){
        if(android_table!=null || load_once){
            return android_table;
        }
        load_once=true;
        FrameworkTable frameworkTable=null;
        try {
            frameworkTable = loadFramework(ANDROID_FRAMEWORK_33);
        } catch (IOException e) {
        }
        android_table=frameworkTable;
        return android_table;
    }
    private static FrameworkTable loadFramework(String name) throws IOException {
        InputStream inputStream=Frameworks.class.getResourceAsStream(name);
        if(inputStream==null){
            return null;
        }
        FrameworkTable frameworkTable=new FrameworkTable();
        frameworkTable.readBytes(inputStream);
        return frameworkTable;
    }

    private static final String ANDROID_FRAMEWORK_33 = "/fwk/android_resources_33.arsc";
}
