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
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.BlockItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.ObjectsUtil;

import java.io.IOException;

public class ProfileMagic extends BlockItem implements IntegerReference {

    public ProfileMagic(int magic) {
        super(4);
        set(magic);
    }
    public ProfileMagic() {
        this(MAGIC_PROF);
    }

    public boolean isProf() {
        return MAGIC_PROF == get();
    }
    public boolean isProfM() {
        return MAGIC_PROFM == get();
    }

    @Override
    public int get() {
        return Block.getInteger(getBytesInternal(), 0);
    }
    @Override
    public void set(int value) {
        Block.putInteger(getBytesInternal(), 0, value);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        int value = get();
        if ((value == MAGIC_PROF) == (getParentInstance(ProfileDataFile.class) == null)) {
            throw new IOException("Invalid magic: " + HexUtil.toHex8(value));
        }
    }

    @Override
    public String toString() {
        int value = get();
        if (value == MAGIC_PROF) {
            return "pro\\0";
        }
        if (value == MAGIC_PROFM) {
            return "prm\\0";
        }
        return HexUtil.toHexString(getBytesInternal());
    }

    public static final int MAGIC_PROF = ObjectsUtil.of(0x006f7270);
    public static final int MAGIC_PROFM = ObjectsUtil.of(0x006d7270);
}
