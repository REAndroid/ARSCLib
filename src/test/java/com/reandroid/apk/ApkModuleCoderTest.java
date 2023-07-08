package com.reandroid.apk;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.value.ResConfig;
import com.reandroid.utils.io.FileUtil;
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
        File dir = FileUtil.getTempDir();
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
        Assert.assertNotNull(apkModule_encoded.getAndroidManifestBlock());
        apkModule_encoded.getAndroidManifestBlock().refreshFull();
        apkModule.getAndroidManifestBlock().removeUnusedNamespaces();
        apkModule.getAndroidManifestBlock().getStringPool().removeUnusedStrings();
        apkModule.getAndroidManifestBlock().refreshFull();
        File apk = new File(mDir_xml.getParentFile(), "encoded_xml.apk");
        apkModule_encoded.writeApk(apk);
        apkModule_encoded = ApkModule.loadApkFile(apk);
        FileUtil.deleteDirectory(mDir_xml);
        Assert.assertFalse("Failed to delete: " + mDir_xml, mDir_xml.exists());
        compare(apkModule, apkModule_encoded);
        apk.delete();
    }
    @Test
    public void c_testDecodeToJson() throws IOException {
        ApkModule apkModule = getApkModule();
        Assert.assertNotNull(apkModule);
        File dir = FileUtil.getTempDir();
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
        Assert.assertNotNull(apkModule_encoded.getAndroidManifestBlock());
        apkModule_encoded.getAndroidManifestBlock().refreshFull();
        apkModule.getAndroidManifestBlock().removeUnusedNamespaces();
        apkModule.getAndroidManifestBlock().getStringPool().removeUnusedStrings();
        apkModule.getAndroidManifestBlock().refreshFull();
        File apk = new File(mDir_json.getParentFile(), "encoded_json.apk");
        apkModule_encoded.writeApk(apk);
        apkModule_encoded = ApkModule.loadApkFile(apk);
        FileUtil.deleteDirectory(mDir_json);
        Assert.assertFalse("Failed to delete: " + mDir_json, mDir_json.exists());
        compare(apkModule, apkModule_encoded);
        apk.delete();
    }
    private void compare(ApkModule module1, ApkModule module2) throws IOException {
        Assert.assertEquals(module1.getApkArchive().size(), module2.getApkArchive().size());

        compareTableBlock(module1.getTableBlock(), module2.getTableBlock());

        compareManifest(module1.getAndroidManifestBlock(), module2.getAndroidManifestBlock());
    }
    private void compareTableBlock(TableBlock tableBlock1, TableBlock tableBlock2) {

        tableBlock1.trimConfigSizes(ResConfig.SIZE_48);
        tableBlock2.trimConfigSizes(ResConfig.SIZE_48);

        tableBlock1.getStringPool().sort();
        tableBlock2.getStringPool().sort();

        tableBlock1.refreshFull();
        tableBlock2.refreshFull();

        int position = findByteDifferencePosition(tableBlock1.getBytes(), tableBlock2.getBytes());
        if(position >= 0){
            position++;
            Block block1 = tableBlock1.locateBlock(position);
            Block block2 = tableBlock2.locateBlock(position);
            Assert.assertEquals("Difference at " + position, block1, block2);
        }
    }
    private int findByteDifferencePosition(byte[] bytes1, byte[] bytes2){
        Assert.assertEquals("TableBlock bytes", bytes1.length, bytes2.length);
        for(int i = 0; i < bytes1.length; i++){
            if(bytes1[i] != bytes2[i]){
                return i;
            }
        }
        return -1;
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
