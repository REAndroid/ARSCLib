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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public boolean isEqualOrMoreSpecificThan(ResConfig resConfig){
        if(resConfig == null){
            return false;
        }
        if(resConfig == this || resConfig.isDefault()){
            return true;
        }
        byte[] bytes = ByteArray.trimTrailZeros(this.mValuesContainer.getBytes());
        byte[] otherBytes = ByteArray.trimTrailZeros(resConfig.mValuesContainer.getBytes());
        int max = otherBytes.length;
        if(max > bytes.length){
            return false;
        }
        for(int i = 0; i<max; i++){
            byte other = otherBytes[i];
            if(other == 0){
                continue;
            }
            if(bytes[i] != other){
                return false;
            }
        }
        return true;
    }
    public void copyFrom(ResConfig resConfig){
        if(resConfig==this||resConfig== null){
            return;
        }
        setConfigSize(resConfig.getConfigSize());
        mValuesContainer.putByteArray(0, resConfig.mValuesContainer.toArray());
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender == configSize){
            setConfigSize(configSize.get());
        }
    }
    @Override
    protected void onPreRefreshRefresh(){
        int count = countBytes();
        configSize.set(count);
    }
    @Override
    protected void onRefreshed() {
    }
    /**
     * returns null if parsing is ok, else returns unknown qualifiers
     * */
    public String[] parseQualifiers(String qualifiers){
        QualifierParser parser = new QualifierParser(this, qualifiers);
        parser.parse();
        String[] errors = parser.getErrors();
        if(errors == null){
            trimToSize(SIZE_48);
        }
        return errors;
    }
    public void setConfigSize(int size){
        if(!isValidSize(size)){
            throw new IllegalArgumentException("Invalid config size = " + size);
        }
        this.configSize.set(size);
        size = size-4;
        mValuesContainer.setSize(size);
    }
    public int getConfigSize(){
        return this.configSize.get();
    }
    public boolean trimToSize(int size){
        int current = getConfigSize();
        if(current == size){
            return true;
        }
        if(!isValidSize(size)){
            return false;
        }
        if(current<size){
            setConfigSize(size);
            return true;
        }
        int offset = size - 4;
        int len = current - 4 - offset;
        byte[] bts = mValuesContainer.getByteArray(offset, len);
        if(!isNull(bts)){
            return false;
        }
        setConfigSize(size);
        return true;
    }
    public void trimToMinimumSize(){
        int size = ByteArray.trimTrailZeros(mValuesContainer.getBytes()).length + 4;
        size = nearestSize(size);
        trimToSize(size);
    }

    public void setMcc(int  value){
        if(getConfigSize() < SIZE_16){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set mcc for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_mcc, value);
    }
    public int getMcc(){
        if(getConfigSize() < SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_mcc);
    }
    public void setMnc(int value){
        if(getConfigSize() < SIZE_16){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set mnc for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_mnc, value);
    }
    public int getMnc(){
        if(getConfigSize() < SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_mnc);
    }

    public byte[] getLanguageBytes(){
        if(getConfigSize() < SIZE_16){
            return new byte[2];
        }
        return mValuesContainer.getByteArray(OFFSET_language, 2);
    }
    public void setLanguageBytes(byte[] bytes){
        if(getConfigSize() < SIZE_16){
            if(isNull(bytes)){
                return;
            }
            throw new IllegalArgumentException("Can not set language bytes for config size ="+getConfigSize());
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
        if(language!= null){
            chs = language.toCharArray();
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
        if(getConfigSize() < SIZE_16){
            return new byte[2];
        }
        return mValuesContainer.getByteArray(OFFSET_region, 2);
    }
    public void setRegionBytes(byte[] bytes){
        if(getConfigSize() < SIZE_16){
            if(isNull(bytes)){
                return;
            }
            throw new IllegalArgumentException("Can not set region bytes for config size ="+getConfigSize());
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
        if(region!= null){
            if(region.length() ==3 && region.charAt(0) == 'r'){
                region = region.substring(1);
            }
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
    public void setOrientation(int orientation){
        if(getConfigSize() < SIZE_16){
            if(orientation == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set orientation for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_orientation, orientation);
    }
    public int getOrientationValue(){
        if(getConfigSize() < SIZE_16){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_orientation);
    }
    public Orientation getOrientation(){
        return Orientation.valueOf(getOrientationValue());
    }
    public void setOrientation(Orientation orientation){
        setOrientation(Orientation.update(orientation, getOrientationValue()));
    }
    public void setTouchscreen(int touchscreen){
        if(getConfigSize() < SIZE_16){
            if(touchscreen == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set touchscreen for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_touchscreen, touchscreen);
    }
    public int getTouchscreenValue(){
        if(getConfigSize() < SIZE_16){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_touchscreen);
    }
    public Touchscreen getTouchscreen(){
        return Touchscreen.valueOf(getTouchscreenValue());
    }
    public void setTouchscreen(Touchscreen touchscreen){
        setTouchscreen(Touchscreen.update(touchscreen, getTouchscreenValue()));
    }
    public void setDensity(int density){
        if(getConfigSize() < SIZE_16){
            if(density== 0){
                return;
            }
            throw new IllegalArgumentException("Can not set density for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_density, density);
    }
    public int getDensityValue(){
        if(getConfigSize() < SIZE_16){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_density);
    }
    public Density getDensity(){
        return Density.valueOf(getDensityValue());
    }
    public void setDensity(Density density){
        setDensity(Density.update(density, getDensityValue()));
    }
    public void setKeyboard(int keyboard){
        if(getConfigSize() < SIZE_28){
            if(keyboard== 0){
                return;
            }
            throw new IllegalArgumentException("Can not set keyboard for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_keyboard, keyboard);
    }
    public int getKeyboardValue(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_keyboard);
    }
    public Keyboard getKeyboard(){
        return Keyboard.valueOf(getKeyboardValue());
    }
    public void setKeyboard(Keyboard keyboard){
        setKeyboard(Keyboard.update(keyboard, getKeyboardValue()));
    }
    public void setNavigation(int navigation){
        if(getConfigSize() < SIZE_28){
            if(navigation == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set navigation for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_navigation, navigation);
    }
    public int getNavigationValue(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_navigation);
    }
    public Navigation getNavigation(){
        return Navigation.valueOf(getNavigationValue());
    }
    public void setNavigation(Navigation navigation){
        setNavigation(Navigation.update(navigation, getNavigationValue()));
    }
    public void setInputFlags(int inputFlags){
        if(getConfigSize() < SIZE_28){
            if(inputFlags == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set inputFlags for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_inputFlags, inputFlags);
    }
    public int getInputFlagsValue(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_inputFlags);
    }
    public InputFlagsKeysHidden getInputFlagsKeysHidden(){
        return InputFlagsKeysHidden.valueOf(getInputFlagsValue());
    }
    public void setInputFlagsKeysHidden(InputFlagsKeysHidden keysHidden){
        setInputFlags(InputFlagsKeysHidden.update(keysHidden, getInputFlagsValue()));
    }
    public InputFlagsNavHidden getInputFlagsNavHidden(){
        return InputFlagsNavHidden.valueOf(getInputFlagsValue());
    }
    public void setInputFlagsNavHidden(InputFlagsNavHidden navHidden){
        setInputFlags(InputFlagsNavHidden.update(navHidden, getInputFlagsValue()));
    }
    public void setInputPad0(byte b){
        if(getConfigSize() < SIZE_28){
            if(b == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set inputPad0 for config size ="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_inputPad0, b);
    }
    public byte getInputPad0(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.get(OFFSET_inputPad0);
    }
    public void setScreenWidth(int value){
        if(getConfigSize() < SIZE_28){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenWidth for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenWidth, value);
    }
    public int getScreenWidth(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenWidth);
    }
    public void setScreenHeight(int value){
        if(getConfigSize() < SIZE_28){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenHeight for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenHeight, value);
    }
    public int getScreenHeight(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenHeight);
    }
    public void setScreenSize(int width, int height){
        this.setScreenWidth(width);
        this.setScreenHeight(height);
    }
    public void setSdkVersion(int value){
        if(getConfigSize() < SIZE_28){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set sdkVersion for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_sdkVersion, value);
    }
    public int getSdkVersion(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_sdkVersion);
    }
    public void setMinorVersion(int value){
        if(getConfigSize() < SIZE_28){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set minorVersion for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_minorVersion, value);
    }
    public int getMinorVersion(){
        if(getConfigSize() < SIZE_28){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_minorVersion);
    }
    public void setScreenLayout(int layout){
        if(getConfigSize() < SIZE_32){
            if(layout == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenLayout for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_screenLayout, layout);
    }
    public int getScreenLayout(){
        if(getConfigSize() < SIZE_32){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_screenLayout);
    }
    public ScreenLayoutSize getScreenLayoutSize(){
        return ScreenLayoutSize.valueOf(getScreenLayout());
    }
    public void setScreenLayoutSize(ScreenLayoutSize layoutSize){
        setScreenLayout(ScreenLayoutSize.update(layoutSize, getScreenLayout()));
    }
    public ScreenLayoutLong getScreenLayoutLong(){
        return ScreenLayoutLong.valueOf(getScreenLayout());
    }
    public void setScreenLayoutLong(ScreenLayoutLong layoutLong){
        setScreenLayout(ScreenLayoutLong.update(layoutLong, getScreenLayout()));
    }
    public ScreenLayoutDir getScreenLayoutDir(){
        return ScreenLayoutDir.valueOf(getScreenLayout());
    }
    public void setScreenLayoutDir(ScreenLayoutDir layoutDir){
        setScreenLayout(ScreenLayoutDir.update(layoutDir, getScreenLayout()));
    }
    public void setUiMode(int mode){
        if(getConfigSize() < SIZE_32){
            if(mode == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set uiMode for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_uiMode, mode);
    }
    public int getUiMode(){
        if(getConfigSize() < SIZE_32){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_uiMode);
    }
    public UiModeType getUiModeType(){
        return UiModeType.valueOf(getUiMode());
    }
    public void setUiModeType(UiModeType uiModeType){
        setUiMode(UiModeType.update(uiModeType, getUiMode()));
    }
    public UiModeNight getUiModeNight(){
        return UiModeNight.valueOf(getUiMode());
    }
    public void setUiModeNight(UiModeNight uiModeNight){
        setUiMode(UiModeNight.update(uiModeNight, getUiMode()));
    }
    public void setSmallestScreenWidthDp(int value){
        if(getConfigSize() < SIZE_32){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set smallestScreenWidthDp for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_smallestScreenWidthDp, value);
    }
    public int getSmallestScreenWidthDp(){
        if(getConfigSize() < SIZE_32){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_smallestScreenWidthDp);
    }
    public void setScreenWidthDp(int value){
        if(getConfigSize() < SIZE_36){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenWidthDp for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenWidthDp, value);
    }
    public int getScreenWidthDp(){
        if(getConfigSize() < SIZE_36){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenWidthDp);
    }
    public void setScreenHeightDp(int value){
        if(getConfigSize() < SIZE_36){
            if(value == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenHeightDp for config size ="+getConfigSize());
        }
        mValuesContainer.putShort(OFFSET_screenHeightDp, value);
    }
    public int getScreenHeightDp(){
        if(getConfigSize() < SIZE_36){
            return 0;
        }
        return mValuesContainer.getShortUnsigned(OFFSET_screenHeightDp);
    }
    public void setLocaleScript(byte[] bts){
        if(getConfigSize() < SIZE_48){
            if(isNull(bts)){
                return;
            }
            throw new IllegalArgumentException("Can not set localeScript for config size ="+getConfigSize());
        }
        bts = ensureArrayLength(bts, LEN_localeScript);
        mValuesContainer.putByteArray(OFFSET_localeScript, bts);
    }
    public void setLocaleScript(char[] chs){
        byte[] bts =toByteArray(chs, LEN_localeScript);
        setLocaleScript(bts);
    }
    public void setLocaleScript(String script){
        char[] chs = null;
        script = trimPostfix(script, POSTFIX_locale);
        if(script!= null){
            chs = script.toCharArray();
        }
        setLocaleScript(chs);
    }
    public char[] getLocaleScriptChars(){
        if(getConfigSize() < SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeScript, LEN_localeScript);
        return trimEndingZero(toCharArray(bts));
    }
    private String getLocaleScriptInternal(){
        char[] chs = getLocaleScriptChars();
        if(chs == null){
            return null;
        }
        return new String(chs);
    }
    public String getLocaleScript(){
        String script = getLocaleScriptInternal();
        if(script == null){
            return null;
        }
        script = ensureLength(script, 3, POSTFIX_locale);
        return script;
    }
    public void setLocaleVariant(byte[] bts){
        if(getConfigSize() < SIZE_48){
            if(isNull(bts)){
                return;
            }
            throw new IllegalArgumentException("Can not set localeVariant for config size ="+getConfigSize());
        }
        bts = ensureArrayLength(bts, LEN_localeVariant);
        mValuesContainer.putByteArray(OFFSET_localeVariant, bts);
    }
    public void setLocaleVariant(char[] chs){
        byte[] bts =toByteArray(chs, LEN_localeVariant);
        setLocaleVariant(bts);
    }
    public void setLocaleVariant(String variant){
        if(variant!= null){
            variant = variant.toLowerCase();
        }
        setLocaleVariantInternal(variant);
    }
    private void setLocaleVariantInternal(String variant){
        char[] chs = null;
        variant = trimPostfix(variant, POSTFIX_locale);
        if(variant!= null){
            chs = variant.toCharArray();
        }
        setLocaleVariant(chs);
    }
    public char[] getLocaleVariantChars(){
        if(getConfigSize() < SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeVariant, LEN_localeVariant);
        return trimEndingZero(toCharArray(bts));
    }
    private String getLocaleVariantInternal(){
        char[] chs = getLocaleVariantChars();
        if(chs == null){
            return null;
        }
        return new String(chs);
    }
    public String getLocaleVariant(){
        String variant = getLocaleVariantInternal();
        if(variant == null){
            return null;
        }
        variant = ensureLength(variant, 5, POSTFIX_locale);
        return variant.toUpperCase();
    }
    public void setScreenLayout2(int screenLayout2){
        if(getConfigSize() < SIZE_52){
            if(screenLayout2== 0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenLayout2 for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_screenLayout2, screenLayout2);
    }
    public int getScreenLayout2(){
        if(getConfigSize() < SIZE_52){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_screenLayout2);
    }
    public ScreenLayoutRound getScreenLayoutRound(){
        return ScreenLayoutRound.valueOf(getScreenLayout2());
    }
    public void setScreenLayoutRound(ScreenLayoutRound layoutRound){
        setScreenLayout2(ScreenLayoutRound.update(layoutRound, getScreenLayout2()));
    }
    public void setColorMode(int colorMode){
        if(getConfigSize() < SIZE_56){
            if(colorMode == 0){
                return;
            }
            throw new IllegalArgumentException("Can not set colorMode for config size ="+getConfigSize());
        }
        mValuesContainer.putByte(OFFSET_colorMode, colorMode);
    }
    public int getColorMode(){
        if(getConfigSize() < SIZE_56){
            return 0;
        }
        return mValuesContainer.getByteUnsigned(OFFSET_colorMode);
    }
    public ColorModeWide getColorModeWide(){
        return ColorModeWide.valueOf(getColorMode());
    }
    public void setColorModeWide(ColorModeWide colorModeWide){
        setColorMode(ColorModeWide.update(colorModeWide, getColorMode()));
    }
    public ColorModeHdr getColorModeHdr(){
        return ColorModeHdr.valueOf(getColorMode());
    }
    public void setColorModeHdr(ColorModeHdr colorModeHdr){
        setColorMode(ColorModeHdr.update(colorModeHdr, getColorMode()));
    }
    public void setLocaleNumberingSystem(byte[] bts){
        if(getConfigSize() < SIZE_64){
            if(isNull(bts)){
                return;
            }
            throw new IllegalArgumentException("Can not set localeNumberingSystem for config size ="+getConfigSize());
        }
        bts = ensureArrayLength(bts, LEN_localeNumberingSystem);
        mValuesContainer.putByteArray(OFFSET_localeNumberingSystem, bts);
    }
    public String getLocaleNumberingSystem(){
        char[] chars = getLocaleNumberingSystemChars();
        if(isNull(chars)){
            return null;
        }
        return new String(chars);
    }
    public char[] getLocaleNumberingSystemChars(){
        if(getConfigSize() < SIZE_64){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeNumberingSystem, LEN_localeNumberingSystem);
        return trimEndingZero(toCharArray(bts));
    }
    public void setLocaleNumberingSystem(char[] chs){
        byte[] bts =toByteArray(chs, LEN_localeNumberingSystem);
        setLocaleNumberingSystem(bts);
    }
    public void setLocaleNumberingSystem(String numberingSystem){
        char[] chars = null;
        if(numberingSystem!= null){
            chars = numberingSystem.toCharArray();
        }
        setLocaleNumberingSystem(chars);
    }

    public String getQualifiers(){
        int hash = this.hashCode();
        if(mQualifiers == null || mQualifiersStamp!=hash){
            mQualifiers = new QualifierBuilder(this).build();
            mQualifiersStamp = hash;
        }
        return mQualifiers;
    }

    public boolean isEqualQualifiers(String qualifiers){
        return this.equals(parse(qualifiers));
    }
    public boolean isDefault(){
        return isNull(mValuesContainer.getBytes());
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        if(isDefault()){
            return jsonObject;
        }
        int val= getMcc();
        if(val!= 0){
            jsonObject.put(NAME_mcc, val);
        }
        val= getMnc();
        if(val!= 0){
            jsonObject.put(NAME_mnc, val);
        }
        String str = getLanguage();
        if(str!= null){
            jsonObject.put(NAME_language, str);
        }
        str = getRegion();
        if(str!= null){
            jsonObject.put(NAME_region, str);
        }
        jsonObject.put(NAME_orientation, Flag.toString(getOrientation()));
        jsonObject.put(NAME_touchscreen, Flag.toString(getTouchscreen()));
        jsonObject.put(NAME_density, Flag.toString(getDensity()));
        jsonObject.put(NAME_keyboard, Flag.toString(getKeyboard()));
        jsonObject.put(NAME_navigation, Flag.toString(getNavigation()));
        jsonObject.put(NAME_input_flags_keys_hidden, Flag.toString(getInputFlagsKeysHidden()));
        jsonObject.put(NAME_input_flags_nav_hidden, Flag.toString(getInputFlagsNavHidden()));
        val = getScreenWidth();
        if(val!= 0){
            jsonObject.put(NAME_screenWidth, val);
        }
        val = getScreenHeight();
        if(val!= 0){
            jsonObject.put(NAME_screenHeight, val);
        }
        val = getSdkVersion();
        if(val!= 0){
            jsonObject.put(NAME_sdkVersion, val);
        }
        val = getMinorVersion();
        if(val!= 0){
            jsonObject.put(NAME_minorVersion, val);
        }
        jsonObject.put(NAME_screen_layout_size, Flag.toString(getScreenLayoutSize()));
        jsonObject.put(NAME_screen_layout_long, Flag.toString(getScreenLayoutLong()));
        jsonObject.put(NAME_screen_layout_dir, Flag.toString(getScreenLayoutDir()));
        jsonObject.put(NAME_ui_mode_type, Flag.toString(getUiModeType()));
        jsonObject.put(NAME_ui_mode_night, Flag.toString(getUiModeNight()));
        val = getSmallestScreenWidthDp();
        if(val!= 0){
            jsonObject.put(NAME_smallestScreenWidthDp, val);
        }
        val = getScreenWidthDp();
        if(val!= 0){
            jsonObject.put(NAME_screenWidthDp, val);
        }
        val = getScreenHeightDp();
        if(val!= 0){
            jsonObject.put(NAME_screenHeightDp, val);
        }
        str = getLocaleScriptInternal();
        if(str!= null){
            jsonObject.put(NAME_localeScript, str);
        }
        str = getLocaleVariantInternal();
        if(str!= null){
            jsonObject.put(NAME_localeVariant, str);
        }
        jsonObject.put(NAME_screen_layout_round, Flag.toString(getScreenLayoutRound()));
        jsonObject.put(NAME_color_mode_wide, Flag.toString(getColorModeWide()));
        jsonObject.put(NAME_color_mode_hdr, Flag.toString(getColorModeHdr()));
        str = getLocaleNumberingSystem();
        if(str!= null){
            jsonObject.put(NAME_localeNumberingSystem, str);
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

        setMcc(json.optInt(NAME_mcc));
        setMnc(json.optInt(NAME_mnc));
        setLanguage(json.optString(NAME_language));
        setRegion(json.optString(NAME_region));
        setOrientation(Orientation.valueOf(json.optString(NAME_orientation)));
        setTouchscreen(Touchscreen.valueOf(json.optString(NAME_touchscreen)));
        setDensity(Density.valueOf(json.optString(NAME_density)));
        setKeyboard(Keyboard.valueOf(json.optString(NAME_keyboard)));
        setNavigation(Navigation.valueOf(json.optString(NAME_navigation)));
        setInputFlagsKeysHidden(InputFlagsKeysHidden.valueOf(json.optString(NAME_input_flags_keys_hidden)));
        setInputFlagsNavHidden(InputFlagsNavHidden.valueOf(json.optString(NAME_input_flags_nav_hidden)));
        setScreenWidth(json.optInt(NAME_screenWidth));
        setScreenHeight(json.optInt(NAME_screenHeight));
        setSdkVersion(json.optInt(NAME_sdkVersion));
        setMinorVersion(json.optInt(NAME_minorVersion));
        setScreenLayoutSize(ScreenLayoutSize.valueOf(json.optString(NAME_screen_layout_size)));
        setScreenLayoutLong(ScreenLayoutLong.valueOf(json.optString(NAME_screen_layout_long)));
        setScreenLayoutDir(ScreenLayoutDir.valueOf(json.optString(NAME_screen_layout_dir)));
        setUiModeType(UiModeType.valueOf(json.optString(NAME_ui_mode_type)));
        setUiModeNight(UiModeNight.valueOf(json.optString(NAME_ui_mode_night)));
        setSmallestScreenWidthDp(json.optInt(NAME_smallestScreenWidthDp));
        setScreenWidthDp(json.optInt(NAME_screenWidthDp));
        setScreenHeightDp(json.optInt(NAME_screenHeightDp));
        setLocaleScript(json.optString(NAME_localeScript));
        setLocaleVariantInternal(json.optString(NAME_localeVariant));
        setScreenLayoutRound(ScreenLayoutRound.valueOf(json.optString(NAME_screen_layout_round)));
        setColorModeWide(ColorModeWide.valueOf(json.optString(NAME_color_mode_wide)));
        setColorModeHdr(ColorModeHdr.valueOf(json.optString(NAME_color_mode_hdr)));
        setLocaleNumberingSystem(json.optString(NAME_localeNumberingSystem));
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
        if(obj== null){
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
        String q= getQualifiers();
        if(q.length() == 0){
            q="DEFAULT";
        }
        return "["+q+"]";
    }
    @Override
    public int compareTo(ResConfig resConfig) {
        return getQualifiers().compareTo(resConfig.getQualifiers());
    }

    public static ResConfig parse(String qualifiers){
        ResConfig resConfig = new ResConfig();
        resConfig.parseQualifiers(qualifiers);
        return resConfig;
    }
    public static int nearestSize(int size){
        if(size <= SIZE_16){
            return SIZE_16;
        }
        if(size <= SIZE_28){
            return SIZE_28;
        }
        if(size <= SIZE_32){
            return SIZE_32;
        }
        if(size <= SIZE_36){
            return SIZE_36;
        }
        if(size <= SIZE_48){
            return SIZE_48;
        }
        if(size <= SIZE_52){
            return SIZE_52;
        }
        if(size <= SIZE_56){
            return SIZE_56;
        }
        return SIZE_64;
    }


    private static char[] unPackLanguage(byte in0, byte in1) {
        return unpackLanguageOrRegion(in0, in1, 'a');
    }
    private static char[] unPackRegion(byte in0, byte in1) {
        return unpackLanguageOrRegion(in0, in1, '0');
    }
    private static char[] unpackLanguageOrRegion(byte in0, byte in1, char base) {
        char[] out;
        if ((in0 & 0x80) != 0) {
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
        if(in == null || in.length<2){
            return out;
        }
        if (in.length == 2 || in[2] == 0 || in[2] == '-') {
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
        byte[] bts = new byte[len];
        if(chs == null){
            return bts;
        }
        int sz = chs.length;
        for(int i = 0; i < sz; i++){
            bts[i]= (byte) chs[i];
        }
        return bts;
    }
    private static char[] toCharArray(byte[] bts){
        if(isNull(bts)){
            return null;
        }
        int sz = bts.length;
        char[] chs = new char[sz];
        for(int i = 0; i < sz; i++){
            int val = 0xff & bts[i];
            chs[i]= (char) val;
        }
        return chs;
    }
    private static char[] trimEndingZero(char[] chars){
        if(chars == null){
            return null;
        }
        int lastNonZero = -1;
        for(int i = 0; i < chars.length; i++){
            if(chars[i]!= 0){
                lastNonZero = i;
            }
        }
        if(lastNonZero==-1){
            return null;
        }
        lastNonZero = lastNonZero+1;
        if(lastNonZero== chars.length){
            return chars;
        }
        char[] result = new char[lastNonZero];
        System.arraycopy(chars, 0, result, 0, lastNonZero);
        return result;
    }
    private static boolean isNull(char[] chs){
        if(chs == null){
            return true;
        }
        for(int i = 0; i < chs.length; i++){
            if(chs[i]!= 0){
                return false;
            }
        }
        return true;
    }
    private static boolean isNull(byte[] bts){
        if(bts == null){
            return true;
        }
        for(int i = 0; i < bts.length; i++){
            if(bts[i] != 0){
                return false;
            }
        }
        return true;
    }
    private static byte[] ensureArrayLength(byte[] bts, int length){
        if(bts == null || length == 0){
            return new byte[length];
        }
        if(bts.length == length){
            return bts;
        }
        byte[] result = new byte[length];
        int max = result.length;
        if(bts.length < max){
            max = bts.length;
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
        for(int i = 0; i < remain; i++){
            builder.append(postfix);
        }
        return builder.toString();
    }
    private static String trimPostfix(String str, char postfix){
        if(str == null){
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

    public static final class Orientation extends Flag{
        public static final int MASK = 0x0f;

        public static final Orientation PORT = new Orientation("port", 0x01);
        public static final Orientation LAND = new Orientation("land", 0x02);
        public static final Orientation SQUARE = new Orientation("square", 0x03);

        public static final Orientation[] VALUES = new Orientation[]{
                PORT,
                LAND,
                SQUARE
        };
        private Orientation(String name, int flag) {
            super(name, flag);
        }
        public static Orientation valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static Orientation valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static Orientation fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static Orientation fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(Orientation flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class Touchscreen extends Flag{
        public static final int MASK = 0x0f;

        public static final Touchscreen NOTOUCH = new Touchscreen("notouch", 0x01);
        public static final Touchscreen STYLUS = new Touchscreen("stylus", 0x02);
        public static final Touchscreen FINGER = new Touchscreen("finger", 0x03);

        public static final Touchscreen[] VALUES = new Touchscreen[]{
                NOTOUCH,
                STYLUS,
                FINGER
        };
        private Touchscreen(String name, int flag) {
            super(name, flag);
        }
        public static Touchscreen valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static Touchscreen valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static Touchscreen fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static Touchscreen fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(Touchscreen flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class Density extends Flag{
        public static final int MASK = 0xffff;

        public static final Density LDPI = new Density("ldpi", 120);
        public static final Density MDPI = new Density("mdpi", 160);
        public static final Density TVDPI = new Density("tvdpi", 213);
        public static final Density HDPI = new Density("hdpi", 240);
        public static final Density XHDPI = new Density("xhdpi", 320);
        public static final Density XXHDPI = new Density("xxhdpi", 480);
        public static final Density XXXHDPI = new Density("xxxhdpi", 640);
        public static final Density ANYDPI = new Density("anydpi", 0xfffe);
        public static final Density NODPI = new Density("nodpi", 0xffff);

        public static final Density[] VALUES = new Density[]{LDPI,
                MDPI,
                TVDPI,
                HDPI,
                XHDPI,
                XXHDPI,
                XXXHDPI,
                ANYDPI,
                NODPI
        };
        private Density(String name, int flag) {
            super(name, flag);
        }
        public static Density valueOf(int flag){
            if(flag== 0){
                return null;
            }
            Density density = Flag.valueOf(VALUES, MASK, flag);
            if(density == null){
                flag = flag & MASK;
                density = new Density(flag+"dpi", flag);
            }
            return density;
        }
        public static Density valueOf(String name){
            if(name == null || name.length() < 4){
                return null;
            }
            name = name.toLowerCase();
            if(name.charAt(0) == '-'){
                name = name.substring(1);
            }
            Density density = Flag.valueOf(VALUES, name);
            if(density == null && name.endsWith("dpi")){
                name = name.substring(0, name.length()-3);
                try{
                    int flag = Integer.parseInt(name);
                    density = new Density(flag+"dpi", flag);
                }catch (NumberFormatException ignored){
                }
            }
            return density;
        }
        public static Density fromQualifiers(String qualifiers){
            return fromQualifiers(qualifiers.split("\\s*-\\s*"));
        }
        public static Density fromQualifiers(String[] qualifiers){
            if(qualifiers == null){
                return null;
            }
            for(int i = 0; i < qualifiers.length; i++){
                Density density = valueOf(qualifiers[i]);
                if(density== null){
                    continue;
                }
                qualifiers[i] = null;
                return density;
            }
            return null;
        }
        public static int update(Density flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class Keyboard extends Flag{
        public static final int MASK = 0x0f;

        public static final Keyboard NOKEYS = new Keyboard("nokeys", 0x01);
        public static final Keyboard QWERTY = new Keyboard("qwerty", 0x02);
        public static final Keyboard KEY12 = new Keyboard("12key", 0x03);

        public static final Keyboard[] VALUES = new Keyboard[]{
                NOKEYS,
                QWERTY,
                KEY12
        };
        private Keyboard(String name, int flag) {
            super(name, flag);
        }
        public static Keyboard valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static Keyboard valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static Keyboard fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static Keyboard fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(Keyboard flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class Navigation extends Flag{
        public static final int MASK = 0x0f;

        public static final Navigation NONAV = new Navigation("nonav", 0x01);
        public static final Navigation DPAD = new Navigation("dpad", 0x02);
        public static final Navigation TRACKBALL = new Navigation("trackball", 0x03);
        public static final Navigation WHEEL = new Navigation("wheel", 0x04);
        public static final Navigation[] VALUES = new Navigation[]{
                NONAV,
                DPAD,
                TRACKBALL,
                WHEEL
        };
        private Navigation(String name, int flag) {
            super(name, flag);
        }
        public static Navigation valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static Navigation valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static Navigation fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static Navigation fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(Navigation flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class InputFlagsKeysHidden extends Flag{
        public static final int MASK = 0x03;

        public static final InputFlagsKeysHidden KEYSEXPOSED = new InputFlagsKeysHidden("keysexposed", 0x01);
        public static final InputFlagsKeysHidden KEYSHIDDEN = new InputFlagsKeysHidden("keyshidden", 0x02);
        public static final InputFlagsKeysHidden KEYSSOFT = new InputFlagsKeysHidden("keyssoft", 0x03);
        public static final InputFlagsKeysHidden[] VALUES = new InputFlagsKeysHidden[]{
                KEYSEXPOSED,
                KEYSHIDDEN,
                KEYSSOFT
        };
        private InputFlagsKeysHidden(String name, int flag) {
            super(name, flag);
        }
        public static InputFlagsKeysHidden valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static InputFlagsKeysHidden valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static InputFlagsKeysHidden fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static InputFlagsKeysHidden fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(InputFlagsKeysHidden flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class InputFlagsNavHidden extends Flag{
        public static final int MASK = 0x0C;

        public static final InputFlagsNavHidden NAVEXPOSED = new InputFlagsNavHidden("navexposed", 0x04);
        public static final InputFlagsNavHidden NAVHIDDEN = new InputFlagsNavHidden("navhidden", 0x08);
        public static final InputFlagsNavHidden[] VALUES = new InputFlagsNavHidden[]{
                NAVEXPOSED,
                NAVHIDDEN
        };
        private InputFlagsNavHidden(String name, int flag) {
            super(name, flag);
        }
        public static InputFlagsNavHidden valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static InputFlagsNavHidden valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static InputFlagsNavHidden fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static InputFlagsNavHidden fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(InputFlagsNavHidden flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class UiModeType extends Flag{
        public static final int MASK = 0x0f;

        public static final UiModeType NORMAL = new UiModeType("normal", 0x01);
        public static final UiModeType DESK = new UiModeType("desk", 0x02);
        public static final UiModeType CAR = new UiModeType("car", 0x03);
        public static final UiModeType TELEVISION = new UiModeType("television", 0x04);
        public static final UiModeType APPLIANCE = new UiModeType("appliance", 0x05);
        public static final UiModeType WATCH = new UiModeType("watch", 0x06);
        public static final UiModeType VRHEADSET = new UiModeType("vrheadset", 0x07);
        public static final UiModeType GODZILLAUI = new UiModeType("godzillaui", 0x0b);
        public static final UiModeType SMALLUI = new UiModeType("smallui", 0x0c);
        public static final UiModeType MEDIUMUI = new UiModeType("mediumui", 0x0d);
        public static final UiModeType LARGEUI = new UiModeType("largeui", 0x0e);
        public static final UiModeType HUGEUI = new UiModeType("hugeui", 0x0f);

        private static final UiModeType[] VALUES = new UiModeType[]{
                NORMAL,
                DESK,
                CAR,
                TELEVISION,
                APPLIANCE,
                WATCH,
                VRHEADSET,
                GODZILLAUI,
                SMALLUI,
                MEDIUMUI,
                LARGEUI,
                HUGEUI
        };

        private UiModeType(String name, int flag) {
            super(name, flag);
        }
        public static UiModeType valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static UiModeType valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static UiModeType fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static UiModeType fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(UiModeType flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class UiModeNight extends Flag{
        public static final int MASK = 0x30;
        public static final UiModeNight NOTNIGHT = new UiModeNight("notnight",0x10);
        public static final UiModeNight NIGHT = new UiModeNight("night",0x20);
        private static final UiModeNight[] VALUES = new UiModeNight[]{
                NOTNIGHT,
                NIGHT
        };
        private UiModeNight(String name, int flag) {
            super(name, flag);
        }
        public static UiModeNight valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static UiModeNight valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static UiModeNight fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static UiModeNight fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(UiModeNight flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ScreenLayoutSize extends Flag{
        public static final int MASK = 0x0f;

        public static final ScreenLayoutSize SMALL = new ScreenLayoutSize("small", 0x01);
        public static final ScreenLayoutSize NORMAL = new ScreenLayoutSize("normal", 0x02);
        public static final ScreenLayoutSize LARGE = new ScreenLayoutSize("large", 0x03);
        public static final ScreenLayoutSize XLARGE = new ScreenLayoutSize("xlarge", 0x04);
        public static final ScreenLayoutSize[] VALUES = new ScreenLayoutSize[]{
                SMALL,
                NORMAL,
                LARGE,
                XLARGE
        };
        private ScreenLayoutSize(String name, int flag) {
            super(name, flag);
        }
        public static ScreenLayoutSize valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ScreenLayoutSize valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ScreenLayoutSize fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ScreenLayoutSize fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ScreenLayoutSize flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ScreenLayoutLong extends Flag{
        public static final int MASK = 0x30;
        public static final ScreenLayoutLong NOTLONG = new ScreenLayoutLong("notlong", 0x10);
        public static final ScreenLayoutLong LONG = new ScreenLayoutLong("long", 0x20);
        public static final ScreenLayoutLong[] VALUES = new ScreenLayoutLong[]{
                NOTLONG,
                LONG
        };
        private ScreenLayoutLong(String name, int flag) {
            super(name, flag);
        }
        public static ScreenLayoutLong valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ScreenLayoutLong valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ScreenLayoutLong fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ScreenLayoutLong fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ScreenLayoutLong flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ScreenLayoutDir extends Flag{
        public static final int MASK = 0xC0;
        public static final ScreenLayoutDir LDLTR = new ScreenLayoutDir("ldltr", 0x40);
        public static final ScreenLayoutDir LDRTL = new ScreenLayoutDir("ldrtl", 0x80);
        public static final ScreenLayoutDir[] VALUES = new ScreenLayoutDir[]{
                LDLTR,
                LDRTL
        };
        private ScreenLayoutDir(String name, int flag) {
            super(name, flag);
        }
        public static ScreenLayoutDir valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ScreenLayoutDir valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ScreenLayoutDir fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ScreenLayoutDir fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ScreenLayoutDir flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ScreenLayoutRound extends Flag{
        public static final int MASK = 0x03;
        public static final ScreenLayoutRound NOTROUND = new ScreenLayoutRound("notround", 0x01);
        public static final ScreenLayoutRound ROUND = new ScreenLayoutRound("round", 0x02);
        public static final ScreenLayoutRound[] VALUES = new ScreenLayoutRound[]{
                NOTROUND,
                ROUND
        };
        private ScreenLayoutRound(String name, int flag) {
            super(name, flag);
        }
        public static ScreenLayoutRound valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ScreenLayoutRound valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ScreenLayoutRound fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ScreenLayoutRound fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ScreenLayoutRound flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ColorModeWide extends Flag{
        public static final int MASK = 0x03;
        public static final ColorModeWide NOWIDECG = new ColorModeWide("nowidecg", 0x01);
        public static final ColorModeWide WIDECG = new ColorModeWide("widecg", 0x02);
        public static final ColorModeWide[] VALUES = new ColorModeWide[]{
                NOWIDECG,
                WIDECG
        };
        private ColorModeWide(String name, int flag) {
            super(name, flag);
        }
        public static ColorModeWide valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ColorModeWide valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ColorModeWide fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ColorModeWide fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ColorModeWide flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }
    public static final class ColorModeHdr extends Flag{
        public static final int MASK = 0x0C;
        public static final ColorModeHdr LOWDR = new ColorModeHdr("lowdr", 0x04);
        public static final ColorModeHdr HIGHDR = new ColorModeHdr("highdr", 0x08);
        public static final ColorModeHdr[] VALUES = new ColorModeHdr[]{
                LOWDR,
                HIGHDR
        };
        private ColorModeHdr(String name, int flag) {
            super(name, flag);
        }
        public static ColorModeHdr valueOf(int flag){
            return Flag.valueOf(VALUES, MASK, flag);
        }
        public static ColorModeHdr valueOf(String name){
            return Flag.valueOf(VALUES, name);
        }
        public static ColorModeHdr fromQualifiers(String qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static ColorModeHdr fromQualifiers(String[] qualifiers){
            return Flag.fromQualifiers(VALUES, qualifiers);
        }
        public static int update(ColorModeHdr flag, int value){
            return Flag.update(MASK, flag, value);
        }
    }

    static class Flag{
        private final String name;
        private final int flag;
        Flag(String name, int flag){
            this.name = name;
            this.flag = flag;
        }
        public int getFlag() {
            return flag;
        }
        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }
        @Override
        public int hashCode() {
            return super.hashCode();
        }
        @Override
        public String toString() {
            return name;
        }
        public static String toString(Flag flag){
            if(flag!= null){
                return flag.toString();
            }
            return null;
        }
        static<T extends Flag> T fromQualifiers(T[] values, String qualifiers){
            if(qualifiers == null){
                return null;
            }
            return fromQualifiers(values, qualifiers.split("\\s*-\\s*"));
        }
        static<T extends Flag> T fromQualifiers(T[] values, String[] qualifiers){
            if(qualifiers == null){
                return null;
            }
            for(int i = 0; i < qualifiers.length; i++){
                T flag = Flag.valueOf(values, qualifiers[i]);
                if(flag != null){
                    qualifiers[i] = null;
                    return flag;
                }
            }
            return null;
        }
        static<T extends Flag> T valueOf(T[] values, int mask, int flagValue){
            flagValue = flagValue & mask;
            for(T flag:values){
                if(flagValue == flag.getFlag()){
                    return flag;
                }
            }
            return null;
        }
        static<T extends Flag> T valueOf(T[] values, String name){
            if(name == null || name.length() == 0){
                return null;
            }
            if(name.charAt(0) == '-'){
                name = name.substring(1);
            }
            name = name.toLowerCase();
            for(T flag:values){
                if(name.equals(flag.toString())){
                    return flag;
                }
            }
            return null;
        }
        public static int update(int mask, Flag flag, int value){
            int flip = (~mask) & 0xff;
            value = value & flip;
            if(flag != null){
                value = value | flag.getFlag();
            }
            return value;
        }
    }

    static class QualifierBuilder{
        private final ResConfig mConfig;
        private StringBuilder mBuilder;
        public QualifierBuilder(ResConfig resConfig){
            this.mConfig = resConfig;
        }
        public String build(){
            ResConfig resConfig = this.mConfig;
            if(resConfig.isDefault()){
                return "";
            }
            this.mBuilder = new StringBuilder();
            appendPrefixedNumber("mcc", resConfig.getMcc());
            appendPrefixedNumber("mnc", resConfig.getMnc());

            appendLanguageAndRegion();

            appendFlag(resConfig.getOrientation());
            appendFlag(resConfig.getTouchscreen());
            appendFlag(resConfig.getDensity());
            appendFlag(resConfig.getKeyboard());
            appendFlag(resConfig.getNavigation());
            appendFlag(resConfig.getInputFlagsKeysHidden());
            appendFlag(resConfig.getInputFlagsNavHidden());

            appendScreenWidthHeight();

            appendPrefixedNumber("v", resConfig.getSdkVersion());
            // append resConfig.getMinorVersion()
            appendFlag(resConfig.getScreenLayoutSize());
            appendFlag(resConfig.getScreenLayoutLong());
            appendFlag(resConfig.getScreenLayoutDir());

            appendFlag(resConfig.getUiModeType());
            appendFlag(resConfig.getUiModeNight());

            appendDp("sw", resConfig.getSmallestScreenWidthDp());
            appendDp("w", resConfig.getScreenWidthDp());
            appendDp("h", resConfig.getScreenHeightDp());

            appendFlag(resConfig.getScreenLayoutRound());

            appendFlag(resConfig.getColorModeWide());
            appendFlag(resConfig.getColorModeHdr());

            appendLocaleNumberingSystem();

            return mBuilder.toString();
        }
        private void appendScreenWidthHeight(){
            ResConfig resConfig = this.mConfig;
            int width = resConfig.getScreenWidth();
            int height = resConfig.getScreenHeight();
            if(width == 0 && height == 0){
                return;
            }
            mBuilder.append('-').append(width).append('x').append(height);
        }
        private void appendLanguageAndRegion(){
            ResConfig resConfig = this.mConfig;
            String language = resConfig.getLanguage();
            String region = resConfig.getRegion();
            String script = resConfig.getLocaleScript();
            String variant = resConfig.getLocaleVariant();
            if(language == null && region == null){
                return;
            }
            StringBuilder builder = this.mBuilder;
            char separator;
            if(script != null || variant != null){
                builder.append('-');
                builder.append('b');
                separator = '+';
            }else {
                separator = '-';
            }
            if(language!= null){
                builder.append(separator);
                builder.append(language);
            }
            if(region!= null){
                builder.append(separator);
                if(region.length() == 2){
                    builder.append('r');
                }
                builder.append(region);
            }
            if(script!= null){
                builder.append(separator);
                builder.append(script);
            }
            if(variant!= null){
                builder.append(separator);
                builder.append(variant);
            }
        }
        private void appendLocaleNumberingSystem(){
            String numberingSystem = mConfig.getLocaleNumberingSystem();
            if(numberingSystem== null){
                return;
            }
            StringBuilder builder = mBuilder;
            builder.append("-u+nu+");
            builder.append(numberingSystem);
        }
        private void appendFlag(ResConfig.Flag flag){
            if(flag== null){
                return;
            }
            mBuilder.append('-').append(flag.toString());
        }
        private void appendDp(String prefix, int number){
            if(number == 0){
                return;
            }
            StringBuilder builder = this.mBuilder;
            builder.append('-');
            if(prefix!= null){
                builder.append(prefix);
            }
            builder.append(number);
            builder.append("dp");
        }
        private void appendPrefixedNumber(String prefix, int number){
            if(number == 0){
                return;
            }
            StringBuilder builder = this.mBuilder;
            builder.append('-');
            builder.append(prefix);
            builder.append(number);
        }
    }
    static class QualifierParser{
        private final ResConfig mConfig;
        private final String[] mQualifiers;
        private final int mPreferredSize;
        private boolean mEmpty;
        private boolean mLanguageRegionParsed;
        private boolean mParseComplete;

        public QualifierParser(ResConfig resConfig, String[] qualifiers){
            this.mConfig = resConfig;
            this.mQualifiers = qualifiers;
            this.mPreferredSize = resConfig.getConfigSize();
        }
        public QualifierParser(ResConfig resConfig, String qualifiers){
            this(resConfig, splitQualifiers(qualifiers));
        }

        public void parse(){
            if(this.mParseComplete){
                return;
            }
            if(isEmpty()){
                onParseComplete();
                return;
            }
            ResConfig resConfig = this.mConfig;
            resConfig.setConfigSize(ResConfig.SIZE_64);
            parsePrefixedNumber();
            parseDp();
            parseWidthHeight();
            parseLocaleNumberingSystem();
            if(isEmpty()){
                onParseComplete();
                return;
            }
            String[] qualifiers = this.mQualifiers;
            resConfig.setOrientation(ResConfig.Orientation.fromQualifiers(qualifiers));
            resConfig.setTouchscreen(ResConfig.Touchscreen.fromQualifiers(qualifiers));
            resConfig.setDensity(ResConfig.Density.fromQualifiers(qualifiers));
            resConfig.setKeyboard(ResConfig.Keyboard.fromQualifiers(qualifiers));
            resConfig.setNavigation(ResConfig.Navigation.fromQualifiers(qualifiers));
            if(isEmpty()){
                onParseComplete();
                return;
            }
            resConfig.setInputFlagsKeysHidden(ResConfig.InputFlagsKeysHidden.fromQualifiers(qualifiers));
            resConfig.setInputFlagsNavHidden(ResConfig.InputFlagsNavHidden.fromQualifiers(qualifiers));
            resConfig.setScreenLayoutSize(ResConfig.ScreenLayoutSize.fromQualifiers(qualifiers));
            resConfig.setScreenLayoutLong(ResConfig.ScreenLayoutLong.fromQualifiers(qualifiers));
            resConfig.setScreenLayoutDir(ResConfig.ScreenLayoutDir.fromQualifiers(qualifiers));
            if(isEmpty()){
                onParseComplete();
                return;
            }
            resConfig.setUiModeType(ResConfig.UiModeType.fromQualifiers(qualifiers));
            resConfig.setUiModeNight(ResConfig.UiModeNight.fromQualifiers(qualifiers));

            resConfig.setScreenLayoutRound(ResConfig.ScreenLayoutRound.fromQualifiers(qualifiers));

            resConfig.setColorModeWide(ResConfig.ColorModeWide.fromQualifiers(qualifiers));
            resConfig.setColorModeHdr(ResConfig.ColorModeHdr.fromQualifiers(qualifiers));
            if(isEmpty()){
                onParseComplete();
                return;
            }
            parseLocaleScriptVariant();
            parseLanguage();
            parseRegion();
            onParseComplete();
        }
        public String[] getErrors(){
            if(!this.mParseComplete){
                return null;
            }
            String[] qualifiers = this.mQualifiers;
            if(qualifiers == null || qualifiers.length == 0){
                return null;
            }
            int length = qualifiers.length;
            String[] tmp = new String[length];
            int count = 0;
            for(int i = 0; i < length; i++){
                String qualifier = qualifiers[i];
                if(qualifier == null || qualifier.length() == 0){
                    continue;
                }
                tmp[count] = qualifier;
                count++;
            }
            if(count == 0){
                return null;
            }
            if(count == length){
                return tmp;
            }
            String[] errors = new String[count];
            System.arraycopy(tmp, 0, errors, 0, count);
            return errors;
        }
        private void onParseComplete(){
            this.mConfig.trimToSize(this.mPreferredSize);
            this.mParseComplete = true;
        }

        private void parsePrefixedNumber(){
            if(isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parsePrefixedNumber(qualifiers[i])){
                    qualifiers[i] = null;
                }
            }
        }
        private boolean parsePrefixedNumber(String qualifier){
            if(qualifier == null){
                return false;
            }
            Matcher matcher = PATTERN_PREFIX_NUMBER.matcher(qualifier);
            if(!matcher.find()){
                return false;
            }
            String prefix = matcher.group(1);
            int value = Integer.parseInt(matcher.group(2));
            ResConfig resConfig = mConfig;
            if("mcc".equals(prefix)){
                resConfig.setMcc(value);
            }else if("mnc".equals(prefix)) {
                resConfig.setMnc(value);
            }else if("v".equals(prefix)){
                resConfig.setSdkVersion(value);
            }else {
                return false;
            }
            return true;
        }
        private void parseDp(){
            if(isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseDp(qualifiers[i])){
                    qualifiers[i] = null;
                }
            }
        }
        private boolean parseDp(String qualifier){
            if(qualifier == null){
                return false;
            }
            Matcher matcher = PATTERN_DP.matcher(qualifier);
            if(!matcher.find()){
                return false;
            }
            String prefix = matcher.group(1);
            int value = Integer.parseInt(matcher.group(2));
            ResConfig resConfig = this.mConfig;
            if("sw".equals(prefix)){
                resConfig.setSmallestScreenWidthDp(value);
            }else if("w".equals(prefix)) {
                resConfig.setScreenWidthDp(value);
            }else if("h".equals(prefix)){
                resConfig.setScreenHeightDp(value);
            }else {
                return false;
            }
            return true;
        }
        private void parseWidthHeight(){
            if(isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseWidthHeight(qualifiers[i])){
                    qualifiers[i] = null;
                    return;
                }
            }
        }
        private boolean parseWidthHeight(String qualifier){
            if(qualifier == null){
                return false;
            }
            Matcher matcher = PATTERN_WIDTH_HEIGHT.matcher(qualifier);
            if(!matcher.find()){
                return false;
            }
            int width = Integer.parseInt(matcher.group(1));
            int height = Integer.parseInt(matcher.group(2));
            ResConfig resConfig = this.mConfig;
            resConfig.setScreenWidth(width);
            resConfig.setScreenHeight(height);
            return true;
        }
        private void parseLocaleNumberingSystem(){
            if(isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseLocaleNumberingSystem(qualifiers[i])){
                    qualifiers[i] = null;
                    return;
                }
            }
        }
        private boolean parseLocaleNumberingSystem(String qualifier){
            if(qualifier == null){
                return false;
            }
            Matcher matcher = PATTERN_LOCALE_NUMBERING_SYSTEM.matcher(qualifier);
            if(!matcher.find()){
                return false;
            }
            this.mConfig.setLocaleNumberingSystem(matcher.group(1));
            return true;
        }
        private void parseLocaleScriptVariant(){
            if(this.mLanguageRegionParsed || isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseLocaleScriptVariant(qualifiers[i])){
                    qualifiers[i] = null;
                    this.mLanguageRegionParsed = true;
                    return;
                }
            }
        }
        private boolean parseLocaleScriptVariant(String qualifier){
            if(qualifier == null || qualifier.length() < 4 ){
                return false;
            }
            char[] chars = qualifier.toCharArray();
            if(chars[0] != 'b' || chars[1] != '+'){
                return false;
            }
            Matcher matcher = PATTERN_LOCALE_SCRIPT_VARIANT.matcher(qualifier);
            if(!matcher.find()){
                return false;
            }
            String language = trimPlus(matcher.group(1));
            String region = trimPlus(matcher.group(2));
            String script = trimPlus(matcher.group(3));
            String variant = trimPlus(matcher.group(4));
            if(script == null && variant == null){
                return false;
            }
            ResConfig resConfig = this.mConfig;
            resConfig.setLanguage(language);
            resConfig.setRegion(region);
            resConfig.setLocaleScript(script);
            resConfig.setLocaleVariant(variant);
            return true;
        }

        private void parseLanguage(){
            if(mLanguageRegionParsed || isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseLanguage(qualifiers[i])){
                    qualifiers[i] = null;
                    return;
                }
            }
        }
        private boolean parseLanguage(String qualifier){
            if(!isLanguage(qualifier)){
                return false;
            }
            this.mConfig.setLanguage(qualifier);
            return true;
        }
        private void parseRegion(){
            if(mLanguageRegionParsed || isEmpty()){
                return;
            }
            String[] qualifiers = this.mQualifiers;
            for(int i = 0; i < qualifiers.length; i++){
                if(parseRegion(qualifiers[i])){
                    qualifiers[i] = null;
                    return;
                }
            }
        }
        private boolean parseRegion(String qualifier){
            if(!isRegion(qualifier)){
                return false;
            }
            this.mConfig.setRegion(qualifier);
            return true;
        }


        private boolean isEmpty(){
            if(!mEmpty){
                mEmpty = isEmpty(mQualifiers);
            }
            return mEmpty;
        }

        private static boolean isEmpty(String[] qualifiers){
            if(qualifiers == null){
                return true;
            }
            for(int i = 0; i < qualifiers.length; i++){
                String qualifier = qualifiers[i];
                if(qualifier == null){
                    continue;
                }
                if(qualifier.length() == 0){
                    qualifiers[i] = null;
                    continue;
                }
                return false;
            }
            return true;
        }
        private static String trimPlus(String text){
            if(text == null||text.length() == 0){
                return null;
            }
            if(text.charAt(0) == '+'){
                text = text.substring(1);
            }
            return text;
        }
        private static boolean isLanguage(String qualifier){
            if(qualifier == null){
                return false;
            }
            char[] chars = qualifier.toCharArray();
            int length = chars.length;
            if(length != 2  && length !=3 ){
                return false;
            }
            for(int i = 0; i < length; i++){
                if(!isAtoZLower(chars[i])) {
                    return false;
                }
            }
            return true;
        }
        private static boolean isRegion(String qualifier){
            if(qualifier == null || qualifier.length() != 3){
                return false;
            }
            char[] chars = qualifier.toCharArray();
            boolean checkDigit = false;
            for(int i = 0; i < chars.length; i++){
                char ch = chars[i];
                if(i == 0){
                    if(ch == 'r'){
                        continue;
                    }
                    checkDigit = isDigit(ch);
                    if(checkDigit){
                        continue;
                    }
                    return false;
                }
                if(checkDigit){
                    if(!isDigit(ch)){
                        return false;
                    }
                }else if(!isAtoZUpper(ch)) {
                    return false;
                }
            }
            return true;
        }
        private static String[] splitQualifiers(String qualifier){
            if(qualifier == null || qualifier.length() == 0){
                return null;
            }
            return qualifier.split("-");
        }
        private static boolean isDigit(char ch){
            return ch <= '9' && ch >= '0';
        }
        private static boolean isAtoZLower(char ch){
            return ch <= 'z' && ch >= 'a';
        }
        private static boolean isAtoZUpper(char ch){
            return ch <= 'Z' && ch >= 'A';
        }

        private static final Pattern PATTERN_PREFIX_NUMBER = Pattern.compile("^([mcnv]+)([0-9]+)$");
        private static final Pattern PATTERN_DP = Pattern.compile("^([swh]+)([0-9]+)dp$");
        private static final Pattern PATTERN_WIDTH_HEIGHT = Pattern.compile("^([0-9]+)[xX]([0-9]+)$");
        private static final Pattern PATTERN_LOCALE_NUMBERING_SYSTEM = Pattern.compile("^u\\+nu\\+(.{1,8})$");
        private static final Pattern PATTERN_LOCALE_SCRIPT_VARIANT = Pattern.compile("^b(\\+[a-z]{2})?(\\+r[A-Z]{2})?(\\+[A-Z][a-z]{3})?(\\+[A-Z]{2,8})?$");
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
    private static final int OFFSET_localeNumberingSystem = 48;
    //SIZE=60

    private static final int LEN_localeScript = 4;
    private static final int LEN_localeVariant = 8;
    private static final int LEN_localeNumberingSystem = 8;

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
    private static final String NAME_input_flags_keys_hidden = "input_flags_keys_hidden";
    private static final String NAME_input_flags_nav_hidden = "input_flags_nav_hidden";
    private static final String NAME_inputPad0 = "inputPad0";
    private static final String NAME_screenWidth = "screenWidth";
    private static final String NAME_screenHeight = "screenHeight";
    private static final String NAME_sdkVersion = "sdkVersion";
    private static final String NAME_minorVersion = "minorVersion";
    //SIZE=28
    private static final String NAME_screen_layout_size = "screen_layout_size";
    private static final String NAME_screen_layout_long = "screen_layout_long";
    private static final String NAME_screen_layout_dir = "screen_layout_dir";
    private static final String NAME_ui_mode_type = "ui_mode_type";
    private static final String NAME_ui_mode_night = "ui_mode_night";
    private static final String NAME_smallestScreenWidthDp = "smallestScreenWidthDp";
    //SIZE=32 = "";
    private static final String NAME_screenWidthDp = "screenWidthDp";
    private static final String NAME_screenHeightDp = "screenHeightDp";
    //SIZE=36
    private static final String NAME_localeScript = "localeScript";
    private static final String NAME_localeVariant = "localeVariant";
    private static final String NAME_screen_layout_round = "screen_layout_round";
    private static final String NAME_color_mode_wide = "color_mode_wide";
    private static final String NAME_color_mode_hdr = "color_mode_hdr";

    private static final String NAME_localeNumberingSystem = "localeNumberingSystem";

    private static final char POSTFIX_locale = '#';

}
