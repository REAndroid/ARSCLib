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
package com.reandroid.arsc.value;

import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.arsc.io.BlockLoad;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class ResConfig extends FixedBlockContainer
        implements BlockLoad, JSONConvert<JSONObject>, Comparable<ResConfig> {

    private final IntegerItem configSize;
    private final ByteArray mValuesContainer;

    private String mQualifiers;
    private int mQualifiersStamp;

    public ResConfig(){
        super(2);
        this.configSize = new IntegerItem(SIZE_64);
        this.mValuesContainer = new ByteArray(SIZE_64 - 4);
        addChild(0, configSize);
        addChild(1, mValuesContainer);
        this.configSize.setBlockLoad(this);
        this.mQualifiersStamp = 0;
    }
    public void copyFrom(ResConfig resConfig){
        if(resConfig==this||resConfig==null){
            return;
        }
        setConfigSize(resConfig.getConfigSize());
        mValuesContainer.putByteArray(0, resConfig.mValuesContainer.toArray());
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==configSize){
            setConfigSize(configSize.get());
        }
    }
    @Override
    protected void onPreRefreshRefresh(){
        int count=countBytes();
        configSize.set(count);
    }
    @Override
    protected void onRefreshed() {
    }
    public void parseQualifiers(String name){
        ResConfigHelper.parseQualifiers(this, name);
    }
    public void setConfigSize(int size){
        if(!isValidSize(size)){
            throw new IllegalArgumentException("Invalid config size = " + size);
        }
        this.configSize.set(size);
        size=size-4;
        mValuesContainer.setSize(size);
    }
    public int getConfigSize(){
        return this.configSize.get();
    }
    public boolean trimToSize(int size){
        int current=getConfigSize();
        if(current==size){
            return true;
        }
        if(!isValidSize(size)){
            return false;
        }
        if(current<size){
            setConfigSize(size);
            return true;
        }
        int offset=size-4;
        int len=current-4-offset;
        byte[] bts=mValuesContainer.getByteArray(offset, len);
        if(!isNull(bts)){
            return false;
        }
        setConfigSize(size);
        return true;
    }

    public void setMcc(short sh){
        if(getConfigSize()<SIZE_16){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set mcc for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_mcc, sh);
    }
    public int getMcc(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_mcc);
    }
    public void setMnc(short sh){
        if(getConfigSize()<SIZE_16){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set mnc for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_mnc, sh);
    }
    public int getMnc(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_mnc);
    }

    public byte[] getLanguageBytes(){
        if(getConfigSize()<SIZE_16){
            return new byte[2];
        }
        return mValuesContainer.getByteArray(OFFSET_language, 2);
    }
    public void setLanguageBytes(byte[] bytes){
        if(getConfigSize()<SIZE_16){
            if(isNull(bytes)){
                return;
            }
            throw new IllegalArgumentException("Can not set language bytes for config size="+getConfigSize());
        }
        mValuesContainer.putByteArray(OFFSET_language, bytes);
    }
    public char[] getLanguageChars(){
        byte[] bytes = getLanguageBytes();
        return unPackLanguage(bytes[0], bytes[1]);
    }
    public void setLanguage(char[] chars){
        setLanguageBytes(packLanguage(chars));
    }
    public void setLanguage(String language){
        char[] chs = null;
        if(language!=null){
            chs=language.toCharArray();
        }
        setLanguage(chs);
    }
    public String getLanguage(){
        char[] chars = getLanguageChars();
        if(isNull(chars)){
            return null;
        }
        return new String(chars);
    }

    public byte[] getRegionBytes(){
        if(getConfigSize()<SIZE_16){
            return new byte[2];
        }
        return mValuesContainer.getByteArray(OFFSET_region, 2);
    }
    public void setRegionBytes(byte[] bytes){
        if(getConfigSize()<SIZE_16){
            if(isNull(bytes)){
                return;
            }
            throw new IllegalArgumentException("Can not set region bytes for config size="+getConfigSize());
        }
        mValuesContainer.putByteArray(OFFSET_region, bytes);
    }
    public char[] getRegionChars(){
        byte[] bytes = getRegionBytes();
        return unPackRegion(bytes[0], bytes[1]);
    }
    public void setRegion(char[] chars){
        setRegionBytes(packRegion(chars));
    }
    public void setRegion(String region){
        char[] chars = null;
        if(region!=null){
            chars = region.toCharArray();
        }
        setRegion(chars);
    }
    public String getRegion(){
        char[] chars = getRegionChars();
        if(isNull(chars)){
            return null;
        }
        return new String(chars);
    }

    public void setOrientation(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set orientation for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_orientation, b);
    }
    public byte getOrientationByte(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_orientation);
    }
    public Orientation getOrientation(){
        return Orientation.fromValue(getOrientationByte());
    }
    public void setOrientation(Orientation orientation){
        byte b=0;
        if(orientation!=null){
            b=orientation.getByteValue();
        }
        setOrientation(b);
    }
    public void setTouchscreen(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set touchscreen for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_touchscreen, b);
    }
    public byte getTouchscreenByte(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_touchscreen);
    }
    public Touchscreen getTouchscreen(){
        return Touchscreen.fromValue(getTouchscreenByte());
    }
    public void setTouchscreen(Touchscreen touchscreen){
        byte b=0;
        if(touchscreen!=null){
            b=touchscreen.getByteValue();
        }
        setTouchscreen(b);
    }
    public void setDensity(short sh){
        if(getConfigSize()<SIZE_16){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set density for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_density, sh);
    }
    public int getDensityValue(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_density);
    }
    public String getDensity(){
        return ResConfigHelper.decodeDensity(getDensityValue());
    }
    public void setDensity(String density){
        setDensity((short) ResConfigHelper.encodeDensity(density));
    }
    public void setKeyboard(byte b){
        if(getConfigSize()<SIZE_28){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set keyboard for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_keyboard, b);
    }
    public byte getKeyboardByte(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.get(OFFSET_keyboard);
    }
    public Keyboard getKeyboard(){
        return Keyboard.fromValue(getKeyboardByte());
    }
    public void setKeyboard(Keyboard keyboard){
        byte b=0;
        if(keyboard!=null){
            b=keyboard.getByteValue();
        }
        setKeyboard(b);
    }
    public void setNavigation(byte b){
        if(getConfigSize()<SIZE_28){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set navigation for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_navigation, b);
    }
    public byte getNavigationByte(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.get(OFFSET_navigation);
    }
    public Navigation getNavigation(){
        return Navigation.fromValue(getNavigationByte());
    }
    public void setNavigation(Navigation navigation){
        byte b=0;
        if(navigation!=null){
            b=navigation.getByteValue();
        }
        setNavigation(b);
    }
    public void setInputFlags(byte b){
        if(getConfigSize()<SIZE_28){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set inputFlags for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_inputFlags, b);
    }
    public byte getInputFlagsValue(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.get(OFFSET_inputFlags);
    }
    public String getInputFlags(){
        return ResConfigHelper.decodeInputFlags(getInputFlagsValue());
    }
    public void setInputFlags(String inputFlags){
        setInputFlags(ResConfigHelper.encodeInputFlags(inputFlags));
    }
    public void setInputPad0(byte b){
        if(getConfigSize()<SIZE_28){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set inputPad0 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_inputPad0, b);
    }
    public byte getInputPad0(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.get(OFFSET_inputPad0);
    }
    public void setScreenWidth(short sh){
        if(getConfigSize()<SIZE_28){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenWidth for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenWidth, sh);
    }
    public int getScreenWidth(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenWidth);
    }
    public void setScreenHeight(short sh){
        if(getConfigSize()<SIZE_28){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenHeight for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenHeight, sh);
    }
    public int getScreenHeight(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenHeight);
    }
    public void setScreenSize(short w, short h){
        this.setScreenWidth(w);
        this.setScreenHeight(h);
    }
    public void setSdkVersion(short sh){
        if(getConfigSize()<SIZE_28){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set sdkVersion for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_sdkVersion, sh);
    }
    public int getSdkVersion(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_sdkVersion);
    }
    public void setMinorVersion(short sh){
        if(getConfigSize()<SIZE_28){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set minorVersion for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_minorVersion, sh);
    }
    public int getMinorVersion(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_minorVersion);
    }
    public void setScreenLayout(byte b){
        if(getConfigSize()<SIZE_32){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenLayout for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_screenLayout, b);
    }
    public byte getScreenLayoutValue(){
        if(getConfigSize()<SIZE_32){
            return 0;
        }
        return mValuesContainer.get(OFFSET_screenLayout);
    }
    public String getScreenLayout(){
        return ResConfigHelper.decodeScreenLayout(getScreenLayoutValue());
    }
    public void setScreenLayout(String screenLayout){
        setScreenLayout(ResConfigHelper.encodeScreenLayout(screenLayout));
    }
    public void setUiMode(byte b){
        if(getConfigSize()<SIZE_32){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set uiMode for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_uiMode, b);
    }
    public byte getUiModeValue(){
        if(getConfigSize()<SIZE_32){
            return 0;
        }
        return mValuesContainer.get(OFFSET_uiMode);
    }
    public String getUiMode(){
        return ResConfigHelper.decodeUiMode(getUiModeValue());
    }
    public void setUiMode(String uiMode){
        setUiMode(ResConfigHelper.encodeUiMode(uiMode));
    }
    public void setSmallestScreenWidthDp(short sh){
        if(getConfigSize()<SIZE_32){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set smallestScreenWidthDp for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_smallestScreenWidthDp, sh);
    }
    public int getSmallestScreenWidthDp(){
        if(getConfigSize()<SIZE_32){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_smallestScreenWidthDp);
    }
    public void setScreenWidthDp(short sh){
        if(getConfigSize()<SIZE_36){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenWidthDp for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenWidthDp, sh);
    }
    public int getScreenWidthDp(){
        if(getConfigSize()<SIZE_36){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenWidthDp);
    }
    public void setScreenHeightDp(short sh){
        if(getConfigSize()<SIZE_36){
            if(sh==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenHeightDp for config size="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenHeightDp, sh);
    }
    public int getScreenHeightDp(){
        if(getConfigSize()<SIZE_36){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenHeightDp);
    }
    public void setLocaleScript(byte[] bts){
        if(getConfigSize()<SIZE_48){
            if(isNull(bts)){
                return;
            }
            throw new IllegalArgumentException("Can not set localeScript for config size="+getConfigSize());
        }
        bts = ensureArrayLength(bts, LEN_localeScript);
        mValuesContainer.putByteArray(OFFSET_localeScript, bts);
    }
    public void setLocaleScript(char[] chs){
        byte[] bts=toByteArray(chs, LEN_localeScript);
        setLocaleScript(bts);
    }
    public void setLocaleScript(String script){
        char[] chs = null;
        script = trimPostfix(script, POSTFIX_locale);
        if(script!=null){
            chs = script.toCharArray();
        }
        setLocaleScript(chs);
    }
    public char[] getLocaleScriptChars(){
        if(getConfigSize()<SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeScript, LEN_localeScript);
        return trimEndingZero(toCharArray(bts));
    }
    private String getLocaleScriptInternal(){
        char[] chs = getLocaleScriptChars();
        if(chs==null){
            return null;
        }
        return new String(chs);
    }
    public String getLocaleScript(){
        String script = getLocaleScriptInternal();
        if(script==null){
            return null;
        }
        script = ensureLength(script, 3, POSTFIX_locale);
        return script;
    }
    public void setLocaleVariant(byte[] bts){
        if(getConfigSize()<SIZE_48){
            if(isNull(bts)){
                return;
            }
            throw new IllegalArgumentException("Can not set localeVariant for config size="+getConfigSize());
        }
        bts = ensureArrayLength(bts, LEN_localeVariant);
        mValuesContainer.putByteArray(OFFSET_localeVariant, bts);
    }
    public void setLocaleVariant(char[] chs){
        byte[] bts=toByteArray(chs, LEN_localeVariant);
        setLocaleVariant(bts);
    }
    public void setLocaleVariant(String variant){
        if(variant!=null){
            variant = variant.toLowerCase();
        }
        setLocaleVariantInternal(variant);
    }
    private void setLocaleVariantInternal(String variant){
        char[] chs = null;
        variant = trimPostfix(variant, POSTFIX_locale);
        if(variant!=null){
            chs = variant.toCharArray();
        }
        setLocaleVariant(chs);
    }
    public char[] getLocaleVariantChars(){
        if(getConfigSize()<SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeVariant, LEN_localeVariant);
        return trimEndingZero(toCharArray(bts));
    }
    private String getLocaleVariantInternal(){
        char[] chs = getLocaleVariantChars();
        if(chs==null){
            return null;
        }
        return new String(chs);
    }
    public String getLocaleVariant(){
        String variant = getLocaleVariantInternal();
        if(variant==null){
            return null;
        }
        variant = ensureLength(variant, 5, POSTFIX_locale);
        return variant.toUpperCase();
    }
    public void setScreenLayout2(byte b){
        if(getConfigSize()<SIZE_52){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenLayout2 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_screenLayout2, b);
    }
    public byte getScreenLayout2(){
        if(getConfigSize()<SIZE_52){
            return 0;
        }
        return mValuesContainer.get(OFFSET_screenLayout2);
    }
    public void setColorMode(byte b){
        if(getConfigSize()<SIZE_56){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set colorMode for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_colorMode, b);
    }
    public byte getColorMode(){
        if(getConfigSize()<SIZE_56){
            return 0;
        }
        return mValuesContainer.get(OFFSET_colorMode);
    }

    public String getQualifiers(){
        int hash = this.hashCode();
        if(mQualifiers==null || mQualifiersStamp!=hash){
            try{
                mQualifiers = ResConfigHelper.toQualifier(this).trim();
                mQualifiersStamp = hash;
            }catch (Exception ex){
                mQualifiers = "";
            }
        }
        return mQualifiers;
    }

    public boolean isEqualQualifiers(String qualifiers){
        if(qualifiers==null){
            qualifiers="";
        }
        qualifiers=ResConfigHelper.sortQualifiers(qualifiers);
        return getQualifiers().equals(qualifiers);
    }
    public String getLocale(){
        return ResConfigHelper.decodeLocale(this);
    }
    public boolean isDefault(){
        return isNull(mValuesContainer.getBytes());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        if(isDefault()){
            return jsonObject;
        }
        int val=getMcc();
        if(val!=0){
            jsonObject.put(NAME_mcc, val);
        }
        val=getMnc();
        if(val!=0){
            jsonObject.put(NAME_mnc, val);
        }
        String str=getLanguage();
        if(str!=null){
            jsonObject.put(NAME_language, str);
        }
        str=getRegion();
        if(str!=null){
            jsonObject.put(NAME_region, str);
        }
        Orientation orientation=getOrientation();
        if(orientation!=null){
            jsonObject.put(NAME_orientation, orientation.toString());
        }
        Touchscreen touchscreen=getTouchscreen();
        if(touchscreen!=null){
            jsonObject.put(NAME_touchscreen, touchscreen.toString());
        }
        str = getDensity();
        if(str!=null){
            jsonObject.put(NAME_density, str);
        }
        Keyboard keyboard = getKeyboard();
        if(keyboard!=null){
            jsonObject.put(NAME_keyboard, keyboard.toString());
        }
        Navigation navigation = getNavigation();
        if(navigation!=null){
            jsonObject.put(NAME_navigation, navigation.toString());
        }
        str = getInputFlags();
        if(str!=null){
            jsonObject.put(NAME_inputFlags, str);
        }
        val = getScreenWidth();
        if(val!=0){
            jsonObject.put(NAME_screenWidth, val);
        }
        val = getScreenHeight();
        if(val!=0){
            jsonObject.put(NAME_screenHeight, val);
        }
        val = getSdkVersion();
        if(val!=0){
            jsonObject.put(NAME_sdkVersion, val);
        }
        val = getMinorVersion();
        if(val!=0){
            jsonObject.put(NAME_minorVersion, val);
        }
        str = getScreenLayout();
        if(str!=null){
            jsonObject.put(NAME_screenLayout, str);
        }
        str = getUiMode();
        if(str!=null){
            jsonObject.put(NAME_uiMode, str);
        }
        val = getSmallestScreenWidthDp();
        if(val!=0){
            jsonObject.put(NAME_smallestScreenWidthDp, val);
        }
        val = getScreenWidthDp();
        if(val!=0){
            jsonObject.put(NAME_screenWidthDp, val);
        }
        val = getScreenHeightDp();
        if(val!=0){
            jsonObject.put(NAME_screenHeightDp, val);
        }
        str = getLocaleScriptInternal();
        if(str!=null){
            jsonObject.put(NAME_localeScript, str);
        }
        str = getLocaleVariantInternal();
        if(str!=null){
            jsonObject.put(NAME_localeVariant, str);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        if(json.isEmpty()){
            mValuesContainer.fill((byte) 0);
            return;
        }
        trimToSize(SIZE_64);

        setMcc((short) json.optInt(NAME_mcc));
        setMnc((short) json.optInt(NAME_mnc));
        setLanguage(json.optString(NAME_language));
        setRegion(json.optString(NAME_region));
        setOrientation(Orientation.fromName(json.optString(NAME_orientation)));
        setTouchscreen(Touchscreen.fromName(json.optString(NAME_touchscreen)));
        setDensity(json.optString(NAME_density));
        setKeyboard(Keyboard.fromName(json.optString(NAME_keyboard)));
        setNavigation(Navigation.fromName(json.optString(NAME_navigation)));
        setInputFlags(json.optString(NAME_inputFlags));
        setScreenWidth((short) json.optInt(NAME_screenWidth));
        setScreenHeight((short) json.optInt(NAME_screenHeight));
        setSdkVersion((short) json.optInt(NAME_sdkVersion));
        setMinorVersion((short) json.optInt(NAME_minorVersion));
        setScreenLayout(json.optString(NAME_screenLayout));
        setUiMode(json.optString(NAME_uiMode));
        setSmallestScreenWidthDp((short) json.optInt(NAME_smallestScreenWidthDp));
        setScreenWidthDp((short) json.optInt(NAME_screenWidthDp));
        setScreenHeightDp((short) json.optInt(NAME_screenHeightDp));
        setLocaleScript(json.optString(NAME_localeScript));
        setLocaleVariantInternal(json.optString(NAME_localeVariant));

        trimToSize(SIZE_48);
    }
    @Override
    public int hashCode(){
        byte[] bts = ByteArray.trimTrailZeros(mValuesContainer.getBytes());
        return Arrays.hashCode(bts);
    }
    @Override
    public boolean equals(Object obj){
        if(obj==this){
            return true;
        }
        if(obj==null){
            return false;
        }
        if(obj instanceof ResConfig){
            ResConfig other = (ResConfig)obj;
            byte[] bts1 = mValuesContainer.getBytes();
            byte[] bts2 = other.mValuesContainer.getBytes();
            return ByteArray.equalsIgnoreTrailZero(bts1, bts2);
        }
        return false;
    }
    @Override
    public String toString(){
        String q=getQualifiers();
        if(q.length()==0){
            q="DEFAULT";
        }
        return "["+q+"]";
    }
    @Override
    public int compareTo(ResConfig resConfig) {
        return getQualifiers().compareTo(resConfig.getQualifiers());
    }


    private static char[] unPackLanguage(byte in0, byte in1) {
        return unpackLanguageOrRegion(in0, in1, 'a');
    }
    private static char[] unPackRegion(byte in0, byte in1) {
        return unpackLanguageOrRegion(in0, in1, '0');
    }
    private static char[] unpackLanguageOrRegion(byte in0, byte in1, char base) {
        char[] out;
        if ((in0 & 0x80) !=0) {
            out = new char[3];
            byte first = (byte) (in1 & 0x1f);
            byte second = (byte) (((in1 & 0xe0) >> 5) + ((in0 & 0x03) << 3));
            byte third = (byte) ((in0 & 0x7c) >> 2);

            out[0] = (char) (first + base);
            out[1] = (char) (second + base);
            out[2] = (char) (third + base);
        }else if (in0 != 0) {
            out = new char[2];
            out[0] = (char) in0;
            out[1] = (char) in1;
        }else {
            out = new char[2];
        }
        return out;
    }
    private static byte[] packLanguage(char[] language) {
        return packLanguageOrRegion(language, 'a');
    }
    private static byte[] packRegion(char[] region) {
        return packLanguageOrRegion(region, '0');
    }
    private static byte[] packLanguageOrRegion(char[] in, char base) {
        byte[] out = new byte[2];
        if(in==null || in.length<2){
            return out;
        }
        if (in.length==2 || in[2] == 0 || in[2] == '-') {
            out[0] = (byte) in[0];
            out[1] = (byte) in[1];
        } else {
            byte first = (byte) ((in[0] - base) & 0x007f);
            byte second = (byte) ((in[1] - base) & 0x007f);
            byte third = (byte) ((in[2] - base) & 0x007f);

            out[0] = (byte) (0x80 | (third << 2) | (second >> 3));
            out[1] = (byte) ((second << 5) | first);
        }
        return out;
    }

    private static byte[] toByteArray(char[] chs, int len){
        byte[] bts=new byte[len];
        if(chs==null){
            return bts;
        }
        int sz=chs.length;
        for(int i=0; i<sz;i++){
            bts[i]= (byte) chs[i];
        }
        return bts;
    }
    private static char[] toCharArray(byte[] bts){
        if(isNull(bts)){
            return null;
        }
        int sz=bts.length;
        char[] chs=new char[sz];
        for(int i=0; i<sz;i++){
            int val = 0xff & bts[i];
            chs[i]= (char) val;
        }
        return chs;
    }
    private static char[] trimEndingZero(char[] chars){
        if(chars==null){
            return null;
        }
        int lastNonZero = -1;
        for(int i=0;i<chars.length;i++){
            if(chars[i]!=0){
                lastNonZero = i;
            }
        }
        if(lastNonZero==-1){
            return null;
        }
        lastNonZero = lastNonZero+1;
        if(lastNonZero==chars.length){
            return chars;
        }
        char[] result = new char[lastNonZero];
        System.arraycopy(chars, 0, result, 0, lastNonZero);
        return result;
    }
    private static boolean isNull(char[] chs){
        if(chs==null){
            return true;
        }
        for(int i=0;i<chs.length;i++){
            if(chs[i]!=0){
                return false;
            }
        }
        return true;
    }
    private static boolean isNull(byte[] bts){
        if(bts==null){
            return true;
        }
        for(int i=0;i<bts.length;i++){
            if(bts[i]!=0){
                return false;
            }
        }
        return true;
    }
    private static byte[] ensureArrayLength(byte[] bts, int length){
        if(bts == null || length == 0){
            return new byte[length];
        }
        if(bts.length==length){
            return bts;
        }
        byte[] result = new byte[length];
        int max=result.length;
        if(bts.length<max){
            max=bts.length;
        }
        System.arraycopy(bts, 0, result, 0, max);
        return result;
    }
    private static String ensureLength(String str, int min, char postfix){
        int length = str.length();
        if(length >= min){
            return str;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(str);
        int remain = min - length;
        for(int i=0; i<remain; i++){
            builder.append(postfix);
        }
        return builder.toString();
    }
    private static String trimPostfix(String str, char postfix){
        if(str==null){
            return null;
        }
        int length = str.length();
        int index = length-1;
        while (length>0 && str.charAt(index) == postfix){
            str = str.substring(0, index);
            length = str.length();
            index = length - 1;
        }
        return str;
    }
    public static boolean isValidSize(int size){
        switch (size){
            case SIZE_16:
            case SIZE_28:
            case SIZE_32:
            case SIZE_36:
            case SIZE_48:
            case SIZE_52:
            case SIZE_56:
            case SIZE_64:
                return true;
            default:
                return size > SIZE_64;
        }
    }

    public enum Orientation{
        PORT((byte) 0x1),
        LAND((byte) 0x2),
        SQUARE((byte) 0x3);
        private final byte mByteValue;
        Orientation(byte b) {
            this.mByteValue=b;
        }
        public byte getByteValue() {
            return mByteValue;
        }
        @Override
        public String toString(){
            return name().toLowerCase();
        }
        public static Orientation fromValue(byte b){
            for(Orientation orientation:values()){
                if(b==orientation.getByteValue()){
                    return orientation;
                }
            }
            return null;
        }
        public static Orientation fromName(String name){
            if(name==null){
                return null;
            }
            name=name.trim().toUpperCase();
            for(Orientation orientation:values()){
                if(name.equals(orientation.name())){
                    return orientation;
                }
            }
            return null;
        }
    }
    public enum Touchscreen{
        NOTOUCH((byte) 0x1),
        STYLUS((byte) 0x2),
        FINGER((byte) 0x3);
        private final byte mByteValue;
        Touchscreen(byte b) {
            this.mByteValue=b;
        }
        public byte getByteValue() {
            return mByteValue;
        }
        @Override
        public String toString(){
            return name().toLowerCase();
        }
        public static Touchscreen fromValue(byte b){
            for(Touchscreen touchscreen:values()){
                if(b==touchscreen.getByteValue()){
                    return touchscreen;
                }
            }
            return null;
        }
        public static Touchscreen fromName(String name){
            if(name==null){
                return null;
            }
            name=name.trim().toUpperCase();
            for(Touchscreen touchscreen:values()){
                if(name.equals(touchscreen.name())){
                    return touchscreen;
                }
            }
            return null;
        }
    }
    public enum Keyboard{
        NOKEYS((byte) 0x1),
        QWERTY((byte) 0x2),
        KEY12((byte) 0x3);
        private final byte mByteValue;
        Keyboard(byte b) {
            this.mByteValue=b;
        }
        public byte getByteValue() {
            return mByteValue;
        }
        @Override
        public String toString(){
            if(this==KEY12){
                return "12key";
            }
            return name().toLowerCase();
        }
        public static Keyboard fromValue(byte b){
            for(Keyboard keyboard:values()){
                if(b==keyboard.getByteValue()){
                    return keyboard;
                }
            }
            return null;
        }
        public static Keyboard fromName(String name){
            if(name==null){
                return null;
            }
            name=name.trim().toUpperCase();
            if(name.equals("12KEY")){
                return KEY12;
            }
            for(Keyboard keyboard:values()){
                if(name.equals(keyboard.name())){
                    return keyboard;
                }
            }
            return null;
        }
    }
    public enum Navigation{
        NONAV((byte) 0x1),
        DPAD((byte) 0x2),
        TRACKBALL((byte) 0x3),
        WHEEL((byte) 0x4);
        private final byte mByteValue;
        Navigation(byte b) {
            this.mByteValue=b;
        }
        public byte getByteValue() {
            return mByteValue;
        }
        @Override
        public String toString(){
            return name().toLowerCase();
        }
        public static Navigation fromValue(byte b){
            for(Navigation navigation:values()){
                if(b==navigation.getByteValue()){
                    return navigation;
                }
            }
            return null;
        }
        public static Navigation fromName(String name){
            if(name==null){
                return null;
            }
            name=name.trim().toUpperCase();
            for(Navigation navigation:values()){
                if(name.equals(navigation.name())){
                    return navigation;
                }
            }
            return null;
        }
    }

    public static final int SIZE_16 = 16;
    public static final int SIZE_28 = 28;
    public static final int SIZE_32 = 32;
    public static final int SIZE_36 = 36;
    public static final int SIZE_48 = 48;
    public static final int SIZE_52 = 52;
    public static final int SIZE_56 = 56;
    public static final int SIZE_64 = 64;

    private static final int OFFSET_mcc = 0;
    private static final int OFFSET_mnc = 2;
    private static final int OFFSET_language = 4;
    private static final int OFFSET_region = 6;
    private static final int OFFSET_orientation = 8;
    private static final int OFFSET_touchscreen = 9;
    private static final int OFFSET_density = 10;
    //SIZE=16
    private static final int OFFSET_keyboard = 12;
    private static final int OFFSET_navigation = 13;
    private static final int OFFSET_inputFlags = 14;
    private static final int OFFSET_inputPad0 = 15;
    private static final int OFFSET_screenWidth = 16;
    private static final int OFFSET_screenHeight = 18;
    private static final int OFFSET_sdkVersion = 20;
    private static final int OFFSET_minorVersion = 22;
    //SIZE=28
    private static final int OFFSET_screenLayout = 24;
    private static final int OFFSET_uiMode = 25;
    private static final int OFFSET_smallestScreenWidthDp = 26;
    //SIZE=32
    private static final int OFFSET_screenWidthDp = 28;
    private static final int OFFSET_screenHeightDp = 30;
    //SIZE=36
    private static final int OFFSET_localeScript = 32;
    private static final int OFFSET_localeVariant = 36;
    //SIZE=48
    private static final int OFFSET_screenLayout2 = 44;
    private static final int OFFSET_colorMode = 45;
    private static final int OFFSET_reservedPadding = 46;
    //SIZE=52
    private static final int OFFSET_endBlock = 52;

    private static final int LEN_localeScript = 4;
    private static final int LEN_localeVariant = 8;

    private static final String NAME_mcc = "mcc";
    private static final String NAME_mnc = "mnc";
    private static final String NAME_language = "language";
    private static final String NAME_region = "region";
    private static final String NAME_orientation = "orientation";
    private static final String NAME_touchscreen = "touchscreen";
    private static final String NAME_density = "density";
    //SIZE=16
    private static final String NAME_keyboard = "keyboard";
    private static final String NAME_navigation = "navigation";
    private static final String NAME_inputFlags = "inputFlags";
    private static final String NAME_inputPad0 = "inputPad0";
    private static final String NAME_screenWidth = "screenWidth";
    private static final String NAME_screenHeight = "screenHeight";
    private static final String NAME_sdkVersion = "sdkVersion";
    private static final String NAME_minorVersion = "minorVersion";
    //SIZE=28
    private static final String NAME_screenLayout = "screenLayout";
    private static final String NAME_uiMode = "uiMode";
    private static final String NAME_smallestScreenWidthDp = "smallestScreenWidthDp";
    //SIZE=32 = "";
    private static final String NAME_screenWidthDp = "screenWidthDp";
    private static final String NAME_screenHeightDp = "screenHeightDp";
    //SIZE=36
    private static final String NAME_localeScript = "localeScript";
    private static final String NAME_localeVariant = "localeVariant";
    private static final String NAME_screenLayout2 = "screenLayout2";
    private static final String NAME_colorMode = "colorMode";

    private static final char POSTFIX_locale = '#';

}
