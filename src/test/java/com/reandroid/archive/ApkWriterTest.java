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
    @Test
    public void testArchiveByteReadAndWriter() throws IOException {
        ApkModule apkModule_1 = getApkModule();
        Assert.assertNotNull("Null source ApkModule", apkModule_1);
        byte[] bytes = apkModule_1.writeApkBytes();
        ApkModule apkModule2 = ApkModule.readApkBytes(bytes);
        Assert.assertNotNull("Null ApkModule loaded from byte array", apkModule_1);
        AndroidManifestBlock manifestBlock = apkModule2.getAndroidManifest();
        Assert.assertNotNull("Null manifest block", manifestBlock);
        TableBlock tableBlock = apkModule2.getTableBlock();
        Assert.assertNotNull("Null table block", tableBlock);
        InputSource inputSource = apkModule2.getInputSource("classes.dex");
        Assert.assertNotNull("Null classes.dex input source", inputSource);
    }
    @Test
    public void testArchiveStreamWriter() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getApkModule().writeApk(outputStream);
        outputStream.close();

        ApkModule apkModule2 = ApkModule.readApkBytes(outputStream.toByteArray());

        AndroidManifestBlock manifestBlock = apkModule2.getAndroidManifest();
        Assert.assertNotNull("Missing manifest", manifestBlock);
        TableBlock tableBlock = apkModule2.getTableBlock();
        Assert.assertNotNull("Missing table",tableBlock);
        InputSource inputSource = apkModule2.getInputSource("classes.dex");
        Assert.assertNotNull("Missing classes.dex", inputSource);
    }

    private ApkModule getApkModule() throws IOException {
        ApkModuleTest apkModuleTest = new ApkModuleTest();
        return apkModuleTest.createApkModule();
    }
}
