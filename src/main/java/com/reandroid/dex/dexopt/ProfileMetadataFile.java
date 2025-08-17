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

import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.ObjectsUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ProfileMetadataFile extends ProfileFile {

    public final ProfileMagic magic;
    public final ProfileVersion version;
    public final ProfileMetadataBody body;

    public ProfileMetadataFile() {
        super(6);
        this.magic = new ProfileMagic();
        ProfileVersion version = new ProfileVersion();
        this.version = version;
        ByteOrShortItem count = new ByteOrShortItem(version().isMetadataV001());
        IntegerItem unCompressedSize = version.newBodySize();
        IntegerItem compressedSize = version.newBodySize();
        this.body = new ProfileMetadataBody(version, count, unCompressedSize, compressedSize);

        addChild(0, magic);
        addChild(1, version);
        addChild(2, count);
        addChild(3, unCompressedSize);
        addChild(4, compressedSize);
        addChild(5, body);

        magic.set(ProfileMagic.MAGIC_PROFM);
        version.set(ProfileVersion.METADATA_V001_N);
    }

    @Override
    public ProfileMagic magic() {
        return magic;
    }
    @Override
    public ProfileVersion version() {
        return version;
    }
    @Override
    public ProfileMetadataBody body() {
        return body;
    }

    @Override
    public ProfileMetadata get(String name) {
        return (ProfileMetadata) super.get(name);
    }
    @Override
    public Iterator<ProfileMetadata> iterator() {
        return ObjectsUtil.cast(super.iterator());
    }

    public static ProfileMetadataFile read(File file) throws IOException {
        ProfileMetadataFile profileFile = new ProfileMetadataFile();
        profileFile.readBytes(new BlockReader(file));
        return profileFile;
    }
    public static ProfileMetadataFile read(InputStream inputStream) throws IOException {
        ProfileMetadataFile profileFile = new ProfileMetadataFile();
        profileFile.readBytes(new BlockReader(inputStream));
        return profileFile;
    }
}
