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
package com.reandroid.dex.smali;

import java.nio.charset.StandardCharsets;

public class SmaliReader {
    private final byte[] array;
    private int position;

    public SmaliReader(byte[] array) {
        this.array = array;
    }

    public int position() {
        return position;
    }
    public void position(int position) {
        this.position = position;
    }
    public int length() {
        return array.length;
    }
    public void offset(int amount){
        position(position() + amount);
    }
    public byte get(){
        return get(position());
    }
    public byte get(int i){
        return array[i];
    }
    public byte read() {
        return this.array[position++];
    }
    public String readString(int length){
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }
    public byte[] readBytes(int length){
        byte[] bytes = getBytes(length);
        offset(length);
        return bytes;
    }

    public byte[] getBytes(int length){
        byte[] result = new byte[length];
        int pos = position();
        for(int i = 0; i < length; i++){
            result[i] = get(pos + i);
        }
        return result;
    }
    public boolean startsWith(byte[] bytes){
        int pos = position();
        int length = length() - pos;
        if(length < bytes.length){
            return false;
        }
        length = bytes.length;
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(pos + i)){
                return false;
            }
        }
        return true;
    }
    public int indexOf(char ch){
        return indexOf((byte) ch);
    }
    public int indexOfWhiteSpace(){
        int pos = position();
        int length = length();
        for(int i = pos; i < length; i++){
            if(isWhiteSpace(get(i))){
                return i;
            }
        }
        return length;
    }
    public int indexOf(byte b){
        int pos = position();
        int length = length();
        for(int i = pos; i < length; i++){
            if(b == get(i)){
                return i;
            }
        }
        return -1;
    }
    public void skipWhitespaces(){
        int pos = position();
        int length = length();
        for(int i = pos; i < length; i++){
            if(!isWhiteSpace(get(i))){
                pos = i;
                break;
            }
        }
        position(pos);
    }
    public void skip(int amount){
        int available = length() - position();
        if(amount > available){
            amount = available;
        }
        position(amount + position());
    }

    @Override
    public String toString() {
        return position() + "/" + length();
    }

    public static boolean isWhiteSpace(byte b){
        switch (b){
            case ' ':
            case '\n':
            case '\t':
            case '\r':
                return true;
            default:
                return false;
        }
    }
    public static SmaliReader of(String text){
        return new SmaliReader(text.getBytes(StandardCharsets.UTF_8));
    }
}
