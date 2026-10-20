package com.reandroid.arsc.value.bag;

import com.reandroid.arsc.item.StringItem;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.value.ResValueMap;
import com.reandroid.arsc.value.DataType;

public abstract class BagItem {
    protected final ResValueMap mBagItem;
    private final DataType dataType;
    private final int data;
    private final StringItem string;

    protected BagItem(ResValueMap bagItem) {
        this.mBagItem = bagItem;
        this.dataType = null;
        this.string = null;
        this.data = 0;
    }

    protected BagItem(DataType dataType, int data) {
        if (dataType == DataType.STRING) {
            throw new IllegalArgumentException("Use the string constructor instead");
        }
        this.mBagItem = null;
        this.string = null;
        this.dataType = dataType;
        this.data = data;
    }

    protected BagItem(StringItem str) {
        this.string = str;
        this.mBagItem = null;
        this.dataType = DataType.STRING;
        this.data = 0;
    }

    public DataType getValueType() {
        if (mBagItem != null) {
            return mBagItem.getDataType();
        }
        return dataType;
    }

    public int getValue() {
        if (mBagItem != null) {
            return mBagItem.getData();
        } else if (dataType == DataType.STRING) {
            return string.getIndex();
        } else {
            return data;
        }
    }

    public void copyTo(ResValueMap target) {
        if (mBagItem != null) {
            target.setTypeAndData(mBagItem.getDataType(), mBagItem.getData());
        } else if (dataType == DataType.STRING) {
            TableStringPool targetStrPool = (TableStringPool) target.getStringPool();
            if (targetStrPool == string.getParent(TableStringPool.class)) {
                target.setTypeAndData(DataType.STRING, string.getIndex());
            } else {
                target.setTypeAndData(DataType.STRING, targetStrPool.getOrCreate(string.get()).getIndex());
            }
        } else {
            target.setTypeAndData(dataType, data);
        }
    }

    public ResValueMap getBagItem() {
        return mBagItem;
    }

    public boolean hasStringValue() {
        return getValueType() == DataType.STRING;
    }

    public boolean hasReferenceValue() {
        return getValueType() == DataType.REFERENCE;
    }

    public String getStringValue() {
        if (mBagItem != null) {
            return mBagItem.getDataAsString();
        } else if (dataType == DataType.STRING) {
            return string.getHtml();
        } else {
            throw new IllegalArgumentException("Not a string");
        }
    }
}
