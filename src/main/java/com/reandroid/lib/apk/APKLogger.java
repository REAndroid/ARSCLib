package com.reandroid.lib.apk;

public interface APKLogger {
    void logMessage(String msg);
    void logError(String msg, Throwable tr);
    void logVerbose(String msg);
}
