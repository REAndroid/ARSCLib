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
package com.reandroid.lib.json;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    public static void readJSONObject(File file, JSONConvert<JSONObject> jsonConvert) throws IOException {
        FileInputStream inputStream=new FileInputStream(file);
        readJSONObject(inputStream, jsonConvert);
        inputStream.close();
    }
    public static void readJSONObject(InputStream inputStream, JSONConvert<JSONObject> jsonConvert){
        InputStreamReader reader=new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        readJSONObject(reader, jsonConvert);
    }
    public static void readJSONObject(Reader reader, JSONConvert<JSONObject> jsonConvert){
        JSONObject jsonObject=new JSONObject(new JSONTokener(reader));
        jsonConvert.fromJson(jsonObject);
    }

    public static void readJSONArray(File file, JSONConvert<JSONArray> jsonConvert) throws IOException {
        FileInputStream inputStream=new FileInputStream(file);
        readJSONArray(inputStream, jsonConvert);
        inputStream.close();
    }
    public static void readJSONArray(InputStream inputStream, JSONConvert<JSONArray> jsonConvert){
        InputStreamReader reader=new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        readJSONArray(reader, jsonConvert);
    }
    public static void readJSONArray(Reader reader, JSONConvert<JSONArray> jsonConvert){
        JSONArray jsonObject=new JSONArray(new JSONTokener(reader));
        jsonConvert.fromJson(jsonObject);
    }

}
