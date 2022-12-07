package com.reandroid.lib.apk;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.value.ResConfig;
import com.reandroid.lib.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class TableBlockJsonBuilder {
    private final StringPoolBuilder poolBuilder;
    public TableBlockJsonBuilder(){
        poolBuilder=new StringPoolBuilder();
    }
    public TableBlock scanDirectory(File resourcesDir) throws IOException {
        if(!resourcesDir.isDirectory()){
            throw new IOException("No such directory: "+resourcesDir);
        }
        List<File> pkgDirList=ApkUtil.listDirectories(resourcesDir);
        if(pkgDirList.size()==0){
            throw new IOException("No package sub directory found in : "+resourcesDir);
        }
        TableBlock tableBlock=new TableBlock();
        poolBuilder.scanDirectory(resourcesDir);
        poolBuilder.apply(tableBlock);
        for(File pkgDir:pkgDirList){
            scanPackageDirectory(tableBlock, pkgDir);
        }
        tableBlock.refresh();
        return tableBlock;
    }
    private void scanPackageDirectory(TableBlock tableBlock, File pkgDir) throws IOException{
        File pkgFile=new File(pkgDir, "package.json");
        if(!pkgFile.isFile()){
            throw new IOException("Invalid package directory! Package file missing: "+pkgFile);
        }
        FileInputStream inputStream=new FileInputStream(pkgFile);
        JSONObject jsonObject=new JSONObject(inputStream);
        int id = jsonObject.getInt(PackageBlock.NAME_package_id);
        String name=jsonObject.optString(PackageBlock.NAME_package_name);
        PackageBlock pkg=tableBlock.getPackageArray().getOrCreate((byte) id);
        pkg.setName(name);
        List<File> typeFileList=ApkUtil.listFiles(pkgDir, ".json");
        typeFileList.remove(pkgFile);
        for(File typeFile:typeFileList){
            loadType(pkg, typeFile);
        }
    }
    private void loadType(PackageBlock packageBlock, File typeJsonFile) throws IOException{
        FileInputStream inputStream=new FileInputStream(typeJsonFile);
        JSONObject jsonObject=new JSONObject(inputStream);
        int id= jsonObject.getInt("id");
        JSONObject configObj=jsonObject.getJSONObject("config");
        ResConfig resConfig=new ResConfig();
        resConfig.fromJson(configObj);
        TypeBlock typeBlock=packageBlock.getSpecTypePairArray().getOrCreate((byte) id, resConfig);
        typeBlock.fromJson(jsonObject);
    }
}
