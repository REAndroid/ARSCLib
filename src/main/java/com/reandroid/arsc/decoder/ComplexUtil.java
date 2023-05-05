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
package com.reandroid.arsc.decoder;

public class ComplexUtil {

    public static String decodeComplex(boolean fraction, int complex_value){
        int radixFlag = (complex_value >> COMPLEX_RADIX_SHIFT) & COMPLEX_RADIX_MASK;
        Radix radix = Radix.forFlag(radixFlag);

        int mantissa = (complex_value & ( COMPLEX_MANTISSA_MASK <<COMPLEX_MANTISSA_SHIFT));
        float value = mantissa * radix.getMultiplier();

        int unit_type = (complex_value >> COMPLEX_UNIT_SHIFT) & COMPLEX_UNIT_MASK;
        Unit unit = Unit.fromFlag(fraction, unit_type);
        return radix.formatFloat(fraction, value) + unit.getSymbol();
    }
    public static int encodeComplex(float value, String unit){
        return encodeComplex(value, Unit.fromSymbol(unit));
    }
    public static int encodeComplex(float value, Unit unit){
        boolean neg = value < 0;
        if (neg) {
            value = -value;
        }
        long bits = (long)(value*(1<<23) + 0.5f);

        Radix radix = Radix.getRadix(bits);
        int mantissa = (int)((bits>>radix.getShift()) & COMPLEX_MANTISSA_MASK);
        if (neg) {
            mantissa = (-mantissa) & COMPLEX_MANTISSA_MASK;
        }
        int result = (radix.getFlag()<<COMPLEX_RADIX_SHIFT)
                | (mantissa<<COMPLEX_MANTISSA_SHIFT);

        result = result | unit.getFlag();

        return result;
    }

    public enum Unit {
        PX(0, "px"),
        DP(1, "dp"),
        DIP(1, "dip"),
        SP(2, "sp"),
        PT(3, "pt"),
        IN(4, "in"),
        MM(5, "mm"),
        FRACTION(0, "%"),
        FRACTION_PARENT(1, "%p");

        private final int flag;
        private final String symbol;
        Unit(int flag, String symbol) {
            this.flag = flag;
            this.symbol = symbol;
        }
        public int getFlag() {
            return flag;
        }
        public String getSymbol(){
            return symbol;
        }
        @Override
        public String toString(){
            return getSymbol();
        }
        public static Unit fromFlag(boolean fraction, int flag){
            Unit unit;
            if(fraction){
                unit = fromFlag(FRACTIONS, flag);
            }else {
                unit = fromFlag(DIMENSIONS, flag);
            }
            if(unit!=null){
                return unit;
            }
            throw new NumberFormatException("Unknown unit flag = "+flag
                    +" for"+(fraction?"fraction":"dimension"));
        }
        private static Unit fromFlag(Unit[] units, int flag){
            for(Unit unit: units){
                if(flag == unit.getFlag()){
                    return unit;
                }
            }
            return null;
        }
        public static Unit fromSymbol(String symbol){
            if(symbol == null){
                return null;
            }
            Unit unit = fromSymbol(DIMENSIONS, symbol);
            if(unit == null){
                unit = fromSymbol(FRACTIONS, symbol);
            }
            return unit;
        }
        private static Unit fromSymbol(Unit[] units, String symbol){
            for(Unit unit: units){
                if(unit.getSymbol().equals(symbol)){
                    return unit;
                }
            }
            return null;
        }

        private static final Unit[] DIMENSIONS = new Unit[]{
                PX,
                DP,
                DIP,
                SP,
                PT,
                IN,
                MM
        };
        private static final Unit[] FRACTIONS = new Unit[]{
                FRACTION,
                FRACTION_PARENT
        };
    }
    public enum Radix{
        RADIX_23p0(0, 23, MANTISSA_MULT),
        RADIX_16p7(1, 16, 1.0f/(1<<7) * MANTISSA_MULT),
        RADIX_8p15(2, 8, 1.0f/(1<<15) * MANTISSA_MULT),
        RADIX_0p23(3, 0, 1.0f/(1<<23) * MANTISSA_MULT);

        private final int flag;
        private final int shift;
        private final float multiplier;
        Radix(int flag, int shift, float multiplier) {
            this.flag = flag;
            this.shift = shift;
            this.multiplier = multiplier;
        }
        public String formatFloat(boolean scale, float value){
            boolean neg = value < 0;
            if(neg){
                value = -value;
            }
            int multiplier = 1;
            int decimalPlaces = flag * 2;
            for(int i = 0; i < decimalPlaces; i++){
                multiplier = multiplier * 10;
            }
            float f = value * multiplier;
            int i = (int) f;
            if((f - i) >= 0.5f){
                i = i + 1;
            }
            value = ((float) i)/multiplier;
            if(neg){
                value = -value;
            }
            if(scale){
                value = value * 100.0f;
            }
            return Float.toString(value);
        }
        public static Radix forFlag(int flag){
            if(flag == 0){
                return RADIX_23p0;
            }
            if(flag == 1){
                return RADIX_16p7;
            }
            if(flag == 2){
                return RADIX_8p15;
            }
            if(flag == 3){
                return RADIX_0p23;
            }
            throw new NumberFormatException("Unknown radix flag = "+flag);
        }
        public static Radix getRadix(long bits){
            if ((bits&0x7fffff) == 0) {
                return RADIX_23p0;
            }
            if ((bits&0xffffffffff800000L) == 0) {
                return RADIX_0p23;
            }
            if ((bits&0xffffffff80000000L) == 0) {
                return RADIX_8p15;
            }
            if ((bits&0xffffff8000000000L) == 0) {
                return RADIX_16p7;
            }
            throw new NumberFormatException("Radix bits out of range bits = "+bits);
        }
        public int getFlag() {
            return flag;
        }
        public int getShift() {
            return shift;
        }
        public float getMultiplier() {
            return multiplier;
        }
    }
    private static final int COMPLEX_RADIX_SHIFT = 4;
    private static final int COMPLEX_RADIX_MASK = 0x3;
    private static final int COMPLEX_MANTISSA_SHIFT = 8;
    private static final int COMPLEX_MANTISSA_MASK = 0x00ffffff;
    private static final float MANTISSA_MULT = (1.0f / (1 << 8));
    private static final int COMPLEX_UNIT_SHIFT = 0;
    private static final int COMPLEX_UNIT_MASK = 0xf;
}
