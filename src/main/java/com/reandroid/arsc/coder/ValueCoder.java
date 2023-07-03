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
package com.reandroid.arsc.coder;

import com.reandroid.utils.HexUtil;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.ValueType;

import java.util.HashMap;
import java.util.Map;

public class ValueCoder {

    public static String decodeUnknownResourceId(boolean is_reference, int referenceId){
        String prefix;
        if(is_reference){
            prefix = UNKNOWN_RESOURCE_ID_PREFIX_REF;
        }else {
            prefix = UNKNOWN_RESOURCE_ID_PREFIX_ATTR;
        }
        return HexUtil.toHex8(prefix, referenceId);
    }
    public static EncodeResult encodeUnknownResourceId(String text){
        if(text == null){
            return null;
        }
        EncodeResult encodeResult = encodeNull(text);
        if(encodeResult != null){
            return encodeResult;
        }
        int length = text.length();
        if(length != UNKNOWN_RESOURCE_ID_LENGTH_ATTR && length != UNKNOWN_RESOURCE_ID_LENGTH_REF){
            return null;
        }
        String prefix;
        ValueType valueType;
        if(text.startsWith(UNKNOWN_RESOURCE_ID_PREFIX_ATTR)){
            prefix = UNKNOWN_RESOURCE_ID_PREFIX_ATTR;
            valueType = ValueType.ATTRIBUTE;
        }else if(text.startsWith(UNKNOWN_RESOURCE_ID_PREFIX_REF)){
            prefix = UNKNOWN_RESOURCE_ID_PREFIX_REF;
            valueType = ValueType.REFERENCE;
        }else {
            return null;
        }
        String hexString = text.substring(prefix.length());
        int referenceId;
        try{
            referenceId = HexUtil.parseHex(hexString);
        }catch (NumberFormatException ignored){
            return null;
        }
        return new EncodeResult(valueType, referenceId);
    }

    public static EncodeResult encode(String text, AttributeDataFormat... expectedDataFormats){
        if(expectedDataFormats == null || expectedDataFormats.length == 0){
            return encodeAny(text);
        }
        return encodeWithin(text, expectedDataFormats);
    }
    public static EncodeResult encode(String text, ValueType... expectedTypes){
        if(expectedTypes == null || expectedTypes.length == 0){
            return encodeAny(text);
        }
        return encodeWithin(text, expectedTypes);
    }
    public static EncodeResult encode(String text){
        return encodeAny(text);
    }
    private static EncodeResult encodeWithin(String text, AttributeDataFormat... expectedDataFormats){
        if(text == null || text.length() == 0){
            return null;
        }
        for(AttributeDataFormat dataFormat : expectedDataFormats){
            EncodeResult encodeResult = encodeWithin(text, dataFormat.valueTypes());
            if(encodeResult != null){
                return encodeResult;
            }
        }
        return null;
    }
    private static EncodeResult encodeWithin(String text, ValueType... expectedTypes){
        if(text == null || text.length() == 0){
            return null;
        }
        EncodeResult encodeResult;
        char first = text.charAt(0);
        if(first == UNKNOWN_RESOURCE_ID_FIRST){
            encodeResult = encodeUnknownResourceId(text);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        for(ValueType valueType : expectedTypes){
            Coder coder = getCoder(valueType);
            if(coder == null){
                continue;
            }
            if(!coder.canStartWith(first)){
                continue;
            }
            encodeResult = coder.encode(text);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        return null;
    }
    private static EncodeResult encodeAny(String text){
        if(text == null || text.length() == 0){
            return null;
        }
        EncodeResult encodeResult;
        char first = text.charAt(0);
        if(first == UNKNOWN_RESOURCE_ID_FIRST){
            encodeResult = encodeUnknownResourceId(text);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        for(Coder coder : CODERS){
            if(!coder.canStartWith(first)){
                continue;
            }
            encodeResult = coder.encode(text);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        return null;
    }
    public static String decode(ValueType valueType, int data){
        String decoded = decodeNull(valueType, data);
        if(decoded != null){
            return decoded;
        }
        Coder coder = CODER_MAP.get(valueType);
        if(coder == null){
            return null;
        }
        return coder.decode(data);
    }
    private static EncodeResult encodeNull(String text){
        if(text == null){
            return null;
        }
        int length = text.length();
        if(length != 5 && length != 6){
            return null;
        }
        char first = text.charAt(0);
        for(Coder coder : CODERS_NULL){
            if(!coder.canStartWith(first)){
                continue;
            }
            EncodeResult encodeResult = coder.encode(text);
            if(encodeResult != null){
                return encodeResult;
            }
        }
        return null;
    }
    private static String decodeNull(ValueType valueType, int data){
        if(data != 0 && data != 1){
            return null;
        }
        if(valueType == ValueType.NULL){
            return CoderNull.INS.decode(data);
        }
        if(data != 0){
            return null;
        }
        if(valueType == ValueType.ATTRIBUTE){
            return CoderNullAttribute.INS.decode(data);
        }
        if(valueType == ValueType.REFERENCE){
            return CoderNullReference.INS.decode(data);
        }
        return null;
    }
    public static Coder getCoder(ValueType valueType){
        return CODER_MAP.get(valueType);
    }


    private static final String UNKNOWN_RESOURCE_ID_PREFIX_ATTR;
    private static final String UNKNOWN_RESOURCE_ID_PREFIX_REF;
    private static final int UNKNOWN_RESOURCE_ID_LENGTH_ATTR;
    private static final int UNKNOWN_RESOURCE_ID_LENGTH_REF;
    private static final char UNKNOWN_RESOURCE_ID_FIRST;

    public static final Coder[] CODERS;
    private static final Map<ValueType, Coder> CODER_MAP;
    private static final Coder[] CODERS_NULL;

    static {
        UNKNOWN_RESOURCE_ID_FIRST = 'U';

        String prefix = "UNK_ATTR0x";
        UNKNOWN_RESOURCE_ID_PREFIX_ATTR = prefix;
        UNKNOWN_RESOURCE_ID_LENGTH_ATTR = prefix.length() + 8;

        prefix = "UNK_REF0x";
        UNKNOWN_RESOURCE_ID_PREFIX_REF = prefix;
        UNKNOWN_RESOURCE_ID_LENGTH_REF = prefix.length() + 8;

        CODERS = new Coder[]{
                CoderNull.INS,
                CoderBoolean.INS,
                CoderDimension.INS,
                CoderFraction.INS,
                CoderColorARGB4.INS,
                CoderColorRGB4.INS,
                CoderColorRGB8.INS,
                CoderColorARGB8.INS,
                CoderFloat.INS,
                CoderHex.INS,
                CoderInteger.INS
        };
        Map<ValueType, Coder> map = new HashMap<>();
        map.put(CoderNull.INS.getValueType(), CoderNull.INS);
        map.put(CoderBoolean.INS.getValueType(), CoderBoolean.INS);
        map.put(CoderDimension.INS.getValueType(), CoderDimension.INS);
        map.put(CoderFraction.INS.getValueType(), CoderFraction.INS);
        map.put(CoderColorRGB4.INS.getValueType(), CoderColorRGB4.INS);
        map.put(CoderColorARGB4.INS.getValueType(), CoderColorARGB4.INS);
        map.put(CoderColorRGB8.INS.getValueType(), CoderColorRGB8.INS);
        map.put(CoderColorARGB8.INS.getValueType(), CoderColorARGB8.INS);
        map.put(CoderFloat.INS.getValueType(), CoderFloat.INS);
        map.put(CoderHex.INS.getValueType(), CoderHex.INS);
        map.put(CoderInteger.INS.getValueType(), CoderInteger.INS);
        CODER_MAP = map;

        CODERS_NULL = new Coder[]{
                CoderNullReference.INS,
                CoderNullAttribute.INS,
                CoderNull.INS,
        };
    }
}
