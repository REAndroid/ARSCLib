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

import com.reandroid.utils.io.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SmaliReader {
    private final byte[] array;
    private final int start;
    private final int length;
    private int position;

    private int lineNumber;
    private int columnNumber;

    private String path;

    public SmaliReader(byte[] array, int start, int length) {
        this.array = array;
        this.start = start;
        this.length = length;
    }
    public SmaliReader(byte[] array) {
        this(array, 0, array.length);
    }

    public int position() {
        return position;
    }
    public void position(int position) {
        this.position = position;
    }
    public int available() {
        return this.length - this.position;
    }
    public boolean finished() {
        return available() == 0;
    }
    public boolean hasMore() {
        return available() != 0;
    }
    public void offset(int amount){
        position(position() + amount);
    }
    public byte get(){
        return get(position());
    }
    public char getASCII(int i){
        int c = get(i) & 0xff;
        return (char) c;
    }
    public byte get(int i){
        return array[this.start + i];
    }
    public char readASCII(){
        int i = read() & 0xff;
        return (char) i;
    }
    public byte read() {
        int i = start + position;
        position ++;
        return this.array[i];
    }
    public String getString(int length){
        return new String(getBytes(length), StandardCharsets.UTF_8);
    }
    public String readString(int length){
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }
    public String readStringForNumber(){
        int pos = position();
        int end = indexOfLineEnd();
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(!isNumber(b)){
                break;
            }
            char ch = (char) (0xff & b);
            builder.append(ch);
            count ++;
        }
        skip(count);
        return builder.toString();
    }

    public int readInteger() throws IOException{
        StringBuilder builder = new StringBuilder();
        int count = 0;
        byte signByte = get();
        boolean negative = signByte == '-';
        if(negative || signByte == '+'){
            count ++;
        }
        int pos = position() + count;
        int end = pos + available();
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(!isDigit(b)){
                break;
            }
            char ch = (char) b;
            builder.append(ch);
            count ++;
        }
        offset(count);
        if(count == 0){
            throw new SmaliParseException("Not an integer character '" + getASCII(position())+ "'", this);
        }
        int value;
        try{
            value = Integer.parseInt(builder.toString());
        }catch (NumberFormatException ex){
            throw new SmaliParseException(ex.getMessage(), this);
        }
        if(negative){
            value = -value;
        }
        return value;
    }
    public String readDigits(){
        StringBuilder builder = new StringBuilder();
        int pos = position();
        int end = pos + available();
        int count = 0;
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(!isDigit(b)){
                break;
            }
            char ch = (char) b;
            builder.append(ch);
            count ++;
        }
        offset(count);
        return builder.toString();
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
        return startsWith(bytes, position());
    }
    public boolean startsWith(byte[] bytes, int start){
        int length = available();
        if(length < bytes.length){
            return false;
        }
        length = bytes.length;
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(start + i)){
                return false;
            }
        }
        return true;
    }
    public int startsWithSqueezeSpaces(byte[] bytes){
        int pos = position();
        int length = available();
        int bytesLength = bytes.length;
        if(length == 0 || bytesLength == 0){
            return -1;
        }
        int index = 0;
        boolean prevSpace = false;
        for(int i = 0; i < length; i++){
            byte b1 = get(pos + i);
            if(b1 == ' '){
                if(prevSpace){
                    continue;
                }
                prevSpace = true;
            }else {
                prevSpace = false;
            }
            if(index == bytesLength){
                return i;
            }
            byte b2 = bytes[index];
            index ++;
            if(b1 != b2){
                return -1;
            }
        }
        return -1;
    }
    public int indexOf(char ch){
        return indexOf((byte) ch);
    }
    public int indexOfWhiteSpace(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isWhiteSpace(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOfWhiteSpaceOrComment(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isWhiteSpaceOrComment(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOfLineEnd(){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(isLineEnd(get(i))){
                return i;
            }
        }
        return end;
    }
    public int indexOf(byte b){
        return indexOf(position(), b);
    }
    public int indexOf(int start, byte b){
        int end = start + available();
        for(int i = start; i < end; i++){
            if(b == get(i)){
                return i;
            }
        }
        return -1;
    }
    public int indexOfBeforeLineEnd(char ch){
        int pos = position();
        int end = pos + available();
        for(int i = pos; i < end; i++){
            byte b = get(i);
            if(ch == b){
                return i;
            }
            if(isLineEnd(b)){
                return -1;
            }
        }
        return -1;
    }
    public int indexOf(byte[] bytes){
        int length = bytes.length;
        if(length == 0){
            return -1;
        }
        int pos = position();
        int end = pos + available() - length;
        for(int i = pos; i <= end; i++){
            if(equalsAt(i, bytes)){
                return i;
            }
        }
        return -1;
    }
    private boolean equalsAt(int index, byte[] bytes){
        int length = bytes.length;
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(i + index)){
                return false;
            }
        }
        return true;
    }
    public boolean skipWhitespacesOrComment(){
        boolean result = false;
        if(get() == '#'){
            nextLine();
            result = true;
        }
        while (skipWhitespaces()){
            if(get() == '#'){
                nextLine();
            }
            result = true;
        }
        return result;
    }
    public boolean skipWhitespaces(){
        int pos = position();
        int nextPosition = pos;
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(!isWhiteSpace(get(i))){
                nextPosition = i;
                break;
            }
        }
        if(nextPosition != pos){
            position(nextPosition);
            return true;
        }
        return false;
    }
    public boolean isLineEnd(){
        if(finished()){
            return true;
        }
        return isLineEnd(get());
    }
    public boolean skipSpaces(){
        int pos = position();
        int nextPosition = pos;
        int end = pos + available();
        for(int i = pos; i < end; i++){
            if(!isSpace(get(i))){
                nextPosition = i;
                break;
            }
        }
        if(nextPosition != pos){
            position(nextPosition);
            return true;
        }
        return false;
    }
    public void nextLine(){
        int i = indexOf('\n');
        if(i < 0){
            i = position() + available();
        }
        position(i);
    }
    public void skipCharWithSpaces(char ch){
        skipSpaces();
        if(hasMore() && get() == ch){
            skip(1);
            skipSpaces();
        }
    }
    public void skipChar(char ch){
        if(hasMore() && get() == ch){
            skip(1);
        }
    }
    public void skip(int amount){
        int available = available();
        if(amount > available){
            amount = available;
        }
        position(amount + position());
    }

    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    private void updatePositions(){
        int pos = position();
        int line = 1;
        int column = 1;
        for(int i = 0; i < pos; i++){
            if(get(i) == '\n'){
                column = 1;
                line ++;
            }else {
                column ++;
            }
        }
        this.lineNumber = line;
        this.columnNumber = column;
    }
    public String getPositionPointer() {
        StringBuilder builder = new StringBuilder();
        int lineStart = position();
        while (get(lineStart) != '\n'){
            if(lineStart == 0){
                break;
            }
            lineStart --;
        }
        if(get(lineStart) == '\n'){
            lineStart ++;
        }
        int end = indexOf(lineStart, (byte) '\n');
        int pos = position();
        if(end - pos > 100){
            end = pos + 100;
        }
        for(int i = lineStart; i < end; i++){
            builder.append(getASCII(i));
        }
        builder.append('\n');
        for(int i = lineStart; i < pos; i++){
            builder.append(' ');
        }
        builder.append('^');

        return builder.toString();
    }
    public String getPositionLabel() {
        updatePositions();
        return "[" + lineNumber + ", " + columnNumber + "]";
    }
    @Override
    public String toString() {
        return getPositionPointer();
    }
    public String toString1() {
        int length = 10;
        if(length > available()){
            length = available();
        }
        return position() + "/" + available() + " '" + getString(length) + "'";
    }

    public static boolean isWhiteSpaceOrComment(byte b){
        return isWhiteSpace(b) || b == '#';
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
    public static boolean isSpace(byte b){
        switch (b){
            case ' ':
            case '\t':
                return true;
            default:
                return false;
        }
    }
    private static boolean isDigit(byte b){
        return b >= '0' && b <= '9';
    }
    private static boolean isNumber(byte b){
        if(b >= '0' && b <= '9'){
            return true;
        }
        if(b >= 'a' && b <= 'z'){
            return true;
        }
        if(b >= 'A' && b <= 'z'){
            return true;
        }
        switch (b){
            case '-':
            case '+':
            case '.':
                return true;
            default:
                return false;
        }
    }
    private static boolean isLineEnd(byte b){
        switch (b){
            case '\n':
            case '\r':
            case '#':
                return true;
            default:
                return false;
        }
    }
    public static SmaliReader of(String text){
        return new SmaliReader(text.getBytes(StandardCharsets.UTF_8));
    }
    public static SmaliReader of(File file) throws IOException {
        SmaliReader reader = new SmaliReader(IOUtil.readFully(file));
        reader.setPath(file.getPath());
        return reader;
    }
}
