package com.reandroid.lib.arsc;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {
    private static Properties sProperties;

    public static String getName(){
        Properties properties=getProperties();
        return properties.getProperty("lib.name", "ARSCLib");
    }
    public static String getVersion(){
        Properties properties=getProperties();
        return properties.getProperty("lib.version", "");
    }
    public static String getRepo(){
        Properties properties=getProperties();
        return properties.getProperty("lib.repo", "https://github.com/REAndroid");
    }
    public static String getDescription(){
        Properties properties=getProperties();
        return properties.getProperty("lib.description", "Failed to load properties");
    }
    
    private static Properties getProperties(){
        if(sProperties==null){
            sProperties=loadProperties();
        }
        return sProperties;
    }
    private static Properties loadProperties(){
        InputStream inputStream=BuildInfo.class.getResourceAsStream("/lib.properties");
        Properties properties=new Properties();
        try{
            properties.load(inputStream);
        }catch (Exception ignored){
        }
        return properties;
    }
}
