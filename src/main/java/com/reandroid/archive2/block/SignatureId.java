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

import java.util.Objects;

public class SignatureId {
    private final String name;
    private final int id;

    private SignatureId(String name, int id) {
        this.name = name;
        this.id = id;
    }
    public String name() {
        return name;
    }
    public int getId() {
        return id;
    }
    public String toFileName() {
        if (this.name != null) {
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
        SignatureId that = (SignatureId) obj;
        return id == that.id;
    }
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    @Override
    public String toString() {
        String name = this.name;
        if (name != null) {
            return name;
        }
        return "UNKNOWN(" + String.format("0x%08x", id) + ")";
    }
    public static SignatureId valueOf(String name) {
        if (name == null) {
            return null;
        }
        String ext = FILE_EXTENSION;
        if (name.endsWith(ext)) {
            name = name.substring(0, name.length() - ext.length());
        }
        for (SignatureId signatureId : VALUES) {
            if (name.equalsIgnoreCase(signatureId.name())) {
                return signatureId;
            }
        }
        if (ValueDecoder.isHex(name)) {
            return new SignatureId(null, ValueDecoder.parseHex(name));
        }
        return null;
    }
    public static SignatureId valueOf(int id) {
        for (SignatureId signatureId : VALUES) {
            if (id == signatureId.getId()) {
                return signatureId;
            }
        }
        return new SignatureId(null, id);
    }

    public static SignatureId[] values() {
        return VALUES.clone();
    }

    public static final SignatureId V2 = new SignatureId("V2", 0x7109871A);
    public static final SignatureId V3 = new SignatureId("V3", 0xF05368C0);
    public static final SignatureId V31 = new SignatureId("V31", 0x1B93AD61);
    public static final SignatureId STAMP_V1 = new SignatureId("STAMP_V1", 0x2B09189E);
    public static final SignatureId STAMP_V2 = new SignatureId("STAMP_V2", 0x6DFF800D);
    public static final SignatureId PADDING = new SignatureId("PADDING", 0x42726577);
    public static final SignatureId NULL = new SignatureId("NULL", 0x0);

    private static final SignatureId[] VALUES = new SignatureId[]{
            V2, V3, V31, STAMP_V1, STAMP_V2, PADDING, NULL
    };

    private static final String FILE_EXTENSION = ".bin";
}
