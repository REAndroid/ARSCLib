package com.reandroid.lib.arsc.util;

import com.reandroid.lib.arsc.chunk.ChunkType;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.header.HeaderBlock;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.io.*;
import java.util.*;

public class FrameworkTable extends TableBlock {

    private String mFrameworkTitle;
    private String mFrameworkName;
    private String mFrameworkVersion;
    public FrameworkTable(){
        super();
    }
    public String getFrameworkTitle(){
        if(mFrameworkTitle==null){
            mFrameworkTitle=loadProperty(PROP_TITLE);
        }
        return mFrameworkTitle;
    }
    public String getFrameworkName(){
        if(mFrameworkName==null){
            mFrameworkName=loadProperty(PROP_NAME);
        }
        return mFrameworkName;
    }
    public String getFrameworkVersion(){
        if(mFrameworkVersion==null){
            mFrameworkVersion=loadProperty(PROP_VERSION);
        }
        return mFrameworkVersion;
    }
    private void setFrameworkTitle(String value){
        mFrameworkTitle=null;
        writeProperty(PROP_TITLE, value);
    }
    public void setFrameworkName(String value){
        mFrameworkName=null;
        writeProperty(PROP_NAME, value);
    }
    public void setFrameworkVersion(String value){
        mFrameworkVersion=null;
        writeProperty(PROP_VERSION, value);
    }
    public int writeTable(File resourcesArscFile) throws IOException{
        File dir=resourcesArscFile.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(resourcesArscFile, false);
        return writeTable(outputStream);
    }
    public int writeTable(OutputStream outputStream) throws IOException{
        return writeBytes(outputStream);
    }
    public void readTable(File resourcesArscFile) throws IOException{
        FileInputStream inputStream=new FileInputStream(resourcesArscFile);
        readTable(inputStream);
    }
    public void readTable(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException{
        int length=super.onWriteBytes(stream);
        stream.flush();
        stream.close();
        return length;
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        super.onReadBytes(reader);
        reader.close();
    }
    public void optimize(String frameworkName, String frameworkVersion){
        Map<Integer, EntryGroup> groupMap=scanAllEntryGroups();
        for(EntryGroup group:groupMap.values()){
            List<EntryBlock> entryBlockList=getEntriesToRemove(group);
            removeEntryBlocks(entryBlockList);
        }
        for(PackageBlock pkg:listPackages()){
            pkg.removeEmpty();
            pkg.refresh();
        }
        optimizeTableString();
        setFrameworkTitle(TITLE_STRING);
        setFrameworkName(frameworkName);
        setFrameworkVersion(frameworkVersion);
        refresh();
    }
    private void optimizeTableString(){
        removeUnusedTableString();
        shrinkTableString();
        removeUnusedTableString();
    }
    private void removeUnusedTableString(){
        TableStringPool tableStringPool=getTableStringPool();
        tableStringPool.getStyleArray().clearChildes();
        tableStringPool.removeUnusedStrings();
        tableStringPool.refresh();
    }
    private void shrinkTableString(){
        TableStringPool tableStringPool=getTableStringPool();
        tableStringPool.getStringsArray().ensureSize(1);
        TableString title=tableStringPool.get(0);
        title.set(PROP_TITLE+":"+TITLE_STRING);
        for(TableString tableString:tableStringPool.getStringsArray().listItems()){
            if(tableString==title){
                continue;
            }
            shrinkTableString(title, tableString);
        }
        tableStringPool.refresh();
    }
    private void shrinkTableString(TableString zero, TableString tableString){
        List<ReferenceItem> allRef = new ArrayList<>(tableString.getReferencedList());
        tableString.removeAllReference();
        for(ReferenceItem item:allRef){
            item.set(zero.getIndex());
        }
        zero.addReference(allRef);
    }
    private void removeEntryBlocks(List<EntryBlock> removeList){
        for(EntryBlock entryBlock:removeList){
            removeEntryBlock(entryBlock);
        }
    }
    private void removeEntryBlock(EntryBlock entryBlock){
        TypeBlock typeBlock=entryBlock.getTypeBlock();
        if(typeBlock==null){
            return;
        }
        typeBlock.removeEntry(entryBlock);

    }
    private List<EntryBlock> getEntriesToRemove(EntryGroup group){
        List<EntryBlock> results=new ArrayList<>();
        EntryBlock mainEntry=group.pickOne();
        if(mainEntry==null){
            return results;
        }
        Iterator<EntryBlock> itr = group.iterator(true);
        while (itr.hasNext()){
            EntryBlock entryBlock=itr.next();
            if(entryBlock==mainEntry){
                continue;
            }
            results.add(entryBlock);
        }
        return results;
    }
    private Map<Integer, EntryGroup> scanAllEntryGroups(){
        Map<Integer, EntryGroup> results=new HashMap<>();
        for(PackageBlock packageBlock:listPackages()){
            Map<Integer, EntryGroup> map=packageBlock.getEntriesGroupMap();
            for(Map.Entry<Integer, EntryGroup> entry:map.entrySet()){
                int id=entry.getKey();
                EntryGroup group=entry.getValue();
                EntryGroup exist=results.get(id);
                if(exist!=null && exist.getDefault()!=null){
                    if(exist.getDefault()!=null){
                        continue;
                    }
                    results.remove(id);
                }
                results.put(id, group);
            }
        }
        return results;
    }
    private TableString writeProperty(String name, String value){
        if(!name.endsWith(":")){
            name=name+":";
        }
        if(value==null){
            value="";
        }
        if(!value.startsWith(name)){
            value=name+value;
        }
        TableString tableString=loadPropertyString(name);
        if(tableString!=null){
            tableString.set(value);
        }else {
            TableStringPool tableStringPool=getTableStringPool();
            tableString=tableStringPool.getOrCreate(value);
        }
        return tableString;
    }
    private String loadProperty(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableString tableString=loadPropertyString(name);
        if(tableString==null){
            return null;
        }
        String str=tableString.get().trim();
        return str.substring(name.length());
    }
    private TableString loadPropertyString(String name){
        if(name==null){
            return null;
        }
        if(!name.endsWith(":")){
            name=name+":";
        }
        TableStringPool tableStringPool=getTableStringPool();
        int max=PROP_COUNT;
        for(int i=0;i<max;i++){
            TableString tableString=tableStringPool.get(i);
            if(tableString==null){
                break;
            }
            String str=tableString.get();
            if(str==null){
                continue;
            }
            str=str.trim();
            if(str.startsWith(name)){
                return tableString;
            }
        }
        return null;
    }
    @Override
    public String toString(){
        HeaderBlock headerBlock=getHeaderBlock();
        if(headerBlock.getChunkType()!= ChunkType.TABLE){
            return super.toString();
        }
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": SIZE=").append(headerBlock.getChunkSize());
        String str=getFrameworkTitle();
        builder.append("\n");
        if(str==null){
            builder.append(PROP_TITLE).append(":null");
        }else {
            builder.append(str);
        }
        str=getFrameworkName();
        builder.append("\n  ").append(PROP_NAME).append(":");
        if(str==null){
            builder.append("null");
        }else {
            builder.append(str);
        }
        str=getFrameworkVersion();
        builder.append("\n  ").append(PROP_VERSION).append(":");
        if(str==null){
            builder.append("null");
        }else {
            builder.append(str);
        }
        Collection<PackageBlock> allPkg = listPackages();
        builder.append("\n  PACKAGES=").append(allPkg.size());
        for(PackageBlock packageBlock:allPkg){
            builder.append("\n    ");
            builder.append(String.format("0x%02x", packageBlock.getPackageId()));
            builder.append(":").append(packageBlock.getPackageName());
        }
        return builder.toString();
    }
    private static final String TITLE_STRING="Framework table";
    private static final String PROP_TITLE="TITLE";
    private static final String PROP_NAME="NAME";
    private static final String PROP_VERSION="VERSION";
    private static final int PROP_COUNT=10;
}
