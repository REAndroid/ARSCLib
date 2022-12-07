package com.reandroid.lib.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;

import java.io.File;

public class JsonManifestInputSource extends JsonXmlInputSource {
    public JsonManifestInputSource(InputSource inputSource) {
        super(inputSource);
    }
    AndroidManifestBlock newInstance(){
        return new AndroidManifestBlock();
    }
    public static JsonManifestInputSource fromFile(File rootDir, File jsonFile){
        String path=ApkUtil.toArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource=new FileInputSource(jsonFile, path);
        return new JsonManifestInputSource(fileInputSource);
    }
}
