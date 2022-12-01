package com.reandroid.lib.json;

public interface JSONConvert<T extends JSONItem> {
    public T toJson();
    public void fromJson(T json);
}
