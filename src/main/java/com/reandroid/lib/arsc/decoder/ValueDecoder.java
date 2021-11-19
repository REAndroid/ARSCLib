package com.reandroid.lib.arsc.decoder;

import com.reandroid.lib.arsc.value.ValueType;

public class ValueDecoder {
    public static String decode(ValueType valueType, int data){
        if(valueType==null){
            return null;
        }
        switch (valueType){
            case INT_BOOLEAN:
                return decodeBoolean(data);
            case FIRST_COLOR_INT:
            case INT_COLOR_ARGB4:
            case INT_COLOR_ARGB8:
            case INT_COLOR_RGB4:
            case INT_COLOR_RGB8:
            case LAST_COLOR_INT:
                return decodeColor(data);
            case DIMENSION:
            case FLOAT:
            case FRACTION:
                return decodeDimensionOrFloat(valueType, data);
            case INT_HEX:
                return decodeHex(data);
            case INT_DEC:
            case FIRST_INT:
            case LAST_INT:
                return decodeInt(data);
        }
        return null;
    }
    private static String decodeHex(int rawVal){
        return String.format("0x%x", rawVal);
    }
    private static String decodeInt(int rawVal){
        return String.valueOf(rawVal);
    }

    private static String decodeBoolean(int data){
        if(data == 0xFFFFFFFF){
            return "true";
        }
        return "false";
    }

    private static String decodeColor(int rawVal){
        String hex=String.format("%x", rawVal);
        if(hex.length()<=6){
            return String.format("#%06x", rawVal);
        }
        return String.format("#%08x", rawVal);
    }
    private static String decodeDimensionOrFloat(ValueType valueType, int rawVal){
        return decodeFloat(rawVal, valueType);
    }
    private static String decodeFloat(int val, ValueType valueType){
        if(valueType==ValueType.FLOAT){
            float f=Float.intBitsToFloat(val);
            return Float.toString(f);
        }
        float f=complexToFloat(val);
        String unit="";
        switch (valueType){
            case FRACTION:
                f=f*100;
                if((val & 0x3)==0){
                    unit="%";
                }else {
                    unit="%p";
                }
                break;
            case DIMENSION:
                int i=(val & 0xf);
                unit=getDimensionUnit(i);
                break;
        }
        return Float.toString(f)+unit;
    }
    private static float complexToFloat(int complex) {
        int y=(complex >> 4) & 0x3;
        float result=complex & 0xffffff00;
        float y2=RADIX_MULTS[y];
        result=result * y2;
        return result;
    }
    private static String getDimensionUnit(int index){
        if(index<0 || index>DIMENSION_UNIT_STRS.length){
            index=1;
        }
        return DIMENSION_UNIT_STRS[index];
    }
    private static int getDimensionIndex(String unit){
        String[] dims=DIMENSION_UNIT_STRS;
        for(int i=0;i<dims.length;i++){
            if(dims[i].equals(unit)){
                return i;
            }
        }
        return 0;
    }

    private static final String[] DIMENSION_UNIT_STRS = new String[] { "px", "dip", "sp", "pt", "in", "mm" };
    private static final float MANTISSA_MULT = 1.0f / (1 << 8);
    private static final float[] RADIX_MULTS = new float[] {
            1.0f * MANTISSA_MULT, 1.0f / (1 << 7) * MANTISSA_MULT,
            1.0f / (1 << 15) * MANTISSA_MULT, 1.0f / (1 << 23) * MANTISSA_MULT };
}
