package com.reandroid;

import com.reandroid.utils.io.FileUtil;
import org.junit.Assert;

import java.io.File;

public class TestUtils {

    public static File getTesApkDirectory(){
        return new File("test-apk");
    }
    public static File getTempDir(){
        File dir = null;
        Throwable throwable = null;
        try{
            dir = FileUtil.getTempDir();
        }catch (Throwable ex){
            throwable = ex;
        }
        Assert.assertNotNull("Failed to create temp root directory: " + throwable, dir);
        dir = new File(dir, "test");
        Assert.assertTrue("Failed to create temp sub dir: " + dir, dir.exists() || dir.mkdirs());
        return dir;
    }
    public static void log(String message){
        System.out.println(message);
    }
}
