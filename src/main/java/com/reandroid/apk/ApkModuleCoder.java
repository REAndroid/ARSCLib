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

import java.util.HashMap;
import java.util.Map;

public abstract class ApkModuleCoder {

    private static final int FEW_LOGS_THRESHOLD = 5;

    private APKLogger apkLogger;
    private Map<Object, Integer> mFewLogsCount;

    public ApkModuleCoder() {
    }

    public abstract ApkModule getApkModule();
    public void setApkLogger(APKLogger apkLogger) {
        if (apkLogger == this.apkLogger) {
            return;
        }
        this.apkLogger = apkLogger;
        this.mFewLogsCount = null;
    }
    APKLogger getApkLogger() {
        return apkLogger;
    }
    void logMessage(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if (apkLogger != null) {
            apkLogger.logMessage(msg);
        }
    }
    void logMessageFew(Object countTag, String msg) {
        APKLogger logger = this.apkLogger;
        if (logger == null) {
            return;
        }
        if (countFewLog(countTag)) {
            logger.logVerbose(msg);
        } else {
            logger.logMessage(msg);
        }
    }
    private boolean countFewLog(Object countTag) {
        if (countTag == null) {
            return false;
        }
        Map<Object, Integer> map = this.mFewLogsCount;
        if (map == null) {
            map = new HashMap<>();
            this.mFewLogsCount = map;
        }
        Integer count = map.get(countTag);
        if (count == null) {
            count = 1;
        } else {
            int i = count;
            if (i >= FEW_LOGS_THRESHOLD) {
                return true;
            }
            count = i + 1;
        }
        map.remove(countTag);
        map.put(countTag, count);
        return false;
    }
    void logError(String msg, Throwable tr) {
        APKLogger apkLogger = this.apkLogger;
        if (apkLogger == null || (msg == null && tr == null)) {
            return;
        }
        apkLogger.logError(msg, tr);
    }
    void logVerbose(String msg) {
        APKLogger apkLogger = this.apkLogger;
        if (apkLogger != null) {
            apkLogger.logVerbose(msg);
        }
    }

    public static final String ROOT_DIRECTORY_NAME = "root";
    public static final String SIGNATURE_DIRECTORY_NAME = "signatures";

}
