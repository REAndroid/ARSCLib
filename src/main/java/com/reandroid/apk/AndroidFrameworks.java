 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AndroidFrameworks {
    private static Map<Integer, String> resource_paths;
    private static FrameworkApk mCurrent;

    public static void setCurrent(FrameworkApk current){
        synchronized (AndroidFrameworks.class){
            mCurrent = current;
        }
    }
    public static FrameworkApk getCurrent(){
        FrameworkApk current = mCurrent;
        if(current==null){
            return null;
        }
        if(current.isDestroyed()){
            mCurrent = null;
            return null;
        }
        return current;
    }
    public static FrameworkApk getLatest() throws IOException {
        Map<Integer, String> pathMap = getResourcePaths();
        synchronized (AndroidFrameworks.class){
            int latest = getHighestVersion();
            FrameworkApk current = getCurrent();
            if(current!=null && latest==current.getVersionCode()){
                return current;
            }
            String path = pathMap.get(latest);
            if(path == null){
                throw new IOException("Could not get latest framework");
            }
            return loadResource(latest);
        }
    }
    public static FrameworkApk getBestMatch(int version) throws IOException {
        Map<Integer, String> pathMap = getResourcePaths();
        synchronized (AndroidFrameworks.class){
            int best = getBestMatchVersion(version);
            FrameworkApk current = getCurrent();
            if(current!=null && best==current.getVersionCode()){
                return current;
            }
            String path = pathMap.get(best);
            if(path == null){
                throw new IOException("Could not get framework for version = "+version);
            }
            return loadResource(best);
        }
    }
    public static void destroyCurrent(){
        synchronized (AndroidFrameworks.class){
            FrameworkApk current = mCurrent;
            if(current==null){
                return;
            }
            current.destroy();
        }
    }
    private static int getHighestVersion() {
        Map<Integer, String> pathMap = getResourcePaths();
        int highest = 0;
        for(int id:pathMap.keySet()){
            if(highest==0){
                highest = id;
                continue;
            }
            if(id>highest){
                highest = id;
            }
        }
        return highest;
    }
    private static int getBestMatchVersion(int version) {
        Map<Integer, String> pathMap = getResourcePaths();
        if(pathMap.containsKey(version)){
            return version;
        }
        int highest = 0;
        int best = 0;
        int prevDifference = 0;
        for(int id:pathMap.keySet()){
            if(highest==0){
                highest = id;
                best = id;
                prevDifference = version*2 + 1000;
                continue;
            }
            if(id>highest){
                highest = id;
            }
            int diff = id-version;
            if(diff<0){
                diff=-diff;
            }
            if(diff<prevDifference || (diff==prevDifference && id>best)){
                best = id;
                prevDifference = diff;
            }
        }
        return best;
    }
    public static FrameworkApk loadResource(int version) throws IOException {
        String path = getResourcePath(version);
        if(path == null){
            throw new IOException("No resource found for version: "+version);
        }
        String simpleName = toSimpleName(path);
        return FrameworkApk.loadApkBuffer(simpleName, AndroidFrameworks.class.getResourceAsStream(path));
    }
    private static String getResourcePath(int version){
        return getResourcePaths().get(version);
    }
    private static Map<Integer, String> getResourcePaths(){
        if(resource_paths!=null){
            return resource_paths;
        }
        synchronized (AndroidFrameworks.class){
            resource_paths = scanAvailableResourcePaths();
            return resource_paths;
        }
    }
    private static Map<Integer, String> scanAvailableResourcePaths(){
        Map<Integer, String> results = new HashMap<>();
        int maxSearch = 50;
        for(int version=20; version<maxSearch; version++){
            String path = toResourcePath(version);
            if(!isAvailable(path)){
                continue;
            }
            results.put(version, path);
            maxSearch = version + 20;
        }
        return results;
    }
    private static String toSimpleName(String path){
        int i = path.lastIndexOf('/');
        if(i<0){
            i = path.lastIndexOf(File.separatorChar);
        }
        if(i>0){
            i++;
            path = path.substring(i);
        }
        i = path.lastIndexOf('.');
        if(i>=0){
            path = path.substring(0, i);
        }
        return path;
    }
    private static int parseVersion(String name){
        int i = name.lastIndexOf('/');
        if(i<0){
            i = name.lastIndexOf(File.separatorChar);
        }
        if(i>0){
            i++;
            name = name.substring(i);
        }
        i = name.lastIndexOf('-');
        if(i>=0){
            i++;
            name = name.substring(i);
        }
        i = name.indexOf('.');
        if(i>=0){
            name = name.substring(0, i);
        }
        return Integer.parseInt(name);
    }
    private static boolean isAvailable(String path){
        InputStream inputStream = AndroidFrameworks.class.getResourceAsStream(path);
        if(inputStream==null){
            return false;
        }
        closeQuietly(inputStream);
        return true;
    }
    private static void closeQuietly(InputStream stream){
        if(stream == null){
            return;
        }
        try {
            stream.close();
        } catch (IOException ignored) {
        }
    }
    private static String toResourcePath(int version){
        return ANDROID_RESOURCE_DIRECTORY + ANDROID_PACKAGE
                + '-' + version
                +FRAMEWORK_EXTENSION;
    }
    private static final String ANDROID_RESOURCE_DIRECTORY = "/frameworks/android/";
    private static final String ANDROID_PACKAGE = "android";
    private static final String FRAMEWORK_EXTENSION = ".apk";
}
