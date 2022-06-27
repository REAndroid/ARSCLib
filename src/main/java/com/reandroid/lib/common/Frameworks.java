package com.reandroid.lib.common;

import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.util.FrameworkTable;

import java.io.IOException;
import java.io.InputStream;

public class Frameworks {
    private static FrameworkTable android_table;
    private static boolean load_once;
    public static FrameworkTable getAndroid(){
        if(android_table!=null || load_once){
            return android_table;
        }
        load_once=true;
        FrameworkTable frameworkTable=null;
        try {
            frameworkTable = loadFramework(ANDROID_FRAMEWORK);
        } catch (IOException e) {
        }
        android_table=frameworkTable;
        return android_table;
    }
    private static FrameworkTable loadFramework(String name) throws IOException {
        InputStream inputStream=Frameworks.class.getResourceAsStream(name);
        if(inputStream==null){
            return null;
        }
        BlockReader reader=new BlockReader(inputStream);
        FrameworkTable frameworkTable=new FrameworkTable();
        frameworkTable.readBytes(reader);
        return frameworkTable;
    }

    private static final String ANDROID_FRAMEWORK= "/fwk/android_resources_30.arsc";
}
