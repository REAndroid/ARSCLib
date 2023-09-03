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
package com.reandroid.archive;

import com.reandroid.archive.writer.ApkFileWriter;
import com.reandroid.archive.writer.ZipAligner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ApkArchive {

    public static void repack(File file) throws IOException {
        if(!file.isFile()){
            throw new FileNotFoundException("No such file: " + file);
        }
        File tmp = toTmpFile(file);
        try{
            repack(file, tmp);
        }catch (IOException ex){
            tmp.delete();
            throw ex;
        }
        file.delete();
        tmp.renameTo(file);
    }

    public static void repack(File zipFile, File outFile) throws IOException {
        if(zipFile.equals(outFile)){
            throw new IOException("Input and output are equal: " + zipFile);
        }
        ArchiveFile archiveFile = new ArchiveFile(zipFile);
        ZipEntryMap zipEntryMap = archiveFile.createZipEntryMap();
        zipEntryMap.autoSortApkFiles();
        ApkFileWriter writer = new ApkFileWriter(outFile, zipEntryMap.toArray());
        writer.setZipAligner(ZipAligner.apkAligner());
        writer.write();
        archiveFile.close();
    }
    private static File toTmpFile(File file){
        String name = file.getName() + ".repack.tmp";
        File dir = file.getParentFile();
        if(dir == null){
            return new File(name);
        }
        return new File(dir, name);
    }
}
