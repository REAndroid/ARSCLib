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
package com.reandroid.utils.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public class IOUtil {
    public static String shortPath(File file, int depth){
        File tmp = file;
        while (depth > 0){
            File dir = tmp.getParentFile();
            if(dir == null){
                break;
            }
            tmp = dir;
            depth --;
        }
        if(file == tmp){
            return file.getName();
        }
        int i = tmp.getAbsolutePath().length() + 1;
        return file.getAbsolutePath().substring(i);
    }
    public static void close(Object obj) throws IOException {
        if(obj instanceof Closeable){
            ((Closeable)obj).close();
        }
    }
}
