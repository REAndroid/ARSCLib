# ARSCLib
## Android binary resources read/write library

```java
    import com.reandroid.lib.arsc.chunk.TableBlock;
    import com.reandroid.lib.arsc.io.BlockReader;

    public static void example() throws IOException {
        File inFile=new File("resources.arsc");
        
        TableBlock tableBlock=new TableBlock();
        tableBlock.readBytes(inFile);

        //edit tableBlock as desired, for example to change the package:
        PackageBlock packageBlock=tableBlock.getPackageArray().get(0);
        packageBlock.setPackageName("com.new.package.name");
        
        //refresh to recalculate offsets
        tableBlock.refresh();
        
        //save the edited table
        File outFile=new File("resources_out.arsc");        
        tableBlock.writeBytes(outFile);        
    }

    public static void exampleManifest() throws IOException {
        File inFile=new File("AndroidManifest.xml");

        AndroidManifestBlock manifestBlock=new AndroidManifestBlock();
        manifestBlock.readBytes(file);

        //edit AndroidManifest as desired, for example to change the package:
        
        manifestBlock.setPackageName("com.new.package.name");

        //refresh to recalculate offsets
        manifestBlock.refresh();

        //save the edited table
        File outFile=new File("AndroidManifest_out.xml");
        manifestBlock.writeBytes(outFile);
    }
    
```
