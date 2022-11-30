package com.reandroid.lib.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class JsonUtil {
    public static void writeJSONObject(JsonItem<JSONObject> jsonItem, File file) throws IOException{
        writeJSONObject(jsonItem, file, INDENT_FACTOR);
    }
    public static void writeJSONObject(JsonItem<JSONObject> jsonItem, File file, int indentFactor) throws IOException{
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        writeJSONObject(jsonItem, outputStream, indentFactor);
    }
    public static void writeJSONObject(JsonItem<JSONObject> jsonItem, OutputStream outputStream) throws IOException{
        writeJSONObject(jsonItem, outputStream, INDENT_FACTOR);
    }
    public static void writeJSONObject(JsonItem<JSONObject> jsonItem, OutputStream outputStream, int indentFactor) throws IOException{
        Writer writer=new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer= writeJSONObject(jsonItem, writer, indentFactor);
        writer.flush();
        writer.close();
    }
    public static Writer writeJSONObject(JsonItem<JSONObject> jsonItem, Writer writer, int indentFactor){
        JSONObject jsonObject=jsonItem.toJson();
        return jsonObject.write(writer, indentFactor, 0);
    }


    public static void writeJSONArray(JsonItem<JSONArray> jsonItem, File file) throws IOException{
        writeJSONArray(jsonItem, file, INDENT_FACTOR);
    }
    public static void writeJSONArray(JsonItem<JSONArray> jsonItem, File file, int indentFactor) throws IOException{
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        FileOutputStream outputStream=new FileOutputStream(file);
        writeJSONArray(jsonItem, outputStream, indentFactor);
    }
    public static void writeJSONArray(JsonItem<JSONArray> jsonItem, OutputStream outputStream) throws IOException{
        writeJSONArray(jsonItem, outputStream, INDENT_FACTOR);
    }
    public static void writeJSONArray(JsonItem<JSONArray> jsonItem, OutputStream outputStream, int indentFactor) throws IOException{
        Writer writer=new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer= writeJSONArray(jsonItem, writer, indentFactor);
        writer.flush();
        writer.close();
    }
    public static Writer writeJSONArray(JsonItem<JSONArray> jsonItem, Writer writer, int indentFactor){
        JSONArray jsonArray=jsonItem.toJson();
        return jsonArray.write(writer, indentFactor, 0);
    }

    private static final int INDENT_FACTOR=4;
}
