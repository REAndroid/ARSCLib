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
package com.reandroid.archive2.block;

import com.reandroid.arsc.decoder.ValueDecoder;

import java.io.*;
import java.util.Objects;

public class ApkSignature extends ZipBlock{
    public ApkSignature() {
        super(MIN_SIZE);
    }
    public boolean isValid(){
        return getSigSize() > MIN_SIZE && getId() != null;
    }
    @Override
    public int readBytes(InputStream inputStream) throws IOException {
        setBytesLength(8, false);
        byte[] bytes = getBytesInternal();
        int read = inputStream.read(bytes, 0, bytes.length);
        if(read != bytes.length){
            return read;
        }
        int offset = bytes.length;
        int length = (int) getSigSize();
        setSigSize(length);

        bytes = getBytesInternal();
        int dataRead = inputStream.read(bytes, offset, length);
        if(dataRead != length){
            setSigSize(0);
        }
        return read + dataRead;
    }
    public long getSigSize(){
        return getLong(OFFSET_size);
    }
    public void setSigSize(long size){
        int length = (int) (OFFSET_id + size);
        setBytesLength(length, false);
        putLong(OFFSET_size, size);
    }
    public int getIdValue(){
        return getInteger(OFFSET_id);
    }
    public Id getId(){
        return Id.valueOf(getIdValue());
    }
    public void setId(int id){
        putInteger(OFFSET_id, id);
    }
    public void setId(Id signatureId){
        setId(signatureId == null? 0 : signatureId.getId());
    }
    public byte[] getData(){
        return getBytes(OFFSET_data, (int) getSigSize() - 4, true);
    }
    public void setData(byte[] data){
        if(data == null){
            data = new byte[0];
        }
        setData(data, 0, data.length);
    }
    public void setData(byte[] data, int offset, int length){
        setSigSize(length);
        putBytes(data, offset, OFFSET_data, data.length);
    }
    public void writeData(OutputStream outputStream) throws IOException{
        byte[] bytes = getBytesInternal();
        outputStream.write(bytes, OFFSET_data, (int) (getSigSize() - 4));
    }
    public void writeData(File file) throws IOException{
        File dir = file.getParentFile();
        if(dir != null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream = new FileOutputStream(file);
        writeData(outputStream);
        outputStream.close();
    }
    public File writeToDir(File dir) throws IOException{
        File file = new File(dir, getId().toFileName());
        writeData(file);
        return file;
    }
    @Override
    public String toString() {
        return getId() + ", size=" + getSigSize();
    }


    public static class Id {
        private final String name;
        private final int id;

        private Id(String name, int id) {
            this.name = name;
            this.id = id;
        }
        public String name() {
            return name;
        }
        public int getId() {
            return id;
        }
        public String toFileName(){
            if(this.name != null){
                return name + FILE_EXTENSION;
            }
            return String.format("0x%08x", id) + FILE_EXTENSION;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Id that = (Id) obj;
            return id == that.id;
        }
        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
        @Override
        public String toString(){
            String name = this.name;
            if(name != null){
                return name;
            }
            return "UNKNOWN("+String.format("0x%08x", id)+")";
        }
        public static Id valueOf(String name){
            if(name == null){
                return null;
            }
            String ext = FILE_EXTENSION;
            if(name.endsWith(ext)){
                name = name.substring(0, name.length()-ext.length());
            }
            for(Id signatureId:VALUES){
                if(name.equalsIgnoreCase(signatureId.name())){
                    return signatureId;
                }
            }
            if(ValueDecoder.isHex(name)){
                return new Id(null, ValueDecoder.parseHex(name));
            }
            return null;
        }
        public static Id valueOf(int id){
            if(id == 0){
                return null;
            }
            for(Id signatureId:VALUES){
                if(id == signatureId.getId()){
                    return signatureId;
                }
            }
            return new Id(null, id);
        }
        public static Id[] values() {
            return VALUES.clone();
        }
        public static final Id V2 = new Id("V2", 0x7109871A);
        public static final Id V3 = new Id("V3",0xF05368C0);
        public static final Id V31 = new Id("V31",0x1B93AD61);
        public static final Id STAMP_V1 = new Id("STAMP_V1", 0x2B09189E);
        public static final Id STAMP_V2 = new Id("STAMP_V2", 0x6DFF800D);
        public static final Id PADDING = new Id("PADDING", 0x42726577);


        private static final Id[] VALUES = new Id[]{
                V2, V3, V31, STAMP_V1, STAMP_V2, PADDING
        };

        private static final String FILE_EXTENSION = ".bin";
    }

    public static final int MIN_SIZE = 12;

    private static final int OFFSET_size = 0;
    private static final int OFFSET_id = 8;
    private static final int OFFSET_data = 12;
}
