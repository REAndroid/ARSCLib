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
package com.reandroid.dex.dexopt;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.BooleanReference;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

public class ProfileVersion extends BlockItem {

    public ProfileVersion() {
        super(4);
    }

    public int get() {
        return Block.getInteger(getBytesInternal(), 0);
    }
    public void set(int value) {
        Block.putInteger(getBytesInternal(), 0, value);
    }
    public void set(byte[] value) {
        System.arraycopy(value, 0, getBytesInternal(), 0, 4);
    }
    public String name() {
        int value = get();
        if (value == V015_S) {
            return "V015_S";
        }
        if (value == V010_P) {
            return "V010_P";
        }
        if (value == V009_O_MR1) {
            return "V009_O_MR1";
        }
        if (value == V005_O) {
            return "V005_O";
        }
        if (value == V001_N && getParentInstance(ProfileDataFile.class) != null) {
            return "V001_N";
        }
        if (value == METADATA_V001_N && getParentInstance(ProfileMetadataFile.class) != null) {
            return "METADATA_V001_N";
        }
        if (value == METADATA_V002) {
            return "METADATA_V002";
        }
        return HexUtil.toHex8(value);
    }
    public void name(String name) {
        int value;
        if ("V015_S".equals(name)) {
            value = V015_S;
        } else if ("V010_P".equals(name)) {
            value = V010_P;
        } else if ("V009_O_MR1".equals(name)) {
            value = V009_O_MR1;
        } else if ("V005_O".equals(name)) {
            value = V005_O;
        } else if ("V001_N".equals(name)) {
            value = V001_N;
        } else if ("METADATA_V001_N".equals(name)) {
            value = METADATA_V001_N;
        } else if ("METADATA_V002".equals(name)) {
            value = METADATA_V002;
        } else {
            throw new RuntimeException("Unknown version name: '" + name + "'");
        }
        set(value);
    }

    boolean hasDeflatedBody() {
        int v = get();
        return v == V010_P || v == METADATA_V001_N || v == METADATA_V002;
    }
    public IntegerItem newBodySize() {
        return new IntegerItem() {
            @Override
            public boolean isNull() {
                return super.isNull() || !ProfileVersion.this.hasDeflatedBody();
            }
        };
    }
    public BooleanReference isMetadataV001() {
        return new BooleanReference() {
            @Override
            public boolean get() {
                return ProfileVersion.this.get() == METADATA_V001_N;
            }
            @Override
            public void set(boolean value) {
            }
            @Override
            public String toString() {
                return Boolean.toString(get());
            }
        };
    }
    public BooleanReference isDeflatedBody() {
        return new BooleanReference() {
            @Override
            public boolean get() {
                return ProfileVersion.this.hasDeflatedBody();
            }
            @Override
            public void set(boolean value) {
            }
            @Override
            public String toString() {
                return Boolean.toString(get());
            }
        };
    }
    @Override
    public String toString() {
        return name();
    }

    public static final int V015_S = ObjectsUtil.of(0x00353130);
    public static final int V010_P = ObjectsUtil.of(0x00303130);
    public static final int V009_O_MR1 = ObjectsUtil.of(0x00393030);
    public static final int V005_O = ObjectsUtil.of(0x00353030);
    public static final int V001_N = ObjectsUtil.of(0x00313030);
    public static final int METADATA_V001_N = ObjectsUtil.of(0x00313030);
    public static final int METADATA_V002 = ObjectsUtil.of(0x00323030);
}
