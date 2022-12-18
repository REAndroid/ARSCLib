package com.reandroid.archive;

public interface WriteProgress {
    void onCompressFile(String path, int mode, long writtenBytes);
}
