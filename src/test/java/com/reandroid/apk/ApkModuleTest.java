package com.reandroid.apk;

import com.reandroid.TestUtils;
import com.reandroid.app.AndroidApiLevel;
import com.reandroid.app.AndroidManifest;
import com.reandroid.archive.ArchiveBytes;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.arsc.array.ResValueMapArray;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.coder.*;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.*;
import com.reandroid.common.ReferenceResolver;
import com.reandroid.dex.SampleDexFileCreator;
import com.reandroid.dex.model.DexFile;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.xml.StyleDocument;
import com.reandroid.xml.StyleElement;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApkModuleTest {

    public static final boolean TEST_WITH_MULTI_LAYOUT_DEX_V041 = true;

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

        DexFile dexFile = SampleDexFileCreator.createApplicationClass(
                TEST_WITH_MULTI_LAYOUT_DEX_V041,
                appClass, mainActivity, mainActivityLayoutId);

        byte[] bytes = dexFile.getBytes();
        apkModule.add(new ByteInputSource(bytes, "classes.dex"));
        apkModule.getUncompressedFiles().addPath(apkModule.getZipEntryMap());

        File generated_apk = new File(TestUtils.getTesApkDirectory(), "generated.apk");
        generated_apk.delete();
        apkModule.writeApk(generated_apk);
        apkModule = ApkModule.loadApkFile(generated_apk);
        last_apkModule = apkModule;

        return apkModule;
    }
    private int createMainActivityContentViewXml(ApkModule apkModule){
        ResXmlDocument document = new ResXmlDocument();
        document.setApkFile(apkModule);

        ResXmlElement root = document.getDocumentElement();
        root.setName("LinearLayout");

        ResXmlAttribute attribute = root.getOrCreateAndroidAttribute("layout_width", 0x010100f4);
        attribute.setTypeAndData(DataType.DEC, -1); // match_parent

        attribute = root.getOrCreateAndroidAttribute("layout_height", 0x010100f5);
        attribute.setTypeAndData(DataType.DEC, -1); // match_parent

        attribute = root.getOrCreateAndroidAttribute("orientation", 0x010100c4);
        attribute.setTypeAndData(DataType.DEC, 1); // vertical

        attribute = root.getOrCreateAndroidAttribute("padding", 0x010100d5);
        attribute.setData(CoderDimension.INS.encode("20.0dp"));

        root.getOrCreateAndroidAttribute("gravity", 0x010100af)
                .setTypeAndData(DataType.HEX, 0x00000011); // center

        root.getOrCreateAndroidAttribute("background", 0x010100d4)
                .setData(CoderColorARGB8.INS.encode("#ff1c1c1c"));

        ResXmlElement textView = root.newElement("TextView");

        attribute = textView.getOrCreateAndroidAttribute("layout_width", 0x010100f4);
        attribute.setTypeAndData(DataType.DEC, -1); // wrap_content


        attribute = textView.getOrCreateAndroidAttribute("layout_height", 0x010100f5);
        attribute.setTypeAndData(DataType.DEC, -2); // wrap_content


        TableBlock tableBlock = apkModule.getTableBlock();
        PackageBlock packageBlock = tableBlock.pickOne();

        Entry idEntry = packageBlock.getOrCreate("", "id", "tv1");
        idEntry.setDataAsBoolean(false);
        idEntry.getHeader().setPublic(true);
        idEntry.getHeader().setWeak(true);

        attribute = textView.getOrCreateAndroidAttribute("id", 0x010100d0);
        attribute.setTypeAndData(DataType.REFERENCE, idEntry.getResourceId());

        attribute = textView.getOrCreateAndroidAttribute("text", 0x0101014f);

        Entry helloEntry = packageBlock.getOrCreate(ResConfig.getDefault(), "string", "hello_world");

        String text = "<hr /><br><font color=\"green\" size=\"30\">Hello World</font></br>" +
                "<ul>" +
                "<li><b>\nType id offset = " +
                helloEntry.getPackageBlock().getHeaderBlock().typeIdOffset().get() +
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
        StyleDocument styleDocument = StyleDocument.create(text);
        Assert.assertNotNull(styleDocument);
        helloEntry.setDataAsString(styleDocument);
        Assert.assertEquals(text, helloEntry.getResValue().getDataAsString());
        attribute.setTypeAndData(DataType.REFERENCE, helloEntry.getResourceId());

        createStyledStringInXmlDocument(root);

        for (PackageBlock pkg : tableBlock) {
            createTextViewEntryOnPackage(pkg, root);
        }

        document.refreshFull();

        String path = "res/layout/activity_main.xml";

        ByteInputSource source = new ByteInputSource(document.getBytes(), path);
        apkModule.add(source);

        Entry layoutEntry = tableBlock.pickOne().getOrCreate("", "layout", "activity_main");
        layoutEntry.setDataAsString(path);


        return layoutEntry.getResourceId();
    }
    private void createTextViewEntryOnPackage(PackageBlock packageBlock, ResXmlElement root) {


        String name = "test_string_entry_on_multi_packages";

        Entry entry = packageBlock.getOrCreate(
                ResConfig.getDefault(), "string", name);
        entry.setDataAsString("String entry @string/" + name + " on package: "
                + packageBlock.getName());

        ResXmlElement textView = root.newElement("TextView");

        textView.getOrCreateAndroidAttribute("layout_width", 0x010100f4)
                .setTypeAndData(DataType.DEC, -1); // wrap_content
        textView.getOrCreateAndroidAttribute("layout_height", 0x010100f5)
                .setTypeAndData(DataType.DEC, -2); // wrap_content
        textView.getOrCreateAndroidAttribute("layout_marginTop", 0x010100f8)
                .setData(CoderDimension.INS.encode("12.0dp"));
        textView.getOrCreateAndroidAttribute("text", 0x0101014f)
                .setTypeAndData(DataType.REFERENCE, entry.getResourceId());
    }
    private void createStyledStringInXmlDocument(ResXmlElement root) {

        ResXmlElement textView2 = root.newElement("TextView");
        ResXmlAttribute attribute = textView2.getOrCreateAndroidAttribute("layout_width", 0x010100f4);
        attribute.setTypeAndData(DataType.DEC, -1); // wrap_content

        attribute = textView2.getOrCreateAndroidAttribute("layout_height", 0x010100f5);
        attribute.setTypeAndData(DataType.DEC, -2); // wrap_content

        attribute = textView2.getOrCreateAndroidAttribute("linksClickable", 0x010100b1);
        attribute.setValueAsBoolean(true);

        attribute = textView2.getOrCreateAndroidAttribute("textIsSelectable", 0x01010316);
        attribute.setValueAsBoolean(true);

        attribute = textView2.getOrCreateAndroidAttribute("text", 0x0101014f);

        createStyledStringInXmlAttribute(attribute);
    }
    private void createStyledStringInXmlAttribute(ResXmlAttribute attribute) {

        String text = "\n\nThis is <font size=\"30\" color=\"red\">STYLED! string" +
                "</font> placed directly in xml attribute android:text, contains clickable link (" +
                " <a href=\"https://www.github.com/REAndroid/ARSCLib\">github.com/REAndroid/ARSCLib</a>)" +
                " and <b> BOLD text</b>\n\n";

        StyleDocument styleDocument = StyleDocument.create(text);
        Assert.assertNotNull(styleDocument);

        attribute.setDataAsString(styleDocument);
    }
    private TableBlock createTableBlock(AndroidManifestBlock manifestBlock){
        TableBlock tableBlock = new TableBlock();
        String packageName = manifestBlock.getPackageName();
        int packageId = 0x7f;
        PackageBlock packageBlock = tableBlock.newPackage(
                packageId, packageName);
        packageBlock.getHeaderBlock().setTypeIdOffset(0);
        StyleDocument xmlDocument = new StyleDocument();
        xmlDocument.newText().setText("The quick");
        StyleElement element = xmlDocument.newElement();
        element.setName("br");
        element.addText("brown fox");
        xmlDocument.newText().setText("jumps over lazy dog");
        TableStringPool pool = tableBlock.getStringPool();
        TableString tableString = pool.getOrCreate(xmlDocument);
        Entry someStyle = packageBlock.getOrCreate("", "string", "some_style");

        someStyle.setDataAsRaw(DataType.STRING, tableString.getIndex());

        String app_name = "ARSCLib Test";
        Entry appName = packageBlock.getOrCreate("", "string", "app_name");
        appName.setDataAsString(app_name);
        packageBlock.getOrCreate("-en-rUS-watch", "string", "app_name").setDataAsString(app_name + "-en-rUS-watch");
        packageBlock.getOrCreate("-en", "string", "app_name").setDataAsString(app_name + "-en");
        packageBlock.getOrCreate("-en-rUS", "string", "app_name").setDataAsString(app_name + "-en-rUS");
        packageBlock.getOrCreate("-en-rCA", "string", "app_name").setDataAsString(app_name + "-en-rCA");

        ReferenceResolver referenceResolver = new ReferenceResolver(packageBlock.getTableBlock());
        List<Entry> entryList = referenceResolver.resolveWithConfig(appName.getResourceId(), ResConfig.parse("-en"));
        Assert.assertEquals("packages count", 4, entryList.size());

        Assert.assertEquals("packages count", 1, tableBlock.size());
        Assert.assertEquals("package id", packageId, packageBlock.getId());
        Assert.assertEquals("package name", packageName, packageBlock.getName());

        Assert.assertEquals("package by id", packageBlock, tableBlock.getPackageBlockById(packageId));
        Assert.assertNull("package by id", tableBlock.getPackageBlockById(packageId - 1));
        Assert.assertEquals("package pick one by id", packageBlock, tableBlock.pickOne(packageId));
        Assert.assertNull("package pick one by wrong id", tableBlock.pickOne(packageId-1));
        Assert.assertEquals("package pick one", packageBlock, tableBlock.pickOne());

        Assert.assertNotNull(appName.getResValue());
        Assert.assertEquals(app_name, appName.getResValue().getDataAsString());
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
        appName_de.setDataAsString(app_name + " - Bewerbung");

        Assert.assertNotEquals(appName.getResConfig(), appName_de.getResConfig());
        Assert.assertNotEquals(appName.getResValue().getDataAsString(),
                appName_de.getResValue().getDataAsString());

        ResourceEntry resourceEntry = tableBlock.getLocalResource(appName_de.getResourceId());
        Assert.assertNotNull("getLocalResource by id", resourceEntry);
        Assert.assertEquals(appName.getName(), resourceEntry.getName());
        Assert.assertEquals(appName.getTypeName(), resourceEntry.getType());

        Entry appName_ru = resourceEntry.getOrCreate("-ru-rRU");
        Assert.assertNotNull("Config entry - ru", appName_ru);
        appName_ru.setDataAsString(app_name + " - заявление");

        Assert.assertNotEquals(appName_de.getResConfig(), appName_ru.getResConfig());
        Assert.assertNotEquals(appName_de.getResValue().getDataAsString(),
                appName_ru.getResValue().getDataAsString());

        Assert.assertEquals("Configs count", 7, resourceEntry.getConfigsCount());

        Assert.assertNull("Table search by error id",
                tableBlock.getResource(appName.getResourceId() + 1));
        Assert.assertNull("Table search by error name: ",
                tableBlock.getResource((String) null, "string", "app_name_error"));


        manifestBlock.setApplicationLabel(appName.getResourceId());

        Entry appIcon = packageBlock.getOrCreate("", "drawable", "ic_launcher");
        EncodeResult encodeResult = ValueCoder.encode("#006400");
        appIcon.setDataAsRaw(encodeResult.dataType, encodeResult.value);

        manifestBlock.setIconResourceId(appIcon.getResourceId());


        createAttrEntry(packageBlock);
        createArrayEntry(packageBlock);
        createMoreStrings(packageBlock);
        addArrayBagStrings_1(packageBlock);
        testCreateDynamicReference(packageBlock);
        createBooleanValues(packageBlock);

        createLibraries(packageBlock);
        createOverlayables(packageBlock);
        createStageAlias(packageBlock);

        addMorePackages(tableBlock);
        tableBlock.refreshFull();

        return tableBlock;
    }
    private void createMoreStrings(PackageBlock packageBlock){
        Entry entry = packageBlock
                .getOrCreate("", "string", "test_1");
        entry.setDataAsString("@integer/value");
        ResValue resValue = entry.getResValue();
        Assert.assertNotNull("resValue", resValue);
        Assert.assertEquals("@integer/value", resValue.getDataAsString());

        entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_65");
        entry.setDataAsString("3");
        resValue = entry.getResValue();
        Assert.assertEquals("3", resValue.getDataAsString());

        createMoreStrings_65(packageBlock);
        createMoreStrings_62(packageBlock);
    }
    private void createMoreStrings_65(PackageBlock packageBlock){

        Entry entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_65");
        String text = "3";
        entry.setDataAsString(text);
        ResValue resValue = entry.getResValue();
        Assert.assertEquals(text, resValue.getDataAsString());
    }
    private void createMoreStrings_62(PackageBlock packageBlock){

        Entry entry = packageBlock
                .getOrCreate("", "string", "test_issue_apkeditor_62");

        String text = "<font color=\"red\" size=\"30\">Multi attribute styled string</font>";
        StyleDocument styleDocument = null;
        Exception exception = null;
        try {
            styleDocument = StyleDocument.parseStyledString(text);
        } catch (Exception ex) {
            exception = ex;
        }
        Assert.assertNull(exception);
        Assert.assertNotNull(styleDocument);
        entry.setDataAsString(styleDocument);
        ResValue resValue = entry.getResValue();
        TableString stringItem = (TableString) resValue.getDataAsPoolString();
        StyleDocument document = stringItem.getStyleDocument();
        Assert.assertNotNull(document);

        packageBlock.getTableBlock().refreshFull();

        Assert.assertEquals(text, resValue.getDataAsString());
    }

    private void testCreateDynamicReference(PackageBlock packageBlock) {
        Entry entry = packageBlock
                .getOrCreate("", "color", "test_dynamic_reference");
        entry.setDataAsRaw(DataType.DYNAMIC_REFERENCE, 0x0196000a);
        entry.getHeader().setPublic(false);

        ResValue resValue = entry.getResValue();
        Assert.assertNotNull(resValue);
        Assert.assertEquals(DataType.DYNAMIC_REFERENCE, resValue.getDataType());
        Assert.assertEquals("@@0x0196000a", resValue.decodeValue());

        entry = packageBlock
                .getOrCreate("", "attr", "test_dynamic_attribute");
        entry.setDataAsRaw(DataType.DYNAMIC_ATTRIBUTE, 0x01940007);
        entry.getHeader().setPublic(true);

        resValue = entry.getResValue();
        Assert.assertNotNull(resValue);
        Assert.assertEquals(DataType.DYNAMIC_ATTRIBUTE, resValue.getDataType());
        Assert.assertEquals("??0x01940007", resValue.decodeValue());
    }

    private void createBooleanValues(PackageBlock packageBlock) {
        packageBlock.getOrCreate("-en", "bool", "test_bool_1")
                .setDataAsBoolean(true);
        packageBlock.getOrCreate("-fr", "bool", "test_bool_1")
                .setDataAsBoolean(false);
        packageBlock.getOrCreate("-land", "bool", "test_bool_1")
                .setDataAsBoolean(true);
        packageBlock.getOrCreate("-hdpi", "bool", "test_bool_1")
                .setDataAsBoolean(true);

        packageBlock.getOrCreate("-en", "bool", "test_bool_2")
                .setDataAsBoolean(false);
        packageBlock.getOrCreate("-fr", "bool", "test_bool_2")
                .setDataAsBoolean(true);
        packageBlock.getOrCreate("-mdpi", "bool", "test_bool_2")
                .setDataAsBoolean(false);
        packageBlock.getOrCreate("-en-rCA", "bool", "test_bool_3")
                .setDataAsBoolean(true);

        ResourceEntry resourceEntry1 = packageBlock
                .getResource("bool", "test_bool_1");
        Assert.assertNotNull("null test_bool_1", resourceEntry1);
        Assert.assertEquals("configs test_bool_1", 4,
                resourceEntry1.getConfigsCount());
        Assert.assertNull("non-null [DEFAULT] test_bool_1", resourceEntry1.get(""));

        Entry entry = resourceEntry1.get("-en");
        Assert.assertNotNull("null -en test_bool_1", entry);
        Assert.assertTrue("-en test_bool_1", entry.getDataAsBoolean());
        entry.getOrCreateSpecFlag().add(SpecBlock.Flag.SPEC_PUBLIC);

        ResourceEntry resourceEntry2 = packageBlock
                .getResource("bool", "test_bool_2");
        Assert.assertNotNull("null test_bool_2", resourceEntry2);
        Assert.assertEquals("configs test_bool_2", 3,
                resourceEntry2.getConfigsCount());
        resourceEntry2.getOrCreateSpecFlag().add(SpecBlock.Flag.SPEC_STAGED_API);

        entry = resourceEntry2.get("-mdpi");
        Assert.assertNotNull("null -mdpi test_bool_2", entry);
        Assert.assertNotNull("null res value -mdpi test_bool_2", entry.getResValue());
        Assert.assertEquals("-mdpi test_bool_2", Boolean.FALSE,
                entry.getDataAsEncoded());

        Assert.assertNull("non-null [DEFAULT] test_bool_2", resourceEntry2.get(""));

        ResourceEntry resourceEntry3 = packageBlock
                .getResource("bool", "test_bool_3");
        Assert.assertNotNull("null test_bool_3", resourceEntry3);
        resourceEntry3.getOrCreateSpecFlag().add(SpecBlock.Flag.SPEC_PUBLIC);
        resourceEntry3.getOrCreateSpecFlag().add(SpecBlock.Flag.SPEC_STAGED_API);
    }
    private void createLibraries(PackageBlock packageBlock) {
        LibraryBlock libraryBlock = packageBlock.getLibraryList().createNext();
        LibraryInfo info1 = libraryBlock.createNext();
        info1.setId(0x6e);
        info1.setName("test.library.lib1");

        LibraryInfo info2 = libraryBlock.createNext();
        info2.setId(0x6f);
        info2.setName("test.library.lib2");
        Assert.assertEquals(2, libraryBlock.size());
    }
    private void createOverlayables(PackageBlock packageBlock) {
        Overlayable overlayable1 = packageBlock.getOverlayableList().createNext();
        overlayable1.setName("test.overlayable1.name");
        overlayable1.setActor("test.overlayable1.actor");
        OverlayablePolicy policy1 = overlayable1.createOverlayablePolicy();
        policy1.setFlags(PolicyFlag.PUBLIC);
        Iterator<ResourceEntry> iterator = packageBlock.getResources();
        while (policy1.size() < 4 && iterator.hasNext()) {
            policy1.createNext().set(iterator.next().getResourceId());
        }

        Overlayable overlayable2 = packageBlock.getOverlayableList().createNext();
        overlayable2.setName("test.overlayable2.name");
        overlayable2.setActor("test.overlayable2.actor");

        OverlayablePolicy policy2 = overlayable2.createOverlayablePolicy();
        policy2.setFlags(PolicyFlag.SIGNATURE);
        while (policy2.size() < 3 && iterator.hasNext()) {
            policy2.createNext().set(iterator.next().getResourceId());
        }

        OverlayablePolicy policy22 = overlayable2.createOverlayablePolicy();
        policy22.setFlags(PolicyFlag.PRODUCT_PARTITION);
        while (policy22.size() < 2 && iterator.hasNext()) {
            policy22.createNext().set(iterator.next().getResourceId());
        }

        OverlayablePolicy policy23 = overlayable2.createOverlayablePolicy();
        policy23.setFlags(PolicyFlag.ODM_PARTITION, PolicyFlag.OEM_PARTITION);

        // add unknown PolicyFlag
        policy23.setFlags(policy23.getFlags() | 0x55000000);

        while (policy23.size() < 2 && iterator.hasNext()) {
            policy23.createNext().set(iterator.next().getResourceId());
        }
        Assert.assertEquals(2, packageBlock.getOverlayableList().size());
    }
    private void createStageAlias(PackageBlock packageBlock) {
        StagedAlias stagedAlias = packageBlock.getStagedAliasList().createNext();
        StagedAliasEntry entry = stagedAlias.createNext();
        entry.set(0x6f010000, packageBlock.getResources().next().getResourceId());
        Assert.assertEquals(1, stagedAlias.size());
        Assert.assertNotNull(packageBlock.getResource(0x6f010000));
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
        valueMap.setDataAsString(text);

        valueMap = mapArray.get(1);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(2);
        text = "10.0dp";
        valueMap.setDataAsString(text);

        valueMap = mapArray.get(2);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(3);
        valueMap.setTypeAndData(DataType.DEC, 123);

        valueMap = mapArray.get(3);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(4);
        text = "#ffaa00";
        valueMap.setDataAsString(text);
        entry.getResTableMapEntry().refresh();

        valueMap = mapArray.get(4);
        Assert.assertNotNull(valueMap);
        valueMap.setArrayIndex(5);
        text = "@string/app_name";
        valueMap.setDataAsString(text);
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

        valueMap.setDataAsString("@integer/value");
        Assert.assertEquals("@integer/value", valueMap.getDataAsString());

        valueMap = mapArray.createNext();
        valueMap.setArrayIndex(2);
        valueMap.setTypeAndData(DataType.REFERENCE, appName.getResourceId());

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
        AndroidApiLevel apiLevel = AndroidApiLevel.O;
        if (TEST_WITH_MULTI_LAYOUT_DEX_V041) {
            apiLevel = AndroidApiLevel.VANILLA_ICE_CREAM;
        }
        manifestBlock.setCompileSdk(apiLevel);

        manifestBlock.setPlatformBuildVersionCode(frameworkApk.getVersionCode());
        manifestBlock.setPlatformBuildVersionName(frameworkApk.getVersionName());
        manifestBlock.setPlatformBuild(apiLevel);
        manifestBlock.setMinSdkVersion(apiLevel.getApi());
        manifestBlock.setTargetSdkVersion(apiLevel.getApi());

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
            return attribute != null && EMPTY_META_NAME.equals(attribute.getDataAsString());
        }));
        Assert.assertNotNull(metaData);
        ResXmlAttribute attribute = metaData.searchAttributeByResourceId(AndroidManifest.ID_value);
        Assert.assertNotNull(attribute);
        Assert.assertEquals("Test empty attribute value failed ",StringsUtil.EMPTY, attribute.getDataAsString());

        return manifestBlock;
    }
    private void addEmptyAttributeValue(AndroidManifestBlock manifestBlock){
        ResXmlElement application = manifestBlock.getOrCreateApplicationElement();
        ResXmlElement meta = application.newElement(AndroidManifest.TAG_meta_data);
        ResXmlAttribute name = meta.getOrCreateAndroidAttribute(AndroidManifest.NAME_name,
                AndroidManifest.ID_name);
        name.setDataAsString(EMPTY_META_NAME);

        ResXmlAttribute value = meta.getOrCreateAndroidAttribute(AndroidManifest.NAME_value,
                AndroidManifest.ID_value);
        value.setDataAsString(StringsUtil.EMPTY);
    }
    private void addMorePackages(TableBlock tableBlock) {
        PackageBlock main = tableBlock.pickOne();
        PackageBlock packageBlock2 = tableBlock.newPackage(
                main.getId() + 1, main.getName() + ".pkg2");
        initPackage2(packageBlock2);
        PackageBlock packageBlock3 = tableBlock.newPackage(
                main.getId() + 2, main.getName() + ".pkg3");
        initPackage3(packageBlock3);
        Assert.assertEquals("Packages count", 3, tableBlock.size());
    }
    private void initPackage2(PackageBlock packageBlock) {
        packageBlock.getOrCreate(
                "", "string", "pkg2_string_entry_1")
                .setDataAsString("Package-2 string entry-1");
        packageBlock.getOrCreate(
                "", "string", "pkg2_string_entry_2")
                .setDataAsString("Package-2 string entry-2");
    }
    private void initPackage3(PackageBlock packageBlock) {
        packageBlock.getOrCreate(
                "", "integer", "pkg3_integer_entry_1")
                .setDataAsDecimal(1);
        packageBlock.getOrCreate(
                "-en", "integer", "pkg3_integer_entry_1")
                .setDataAsDecimal(2);
        packageBlock.getOrCreate(
                "-mdpi", "integer", "pkg3_integer_entry_1")
                .setDataAsDecimal(3);

        packageBlock.getOrCreate(
                "", "string", "pkg3_string_entry_1")
                .setDataAsString("Package-3 string entry-1 -default");
        Entry entry = packageBlock.getOrCreate(
                "-en", "string", "pkg3_string_entry_1");
        entry.setDataAsString("Package-3 string entry-1 -en");
        entry.getOrCreateSpecFlag().add(SpecBlock.Flag.SPEC_PUBLIC);

        packageBlock.getOrCreate(
                "-fr", "string", "pkg3_string_entry_1")
                .setDataAsString("Package-3 string entry-1 -fr");
    }
    public static ApkModule getLastApkModule(){
        return last_apkModule;
    }

    static final String EMPTY_META_NAME = "test-empty-attribute";

}
