# ARSCLib
## Android binary resources read/write library

```java
    import com.reandroid.lib.arsc.chunk.TableBlock;
    import com.reandroid.lib.arsc.io.BlockReader;

    public static void example() throws IOException {
        File inFile=new File("resources.arsc");
        BlockReader blockReader=new BlockReader(inFile);
        
        TableBlock tableBlock=new TableBlock();
        tableBlock.readBytes(blockReader);

        // edit tableBlock as desired
        
        tableBlock.refresh();

        File outFile=new File("resources_out.arsc");
        OutputStream outputStream=new FileOutputStream(outFile, false);
        
        tableBlock.writeBytes(outputStream);
        
        outputStream.flush();
        outputStream.close();
    }
    
```
