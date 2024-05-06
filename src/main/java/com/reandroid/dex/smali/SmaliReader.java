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

import com.reandroid.utils.HexUtil;
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
    public String readEscapedString(char stopChar) throws IOException{
        StringBuilder builder = new StringBuilder();
        boolean skipped = false;
        while (true){
            if(finished()){
                skip(-1);
                throw new SmaliParseException("Missing character '" + stopChar + "'", this);
            }
            char ch = readASCII();
            if(skipped){
                builder.append(decodeSkipped(this, ch));
                skipped = false;
                continue;
            }
            if(ch == '\\'){
                skipped = true;
                continue;
            }
            if(ch == stopChar){
                skip(-1);
                break;
            }
            builder.append(ch);
        }
        return builder.toString();
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
        byte signByte = get();
        boolean negative = signByte == '-';
        if(negative || signByte == '+'){
            skip(1);
        }
        int result = 0;
        int pos = position();
        while (!finished()){
            int i = base10Digit(read());
            if(i == -1 || result < 0){
                skip(-1);
                break;
            }
            result = result * 10;
            result = result + i;
        }
        if(pos == position()){
            throw new SmaliParseException("Invalid integer format", this);
        }
        if(result < 0){
            skip(-1);
            throw new SmaliParseException("Integer overflow", this);
        }
        if(negative){
            result = -result;
        }
        return result;
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
        if(index == bytesLength) {
            return bytesLength;
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
        if(length > available() - index) {
            return false;
        }
        for(int i = 0; i < length; i++){
            if(bytes[i] != get(i + index)){
                return false;
            }
        }
        return true;
    }
    public boolean skipWhitespacesOrComment(){
        if(finished()) {
            return false;
        }
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
        if(finished()) {
            return false;
        }
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
        if(finished()){
            return "EOF";
        }
        StringBuilder builder = new StringBuilder();
        int pos = position();
        int lineStart = pos;
        while (get(lineStart) != '\n'){
            if(lineStart == 0){
                break;
            }
            lineStart --;
        }
        if(get(lineStart) == '\n'){
            lineStart ++;
        }
        int limit = 38;
        if(pos - lineStart > limit){
            lineStart = pos - limit;
        }
        int end = indexOf(lineStart, (byte) '\n');
        if(end < 0){
            if(pos == 0){
                end = lineStart;
            }else {
                end = pos;
            }
            end = end + available();
        }
        if(end - pos > limit){
            end = pos + limit;
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
    private static int base10Digit(byte b){
        int bound = '0';
        if(b >= bound && b <= '9'){
            return b - bound;
        }
        return -1;
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
    private static char decodeSkipped(SmaliReader reader, char ch){
        switch (ch){
            case 'b':
                return '\b';
            case 'f':
                return  '\f';
            case 'n':
                return '\n';
            case 'r':
                return  '\r';
            case 't':
                return '\t';
            case 'u':
                return decodeFourHex(reader);
            default:
                return ch;
        }
    }
    private static char decodeFourHex(SmaliReader reader){
        int i = HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        i = i << 4;
        i |= HexUtil.decodeHexChar(reader.read());
        return (char) i;
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
