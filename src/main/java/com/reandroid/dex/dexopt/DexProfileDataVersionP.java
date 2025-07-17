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

public class DexProfileDataVersionP extends DexProfileData {

    private final ProfileDataHeader header;

    private final HotMethodRegionList hotMethodList;
    private final ProfileClassList classList;
    private final MethodBitmap methodBitmap;

    public DexProfileDataVersionP(ProfileDataHeader header) {
        super(3);
        this.header = header;
        this.hotMethodList = new HotMethodRegionList(header.hotMethodRegionSize);
        this.classList = new ProfileClassList(header.classSetSize);
        this.methodBitmap = new MethodBitmap(header.numMethodIds);

        addChild(0, hotMethodList);
        addChild(1, classList);
        addChild(2, methodBitmap);
    }

    @Override
    public String getName() {
        return header().name.get();
    }
    @Override
    public void setName(String name) {
        header().name.set(name);
    }

    public ProfileDataHeader header() {
        return header;
    }

    @Override
    public long getChecksum() {
        return header.dexChecksum.get() & 0xffffffffL;
    }
    @Override
    public void setChecksum(long crc32) {
        header().dexChecksum.set((int) crc32);
    }

    @Override
    public HotMethodRegionList hotMethodList() {
        return hotMethodList;
    }
    @Override
    public ProfileClassList classList() {
        return classList;
    }
    @Override
    public MethodBitmap methodBitmap() {
        return methodBitmap;
    }
}
