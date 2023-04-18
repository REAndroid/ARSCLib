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

<details><summary> <code><b>See java example</b></code></summary>

```java
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;

public static void exampleManifest()throws IOException{
        File inFile=new File("AndroidManifest.xml");

        // *** Loading AndroidManifest ***
        AndroidManifestBlock manifestBlock=AndroidManifestBlock.load(inFile);

        System.out.println("Package name: "+manifestBlock.getPackageName());

        List<String> usesPermissionList=manifestBlock.getUsesPermissions();
        for(String usesPermission:usesPermissionList){
        System.out.println("Uses permission: "+usesPermission);
        }

        // *** Modifying AndroidManifest ***
        // Change package name
        manifestBlock.setPackageName("com.new.package-name");
        // Add uses-permission
        manifestBlock.addUsesPermission("android.permission.WRITE_EXTERNAL_STORAGE");
        // Modify version code
        manifestBlock.setVersionCode(904);
        // Modify version name
        manifestBlock.setVersionName("9.0.4");

        // Modify xml attribute
        List<ResXmlElement> activityList=manifestBlock.listActivities();
        for(ResXmlElement activityElement:activityList){
        ResXmlAttribute attributeName=activityElement.searchAttributeByResourceId(AndroidManifestBlock.ID_name);
        System.out.println("Old activity name: "+attributeName.getValueAsString());
        attributeName.setValueAsString("com.app.MyActivity");
        System.out.println("New activity name: "+attributeName.getValueAsString());
        break;
        }

        // Refresh to re-calculate offsets
        manifestBlock.refresh();
        // Save
        File outFile=new File("AndroidManifest_out.xml");
        manifestBlock.writeBytes(outFile);

        System.out.println("Saved: "+outFile);
        }

```



```java
    public static void exampleResourceTable() throws IOException{
        File inFile=new File("resources.arsc");

        // *** Loading resource table ***
        TableBlock tableBlock=TableBlock.load(inFile);

        Collection<PackageBlock> packageBlockList=tableBlock.listPackages();
        System.out.println("Packages count = "+packageBlockList.size());
        for(PackageBlock packageBlock:packageBlockList){
        System.out.println("Package id = "+packageBlock.getId()
        +", name = "+packageBlock.getName());
        }

        // *** Modify resource table
        // Change package name
        for(PackageBlock packageBlock:packageBlockList){
        String name = packageBlock.getName();
        String newName = name + ".new-name";
        packageBlock.setName(newName);
        }

        // Refresh to re-calculate offsets
        tableBlock.refresh();
        // Save
        File outFile=new File("resources_out.arsc");
        tableBlock.writeBytes(outFile);

        System.out.println("Saved: "+outFile);
        }

```

```java   
    public static void exampleLoadApk() throws IOException{
        File inFile=new File("test.apk");
        File outDir=new File("test_out");

        ApkModule apkModule=ApkModule.loadApkFile(inFile);

        ApkJsonDecoder decoder=new ApkJsonDecoder(apkModule);
        outDir=decoder.writeToDirectory(outDir);
        System.out.println("Decoded to: "+outDir);

        // You can do any logical modification on any json files here

        // To convert back json to apk

        ApkJsonEncoder encoder=new ApkJsonEncoder();
        ApkModule encodedModule=encoder.scanDirectory(outDir);

        File outApk=new File("test_out_re-encoded.apk");
        encodedModule.writeApk(outApk);

        System.out.println("Created apk: "+outApk);
    }
    
```
</details>

