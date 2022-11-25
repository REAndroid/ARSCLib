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
    private final ByteArray screenLayout;
    private final ByteArray uiMode;
    private final ByteArray smallestScreenWidthDp;
    private final ByteArray screenWidthDp;
    private final ByteArray screenHeightDp;
    private final ByteArray localeScript;
    private final ByteArray localeVariant;
    private final ByteArray screenLayout2;
    private final ByteArray colorMode;
    private final ByteArray reservedPadding;
    private final ByteArray skipSizeGreater56;
    private final ByteArray exceedingSize;
    private final ByteArray remainingSize;
    private int mCurrentSize;
    private boolean mIsUpdatingSize;

    private String mQualifiers;


    public ResConfig(){
        super();

        this.configSize = new IntegerItem(64);//4
        this.mcc = new ShortItem();//6
        this.mnc = new ShortItem();//8
        this.languageIn0 = new ByteItem();//9
        this.languageIn1 = new ByteItem();//10
        this.countryIn0 = new ByteItem();//11
        this.countryIn1 = new ByteItem();//12
        this.orientation = new ByteItem();//13
        this.touchscreen = new ByteItem();//14
        this.density = new ShortItem();//16
        this.keyboard = new ByteItem();//17
        this.navigation = new ByteItem();//18
        this.inputFlags = new ByteItem();//19
        this.inputPad0 = new ByteItem();//20
        this.screenWidth = new ShortItem();//22
        this.screenHeight = new ShortItem();//24
        this.sdkVersion = new ShortItem();//26
        this.minorVersion = new ShortItem();//28
        //28
        this.screenLayout = new ByteArray();//29
        this.uiMode = new ByteArray();//30
        this.smallestScreenWidthDp = new ByteArray();//32
        this.screenWidthDp = new ByteArray();//34
        this.screenHeightDp = new ByteArray();//36
        // size sum=44;
        this.localeScript = new ByteArray(4);
        this.localeVariant = new ByteArray(8);
        this.screenLayout2 = new ByteArray();
        this.colorMode = new ByteArray();
        this.reservedPadding = new ByteArray();
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
        screenLayout.setSize(0);
        uiMode.setSize((byte)0);
        smallestScreenWidthDp.setSize((byte)0);
        screenWidthDp.setSize((byte)0);
        screenHeightDp.setSize((byte)0);
        localeScript.clear();
        localeVariant.clear();
        screenLayout2.setSize(0);
        colorMode.setSize(0);
        reservedPadding.setSize( 0);
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
        if(screenLayout.size()==0){
            return;
        }
        this.screenLayout.put(0,b);
    }
    public byte getScreenLayout(){
        if(screenLayout.size()==0){
            return 0;
        }
        return this.screenLayout.get(0);
    }
    public void setUiMode(byte b){
        if(uiMode.size()==0){
            return;
        }
        this.uiMode.put(0, b);
    }
    public byte getUiMode(){
        if(uiMode.size()==0){
            return 0;
        }
        return this.uiMode.get(0);
    }
    public void setSmallestScreenWidthDp(short sh){
        if(smallestScreenWidthDp.size()==0){
            return;
        }
        this.smallestScreenWidthDp.setShort(sh);
    }
    public short getSmallestScreenWidthDp(){
        if(this.smallestScreenWidthDp.size()==0){
            return 0;
        }
        return smallestScreenWidthDp.getShort();
    }
    public void setScreenWidthDp(short sh){
        if(screenWidthDp.size()==0){
            return;
        }
        this.screenWidthDp.setShort(sh);
    }
    public short getScreenWidthDp(){
        if(screenWidthDp.size()==0){
            return 0;
        }
        return screenWidthDp.getShort();
    }
    public void setScreenHeightDp(short sh){
        if(screenHeightDp.size()==0){
            return;
        }
        this.screenHeightDp.setShort(sh);
    }
    public short getScreenHeightDp(){
        if(screenHeightDp.size()==0){
            return 0;
        }
        return this.screenHeightDp.getShort();
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
        if(screenLayout2.size()==0){
            return;
        }
       this.screenLayout2.put(0,b);
    }
    public byte getScreenLayout2(){
        if(screenLayout2.size()==0){
            return 0;
        }
        return this.screenLayout2.get(0);
    }
    public void setColorMode(byte b){
        if(colorMode.size()==0){
            return;
        }
        this.colorMode.put(0,b);
    }
    public byte getColorMode(){
        if(colorMode.size()==0){
            return 0;
        }
        return this.colorMode.get(0);
    }
    public void setReservedPadding(short sh){
        if(reservedPadding.size()==0){
            return;
        }
        this.reservedPadding.setShort(sh);
    }
    public short getReservedPadding(){
        if(reservedPadding.size()==0){
            return 0;
        }
        return this.reservedPadding.get(0);
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
        localeScript.setSize(0);
        skipSizeGreater56.setSize(0);
        localeVariant.setSize(0);
        exceedingSize.setSize(0);
        if(sz==28){
            mIsUpdatingSize=false;
            return;
        }
        localeScript.setSize(4);
        if(sz==32){
            mIsUpdatingSize=false;
            return;
        }
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
