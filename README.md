# ARSCLib
## Android binary resources read/write java library
This library is developed based on AOSP structure of androidfw/ResourceTypes.h , to totally replace aapt/aapt2
### Read, write, modify and create
* Resource table (resources.arsc)
* Binary xml files (AndroidManifest.xml & resource xml)
### Convert from/to json string
* Decompiling resources to readable XML using traditional techniques is now becoming very challenging due to resource obfuscation (like Proguard & ResGuard)
* Just like SMALI coding this library brings unbeatable coding style

 ```html
 <string name="app_name">My Application</string> 
 ```
can be represented in json as
 ```json 
      {
       "entry_name": "app_name",
       "id": 16,
       "value": {
        "value_type": "STRING",
        "data": "My Application"
       }
 ```
### Works on all java supported platforms (Android, Linux, Windows)
* Use the jar file as dependency
### Check this tool developed using this library
[https://github.com/REAndroid/APKEditor](https://github.com/REAndroid/APKEditor)

<details><summary> <b>See java example</b></summary>

```java
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.lib.arsc.chunk.xml.ResXmlElement;
import com.reandroid.lib.arsc.chunk.xml.ResXmlAttribute;

    public static void exampleManifest() throws IOException{
        File inFile=new File("AndroidManifest.xml");

        // *** Loading AndroidManifest ***
        AndroidManifestBlock manifestBlock = AndroidManifestBlock.load(inFile);

        System.out.println("Package name: "+manifestBlock.getPackageName());

        List<String> usesPermissionList = manifestBlock.getUsesPermissions();
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
        List<ResXmlElement> activityList = manifestBlock.listActivities();
        for(ResXmlElement activityElement:activityList){
          ResXmlAttribute attributeName = activityElement.searchAttributeByResourceId(AndroidManifestBlock.ID_name);
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

