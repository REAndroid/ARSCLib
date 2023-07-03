package com.reandroid.apk;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.arsc.value.Entry;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class ApkModuleTest {
    private static ApkModule last_apkModule;
    @Test
    public void testApkModule() throws IOException {

        if(last_apkModule != null){
            return;
        }
        ApkModule apkModule = createApkModule();

        Assert.assertNotNull("Manifest block", apkModule.getAndroidManifestBlock());
        Assert.assertNotNull("Table block", apkModule.getTableBlock());

        ApkModuleXmlDecoder decoder = new ApkModuleXmlDecoder(apkModule);
        File dir = FileUtil.getTempDir();
        decoder.decode(dir);
    }
    public ApkModule createApkModule() throws IOException {

        ApkModule apkModule = new ApkModule();
        AndroidManifestBlock manifestBlock = createManifest();
        TableBlock tableBlock = createTableBlock(manifestBlock);

        apkModule.setManifest(manifestBlock);
        // apkModule.setTableBlock(tableBlock);
        //
        // Instead of setTableBlock add bytes to see if there is error
        ByteInputSource tableSource = new ByteInputSource(tableBlock.getBytes(),
                TableBlock.FILE_NAME);

        apkModule.add(tableSource);
        tableSource.setMethod(ZipEntry.STORED);

        apkModule.add(new ByteInputSource(EMPTY_DEX_FILE, "classes.dex"));

        last_apkModule = apkModule;

        return apkModule;
    }
    private TableBlock createTableBlock(AndroidManifestBlock manifestBlock){
        TableBlock tableBlock = new TableBlock();
        PackageBlock packageBlock = tableBlock.newPackage(
                0x7f, manifestBlock.getPackageName());
        String app_name = "ARSCLib Test";
        Entry appName = packageBlock.getOrCreate("", "string", "app_name");
        appName.setValueAsString(app_name);

        Assert.assertNotNull(appName.getResValue());
        Assert.assertEquals(app_name, appName.getResValue().getValueAsString());
        Assert.assertNotNull("Table search by name", tableBlock.getResource(
                packageBlock.getName(),
                appName.getTypeName(), appName.getName()));
        Assert.assertNotNull("Table search by name (no package)", tableBlock.getResource(
                null,
                appName.getTypeName(), appName.getName()));
        Assert.assertNotNull("Package search by name", packageBlock.getResource(
                appName.getTypeName(), appName.getName()));
        Assert.assertNotNull("Package search by id", packageBlock.getResource(appName.getResourceId()));
        Assert.assertNotNull("Table search by id", tableBlock.getResource(appName.getResourceId()));
        Assert.assertNotNull("getLocalResource by name", tableBlock.getLocalResource(
                appName.getTypeName(), appName.getName()));

        Entry appName_de = packageBlock.getOrCreate("-de-rDE", "string", "app_name");
        Assert.assertNotNull("Config entry - de", appName_de);
        appName_de.setValueAsString(app_name + " - Bewerbung");

        Assert.assertNotEquals(appName.getResConfig(), appName_de.getResConfig());
        Assert.assertNotEquals(appName.getResValue().getValueAsString(),
                appName_de.getResValue().getValueAsString());

        ResourceEntry resourceEntry = tableBlock.getLocalResource(appName_de.getResourceId());
        Assert.assertNotNull("getLocalResource by id", resourceEntry);
        Assert.assertEquals(appName.getName(), resourceEntry.getName());
        Assert.assertEquals(appName.getTypeName(), resourceEntry.getType());

        Entry appName_ru = resourceEntry.getOrCreate("-ru-rRU");
        Assert.assertNotNull("Config entry - ru", appName_ru);
        appName_ru.setValueAsString(app_name + " - заявление");

        Assert.assertNotEquals(appName_de.getResConfig(), appName_ru.getResConfig());
        Assert.assertNotEquals(appName_de.getResValue().getValueAsString(),
                appName_ru.getResValue().getValueAsString());

        Assert.assertEquals("Configs count", 3, resourceEntry.getConfigsCount());

        Assert.assertNull("Table search by error id",
                tableBlock.getResource(appName.getResourceId() + 1));
        Assert.assertNull("Table search by error name: ",
                tableBlock.getResource(null, "string", "app_name_error"));


        manifestBlock.setApplicationLabel(appName.getResourceId());

        Entry appIcon = packageBlock.getOrCreate("", "drawable", "ic_launcher");
        EncodeResult encodeResult = ValueCoder.encode("#006400");
        appIcon.setValueAsRaw(encodeResult.valueType, encodeResult.value);

        manifestBlock.setIconResourceId(appIcon.getResourceId());

        tableBlock.refreshFull();

        return tableBlock;
    }
    private AndroidManifestBlock createManifest() throws IOException {
        FrameworkApk frameworkApk = AndroidFrameworks.getLatest();
        AndroidManifestBlock manifestBlock = new AndroidManifestBlock();
        manifestBlock.setPackageName("com.reandroid.arsc");
        manifestBlock.setVersionCode(1);
        manifestBlock.setVersionName("1.0");
        manifestBlock.setCompileSdkVersion(frameworkApk.getVersionCode());
        manifestBlock.setCompileSdkVersionCodename(frameworkApk.getVersionName());
        manifestBlock.setPlatformBuildVersionCode(frameworkApk.getVersionCode());
        manifestBlock.setPlatformBuildVersionName(frameworkApk.getVersionName());

        manifestBlock.addUsesPermission("android.permission.INTERNET");
        manifestBlock.addUsesPermission("android.permission.READ_EXTERNAL_STORAGE");

        manifestBlock.getOrCreateMainActivity("android.app.Activity");

        manifestBlock.refresh();
        return manifestBlock;
    }
    public static ApkModule getLastApkModule(){
        return last_apkModule;
    }

    private static final byte[] EMPTY_DEX_FILE =  new byte[]{
            (byte)0x64, (byte)0x65, (byte)0x78, (byte)0x0A, (byte)0x30, (byte)0x33, (byte)0x35, (byte)0x00,
            (byte)0xE0, (byte)0x0E, (byte)0x82, (byte)0xEC, (byte)0xC5, (byte)0xCC, (byte)0x6A, (byte)0xFF,
            (byte)0x1E, (byte)0x65, (byte)0xE2, (byte)0x24, (byte)0x9A, (byte)0x48, (byte)0x13, (byte)0x52,
            (byte)0x4C, (byte)0xEE, (byte)0xA2, (byte)0xA1, (byte)0x71, (byte)0x9D, (byte)0x67, (byte)0xE6,
            (byte)0x9C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x78, (byte)0x56, (byte)0x34, (byte)0x12, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x74, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x2C, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x10, (byte)0x00, (byte)0x00,
            (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x70, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x74, (byte)0x00, (byte)0x00, (byte)0x00
    };
}
