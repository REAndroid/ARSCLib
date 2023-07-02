package com.reandroid.apk;

import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.util.FileUtil;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApkModuleCoderTest {
    private static ApkModule apkModule;
    private static File mDir;
    @Test
    public void a_testDecodeToXml() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        File dir = FileUtil.getTempDir();
        dir = new File(dir, "decode_xml");
        ApkModuleXmlDecoder decoder = new ApkModuleXmlDecoder(apkModule);
        decoder.decode(dir);
        mDir = dir;
    }
    @Test
    public void b_testEncodeXml() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        ApkModuleXmlEncoder encoder = new ApkModuleXmlEncoder();
        encoder.scanDirectory(mDir);
        ApkModule apkModule_encoded = encoder.getApkModule();
        Assert.assertNotNull(apkModule_encoded.getAndroidManifestBlock());
        apkModule_encoded.getAndroidManifestBlock().refreshFull();
        apkModule.getAndroidManifestBlock().removeUnusedNamespaces();
        apkModule.getAndroidManifestBlock().getStringPool().removeUnusedStrings();
        apkModule.getAndroidManifestBlock().refreshFull();
        File apk = new File(mDir.getParentFile(), "encoded_xml.apk");
        apkModule_encoded.writeApk(apk);
        apkModule_encoded = ApkModule.loadApkFile(apk);
        FileUtil.deleteDirectory(mDir);
        Assert.assertFalse("Failed to delete: " + mDir, mDir.exists());
        compare(apkModule, apkModule_encoded);
        apk.delete();
    }
    private void compare(ApkModule module1, ApkModule module2) throws IOException {
        Assert.assertEquals(module1.getApkArchive().size(), module2.getApkArchive().size());

        Assert.assertArrayEquals(module1.getTableBlock().getBytes(), module2.getTableBlock().getBytes());

        compareManifest(module1.getAndroidManifestBlock(), module2.getAndroidManifestBlock());
    }
    private void compareManifest(AndroidManifestBlock manifest1, AndroidManifestBlock manifest2) throws IOException {
        String xml1 = manifest1.serializeToXml();
        String xml2 = manifest2.serializeToXml();
        Assert.assertEquals(xml1, xml2);
    }
    private ApkModule getApkModule() throws IOException {
        ApkModule apkModule = ApkModuleCoderTest.apkModule;
        if(apkModule != null){
            return apkModule;
        }
        apkModule = ApkModuleTest.getLastApkModule();
        if(apkModule == null){
            ApkModuleTest apkModuleTest = new ApkModuleTest();
            apkModule = apkModuleTest.createApkModule();
        }
        ApkModuleCoderTest.apkModule = apkModule;
        return apkModule;
    }
}
