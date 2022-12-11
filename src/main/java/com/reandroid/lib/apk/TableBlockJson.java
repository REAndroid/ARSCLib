package com.reandroid.lib.apk;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class TableBlockJson {
    private final TableBlock tableBlock;
    public TableBlockJson(TableBlock tableBlock){
        this.tableBlock=tableBlock;
    }
    public void writeJsonFiles(File outDir) throws IOException {
        for(PackageBlock packageBlock: tableBlock.listPackages()){
            writeJsonFiles(outDir, packageBlock);
        }
    }
    private void writeJsonFiles(File rootDir, PackageBlock packageBlock) throws IOException {
        File pkgDir=new File(rootDir, getDirName(packageBlock));
        if(!pkgDir.exists()){
            pkgDir.mkdirs();
        }
        File infoFile=new File(pkgDir, PackageBlock.JSON_FILE_NAME);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(PackageBlock.NAME_package_id, packageBlock.getId());
        jsonObject.put(PackageBlock.NAME_package_name, packageBlock.getName());
        jsonObject.write(infoFile);
        for(SpecTypePair specTypePair: packageBlock.listAllSpecTypePair()){
            for(TypeBlock typeBlock:specTypePair.getTypeBlockArray().listItems()){
                writeJsonFiles(pkgDir, typeBlock);
            }
        }
    }
    private void writeJsonFiles(File pkgDir, TypeBlock typeBlock) throws IOException {
        String name= getFileName(typeBlock)+".json";
        File file=new File(pkgDir, name);
        JSONObject jsonObject = typeBlock.toJson();
        jsonObject.write(file);
    }
    private String getFileName(TypeBlock typeBlock){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", typeBlock.getTypeId()));
        String name= typeBlock.getTypeName();
        builder.append('-').append(name);
        builder.append(typeBlock.getResConfig().getQualifiers());
        return builder.toString();
    }
    private String getDirName(PackageBlock packageBlock){
        StringBuilder builder=new StringBuilder();
        builder.append(String.format("0x%02x", packageBlock.getId()));
        String name= packageBlock.getName();
        if(name!=null){
            builder.append('-');
            builder.append(name);
        }
        return builder.toString();
    }
}
