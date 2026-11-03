package com.reandroid;

import com.reandroid.utils.io.FileUtil;
import org.junit.Assert;

import java.io.File;

public class TestUtils {

    public static final int MAIN_PACKAGE_ID = 0x7f;
    public static final String MAIN_PACKAGE_NAME = "com.reandroid.arsc";

    public static String squeezeWhiteSpaces(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        s = s.replace('\n', ' ');
        s = s.replace('\r', ' ');
        s = s.replace('\t', ' ');
        s = s.replace('\f', ' ');
        s = s.replace('\b', ' ');
        while (s.contains("  ")) {
            s = s.replace("  ", " ");
        }
        return s;
    }
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
