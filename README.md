# ARSCLib
## Android binary resources read/write java library
This library is developed based on AOSP structure of androidfw/ResourceTypes.h , to totally replace aapt/aapt2
#### Read, write, modify and create
* Resource table (resources.arsc)
* Binary xml files (AndroidManifest.xml & resource xml)
#### Convert from/to json string (for obfuscated resources)

* Decodes resources to readable json
* Encodes/Builds sources in json format to binary resources

#### Convert from/to XML string (for un-obfuscated resources)

* Decodes resources to source code
* Encodes/Builds source XML to binary resources

##### _NOTES:_

_1- Decoding resources to XML requires all source names should be un-obfuscated and valid_

_2- User of this lib is assumed to have good knowledge of android source XML syntax, thus
during encoding/building it does not validate or throw XML syntax errors as often as aapt/aapt2. For
example, you are allowed to set wrong values on some places and doesn't prevent from
successful building. On AndroidManifest.xml you can set  ``` package="Wrong 😂 (package) name!" ```
then you have to know such values are acceptable by android devices._


#### Example application
_Check this tool developed using this library_
[https://github.com/REAndroid/APKEditor](https://github.com/REAndroid/APKEditor)

#### Works on all java supported platforms (Android, Linux, Mac, Windows)


* Maven
 ```gradle
repositories {
    mavenCentral()
}
dependencies {
    implementation("io.github.reandroid:ARSCLib:+")
}
```
* Jar

```gradle
dependencies {
    implementation(files("$rootProject.projectDir/libs/ARSCLib.jar"))
}
```
#### Build jar

```ShellSession
git clone https://github.com/REAndroid/ARSCLib.git
cd ARSCLib
./gradlew jar
# Built jar will be placed ./build/libs/ARSCLib-x.x.x.jar
```

#### Examples
<details><summary> <code><b>Java example</b></code></summary>

```java   
import com.reandroid.apk.AndroidFrameworks;
import com.reandroid.apk.ApkModule;
import com.reandroid.apk.FrameworkApk;
import com.reandroid.archive.APKArchive;
import com.reandroid.archive.ByteInputSource;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.value.Entry;

import java.io.File;
import java.io.IOException;

public class ARSCLibExample {

    public static void createNewApk() throws IOException {
  
        ApkModule apkModule = new ApkModule("base", new APKArchive());

        TableBlock tableBlock = new TableBlock();
        AndroidManifestBlock manifest = new AndroidManifestBlock();

        apkModule.setTableBlock(tableBlock);
        apkModule.setManifest(manifest);

        FrameworkApk framework = apkModule.initializeAndroidFramework(
                AndroidFrameworks.getLatest().getVersionCode());

        PackageBlock packageBlock = tableBlock.newPackage(0x7f, "com.example");

        Entry appIcon = packageBlock.getOrCreate("", "drawable", "ic_launcher");

        EncodeResult color = ValueCoder.encode("#006400");
        appIcon.setValueAsRaw(color.valueType, color.value);

        Entry appNameDefault = packageBlock.getOrCreate("", "string", "app_name");
        appNameDefault.setValueAsString("My Application");

        Entry appNameDe = packageBlock.getOrCreate("-de", "string", "app_name");
        appNameDe.setValueAsString("Meine Bewerbung");

        Entry appNameRu = packageBlock.getOrCreate("-ru-rRU", "string", "app_name");
        appNameRu.setValueAsString("Мое заявление");

        manifest.setPackageName("com.example");
        manifest.setVersionCode(100);
        manifest.setVersionName("1.0.0");
        manifest.setIconResourceId(appIcon.getResourceId());
        manifest.setCompileSdkVersion(framework.getVersionCode());
        manifest.setCompileSdkVersionCodename(framework.getVersionName());
        manifest.setPlatformBuildVersionCode(framework.getVersionCode());
        manifest.setPlatformBuildVersionName(framework.getVersionName());

        manifest.addUsesPermission("android.permission.INTERNET");
        manifest.addUsesPermission("android.permission.READ_EXTERNAL_STORAGE");

        //all appName entries created above have the same resource ids
        manifest.setApplicationLabel(appNameDefault.getResourceId());

        ResXmlElement mainActivity = manifest.getOrCreateMainActivity("android.app.Activity");
        ResXmlAttribute labelAttribute = mainActivity
                .getOrCreateAndroidAttribute(AndroidManifestBlock.NAME_label, AndroidManifestBlock.ID_label);
        labelAttribute.setValueAsString("Hello World");

        //Android os requires at least one dex file on base apk
        ByteInputSource dummyDex = new ByteInputSource(new byte[0], "classes.dex");
        apkModule.add(dummyDex);

        File outFile = new File("test_out.apk");
        apkModule.writeApk(outFile);
        //Sign and install
    }
}
```
</details>

