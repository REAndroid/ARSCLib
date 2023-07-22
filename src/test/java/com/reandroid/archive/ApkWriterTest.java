package com.reandroid.archive;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ApkModuleTest;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ApkWriterTest {
    private static ApkModule apk_module;
    @Test
    public void testArchiveByteReadAndWriter() throws IOException {
        byte[] bytes = getApkModule().writeApkBytes();
        ApkModule apkModule2 = ApkModule.readApkBytes(bytes);
        AndroidManifestBlock manifestBlock = apkModule2.getAndroidManifestBlock();
        Assert.assertNotNull(manifestBlock);
        TableBlock tableBlock = apkModule2.getTableBlock();
        Assert.assertNotNull(tableBlock);
        InputSource inputSource = apkModule2.getInputSource("classes.dex");
        Assert.assertNotNull(inputSource);
    }
    @Test
    public void testArchiveStreamWriter() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getApkModule().writeApk(outputStream);
        outputStream.close();

        ApkModule apkModule2 = ApkModule.readApkBytes(outputStream.toByteArray());

        AndroidManifestBlock manifestBlock = apkModule2.getAndroidManifestBlock();
        Assert.assertNotNull("Missing manifest", manifestBlock);
        TableBlock tableBlock = apkModule2.getTableBlock();
        Assert.assertNotNull("Missing table",tableBlock);
        InputSource inputSource = apkModule2.getInputSource("classes.dex");
        Assert.assertNotNull("Missing classes.dex", inputSource);
    }

    private ApkModule getApkModule() throws IOException {
        ApkModule apkModule = ApkWriterTest.apk_module;
        if(apkModule != null){
            return apkModule;
        }
        apkModule = ApkModuleTest.getLastApkModule();
        if(apkModule == null){
            ApkModuleTest apkModuleTest = new ApkModuleTest();
            apkModule = apkModuleTest.createApkModule();
        }
        ApkWriterTest.apk_module = apkModule;
        return apkModule;
    }
}
