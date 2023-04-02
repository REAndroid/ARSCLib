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
package com.reandroid.archive2.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ArchiveUtil {

    public static void writeAll(InputStream inputStream, OutputStream outputStream) throws IOException{
        int bufferLength = 1024 * 1000;
        byte[] buffer = new byte[bufferLength];
        int read;
        while ((read = inputStream.read(buffer, 0, bufferLength))>0){
            outputStream.write(buffer, 0, read);
        }
    }
    public static void skip(InputStream inputStream, long amount) throws IOException {
        if(amount==0){
            return;
        }
        int bufferLength = 1024*1024*100;
        if(bufferLength>amount){
            bufferLength = (int) amount;
        }
        byte[] buffer = new byte[bufferLength];
        int read;
        long remain = amount;
        while (remain > 0 && (read = inputStream.read(buffer, 0, bufferLength))>0){
            remain = remain - read;
            if(remain<bufferLength){
                bufferLength = (int) remain;
            }
        }
    }
}
