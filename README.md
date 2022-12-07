# ARSCLib
## Android binary resources read/write library

```java
    import com.reandroid.lib.arsc.chunk.TableBlock;
    import com.reandroid.lib.arsc.io.BlockReader;

    public static void example() throws IOException{
        File inFile=new File("test.apk");
        File outDir=new File("test_out");

        ApkModule apkModule=ApkModule.loadApkFile(inFile);

        ApkJsonDecoder decoder=new ApkJsonDecoder(apkModule);
        outDir=decoder.writeToDirectory(outDir);
        System.out.println("Decoded to: "+outDir);

        // You can do any logical modification on any json files

        // To convert back json to apk

        ApkJsonEncoder encoder=new ApkJsonEncoder();
        ApkModule encodedModule=encoder.scanDirectory(outDir);

        File outApk=new File("test_out_re-encoded.apk");
        encodedModule.writeApk(outApk);

        System.out.println("Created apk: "+outApk);
        }
    
```
