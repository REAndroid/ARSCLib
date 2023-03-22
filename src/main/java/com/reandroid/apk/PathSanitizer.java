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

import com.reandroid.archive.InputSource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathSanitizer {
    private final ApkModule apkModule;
    private APKLogger apkLogger;
    private final Set<String> mSanitizedPaths;
    public PathSanitizer(ApkModule apkModule){
        this.apkModule = apkModule;
        this.apkLogger = apkModule.getApkLogger();
        this.mSanitizedPaths = new HashSet<>();
    }
    public void sanitize(){
        mSanitizedPaths.clear();
        logMessage("Sanitizing paths ...");
        List<ResFile> resFileList = apkModule.listResFiles();
        for(ResFile resFile:resFileList){
            sanitize(resFile);
        }
        List<InputSource> sourceList = apkModule.getApkArchive().listInputSources();
        for(InputSource inputSource:sourceList){
            sanitize(inputSource, 1, false);
        }
        logMessage("DONE = "+mSanitizedPaths.size());
    }
    private void sanitize(ResFile resFile){
        InputSource inputSource = resFile.getInputSource();
        String replace = sanitize(inputSource, 3, true);
        if(replace==null){
            return;
        }
        resFile.setFilePath(replace);
    }
    private String sanitize(InputSource inputSource, int depth, boolean fixedDepth){
        String name = inputSource.getName();
        if(mSanitizedPaths.contains(name)){
            return null;
        }
        mSanitizedPaths.add(name);
        String alias = inputSource.getAlias();
        String replace = sanitize(alias, depth, fixedDepth);
        if(alias.equals(replace)){
            return null;
        }
        inputSource.setAlias(replace);
        logVerbose("REN: '"+alias+"' -> '"+replace+"'");
        return replace;
    }

    private String sanitize(String name, int depth, boolean fixedDepth){
        StringBuilder builder = new StringBuilder();
        String[] nameSplit = name.split("/");

        boolean pathIsLong = name.length() >= MAX_PATH_LENGTH;
        int length = nameSplit.length;
        for(int i=0;i<length;i++){
            String split = nameSplit[i];
            boolean good = isGoodSimpleName(split);
            if(!good || (pathIsLong && i>=depth)){
                split = createUniqueName(name);
                appendPathName(builder, split);
                break;
            }
            if(fixedDepth && i>=(depth-1)){
                if(i < length-1){
                    split = createUniqueName(name);
                }
                appendPathName(builder, split);
                break;
            }
            appendPathName(builder, split);
        }
        return builder.toString();
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    private String getLogTag(){
        return "[SANITIZE]: ";
    }
    private void logMessage(String msg){
        APKLogger logger = this.apkLogger;
        if(logger!=null){
            logger.logMessage(getLogTag()+msg);
        }
    }
    private void logVerbose(String msg){
        APKLogger logger = this.apkLogger;
        if(logger!=null){
            logger.logVerbose(getLogTag()+msg);
        }
    }

    private static void appendPathName(StringBuilder builder, String name){
        if(builder.length()>0){
            builder.append('/');
        }
        builder.append(name);
    }
    private static String createUniqueName(String name){
        int hash = name.hashCode();
        return String.format("alias_%08x", hash).toLowerCase();
    }
    private static boolean isGoodSimpleName(String name){
        if(name==null){
            return false;
        }
        String alias = sanitizeSimpleName(name);
        return name.equals(alias);
    }
    public static String sanitizeSimpleName(String name){
        if(name==null){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        char[] chars = name.toCharArray();
        boolean skipNext = true;
        int length = 0;
        int lengthMax = MAX_NAME_LENGTH;
        for(int i=0;i<chars.length;i++){
            if(length>=lengthMax){
                break;
            }
            char ch = chars[i];
            if(isGoodFileNameSymbol(ch)){
                if(!skipNext){
                    builder.append(ch);
                    length++;
                }
                skipNext=true;
                continue;
            }
            if(!isGoodFileNameChar(ch)){
                skipNext = true;
                continue;
            }
            builder.append(ch);
            length++;
            skipNext=false;
        }
        if(length==0){
            return null;
        }
        return builder.toString();
    }

    private static boolean isGoodFileNameSymbol(char ch){
        return ch == '.'
                || ch == '+'
                || ch == '-'
                || ch == '_'
                || ch == '#';
    }
    private static boolean isGoodFileNameChar(char ch){
        return (ch >= '0' && ch <= '9')
                || (ch >= 'A' && ch <= 'Z')
                || (ch >= 'a' && ch <= 'z');
    }

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_PATH_LENGTH = 100;
}
