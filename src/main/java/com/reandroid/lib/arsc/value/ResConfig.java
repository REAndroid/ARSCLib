package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.io.BlockLoad;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.ByteArray;
import com.reandroid.lib.arsc.item.ByteItem;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.ShortItem;

import java.io.IOException;

public class ResConfig extends BlockArray<Block> implements BlockLoad {
    private final IntegerItem configSize;
    private final ShortItem mcc;
    private final ShortItem mnc;
    private final ByteItem languageIn0;
    private final ByteItem languageIn1;
    private final ByteItem countryIn0;
    private final ByteItem countryIn1;
    private final ByteItem orientation;
    private final ByteItem touchscreen;
    private final ShortItem density;
    private final ByteItem keyboard;
    private final ByteItem navigation;
    private final ByteItem inputFlags;
    private final ByteItem inputPad0;
    private final ShortItem screenWidth;
    private final ShortItem  screenHeight;
    private final ShortItem sdkVersion;
    private final ShortItem minorVersion;
    private final ByteItem screenLayout;
    private final ByteItem uiMode;
    private final ShortItem smallestScreenWidthDp;
    private final ShortItem screenWidthDp;
    private final ShortItem screenHeightDp;
    private final ByteArray localeScript;
    private final ByteArray localeVariant;
    private final ByteItem screenLayout2;
    private final ByteItem colorMode;
    private final ShortItem reservedPadding;
    private final ByteArray skipSizeGreater56;
    private final ByteArray exceedingSize;
    private final ByteArray remainingSize;
    private int mCurrentSize;
    private boolean mIsUpdatingSize;

    private String mQualifiers;


    public ResConfig(){
        super();

        this.configSize = new IntegerItem(64);
        this.mcc = new ShortItem();
        this.mnc = new ShortItem();
        this.languageIn0 = new ByteItem();
        this.languageIn1 = new ByteItem();
        this.countryIn0 = new ByteItem();
        this.countryIn1 = new ByteItem();
        this.orientation = new ByteItem();
        this.touchscreen = new ByteItem();
        this.density = new ShortItem();
        this.keyboard = new ByteItem();
        this.navigation = new ByteItem();
        this.inputFlags = new ByteItem();
        this.inputPad0 = new ByteItem();
        this.screenWidth = new ShortItem();
        this.screenHeight = new ShortItem();
        this.sdkVersion = new ShortItem();
        this.minorVersion = new ShortItem();
        this.screenLayout = new ByteItem();
        this.uiMode = new ByteItem();
        this.smallestScreenWidthDp = new ShortItem();
        this.screenWidthDp = new ShortItem();
        this.screenHeightDp = new ShortItem();
        // size sum=44;
        this.localeScript = new ByteArray(4);
        this.localeVariant = new ByteArray(8);
        this.screenLayout2 = new ByteItem();
        this.colorMode = new ByteItem();
        this.reservedPadding = new ShortItem();
        this.skipSizeGreater56 = new ByteArray(4);
        this.exceedingSize = new ByteArray(8);
        this.remainingSize = new ByteArray();

        configSize.setBlockLoad(this);
        initChildes();
        mCurrentSize=64;
        setConfigSize(mCurrentSize);
    }

    @Override
    protected void onRefreshed() {

    }

    @Override
    public Block newInstance() {
        return null;
    }

    @Override
    public Block[] newInstance(int len) {
        return new Block[len];
    }

    public void parseQualifiers(String name){
        ResConfigHelper.parseQualifiers(this, name);
        mQualifiers=null;
    }


    private void initChildes(){
        add(configSize);
        add(mcc);
        add(mnc);
        add(languageIn0);
        add(languageIn1);
        add(countryIn0);
        add(countryIn1);
        add(orientation);
        add(touchscreen);
        add(density);
        add(keyboard);
        add(navigation);
        add(inputFlags);
        add(inputPad0);
        add(screenWidth);
        add(screenHeight);
        add(sdkVersion);
        add(minorVersion);
        add(screenLayout);
        add(uiMode);
        add(smallestScreenWidthDp);
        add(screenWidthDp);
        add(screenHeightDp);
        add(localeScript);
        add(localeVariant);
        add(screenLayout2);
        add(colorMode);
        add(reservedPadding);
        add(skipSizeGreater56);
        add(exceedingSize);
        add(remainingSize);
    }
    private void resetToDefault(){
        configSize.set(0);
        mcc.set((short) 0);
        mnc.set((short) 0);
        languageIn0.set((byte)0);
        languageIn1.set((byte)0);
        countryIn0.set((byte)0);
        countryIn1.set((byte)0);
        orientation.set((byte)0);
        touchscreen.set((byte)0);
        density.set((short) 0);
        keyboard.set((byte)0);
        navigation.set((byte)0);
        inputFlags.set((byte)0);
        inputPad0.set((byte)0);
        screenWidth.set((short) 0);
        screenHeight.set((short) 0);
        sdkVersion.set((short) 0);
        minorVersion.set((short) 0);
        screenLayout.set((byte)0);
        uiMode.set((byte)0);
        smallestScreenWidthDp.set((byte)0);
        screenWidthDp.set((byte)0);
        screenHeightDp.set((byte)0);
        localeScript.clear();
        localeVariant.clear();
        screenLayout2.set((byte)0);
        colorMode.set((byte)0);
        reservedPadding.set((short) 0);
        skipSizeGreater56.clear();
        exceedingSize.clear();
        remainingSize.clear();
        mCurrentSize=0;
        setConfigSize(DEFAULT_CONFIG_SIZE);
    }
    public void setConfigSize(int i){
        this.configSize.set(i);
        updateConfigSize(i);
    }
    public int getConfigSize(){
        return this.configSize.get();
    }
    public void setMcc(short sh){
        this.mcc.set(sh);
    }
    public short getMcc(){
        return this.mcc.get();
    }
    public void setMnc(short sh){
        this.mnc.set(sh);
    }
    public short getMnc(){
        return this.mnc.get();
    }
    public void setLanguageIn0(byte b){
        byte old=languageIn0.get();
        this.languageIn0.set(b);
        valuesChanged(b, old);
    }
    public byte getLanguageIn0(){
        return this.languageIn0.get();
    }
    public void setLanguageIn1(byte b){
        byte old=languageIn1.get();
        this.languageIn1.set(b);
        valuesChanged(b, old);
    }
    public byte getLanguageIn1(){
        return this.languageIn1.get();
    }
    public char[] getLanguage(){
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
        byte old=countryIn0.get();
        this.countryIn0.set(b);
        valuesChanged(b, old);
    }
    public byte getCountryIn0(){
        return this.countryIn0.get();
    }
    public void setCountryIn1(byte b){
        byte old=countryIn1.get();
        this.countryIn1.set(b);
        valuesChanged(b, old);
    }
    public byte getCountryIn1(){
        return this.countryIn1.get();
    }
    public char[] getRegion(){
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
    public void setOrientation(byte b){
        byte old=orientation.get();
        this.orientation.set(b);
        valuesChanged(b, old);
    }
    public byte getOrientation(){
        return this.orientation.get();
    }
    public void setTouchscreen(byte b){
        byte old=touchscreen.get();
        this.touchscreen.set(b);
        valuesChanged(b, old);
    }
    public byte getTouchscreen(){
        return this.touchscreen.get();
    }
    public void setDensity(short sh){
        short old=density.get();
        this.density.set(sh);
        valuesChanged(sh, old);
    }
    public short getDensity(){
        return this.density.get();
    }
    public void setKeyboard(byte b){
        this.keyboard.set(b);
    }
    public byte getKeyboard(){
        return this.keyboard.get();
    }
    public void setNavigation(byte b){
        this.navigation.set(b);
    }
    public byte getNavigation(){
        return this.navigation.get();
    }
    public void setInputFlags(byte b){
        this.inputFlags.set(b);
    }
    public byte getInputFlags(){
        return this.inputFlags.get();
    }
    public void setInputPad0(byte b){
        this.inputPad0.set(b);
    }
    public byte getInputPad0(){
        return this.inputPad0.get();
    }
    public void setScreenSize(short w, short h){
        this.setScreenWidth(w);
        this.setScreenHeight(h);
    }
    public void setScreenWidth(short sh){
        short old=screenWidth.get();
        this.screenWidth.set(sh);
        valuesChanged(sh, old);
    }
    public short getScreenWidth(){
        return this.screenWidth.get();
    }
    public void setScreenHeight(short sh){
        short old=screenHeight.get();
        this.screenHeight.set(sh);
        valuesChanged(sh, old);
    }
    public short getScreenHeight(){
        return this.screenHeight.get();
    }
    public void setSdkVersion(short sh){
        short old=sdkVersion.get();
        this.sdkVersion.set(sh);
        valuesChanged(sh, old);
    }
    public short getSdkVersion(){
        return this.sdkVersion.get();
    }
    public void setMinorVersion(short sh){
        this.minorVersion.set(sh);
    }
    public short getMinorVersion(){
        return this.minorVersion.get();
    }
    public void setScreenLayout(byte b){
        this.screenLayout.set(b);
    }
    public byte getScreenLayout(){
        return this.screenLayout.get();
    }
    public void setUiMode(byte b){
        this.uiMode.set(b);
    }
    public byte getUiMode(){
        return this.uiMode.get();
    }
    public void setSmallestScreenWidthDp(short sh){
        this.smallestScreenWidthDp.set(sh);
    }
    public short getSmallestScreenWidthDp(){
        return this.smallestScreenWidthDp.get();
    }
    public void setScreenWidthDp(short sh){
        this.screenWidthDp.set(sh);
    }
    public short getScreenWidthDp(){
        return this.screenWidthDp.get();
    }
    public void setScreenHeightDp(short sh){
        this.screenHeightDp.set(sh);
    }
    public short getScreenHeightDp(){
        return this.screenHeightDp.get();
    }
    public void setLocaleScript(byte[] bts){
        this.localeScript.set(bts);
    }
    public void setLocaleScript(char[] chs){
        byte[] bts=toByteArray(chs, localeScript.size());
        this.localeScript.set(bts);
    }
    public char[] getLocaleScript(){
        byte[] bts=localeScript.toArray();
        return toCharArray(bts);
    }
    public void setLocaleVariant(byte[] bts){
        this.localeVariant.set(bts);
    }
    public void setLocaleVariant(char[] chs){
        byte[] bts=toByteArray(chs, localeVariant.size());
        this.localeVariant.set(bts);
    }
    public char[] getLocaleVariant(){
        return toCharArray(localeVariant.toArray());
    }
    public void setScreenLayout2(byte b){
        this.screenLayout2.set(b);
    }
    public byte getScreenLayout2(){
        return this.screenLayout2.get();
    }
    public void setColorMode(byte b){
        this.colorMode.set(b);
    }
    public byte getColorMode(){
        return this.colorMode.get();
    }
    public void setReservedPadding(short sh){
        this.reservedPadding.set(sh);
    }
    public short getReservedPadding(){
        return this.reservedPadding.get();
    }

    private void valuesChanged(int val, int old){
        if(val==old){
            return;
        }
        valuesChanged();
    }
    private void valuesChanged(){
        mQualifiers=null;
    }

    public String getQualifiers(){
        if(mQualifiers==null){
            mQualifiers = ResConfigHelper.toQualifier(this).trim();
        }
        return mQualifiers;
    }
    @Override
    public int countBytes(){
        if(mIsUpdatingSize){
            return super.countBytes();
        }
        return mCurrentSize;
    }

    private void updateConfigSize(int sz){
        if(sz==mCurrentSize){
            return;
        }
        mIsUpdatingSize=true;
        mCurrentSize=sz;
        localeScript.setSize(4);
        if(sz<=48){
            localeVariant.setSize(4);
            exceedingSize.setSize(0);
            remainingSize.setSize(0);
            skipSizeGreater56.setSize(0);
            mIsUpdatingSize=false;
            return;
        }
        if(sz==KNOWN_CONFIG_BYTES){
            localeVariant.setSize(8);
            skipSizeGreater56.setSize(4);
            exceedingSize.setSize(0);
            remainingSize.setSize(0);
            mIsUpdatingSize=false;
            return;
        }
        if(sz<KNOWN_CONFIG_BYTES){
            int i=sz-KNOWN_CONFIG_BYTES;
            localeVariant.setSize(i);
            skipSizeGreater56.setSize(0);
            exceedingSize.setSize(0);
            remainingSize.setSize(0);
            mIsUpdatingSize=false;
            return;
        }
        int ex=sz-KNOWN_CONFIG_BYTES;
        localeVariant.setSize(8);
        skipSizeGreater56.setSize(4);
        exceedingSize.setSize(ex);
        int rem=sz-64;
        remainingSize.setSize(rem);
        mIsUpdatingSize=false;
    }

    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==configSize){
            if(configSize.get()!=64){
                configSize.get();
            }
            setConfigSize(configSize.get());
        }
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
    @Override
    public boolean equals(Object o){
        if(o instanceof ResConfig){
            ResConfig config=(ResConfig)o;
            return getQualifiers().equals(config.getQualifiers());
        }
        return false;
    }
    public boolean isDefault(){
        return getQualifiers().length()==0;
    }
    @Override
    public String toString(){
        String q=getQualifiers();
        if(q.length()==0){
            q="DEFAULT";
        }
        return "["+q+"]";
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


    private static final int KNOWN_CONFIG_BYTES = 56;

    private static final int DEFAULT_CONFIG_SIZE = 64;


}
