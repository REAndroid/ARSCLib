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
import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.arsc.item.ShortItem;
import com.reandroid.arsc.item.StringReference;


public class ProfileMetadataHeaderV1 extends FixedBlockContainer {

    public final StringReference name;
    public final IntegerReference classSetSize;

    public ProfileMetadataHeaderV1() {
        super(3);

        ShortItem dexNameSize = new ShortItem();
        this.name = new ProfString(dexNameSize);

        this.classSetSize = new ShortItem();

        addChild(0, dexNameSize);
        addChild(1, (Block) name);
        addChild(2, (Block) classSetSize);
    }

    @Override
    public String toString() {
        return "ProfileMetadataHeaderV1{" +
                "name=" + name +
                ", classSetSize=" + classSetSize +
                '}';
    }

    public static final Creator<ProfileMetadataHeaderV1> CREATOR = ProfileMetadataHeaderV1::new;
}
