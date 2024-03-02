package com.reandroid.apk;

import com.reandroid.TestUtils;
import com.reandroid.app.AndroidApiLevel;
import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.ArchiveBytes;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.*;
import com.reandroid.dex.SampleDexFileCreator;
import com.reandroid.dex.model.DexFile;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.xml.*;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApkModuleTest {
    private static ApkModule last_apkModule;
    @Test
    public void a_testApkModule() throws IOException {

        if(last_apkModule != null){
            return;
        }
        ApkModule apkModule = createApkModule();

        Assert.assertNotNull("Manifest block", apkModule.getAndroidManifest());
        Assert.assertNotNull("Table block", apkModule.getTableBlock());

        ApkModuleXmlDecoder decoder = new ApkModuleXmlDecoder(apkModule);
        File dir = TestUtils.getTempDir();
        decoder.decode(dir);
    }
    @Test
    public void b_testTypeIdOffset() throws IOException {
        InputStream inputStream = ApkModuleTest.class
                .getResourceAsStream("/type_id_offset.apk");
        ArchiveBytes archiveBytes = new ArchiveBytes(inputStream);
        ApkModule apkModule = new ApkModule(archiveBytes.createZipEntryMap());
        TableBlock tableBlock = apkModule.getTableBlock();
        PackageBlock packageBlock = tableBlock.pickOne();

        Assert.assertNotEquals(0, packageBlock.getTypeIdOffset());

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

        int mainActivityLayoutId = createMainActivityContentViewXml(apkModule);

        apkModule.getTableBlock().refreshFull();

        String appClass = manifestBlock.getApplicationClassName();
        String mainActivity = manifestBlock.getMainActivityClassName();

        DexFile dexFile = SampleDexFileCreator.createApplicationClass(appClass, mainActivity, mainActivityLayoutId);
        byte[] bytes = dexFile.getBytes();
        apkModule.add(new ByteInputSource(bytes, "classes.dex"));

        last_apkModule = apkModule;

        return apkModule;
    }
    private int createMainActivityContentViewXml(ApkModule apkModule){
        ResXmlDocument document = new ResXmlDocument();
        ResXmlElement root = document.getDocumentElement();
        root.setName("LinearLayout");

        ResXmlAttribute attribute = root.getOrCreateAndroidAttribute("layout_width", 0x010100f4);
        attribute.setTypeAndData(ValueType.DEC, -1); // match_parent

        attribute = root.getOrCreateAndroidAttribute("layout_height", 0x010100f5);
        attribute.setTypeAndData(ValueType.DEC, -1); // match_parent

        attribute = root.getOrCreateAndroidAttribute("orientation", 0x010100c4);
        attribute.setTypeAndData(ValueType.DEC, 1); // vertical

        ResXmlElement textView = root.createChildElement("TextView");
        attribute = textView.getOrCreateAndroidAttribute("layout_width", 0x010100f4);
        attribute.setTypeAndData(ValueType.DEC, -1); // wrap_content

        attribute = textView.getOrCreateAndroidAttribute("layout_height", 0x010100f5);
        attribute.setTypeAndData(ValueType.DEC, -2); // wrap_content

        attribute = textView.getOrCreateAndroidAttribute("text", 0x0101014f);
        TableBlock tableBlock = apkModule.getTableBlock();
        PackageBlock packageBlock = tableBlock.pickOne();

        Entry helloEntry = packageBlock.getOrCreate(ResConfig.getDefault(), "string", "hello_world");

        String text = "<hr/><br><font size=\"30\" color=\"green\">Hello World</font></br>" +
                "<ul>" +
                "<li><b>\nType id offset = " +
                helloEntry.getPackageBlock().getHeaderBlock().getTypeIdOffsetItem().get() +
                "</b></li>" +
                "<li><b>\nType = " +
                helloEntry.getTypeName() +
                "</b></li>" +
                "<li><b>\nName = " +
                helloEntry.getName() +
                "</b></li>" +
                "<li><b>\nType index = " +
                helloEntry.getTypeBlock().getTypeString().getIndex() +
                "</b></li>" +
                "<li><b>\nType id = " +
                helloEntry.getTypeId() +
                "</b></li>" +
                "<li><b>\nResource id = " +
                HexUtil.toHex(helloEntry.getResourceId(), 8) +
                "</b></li>" +
                "</ul>";
        StyleDocument styleDocument = null;
        try {
            styleDocument = StyleDocument.parseStyledString(text);
        } catch (Exception ignored) {
        }
        Assert.assertNotNull(styleDocument);
        helloEntry.setValueAsString(styleDocument);
        Assert.assertEquals(text, helloEntry.getResValue().getValueAsString());
        attribute.setTypeAndData(ValueType.REFERENCE, helloEntry.getResourceId());

        document.refreshFull();

        String path = "res/layout/activity_main.xml";

        ByteInputSource source = new ByteInputSource(document.getBytes(), path);
        apkModule.add(source);

        Entry layoutEntry = tableBlock.pickOne().getOrCreate("", "layout", "activity_main");
        layoutEntry.setValueAsString(path);


        return layoutEntry.getResourceId();
    }
    private TableBlock createTableBlock(AndroidManifestBlock manifestBlock){
        TableBlock tableBlock = new TableBlock();
        String packageName = manifestBlock.getPackageName();
        int packageId = 0x7f;
        PackageBlock packageBlock = tableBlock.newPackage(
                packageId, packageName);
        packageBlock.getHeaderBlock().setTypeIdOffset(0);
        StyleDocument xmlDocument = new StyleDocument();
        xmlDocument.add(new StyleText("The quick"));
        StyleElement element = new StyleElement();
        xmlDocument.add(element);
        element.setName("br");
        element.addText("brown fox");
        xmlDocument.add(new StyleText("jumps over lazy dog"));
        TableStringPool pool = tableBlock.getStringPool();
        TableString tableString = pool.getOrCreate(xmlDocument);
        Entry someStyle = packageBlock.getOrCreate("", "string", "some_style");

        someStyle.setValueAsRaw(ValueType.STRING, tableString.getIndex());

        String app_name = "ARSCLib Test";
        Entry appName = packageBlock.getOrCreate("", "string", "app_name");
        appName.setValueAsString(app_name);

        Assert.assertEquals("packages count", 1, tableBlock.countPackages());
        Assert.assertEquals("package id", packageId, packageBlock.getId());
        Assert.assertEquals("package name", packageName, packageBlock.getName());

        Assert.assertEquals("package by id", packageBlock, tableBlock.getPackageBlockById(packageId));
        Assert.assertNull("package by id", tableBlock.getPackageBlockById(packageId - 1));
        Assert.assertEquals("package pick one by id", packageBlock, tableBlock.pickOne(packageId));
        Assert.assertNull("package pick one by wrong id", tableBlock.pickOne(packageId-1));
        Assert.assertEquals("package pick one", packageBlock, tableBlock.pickOne());

        Assert.assertNotNull(appName.getResValue());
        Assert.assertEquals(app_name, appName.getResValue().getValueAsString());
        Assert.assertNotNull("Table search by name", tableBlock.getResource(
                packageBlock.getName(),
                appName.getTypeName(), appName.getName()));
        Assert.assertNotNull("Table search by name (no package)", tableBlock.getResource(
                (String) null,
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
                tableBlock.getResource((String) null, "string", "app_name_error"));


        manifestBlock.setApplicationLabel(appName.getResourceId());

        Entry appIcon = packageBlock.getOrCreate("", "drawable", "ic_launcher");
        EncodeResult encodeResult = ValueCoder.encode("#006400");
        appIcon.setValueAsRaw(encodeResult.valueType, encodeResult.value);

        manifestBlock.setIconResourceId(appIcon.getResourceId());


        createAttrEntry(packageBlock);
        createArrayEntry(packageBlock);
        createMoreStrings(packageBlock);
        addArrayBagStrings_1(packageBlock);

        tableBlock.refreshFull();

        return tableBlock;
    }
    private void createMoreStrings(PackageBlock packageBlock){
        Entry entry = packageBlock
                .getOrCreate("", "string", "test_1");
        entry.setValueAsString("@integer/value");
        ResValue resValue = entry.getResValue();
        Assert.assertNotNull("resValue", resValue);
        Assert.assertEquals("@integer/value", resValue.getValueAsString());

        entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_65");
        entry.setValueAsString("3");
        resValue = entry.getResValue();
        Assert.assertEquals("3", resValue.getValueAsString());

        createMoreStrings_65(packageBlock);
        createMoreStrings_62(packageBlock);
    }
    private void createMoreStrings_65(PackageBlock packageBlock){

        Entry entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_65");
        String text = "3";
        entry.setValueAsString(text);
        ResValue resValue = entry.getResValue();
        Assert.assertEquals(text, resValue.getValueAsString());
    }
    private void createMoreStrings_62(PackageBlock packageBlock){

        Entry entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_62");

        String text = "<font size=\"30\" color=\"red\">Multi attribute styled string</font>";
        StyleDocument styleDocument = null;
        Exception exception = null;
        try {
            styleDocument = StyleDocument.parseStyledString(text);
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(styleDocument);
        entry.setValueAsString(styleDocument);
        ResValue resValue = entry.getResValue();
        TableString stringItem = (TableString) resValue.getDataAsPoolString();
        StyleDocument document = stringItem.getStyleDocument();
        Assert.assertNotNull(document);

        packageBlock.getTableBlock().refreshFull();

        Assert.assertEquals(text, resValue.getValueAsString());
    }
    private void addArrayBagStrings_1(PackageBlock packageBlock){

        Entry entry = packageBlock
                .getOrCreate("", "array", "test_array_bag_1");
        entry.ensureComplex(true);
        ResTableMapEntry mapEntry = entry.getResTableMapEntry();

        mapEntry.setValuesCount(5);

        ResValueMapArray mapArray = mapEntry.getValue();

        ResValueMap valueMap;

        valueMap = mapArray.get(0);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(1);
        String text = "123";
        valueMap.setValueAsString(text);

        valueMap = mapArray.get(1);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(2);
        text = "10.0dp";
        valueMap.setValueAsString(text);

        valueMap = mapArray.get(2);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(3);
        valueMap.setTypeAndData(ValueType.DEC, 123);

        valueMap = mapArray.get(3);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(4);
        text = "#ffaa00";
        valueMap.setValueAsString(text);
        entry.getResTableMapEntry().refresh();

        valueMap = mapArray.get(4);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(5);
        text = "@string/app_name";
        valueMap.setValueAsString(text);
    }
    private void createAttrEntry(PackageBlock packageBlock){
        Entry entry = packageBlock
                .getOrCreate("", "attr", "attrWidth");
        entry.ensureComplex(true);

        ResValueMapArray mapArray = entry.getResValueMapArray();

        entry.getResTableMapEntry().getHeader().setPublic(true);

        ResValueMap valueMap = mapArray.createNext();
        valueMap.setAttributeType(AttributeType.FORMATS);

        valueMap.addAttributeTypeFormats(AttributeDataFormat.REFERENCE,
                AttributeDataFormat.DIMENSION,
                AttributeDataFormat.COLOR);

        mapArray.refresh();

    }
    private void createArrayEntry(PackageBlock packageBlock){
        Entry entry = packageBlock
                .getOrCreate("", "array", "array_1");
        entry.ensureComplex(true);
        Entry appName = packageBlock.getOrCreate("", "string", "app_name");


        ResValueMapArray mapArray = entry.getResValueMapArray();

        ResValueMap valueMap = mapArray.createNext();
        valueMap.setArrayIndex(1);

        valueMap.setValueAsString("@integer/value");
        Assert.assertEquals("@integer/value", valueMap.getValueAsString());

        valueMap = mapArray.createNext();
        valueMap.setArrayIndex(2);
        valueMap.setTypeAndData(ValueType.REFERENCE, appName.getResourceId());

        mapArray.refresh();

    }
    private AndroidManifestBlock createManifest() throws IOException {
        FrameworkApk frameworkApk = AndroidFrameworks.getLatest();
        AndroidManifestBlock manifestBlock = new AndroidManifestBlock();
        manifestBlock.setPackageName("com.reandroid.arsc");
        manifestBlock.setVersionCode(1);
        manifestBlock.setVersionName("1.0");

        manifestBlock.setCompileSdkVersion(frameworkApk.getVersionCode());
        manifestBlock.setCompileSdkVersionCodename(frameworkApk.getVersionName());
        manifestBlock.setCompileSdk(AndroidApiLevel.J);

        manifestBlock.setPlatformBuildVersionCode(frameworkApk.getVersionCode());
        manifestBlock.setPlatformBuildVersionName(frameworkApk.getVersionName());
        manifestBlock.setPlatformBuild(AndroidApiLevel.J);
        manifestBlock.setMinSdkVersion(AndroidApiLevel.J.getApi());
        manifestBlock.setTargetSdkVersion(AndroidApiLevel.J.getApi());

        manifestBlock.addUsesPermission("android.permission.INTERNET");
        manifestBlock.addUsesPermission("android.permission.READ_EXTERNAL_STORAGE");

        addEmptyAttributeValue(manifestBlock);

        manifestBlock.setApplicationClassName(manifestBlock.getPackageName() + ".MyApplication");
        manifestBlock.setMainActivityClassName(".MyActivity");

        manifestBlock.refresh();

        Assert.assertEquals("package", "com.reandroid.arsc", manifestBlock.getPackageName());
        Assert.assertEquals("versionCode", Integer.valueOf(1), manifestBlock.getVersionCode());
        Assert.assertEquals("versionName", "1.0", manifestBlock.getVersionName());

        Assert.assertEquals("Application class", manifestBlock.getPackageName() + ".MyApplication", manifestBlock.getApplicationClassName());
        Assert.assertEquals("Main activity", manifestBlock.getPackageName() + ".MyActivity", manifestBlock.getMainActivityClassName());

        Assert.assertEquals("compileSdkVersion",
                Integer.valueOf(1), manifestBlock.getVersionCode());
        Assert.assertEquals("compileSdkVersionCodeName",
                "1.0", manifestBlock.getVersionName());

        /*
        Assert.assertEquals("platformBuildVersionCode",
                Integer.valueOf(frameworkApk.getVersionCode()), manifestBlock.getPlatformBuildVersionCode());

        Assert.assertEquals("platformBuildVersionName",
                frameworkApk.getVersionName(), manifestBlock.getPlatformBuildVersionName());


         */
        Assert.assertNotNull("android.permission.INTERNET",
                manifestBlock.getUsesPermission("android.permission.INTERNET"));
        Assert.assertNotNull("android.permission.READ_EXTERNAL_STORAGE",
                manifestBlock.getUsesPermission("android.permission.READ_EXTERNAL_STORAGE"));

        Assert.assertNull("android.permission.NOTHING",
                manifestBlock.getUsesPermission("android.permission.NOTHING"));
        manifestBlock.refreshFull();
        byte[] bytes = manifestBlock.getBytes();
        manifestBlock = new AndroidManifestBlock();
        manifestBlock.readBytes(new BlockReader(bytes));

        ResXmlElement application = manifestBlock.getApplicationElement();
        Assert.assertNotNull(application);
        ResXmlElement metaData = CollectionUtil.getFirst(application.getElements(element -> {
            if(!element.equalsName(AndroidManifest.TAG_meta_data)){
                return false;
            }
            ResXmlAttribute attribute = element.searchAttributeByResourceId(AndroidManifest.ID_name);
            return attribute != null && EMPTY_META_NAME.equals(attribute.getValueAsString());
        }));
        Assert.assertNotNull(metaData);
        ResXmlAttribute attribute = metaData.searchAttributeByResourceId(AndroidManifest.ID_value);
        Assert.assertNotNull(attribute);
        Assert.assertEquals("Test empty attribute value failed ",StringsUtil.EMPTY, attribute.getValueAsString());

        return manifestBlock;
    }
    private void addEmptyAttributeValue(AndroidManifestBlock manifestBlock){
        ResXmlElement application = manifestBlock.getOrCreateApplicationElement();
        ResXmlElement meta = application.createChildElement(AndroidManifest.TAG_meta_data);
        ResXmlAttribute name = meta.getOrCreateAndroidAttribute(AndroidManifest.NAME_name,
                AndroidManifest.ID_name);
        name.setValueAsString(EMPTY_META_NAME);

        ResXmlAttribute value = meta.getOrCreateAndroidAttribute(AndroidManifest.NAME_value,
                AndroidManifest.ID_value);
        value.setValueAsString(StringsUtil.EMPTY);
    }
    public static ApkModule getLastApkModule(){
        return last_apkModule;
    }

    static final String EMPTY_META_NAME = "test-empty-attribute";

}
