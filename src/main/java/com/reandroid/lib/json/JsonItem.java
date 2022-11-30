package com.reandroid.lib.json;

public interface JsonItem<T> {
    public T toJson();
    public void fromJson(T json);
}
