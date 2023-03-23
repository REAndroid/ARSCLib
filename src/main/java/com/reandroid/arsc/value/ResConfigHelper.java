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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResConfigHelper {
    public static ResConfig fromQualifiers(String qualifiers){
        ResConfig result=new ResConfig();
        result.setConfigSize(ResConfig.SIZE_64);
        parseQualifiers(result, qualifiers);
        result.refresh();
        return result;
    }
    public static String toQualifier(ResConfig resConfig){
        StringBuilder builder=new StringBuilder();
        builder.append(decodeLanguageAndCountry(resConfig));
        builder.append(decodeOrientation(resConfig));
        builder.append(decodeScreenLayout(resConfig));
        builder.append(decodeScreenLayout2(resConfig));
        builder.append(decodeScreenSize(resConfig));
        builder.append(decodeDensity(resConfig));
        builder.append(decodeScreenHeightDp(resConfig));
        builder.append(decodeSmallestScreenWidthDp(resConfig));
        builder.append(decodeScreenWidthDp(resConfig));
        builder.append(decodeNavigation(resConfig));
        builder.append(decodeUiMode(resConfig));
        builder.append(decodeTouchscreen(resConfig));
        builder.append(decodeKeyboard(resConfig));
        builder.append(decodeInputFlags(resConfig));
        builder.append(decodeMccMnc(resConfig));
        builder.append(decodeSdkVersion(resConfig));
        return sortQualifiers(builder.toString());
    }

    static void parseQualifiers(ResConfig resConfig, String name){
        if(resConfig == null || name==null){
            return;
        }
        String[] split=name.split("-");
        if(isNull(split)){
            return;
        }
        encode(resConfig, split);
    }
    public static String sortQualifiers(String qualifiers){
        if(!qualifiers.startsWith("-")){
            return qualifiers;
        }
        String[] split=qualifiers.split("-");
        if(isNull(split)){
            return qualifiers;
        }
        List<String> qList=new ArrayList<>();
        for(int i=0;i<split.length;i++){
            String q=split[i];
            if(q!=null && !qList.contains(q)){
                qList.add(q);
            }
        }
        Comparator<String> cmp=new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        };
        qList.sort(cmp);
        StringBuilder builder=new StringBuilder();
        for(String q:qList){
            builder.append('-');
            builder.append(q);
        }
        return builder.toString();
    }
    private static void encode(ResConfig resConfig, String[] split){
        encodeDensity(resConfig, split);
        encodeScreenHeightDp(resConfig, split);
        encodeScreenWidthDp(resConfig, split);
        encodeSmallestScreenWidthDp(resConfig, split);
        encodeNavigation(resConfig, split);
        encodeInputFlags(resConfig, split);
        encodeSdkVersion(resConfig, split);
        encodeKeyboard(resConfig, split);
        encodeTouchscreen(resConfig, split);
        encodeUiMode(resConfig, split);
        encodeOrientation(resConfig, split);
        encodeScreenLayout(resConfig, split);
        encodeScreenLayout2(resConfig, split);
        encodeMcc(resConfig, split);
        encodeMnc(resConfig, split);
        encodeScreenSize(resConfig, split);
        encodeLanguageAndCountry(resConfig, split);
        encodeScriptAndVariant(resConfig, split);
    }

    private static void encodeLanguageAndCountry(ResConfig resConfig, String[] split){
        if(split==null){
            return;
        }
        String lang=null;
        String country=null;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            if(lang==null){
                if(isLanguageName(s)){
                    lang=s;
                    split[i]=null;
                }else if(isCountryName(s)){
                    country=s;
                    split[i]=null;
                }
                continue;
            }
            if(country!=null){
                break;
            }
            if(isCountryName(s)){
                country=s;
                split[i]=null;
                break;
            }
            break;
        }
        if(lang!=null){
            char[] chs=lang.toCharArray();
            resConfig.setLanguage(chs);
        }
        if(country==null){
            return;
        }
        if(country.length()>2 && country.startsWith("r")){
            country=country.substring(1);
        }
        char[] chs=country.toCharArray();
        resConfig.setRegion(chs);
    }
    private static void encodeScriptAndVariant(ResConfig resConfig, String[] split){
        if(split==null){
            return;
        }
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(encodeScriptAndVariant(resConfig, s)){
                split[i] = null;
                break;
            }
        }
    }
    private static boolean encodeScriptAndVariant(ResConfig resConfig, String str){
        if(str==null){
            return false;
        }
        if(!str.startsWith("b+")){
            return false;
        }
        str = str.substring(2);
        String[] splits = str.split("\\+");
        String lang = null;
        String region = null;
        String script = null;
        String variant = null;
        for(int i=0; i<splits.length; i++){
            String s = splits[i];
            int len = s.length();
            if(len==0){
                continue;
            }
            if(isLocaleVariant(s)){
                if(variant!=null){
                    return false;
                }
                variant = s;
                continue;
            }
            if(isLanguageName(s)){
                if(lang!=null){
                    return false;
                }
                lang = s;
                continue;
            }
            boolean is_region = (region==null && isCountryName(s));
            boolean is_script = (script==null && isLocaleScript(s));
            if(is_region && is_script){
                if(s.charAt(len-1)=='#'){
                    script = s;
                    continue;
                }
                if(len==3 && s.charAt(0)=='r'){
                    region = s;
                    continue;
                }
                // TODO: should throw error or false ?
                return false;
            }
            if(is_region){
                region = s;
                continue;
            }
            if(is_script){
                script = s;
                continue;
            }
            // TODO: should throw error or false ?
            return false;
        }
        if(lang!=null){
            resConfig.setLanguage(lang);
        }
        if(region!=null){
            if(region.charAt(0)=='r'){
                region = region.substring(1);
            }
            resConfig.setRegion(region);
        }
        if(script!=null){
            resConfig.setLocaleScript(script);
        }
        if(variant!=null){
            resConfig.setLocaleVariant(variant);
        }
        return true;
    }
    public static String decodeLocale(ResConfig resConfig){
        String region = resConfig.getRegion();
        String language = resConfig.getLanguage();
        StringBuilder builder=new StringBuilder();
        if(language!=null){
            builder.append(language);
        }
        if(region!=null){
            if(language!=null){
                builder.append('-');
            }
            builder.append(region);
        }
        if(builder.length()==0){
            return null;
        }
        return builder.toString();
    }
    private static String decodeLanguageAndCountry(ResConfig resConfig) {
        StringBuilder builder = new StringBuilder();
        String localeVariant = resConfig.getLocaleVariant();
        String localeScript = resConfig.getLocaleScript();
        char[] region=resConfig.getRegionChars();
        char[] language=resConfig.getLanguageChars();
        if (localeVariant == null && localeScript == null && (region[0] != '\00' || language[0] != '\00') &&
                region.length != 3) {
            builder.append("-").append(language);
            if (region[0] != '\00') {
                builder.append("-r").append(region);
            }
        } else if(language[0] != 0 || region[0] != 0 || localeScript!=null || localeVariant!=null){
            builder.append("-b");
            if (language[0] != '\00') {
                builder.append('+').append(language);
            }
            if (localeScript != null) {
                builder.append('+').append(localeScript);
            }
            if ((region.length == 2 || region.length == 3) && region[0] != '\00') {
                builder.append('+').append(region);
            }
            if (localeVariant != null) {
                builder.append('+').append(localeVariant);
            }
        }
        return builder.toString();
    }
    private static void encodeNavigation(ResConfig resConfig, String[] split){
        byte navigation=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            switch (s) {
                case "nonav":
                    navigation = NAVIGATION_NONAV;
                    break;
                case "dpad":
                    navigation = NAVIGATION_DPAD;
                    break;
                case "trackball":
                    navigation = NAVIGATION_TRACKBALL;
                    break;
                case "wheel":
                    navigation = NAVIGATION_WHEEL;
                    break;
                default:
                    continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setNavigation(navigation);
    }
    private static String decodeNavigation(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        switch (resConfig.getNavigationByte()) {
            case NAVIGATION_NONAV:
                ret.append("-nonav");
                break;
            case NAVIGATION_DPAD:
                ret.append("-dpad");
                break;
            case NAVIGATION_TRACKBALL:
                ret.append("-trackball");
                break;
            case NAVIGATION_WHEEL:
                ret.append("-wheel");
                break;
        }
        return ret.toString();
    }
    private static void encodeInputFlags(ResConfig resConfig, String[] split){
        int inputFlags=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            switch (s) {
                case "keysexposed":
                    val = (KEYSHIDDEN_NO);
                    break;
                case "keyshidden":
                    val = (KEYSHIDDEN_YES);
                    break;
                case "keyssoft":
                    val = (KEYSHIDDEN_SOFT);
                    break;
                case "navexposed":
                    val = (NAVHIDDEN_NO);
                    break;
                case "navhidden":
                    val = (NAVHIDDEN_YES);
                    break;
                default:
                    continue;
            }
            inputFlags=(inputFlags | val);
            split[i]=null;
        }
        resConfig.setInputFlags((byte)inputFlags);
    }
    public static byte encodeInputFlags(String inputFlags){
        if(inputFlags==null){
            return 0;
        }
        String[] split=inputFlags.split("-");
        int result=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            switch (s) {
                case "keysexposed":
                    val = (KEYSHIDDEN_NO);
                    break;
                case "keyshidden":
                    val = (KEYSHIDDEN_YES);
                    break;
                case "keyssoft":
                    val = (KEYSHIDDEN_SOFT);
                    break;
                case "navexposed":
                    val = (NAVHIDDEN_NO);
                    break;
                case "navhidden":
                    val = (NAVHIDDEN_YES);
                    break;
                default:
                    continue;
            }
            result=(result | val);
            split[i]=null;
        }
        return (byte)result;
    }
    private static String decodeInputFlags(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        int inputFlags=resConfig.getInputFlagsValue();
        switch (inputFlags & MASK_KEYSHIDDEN) {
            case KEYSHIDDEN_NO:
                ret.append("-keysexposed");
                break;
            case KEYSHIDDEN_YES:
                ret.append("-keyshidden");
                break;
            case KEYSHIDDEN_SOFT:
                ret.append("-keyssoft");
                break;
        }
        switch (inputFlags & MASK_NAVHIDDEN) {
            case NAVHIDDEN_NO:
                ret.append("-navexposed");
                break;
            case NAVHIDDEN_YES:
                ret.append("-navhidden");
                break;
        }
        return ret.toString();
    }
    public static String decodeInputFlags(byte inputFlags){
        StringBuilder builder=new StringBuilder();
        switch (inputFlags & MASK_KEYSHIDDEN) {
            case KEYSHIDDEN_NO:
                builder.append("-keysexposed");
                break;
            case KEYSHIDDEN_YES:
                builder.append("-keyshidden");
                break;
            case KEYSHIDDEN_SOFT:
                builder.append("-keyssoft");
                break;
        }
        switch (inputFlags & MASK_NAVHIDDEN) {
            case NAVHIDDEN_NO:
                builder.append("-navexposed");
                break;
            case NAVHIDDEN_YES:
                builder.append("-navhidden");
                break;
        }
        if(builder.length()==0){
            return null;
        }
        return builder.toString();
    }
    private static void encodeKeyboard(ResConfig resConfig, String[] split){
        byte keyboard=0;
        for(int i=0;i<split.length;i++){
            ResConfig.Keyboard key = ResConfig.Keyboard.fromName(split[i]);
            if(key==null){
                continue;
            }
            keyboard=key.getByteValue();
            split[i]=null;
            break;
        }
        resConfig.setKeyboard(keyboard);
    }
    private static String decodeKeyboard(ResConfig resConfig){
        ResConfig.Keyboard keyboard=resConfig.getKeyboard();
        if(keyboard==null){
            return "";
        }
        return "-"+keyboard.toString();
    }
    /*
    * Encodes density to value
    * densityName is full name like: mdpi, xxxdpi, 580dpi ... */
    public static int encodeDensity(String densityName){
        short density=0;
        if(densityName==null){
            return density;
        }
        Matcher matcher=PATTERN_DENSITY.matcher(densityName);
        if(!matcher.find()){
            return density;
        }
        return encodeDensityName(matcher.group(1));
    }
    private static int encodeDensityName(String name){
        if("l".equals(name)){
            return  DENSITY_LOW;
        }else if("m".equals(name)){
            return  DENSITY_MEDIUM;
        }else if("h".equals(name)){
            return  DENSITY_HIGH;
        }else if("tv".equals(name)){
            return  DENSITY_TV;
        }else if("xh".equals(name)){
            return  DENSITY_XHIGH;
        }else if("xxh".equals(name)){
            return  DENSITY_XXHIGH;
        }else if("xxxh".equals(name)){
            return  DENSITY_XXXHIGH;
        }else if("any".equals(name)){
            return  DENSITY_ANY;
        }else if("no".equals(name)){
            return  DENSITY_NONE;
        }else if(isDecimal(name)){
            return  (short) Integer.parseInt(name);
        }
        return 0;
    }
    private static void encodeDensity(ResConfig resConfig, String[] split){
        int density=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_DENSITY.matcher(s);
            if(!matcher.find()){
                continue;
            }
            density=encodeDensityName(matcher.group(1));
            if(density==0){
                continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setDensity((short) density);
    }
    private static String decodeDensity(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        int density=resConfig.getDensityValue();
        switch (density) {
            case DENSITY_DEFAULT:
                break;
            case DENSITY_LOW:
                ret.append("-ldpi");
                break;
            case DENSITY_MEDIUM:
                ret.append("-mdpi");
                break;
            case DENSITY_HIGH:
                ret.append("-hdpi");
                break;
            case DENSITY_TV:
                ret.append("-tvdpi");
                break;
            case DENSITY_XHIGH:
                ret.append("-xhdpi");
                break;
            case DENSITY_XXHIGH:
                ret.append("-xxhdpi");
                break;
            case DENSITY_XXXHIGH:
                ret.append("-xxxhdpi");
                break;
            case DENSITY_ANY:
                ret.append("-anydpi");
                break;
            case DENSITY_NONE:
                ret.append("-nodpi");
                break;
            default:
                ret.append('-').append(density).append("dpi");
        }
        return ret.toString();
    }
    public static String decodeDensity(int density){
        switch (density) {
            case DENSITY_DEFAULT:
                return null;
            case DENSITY_LOW:
                return "ldpi";
            case DENSITY_MEDIUM:
                return "mdpi";
            case DENSITY_HIGH:
                return "hdpi";
            case DENSITY_TV:
                return "tvdpi";
            case DENSITY_XHIGH:
                return "xhdpi";
            case DENSITY_XXHIGH:
                return "xxhdpi";
            case DENSITY_XXXHIGH:
                return "xxxhdpi";
            case DENSITY_ANY:
                return "anydpi";
            case DENSITY_NONE:
                return "nodpi";
            default:
                return density+"dpi";
        }
    }
    private static void encodeTouchscreen(ResConfig resConfig, String[] split){
        byte touchscreen=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            ResConfig.Touchscreen touch= ResConfig.Touchscreen.fromName(s);
            if(touch==null){
                continue;
            }
            touchscreen=touch.getByteValue();
            split[i]=null;
            break;
        }
        resConfig.setTouchscreen(touchscreen);
    }
    private static String decodeTouchscreen(ResConfig resConfig){
        ResConfig.Touchscreen touchscreen=resConfig.getTouchscreen();
        if(touchscreen==null){
            return "";
        }
        return "-"+touchscreen.toString();
    }
    private static void encodeUiMode(ResConfig resConfig, String[] split){
        resConfig.setUiModeNight(ResConfig.UiModeNight.fromQualifiers(split));
        resConfig.setUiModeType(ResConfig.UiModeType.fromQualifiers(split));
    }
    private static String decodeUiMode(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        ResConfig.UiModeType uiModeType = resConfig.getUiModeType();
        if(uiModeType!=null){
            ret.append('-').append(uiModeType.toString());
        }
        ResConfig.UiModeNight uiModeNight = resConfig.getUiModeNight();
        if(uiModeNight!=null){
            ret.append('-').append(uiModeNight.toString());
        }
        return ret.toString();
    }
    private static void encodeOrientation(ResConfig resConfig, String[] split){
        byte orientationByte=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            ResConfig.Orientation orientation= ResConfig.Orientation.fromName(s);
            if(orientation==null){
                continue;
            }
            orientationByte=orientation.getByteValue();
            split[i]=null;
            break;
        }
        resConfig.setOrientation(orientationByte);
    }
    private static String decodeOrientation(ResConfig resConfig){
        ResConfig.Orientation orientation=resConfig.getOrientation();
        if(orientation==null){
            return "";
        }
        return "-"+orientation.toString();
    }
    private static void encodeScreenLayout(ResConfig resConfig, String[] split){
        resConfig.setScreenLayoutSize(ResConfig.ScreenLayoutSize.fromQualifiers(split));
        resConfig.setScreenLayoutLong(ResConfig.ScreenLayoutLong.fromQualifiers(split));
        resConfig.setScreenLayoutDir(ResConfig.ScreenLayoutDir.fromQualifiers(split));
    }
    private static String decodeScreenLayout(ResConfig resConfig){
        StringBuilder builder=new StringBuilder();
        ResConfig.ScreenLayoutSize layoutSize = resConfig.getScreenLayoutSize();
        if(layoutSize!=null){
            builder.append('-').append(layoutSize.toString());
        }
        ResConfig.ScreenLayoutLong layoutLong = resConfig.getScreenLayoutLong();
        if(layoutLong!=null){
            builder.append('-').append(layoutLong.toString());
        }
        ResConfig.ScreenLayoutDir layoutDir = resConfig.getScreenLayoutDir();
        if(layoutDir!=null){
            builder.append('-').append(layoutDir.toString());
        }
        return builder.toString();
    }
    private static void encodeScreenLayout2(ResConfig resConfig, String[] split){
        int screenLayout2=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            if("round".equals(s)){
                val = (SCREENLAYOUT_ROUND_YES);
            }else if("notround".equals(s)){
                val = (SCREENLAYOUT_ROUND_NO);
            }else {
                continue;
            }
            screenLayout2 = (screenLayout2 | val);
            split[i]=null;
            break;
        }
        resConfig.setScreenLayout2((byte) screenLayout2);
    }
    private static String decodeScreenLayout2(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        switch (resConfig.getScreenLayout2() & MASK_SCREENROUND) {
            case SCREENLAYOUT_ROUND_NO:
                ret.append("-notround");
                break;
            case SCREENLAYOUT_ROUND_YES:
                ret.append("-round");
                break;
        }
        return ret.toString();
    }
    private static void encodeMcc(ResConfig resConfig, String[] split){
        short sh=(short)0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_MCC_MNC.matcher(s);
            if(!matcher.find()){
                continue;
            }
            String mccMnc=matcher.group(1);
            int val=Integer.parseInt(matcher.group(2));
            if(val==0){
                val=-1;
            }
            sh=(short)val;
            if(!"mcc".equals(mccMnc)){
                continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setMcc(sh);
    }
    private static void encodeMnc(ResConfig resConfig, String[] split){
        short sh=(short)0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_MCC_MNC.matcher(s);
            if(!matcher.find()){
                continue;
            }
            String mccMnc=matcher.group(1);
            int val=Integer.parseInt(matcher.group(2));
            sh=(short)val;
            if(!"mnc".equals(mccMnc)){
                continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setMnc(sh);
    }
    private static String decodeMccMnc(ResConfig resConfig){
        int mcc=resConfig.getMcc();
        int mnc=resConfig.getMnc();
        int size=resConfig.getConfigSize();
        StringBuilder ret = new StringBuilder();
        if (mcc != 0) {
            ret.append("-mcc").append(String.format("%03d", mcc));
            if (mnc != -1) {
                if (mnc != 0) {
                    ret.append("-mnc");
                    if (size <= 32) {
                        if (mnc > 0 && mnc < 10) {
                            ret.append(String.format("%02d", mnc));
                        } else {
                            ret.append(String.format("%03d", mnc));
                        }
                    } else {
                        ret.append(mnc);
                    }
                }
            } else {
                ret.append("-mnc00");
            }
        } else {
            if (mnc != 0) {
                ret.append("-mnc").append(mnc);
            }
        }
        return ret.toString();
    }
    private static void encodeSmallestScreenWidthDp(ResConfig resConfig, String[] split){
        int val=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_SCREEN_SMALLEST_DP.matcher(s);
            if(!matcher.find()){
                continue;
            }
            val=Integer.parseInt(matcher.group(1));
            split[i]=null;
            break;
        }
        resConfig.setSmallestScreenWidthDp((short)val);
    }
    private static String decodeSmallestScreenWidthDp(ResConfig resConfig){
        int smallestScreenWidthDp=resConfig.getSmallestScreenWidthDp();
        StringBuilder builder=new StringBuilder();
        if(smallestScreenWidthDp!=0){
            builder.append("-sw");
            builder.append(smallestScreenWidthDp);
            builder.append("dp");
        }
        return builder.toString();
    }
    private static void encodeScreenHeightDp(ResConfig resConfig, String[] split){
        int val=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_SCREEN_DP.matcher(s);
            if(!matcher.find()){
                continue;
            }
            String pre=matcher.group(1);
            if(!pre.equals("h")){
                continue;
            }
            val=Integer.parseInt(matcher.group(2));
            split[i]=null;
            break;
        }
        resConfig.setScreenHeightDp((short)val);
    }
    private static String decodeScreenHeightDp(ResConfig resConfig){
        int screenHeightDp=resConfig.getScreenHeightDp();
        StringBuilder builder=new StringBuilder();
        if(screenHeightDp!=0){
            builder.append("-h");
            builder.append(screenHeightDp);
            builder.append("dp");
        }
        return builder.toString();
    }
    private static void encodeScreenWidthDp(ResConfig resConfig, String[] split){
        int val=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_SCREEN_DP.matcher(s);
            if(!matcher.find()){
                continue;
            }
            String pre=matcher.group(1);
            if(!pre.equals("w")){
                continue;
            }
            val=Integer.parseInt(matcher.group(2));
            split[i]=null;
            break;
        }
        resConfig.setScreenWidthDp((short)val);
    }
    private static String decodeScreenWidthDp(ResConfig resConfig){
        int screenWidthDp=resConfig.getScreenWidthDp();
        StringBuilder builder=new StringBuilder();
        if(screenWidthDp!=0){
            builder.append("-w");
            builder.append(screenWidthDp);
            builder.append("dp");
        }
        return builder.toString();
    }
    private static void encodeSdkVersion(ResConfig resConfig, String[] split){
        int val=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_SDK_VERSION.matcher(s);
            if(!matcher.find()){
                continue;
            }
            val=Integer.parseInt(matcher.group(1));
            split[i]=null;
            break;
        }
        resConfig.setSdkVersion((short)val);
    }
    private static String decodeSdkVersion(ResConfig resConfig){
        int sdkVersion=resConfig.getSdkVersion();
        StringBuilder builder=new StringBuilder();
        if(sdkVersion!=0){
            builder.append("-v");
            builder.append(sdkVersion);
        }
        return builder.toString();
    }
    private static String decodeScreenSize(ResConfig resConfig){
        int width=resConfig.getScreenWidth();
        int height=resConfig.getScreenHeight();
        if(width==0||height==0){
            return "";
        }
        if (width > height) {
            return String.format("%dx%d", width, height);
        } else {
            return String.format("%dx%d", height, width);
        }
    }
    private static void encodeScreenSize(ResConfig resConfig, String[] split){
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            Matcher matcher=PATTERN_SCREEN_SIZE.matcher(s);
            if(!matcher.find()){
                continue;
            }
            int a=Integer.parseInt(matcher.group(1));
            int b=Integer.parseInt(matcher.group(2));
            short w;
            short h;
            if(a>b){
                w=(short)a;
                h=(short)b;
            }else {
                w=(short)b;
                h=(short)a;
            }
            resConfig.setScreenSize(w, h);
            split[i]=null;
            break;
        }
    }

    private static boolean isLanguageName(String str){
        if(str==null){
            return false;
        }
        Matcher matcher=PATTERN_LANG_NAME.matcher(str);
        return matcher.find();
    }
    private static boolean isLocaleScript(String str){
        if(str==null){
            return false;
        }
        Matcher matcher=PATTERN_LOCALE_SCRIPT.matcher(str);
        return matcher.find();
    }
    private static boolean isLocaleVariant(String str){
        if(str==null){
            return false;
        }
        Matcher matcher=PATTERN_LOCALE_VARIANT.matcher(str);
        return matcher.find();
    }
    private static boolean isCountryName(String str){
        if(str==null){
            return false;
        }
        Matcher matcher=PATTERN_COUNTRY_NAME.matcher(str);
        return matcher.find();
    }
    private static char[] toUpper(char[] chs){
        for(int i=0;i<chs.length;i++){
            chs[i]=Character.toUpperCase(chs[i]);
        }
        return chs;
    }
    private static boolean isNull(String[] split){
        if(split==null){
            return true;
        }
        if(split.length==0){
            return true;
        }
        boolean result=true;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            s=s.trim();
            if(s.length()==0){
                split[i]=null;
                continue;
            }
            result=false;
        }
        return result;
    }
    private static boolean isDecimal(String str){
        if(str==null){
            return false;
        }
        Matcher matcher=PATTERN_NUMBER.matcher(str);
        return matcher.find();
    }

    private static final Pattern PATTERN_SDK_VERSION=Pattern.compile("^-?v([0-9]+)$");

    private static final Pattern PATTERN_SCREEN_DP=Pattern.compile("^([wh])([0-9]+)dp$");

    private static final Pattern PATTERN_SCREEN_SMALLEST_DP=Pattern.compile("^sw([0-9]+)dp$");

    private static final Pattern PATTERN_LANG_NAME=Pattern.compile("^[a-z]{2}$");

    private static final Pattern PATTERN_COUNTRY_NAME=Pattern.compile("^[a-zA-Z]{2,3}$");

    private static final Pattern PATTERN_LOCALE_SCRIPT=Pattern.compile("^[a-zA-Z0-9]{3,4}$");

    private static final Pattern PATTERN_LOCALE_VARIANT=Pattern.compile("^[a-zA-Z0-9#]{5,8}$");

    private static final Pattern PATTERN_MCC_MNC=Pattern.compile("^(m[cn]c)([0-9]{2,3})$");

    private static final Pattern PATTERN_DENSITY=Pattern.compile("^([^\\s]+)dpi$");

    private static final Pattern PATTERN_NUMBER=Pattern.compile("^[0-9]+$");

    private static final Pattern PATTERN_SCREEN_SIZE=Pattern.compile("^-?([0-9]+)x([0-9]+)$");

    public final static short MASK_SCREENROUND = 0x03;
    public final static short SCREENLAYOUT_ROUND_ANY = 0;
    public final static short SCREENLAYOUT_ROUND_NO = 0x1;
    public final static short SCREENLAYOUT_ROUND_YES = 0x2;

    public final static byte MASK_KEYSHIDDEN = 0x3;
    public final static byte KEYSHIDDEN_ANY = 0x0;
    public final static byte KEYSHIDDEN_NO = 0x1;
    public final static byte KEYSHIDDEN_YES = 0x2;
    public final static byte KEYSHIDDEN_SOFT = 0x3;

    public final static byte MASK_NAVHIDDEN = 0xc;
    public final static byte NAVHIDDEN_ANY = 0x0;
    public final static byte NAVHIDDEN_NO = 0x4;
    public final static byte NAVHIDDEN_YES = 0x8;

    private final static byte NAVIGATION_ANY = 0;
    private final static byte NAVIGATION_NONAV = 1;
    private final static byte NAVIGATION_DPAD = 2;
    private final static byte NAVIGATION_TRACKBALL = 3;
    private final static byte NAVIGATION_WHEEL = 4;


    private final static int DENSITY_DEFAULT = 0;
    private final static int DENSITY_LOW = 120;
    private final static int DENSITY_MEDIUM = 160;
    private final static int DENSITY_400 = 190;
    private final static int DENSITY_TV = 213;
    private final static int DENSITY_HIGH = 240;
    private final static int DENSITY_XHIGH = 320;
    private final static int DENSITY_XXHIGH = 480;
    private final static int DENSITY_XXXHIGH = 640;
    private final static int DENSITY_ANY = 0xfffe;
    private final static int DENSITY_NONE = 0xffff;


}

