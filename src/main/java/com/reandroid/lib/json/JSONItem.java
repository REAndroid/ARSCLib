package com.reandroid.lib.json;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class JSONItem {
    public abstract Writer write(Writer writer, int indentFactor, int indent) throws JSONException;

    public void write(File file) throws IOException{
        write(file, INDENT_FACTOR);
    }
    public void write(File file, int indentFactor) throws IOException{
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        write(outputStream, indentFactor);
        outputStream.close();
    }
    public void write(OutputStream outputStream) throws IOException {
        write(outputStream, INDENT_FACTOR);
    }
    public void write(OutputStream outputStream, int indentFactor) throws IOException {
        Writer writer=new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer= write(writer, indentFactor, 0);
        writer.flush();
        writer.close();
    }
    public Writer write(Writer writer) throws JSONException {
        return this.write(writer, 0, 0);
    }
    @Override
    public String toString() {
        try {
            return this.toString(0);
        } catch (Exception e) {
            return null;
        }
    }
    public String toString(int indentFactor) throws JSONException {
        StringWriter w = new StringWriter();
        synchronized (w.getBuffer()) {
            return this.write(w, indentFactor, 0).toString();
        }
    }

    private static final int INDENT_FACTOR=1;
}
