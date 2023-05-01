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
import com.reandroid.archive2.block.ApkSignatureBlock;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class ApkDecoder {
    private final Set<String> mDecodedPaths;
    private APKLogger apkLogger;
    private boolean mLogErrors;

    public ApkDecoder(){
        mDecodedPaths = new HashSet<>();
    }
    public final void decodeTo(File outDir) throws IOException{
        reset();
        onDecodeTo(outDir);
    }
    abstract void onDecodeTo(File outDir) throws IOException;

    boolean containsDecodedPath(String path){
        return mDecodedPaths.contains(path);
    }
    void addDecodedPath(String path){
        mDecodedPaths.add(path);
    }
    void writePathMap(File dir, Collection<? extends InputSource> sourceList) throws IOException {
        PathMap pathMap = new PathMap();
        pathMap.add(sourceList);
        File file = new File(dir, PathMap.JSON_FILE);
        pathMap.toJson().write(file);
    }
    void dumpSignatures(File outDir, ApkSignatureBlock signatureBlock) throws IOException {
        if(signatureBlock == null){
            return;
        }
        logMessage("Dumping signatures ...");
        File dir = new File(outDir, ApkUtil.SIGNATURE_DIR_NAME);
        signatureBlock.writeSplitRawToDirectory(dir);
    }
    void logOrThrow(String message, IOException exception) throws IOException{
        if(isLogErrors()){
            logError(message, exception);
            return;
        }
        if(message == null && exception == null){
            return;
        }
        if(exception == null){
            exception = new IOException(message);
        }
        throw exception;
    }
    private void reset(){
        mDecodedPaths.clear();
    }

    public boolean isLogErrors() {
        return mLogErrors;
    }
    public void setLogErrors(boolean logErrors) {
        this.mLogErrors = logErrors;
    }

    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    APKLogger getApkLogger() {
        return apkLogger;
    }
    void logMessage(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    void logError(String msg, Throwable tr) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger == null || (msg == null && tr == null)){
            return;
        }
        apkLogger.logError(msg, tr);
    }
    void logVerbose(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }
}
