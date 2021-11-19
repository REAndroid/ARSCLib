package com.reandroid.lib.arsc.util;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ReferenceItem;
import com.reandroid.lib.arsc.item.TableString;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;

import java.io.*;
import java.util.*;

public class FrameworkTable extends TableBlock {

    public FrameworkTable(){
        super();
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
    public void optimize(){
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
        TableString zero=tableStringPool.get(0);
        zero.set("Framework string table");
        for(TableString tableString:tableStringPool.getStringsArray().listItems()){
            if(tableString==zero){
                continue;
            }
            shrinkTableString(zero, tableString);
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
}
