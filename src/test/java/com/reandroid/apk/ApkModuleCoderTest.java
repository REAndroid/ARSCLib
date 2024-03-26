package com.reandroid.apk;

import com.reandroid.TestUtils;
import com.reandroid.arsc.base.BlockDiff;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.arsc.value.ResValue;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.xml.StyleDocument;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApkModuleCoderTest {
    private static ApkModule apkModule;
    private static File mDir_xml;
    private static File mDir_json;
    @Test
    public void a_testDecodeToXml() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        File dir = TestUtils.getTempDir();
        dir = new File(dir, "decode_xml");
        ApkModuleXmlDecoder decoder = new ApkModuleXmlDecoder(apkModule);
        decoder.decode(dir);
        mDir_xml = dir;
    }
    @Test
    public void b_testEncodeXml() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        ApkModuleXmlEncoder encoder = new ApkModuleXmlEncoder();
        encoder.scanDirectory(mDir_xml);
        ApkModule apkModule_encoded = encoder.getApkModule();
        apkModule_encoded.getTableBlock().getStringPool().sort();
        Assert.assertNotNull(apkModule_encoded.getAndroidManifest());
        apkModule_encoded.getAndroidManifest().refreshFull();
        apkModule.getAndroidManifest().removeUnusedNamespaces();
        apkModule.getAndroidManifest().getStringPool().removeUnusedStrings();
        apkModule.getAndroidManifest().refreshFull();

        // Preserve generated apk for signing and install on device
        File apk = new File(TestUtils.getTesApkDirectory(), "encoded_xml.apk");

        apkModule_encoded.writeApk(apk);
        apkModule_encoded = ApkModule.loadApkFile(apk);
        TestUtils.log("Generated apk: " + apk.getAbsolutePath());

        FileUtil.deleteDirectory(mDir_xml);
        Assert.assertFalse("Failed to delete: " + mDir_xml, mDir_xml.exists());
        compare(apkModule, apkModule_encoded);
    }
    @Test
    public void c_testDecodeToJson() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        File dir = TestUtils.getTempDir();
        dir = new File(dir, "decode_json");
        ApkModuleJsonDecoder decoder = new ApkModuleJsonDecoder(apkModule);
        decoder.decode(dir);
        mDir_json = dir;
    }
    @Test
    public void d_testEncodeJson() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        ApkModuleJsonEncoder encoder = new ApkModuleJsonEncoder();
        encoder.scanDirectory(mDir_json);
        ApkModule apkModule_encoded = encoder.getApkModule();
        Assert.assertNotNull(apkModule_encoded.getAndroidManifest());
        apkModule_encoded.getAndroidManifest().refreshFull();
        apkModule.getAndroidManifest().removeUnusedNamespaces();
        apkModule.getAndroidManifest().getStringPool().removeUnusedStrings();
        apkModule.getAndroidManifest().refreshFull();

        // Preserve generated apk for signing and install on device
        File apk = new File(TestUtils.getTesApkDirectory(), "encoded_json.apk");

        apkModule_encoded.writeApk(apk);
        TestUtils.log("Generated apk: " + apk.getAbsolutePath());

        apkModule_encoded = ApkModule.loadApkFile(apk);
        FileUtil.deleteDirectory(mDir_json);
        Assert.assertFalse("Failed to delete: " + mDir_json, mDir_json.exists());
        compare(apkModule, apkModule_encoded);
    }
    private void compare(ApkModule module1, ApkModule module2) throws IOException {
        Assert.assertEquals(module1.getZipEntryMap().size(), module2.getZipEntryMap().size());

        compareTableBlock(module1.getTableBlock(), module2.getTableBlock());

        compareManifest(module1.getAndroidManifest(), module2.getAndroidManifest());
    }
    private void compareTableBlock(TableBlock tableBlock1, TableBlock tableBlock2) {

        tableBlock1.trimConfigSizes(ResConfig.SIZE_48);
        tableBlock2.trimConfigSizes(ResConfig.SIZE_48);

        tableBlock1.getStringPool().sort();
        tableBlock2.getStringPool().sort();

        tableBlock1.refreshFull();
        tableBlock2.refreshFull();

        BlockDiff.DiffResult[] diffResults = new BlockDiff(tableBlock1, tableBlock2).find();
        String message = BlockDiff.toString(diffResults);

        Assert.assertNull(message);
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
