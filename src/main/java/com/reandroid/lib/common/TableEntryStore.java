package com.reandroid.lib.common;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.item.TableString;

import java.util.*;

public class TableEntryStore implements EntryStore{
    private final Map<Byte, Set<PackageBlock>> mPackagesMap;
    public TableEntryStore(){
        this.mPackagesMap = new HashMap<>();
    }
    public void add(TableBlock tableBlock){
        if(tableBlock==null){
            return;
        }
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            add(packageBlock);
        }
    }
    public void add(PackageBlock packageBlock){
        if(packageBlock==null){
            return;
        }
        byte pkgId= (byte) packageBlock.getId();
        Set<PackageBlock> packageBlockSet=getOrCreate(pkgId);
        packageBlockSet.add(packageBlock);
    }
    private Set<PackageBlock> getOrCreate(byte packageId){
        Byte id=packageId;
        Set<PackageBlock> packageBlockSet=mPackagesMap.get(id);
        if(packageBlockSet==null){
            packageBlockSet=new HashSet<>();
            mPackagesMap.put(id, packageBlockSet);
        }
        return packageBlockSet;
    }
    @Override
    public EntryGroup searchEntryGroup(String packageName, String type, String name) {
        return null;
    }
    private Set<TableBlock> getTableBlocks(byte packageId) {
        Set<TableBlock> results=new HashSet<>();
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(packageId);
        if(packageBlockSet!=null){
            for(PackageBlock packageBlock:packageBlockSet){
                TableBlock tableBlock=packageBlock.getTableBlock();
                results.add(tableBlock);
            }
        }
        return results;
    }

    @Override
    public Collection<EntryGroup> getEntryGroups(int resourceId) {
        List<EntryGroup> results=new ArrayList<>();
        byte pkgId= (byte) ((resourceId>>24)&0xff);
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(pkgId);
        if(packageBlockSet==null){
            return results;
        }
        for(PackageBlock packageBlock: packageBlockSet){
            EntryGroup group=packageBlock.getEntryGroup(resourceId);
            if(group!=null){
                results.add(group);
            }
        }
        return results;
    }

    @Override
    public EntryGroup getEntryGroup(int resourceId) {
        byte pkgId= (byte) ((resourceId>>24)&0xff);
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(pkgId);
        if(packageBlockSet==null){
            return null;
        }
        for(PackageBlock packageBlock: packageBlockSet){
            EntryGroup group=packageBlock.getEntryGroup(resourceId);
            if(group!=null && group.pickOne()!=null){
                return group;
            }
        }
        return null;
    }

    @Override
    public Collection<PackageBlock> getPackageBlocks(byte packageId) {
        List<PackageBlock> results=new ArrayList<>();
        Set<PackageBlock> packageBlockSet = mPackagesMap.get(packageId);
        if(packageBlockSet!=null){
            results.addAll(packageBlockSet);
        }
        return results;
    }

    @Override
    public Collection<TableString> getTableStrings(byte packageId, int stringReference) {
        List<TableString> results=new ArrayList<>();
        Set<TableBlock> tableBlockSet=getTableBlocks(packageId);
        for(TableBlock tableBlock:tableBlockSet){
            TableString tableString=tableBlock.getTableStringPool().get(stringReference);
            if(tableString!=null){
                results.add(tableString);
            }
        }
        return results;
    }
}
