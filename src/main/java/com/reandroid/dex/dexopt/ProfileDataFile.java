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

import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteItem;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.utils.Crc32;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

// https://android.googlesource.com/platform/frameworks/support/+/refs/heads/androidx-main/profileinstaller/profileinstaller/src/main/java/androidx/profileinstaller/ProfileTranscoder.java
public class ProfileDataFile extends ProfileFile {

    private final ProfileMagic magic;
    private final ProfileVersion version;
    private final ProfileDataBody body;

    public ProfileDataFile() {
        super(6);
        this.magic = new ProfileMagic();
        ProfileVersion version = new ProfileVersion();
        this.version = version;
        ByteItem count = new ByteItem();
        IntegerItem unCompressedSize = version.newBodySize();
        IntegerItem compressedSize = version.newBodySize();
        this.body = new ProfileDataBody(version, count, unCompressedSize, compressedSize);

        addChild(0, magic);
        addChild(1, version);
        addChild(2, count);
        addChild(3, unCompressedSize);
        addChild(4, compressedSize);
        addChild(5, body);

        magic.set(ProfileMagic.MAGIC_PROF);
        version.set(ProfileVersion.V010_P);
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
    public ProfileDataBody body() {
        return body;
    }

    @Override
    public DexProfileData get(String name) {
        return (DexProfileData) super.get(name);
    }
    @Override
    public Iterator<DexProfileData> iterator() {
        return body().iterator();
    }

    public void updateFileChecksum(File file) throws IOException {
        DexProfileData data = get(file.getName());
        if (data != null) {
            data.setChecksum(Crc32.of(file));
        }
    }
    public void updateChecksum(ZipEntryMap zipEntryMap) throws IOException {
        removeIfName(name -> !zipEntryMap.contains(name));
        Iterator<DexProfileData> iterator = iterator();
        while (iterator.hasNext()) {
            DexProfileData data = iterator.next();
            InputSource inputSource = zipEntryMap.getInputSource(data.getName());
            data.setChecksum(inputSource.getCrc());
        }
    }
    public static ProfileDataFile read(File file) throws IOException {
        ProfileDataFile profileDataFile = new ProfileDataFile();
        profileDataFile.readBytes(new BlockReader(file));
        return profileDataFile;
    }
    public static ProfileDataFile read(InputStream inputStream) throws IOException {
        ProfileDataFile profileDataFile = new ProfileDataFile();
        profileDataFile.readBytes(new BlockReader(inputStream));
        return profileDataFile;
    }

    public static void update(ZipEntryMap apk) throws IOException {
        InputSource source = apk.getInputSource(PATH_PROF);
        if (source == null) {
            return;
        }
        ProfileDataFile profileDataFile = ProfileDataFile.read(source.openStream());
        profileDataFile.updateChecksum(apk);
        profileDataFile.refresh();
        InputSource update = new ByteInputSource(profileDataFile.getBytes(), PATH_PROF);
        update.copyAttributes(source);
        apk.add(update);
    }
}
