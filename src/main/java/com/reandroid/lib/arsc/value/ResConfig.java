package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.FixedBlockContainer;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ByteArray;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

public class ResConfig extends FixedBlockContainer
        implements BlockLoad, JSONConvert<JSONObject>, Comparable<ResConfig> {

    private final IntegerItem configSize;
    private final ByteArray mValuesContainer;

    private String mQualifiers;

    public ResConfig(){
        super(2);
        this.configSize = new IntegerItem(SIZE_64);
        this.mValuesContainer = new ByteArray(SIZE_64 - 4);
        addChild(0, configSize);
        addChild(1, mValuesContainer);
        this.configSize.setBlockLoad(this);
        this.mValuesContainer.setBlockLoad(this);
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
        }else if(sender==mValuesContainer){
            valuesChanged();
        }
    }
    @Override
    protected void onPreRefreshRefresh(){
        int count=countBytes();
        configSize.set(count);
    }
    @Override
    protected void onRefreshed() { 
        valuesChanged();
    }
    public void parseQualifiers(String name){
        ResConfigHelper.parseQualifiers(this, name);
        mQualifiers=null;
    }
    public void setConfigSize(int size){
        if(    size != SIZE_16
                && size != SIZE_28
                && size != SIZE_32
                && size != SIZE_36
                && size != SIZE_48
                && size != SIZE_56
                && size != SIZE_64
        ){
            throw new IllegalArgumentException("Invalid config size = " + size);
        }
        this.configSize.set(size);
        size=size-4;
        mValuesContainer.setSize(size);
        valuesChanged();
    }
    public int getConfigSize(){
        return this.configSize.get();
    }
    public boolean trimToSize(int size){
        int current=getConfigSize();
        if(current==size){
            return true;
        }
        if(    size != SIZE_16
                && size != SIZE_28
                && size != SIZE_32
                && size != SIZE_36
                && size != SIZE_48
                && size != SIZE_56
                && size != SIZE_64
        ){
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
    public short getMcc(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_mcc);
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
    public short getMnc(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_mnc);
    }
    public void setLanguageIn0(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set languageIn0 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_languageIn0, b);
    }
    public byte getLanguageIn0(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_languageIn0);
    }
    public void setLanguageIn1(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set languageIn1 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_languageIn1, b);
    }
    public byte getLanguageIn1(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_languageIn1);
    }
    public String getLanguage(){
        return ResConfigHelper.decodeLanguage(getLanguageChars());
    }
    public void setLanguage(String language){
        char[] chs=new char[2];
        if(language!=null){
            chs=language.toCharArray();
        }
        setLanguage(chs);
    }
    public char[] getLanguageChars(){
        byte b0=getLanguageIn0();
        byte b1=getLanguageIn1();
        return unpackLanguageOrRegion(b0, b1, 'a');
    }
    public void setLanguage(char[] chs){
        byte[] bts;
        if(isNull(chs)){
            bts=new byte[2];
        }else {
            bts=packLanguageOrRegion(chs);
        }
        setLanguageIn0(bts[0]);
        setLanguageIn1(bts[1]);
    }
    public void setCountryIn0(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set countryIn0 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_countryIn0, b);
    }
    public byte getCountryIn0(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_countryIn0);
    }
    public void setCountryIn1(byte b){
        if(getConfigSize()<SIZE_16){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set countryIn1 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_countryIn1, b);
    }
    public byte getCountryIn1(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.get(OFFSET_countryIn1);
    }
    public String getRegion(){
        return ResConfigHelper.decodeRegion(getRegionChars());
    }
    public char[] getRegionChars(){
        byte b0=getCountryIn0();
        byte b1=getCountryIn1();
        return unpackLanguageOrRegion(b0, b1, '0');
    }
    public void setRegion(char[] chs){
        byte[] bts;
        if(isNull(chs)){
            bts=new byte[2];
        }else {
            bts=packLanguageOrRegion(chs);
        }
        setCountryIn0(bts[0]);
        setCountryIn1(bts[1]);
    }
    public void setRegion(String region){
        char[] chs=new char[2];
        if(region!=null){
            chs=region.toCharArray();
        }
        setRegion(chs);
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
        setOrientation(orientation.getByteValue());
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
        setTouchscreen(touchscreen.getByteValue());
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
    public short getDensityValue(){
        if(getConfigSize()<SIZE_16){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_density);
    }
    public String getDensity(){
        return ResConfigHelper.decodeDensity(getDensityValue());
    }
    public void setDensity(String density){
        setDensity(ResConfigHelper.encodeDensity(density));
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
        setKeyboard(keyboard.getByteValue());
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
        setNavigation(navigation.getByteValue());
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
    public short getScreenWidth(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_screenWidth);
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
    public short getScreenHeight(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_screenHeight);
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
    public short getSdkVersion(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_sdkVersion);
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
    public short getMinorVersion(){
        if(getConfigSize()<SIZE_28){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_minorVersion);
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
    public short getSmallestScreenWidthDp(){
        if(getConfigSize()<SIZE_32){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_smallestScreenWidthDp);
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
    public short getScreenWidthDp(){
        if(getConfigSize()<SIZE_36){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_screenWidthDp);
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
    public short getScreenHeightDp(){
        if(getConfigSize()<SIZE_36){
            return 0;
        }
        return mValuesContainer.getShort(OFFSET_screenHeightDp);
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
    public char[] getLocaleScript(){
        if(getConfigSize()<SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeScript, LEN_localeScript);
        return toCharArray(bts);
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
    public char[] getLocaleVariant(){
        if(getConfigSize()<SIZE_48){
            return null;
        }
        byte[] bts = mValuesContainer.getByteArray(OFFSET_localeVariant, LEN_localeVariant);
        return toCharArray(bts);
    }
    public void setScreenLayout2(byte b){
        if(getConfigSize()<SIZE_56){
            if(b==0){
                return;
            }
            throw new IllegalArgumentException("Can not set screenLayout2 for config size="+getConfigSize());
        }
        mValuesContainer.put(OFFSET_screenLayout2, b);
    }
    public byte getScreenLayout2(){
        if(getConfigSize()<SIZE_56){
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

    private void valuesChanged(){
        mQualifiers=null;
    }

    public String getQualifiers(){
        if(mQualifiers==null){
            try{
                mQualifiers = ResConfigHelper.toQualifier(this).trim();
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
        if(orientation!=Orientation.ANY){
            jsonObject.put(NAME_orientation, orientation.toString());
        }
        Touchscreen touchscreen=getTouchscreen();
        if(touchscreen!=Touchscreen.NONE){
            jsonObject.put(NAME_touchscreen, touchscreen.toString());
        }
        str = getDensity();
        if(str!=null){
            jsonObject.put(NAME_density, str);
        }
        Keyboard keyboard = getKeyboard();
        if(keyboard!=Keyboard.NONE){
            jsonObject.put(NAME_keyboard, keyboard.toString());
        }
        Navigation navigation = getNavigation();
        if(navigation!=Navigation.NONE){
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

        trimToSize(SIZE_48);
        valuesChanged();
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


    private static char[] unpackLanguageOrRegion(byte b0, byte b1, char base){
        if ((((b0 >> 7) & 1) == 1)) {
            int first = b1 & 0x1F;
            int x=((b1 & 0xE0) >> 5);
            int y=((b0 & 0x03) << 3);
            int second = x + y;
            int third = (b0 & 0x7C) >> 2;

            return new char[] { (char) (first + base), (char) (second + base), (char) (third + base) };
        }
        return new char[] { (char) b0, (char) b1 };
    }
    private static byte[] packLanguageOrRegion(char[] chs){
        if(chs==null || chs.length<2){
            return new byte[2];
        }
        if(chs.length==2 || chs.length>3){
            byte[] result=new byte[2];
            result[0]=(byte) chs[0];
            result[1]=(byte) chs[1];
            return result;
        }
        int base=getBase(chs[0]);
        int first=chs[0] - base;
        int second=chs[1] - base;
        int third=chs[2] - base;

        int b1Right=first & 0x1F;

        int b0Left=third;
        b0Left=b0Left << 2;

        int b1Left=second & 7;
        b1Left=b1Left<<5;

        int b1=(b1Left | b1Right);

        int b0Right=second >> 3;
        b0Right=b0Right & 0x3;

        int b0=(b0Left | b0Right);
        return new byte[] { (byte) b0, (byte) b1 };
    }
    private static int getBase(char ch){
        int result=ch;
        if(ch>='0' && ch<='9'){
            result='0';
        }else if(ch>='a' && ch<='z'){
            result='a';
        }else if(ch>='A' && ch<='Z'){
            result='A';
        }
        result=result+32;
        return result;
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
            chs[i]= (char) bts[i];
        }
        return chs;
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
    public enum Orientation{
        ANY((byte) 0),
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
            return ANY;
        }
        public static Orientation fromName(String name){
            if(name==null){
                return ANY;
            }
            name=name.trim().toUpperCase();
            for(Orientation orientation:values()){
                if(name.equals(orientation.name())){
                    return orientation;
                }
            }
            return ANY;
        }
    }
    public enum Touchscreen{
        NONE((byte) 0x0),
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
            return NONE;
        }
        public static Touchscreen fromName(String name){
            if(name==null){
                return NONE;
            }
            name=name.trim().toUpperCase();
            for(Touchscreen touchscreen:values()){
                if(name.equals(touchscreen.name())){
                    return touchscreen;
                }
            }
            return NONE;
        }
    }
    public enum Keyboard{
        NONE((byte) 0x0),
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
            return NONE;
        }
        public static Keyboard fromName(String name){
            if(name==null){
                return NONE;
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
            return NONE;
        }
    }
    public enum Navigation{
        NONE((byte) 0),
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
            return NONE;
        }
        public static Navigation fromName(String name){
            if(name==null){
                return NONE;
            }
            name=name.trim().toUpperCase();
            for(Navigation navigation:values()){
                if(name.equals(navigation.name())){
                    return navigation;
                }
            }
            return NONE;
        }
    }

    public static final int SIZE_16 = 16;
    public static final int SIZE_28 = 28;
    public static final int SIZE_32 = 32;
    public static final int SIZE_36 = 36;
    public static final int SIZE_48 = 48;
    public static final int SIZE_56 = 56;
    public static final int SIZE_64 = 64;

    private static final int OFFSET_mcc = 0;
    private static final int OFFSET_mnc = 2;
    private static final int OFFSET_languageIn0 = 4;
    private static final int OFFSET_languageIn1 = 5;
    private static final int OFFSET_countryIn0 = 6;
    private static final int OFFSET_countryIn1 = 7;
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


}
