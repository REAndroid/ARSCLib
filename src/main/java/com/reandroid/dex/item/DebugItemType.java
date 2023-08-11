package com.reandroid.dex.item;

public class DebugItemType {
    // The debug items that directly correspond with one of the dexlib2.iface.debug interfaces
    public static final int START_LOCAL = 0x03;
    public static final int END_LOCAL = 0x05;
    public static final int RESTART_LOCAL = 0x06;
    public static final int PROLOGUE_END = 0x07;
    public static final int EPILOGUE_BEGIN = 0x08;
    public static final int SET_SOURCE_FILE = 0x09;
    public static final int LINE_NUMBER = 0x0a;

    // Other items, which are typically handled internally
    public static final int END_SEQUENCE = 0x00;
    public static final int ADVANCE_PC = 0x01;
    public static final int ADVANCE_LINE = 0x02;
    public static final int START_LOCAL_EXTENDED = 0x04;
}
