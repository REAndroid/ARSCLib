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
import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.item.StringReference;

import java.io.IOException;

public class ProfileDataHeader extends FixedBlockContainer {

    public final IntegerReference classSetSize;
    public final IntegerReference hotMethodRegionSize;
    // CRC32 sum of full dex bytes
    public final IntegerReference dexChecksum;
    public final IntegerReference numMethodIds;

    public final StringReference name;

    public ProfileDataHeader() {
        super(6);
        ShortItem dexNameSize = new ShortItem();
        this.classSetSize = new ShortItem();
        this.hotMethodRegionSize = new IntegerItem();
        this.dexChecksum = new IntegerItem();
        this.numMethodIds = new IntegerItem();
        this.name = new ProfString(dexNameSize);

        addChild(0, dexNameSize);
        addChild(1, (Block) classSetSize);
        addChild(2, (Block) hotMethodRegionSize);
        addChild(3, (Block) dexChecksum);
        addChild(4, (Block) numMethodIds);
        addChild(5, (Block) name);
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
    }

    @Override
    public String toString() {
        return "ProfileDataHeader{" +
                "name=" + name +
                ", classSetSize=" + classSetSize +
                ", hotMethodRegionSize=" + hotMethodRegionSize +
                ", dexChecksum=" + dexChecksum +
                ", numMethodIds=" + numMethodIds +
                '}';
    }

    public static final Creator<ProfileDataHeader> CREATOR = ProfileDataHeader::new;
}
