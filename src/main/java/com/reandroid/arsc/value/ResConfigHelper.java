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
        encodeColorMode(resConfig, split);
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
    private static void encodeNavigation(ResConfig resConfig, String[] split){
        resConfig.setNavigation(ResConfig.Navigation.fromQualifiers(split));
    }
    private static void encodeInputFlags(ResConfig resConfig, String[] split){
        resConfig.setInputFlagsKeysHidden(ResConfig.InputFlagsKeysHidden.fromQualifiers(split));
        resConfig.setInputFlagsNavHidden(ResConfig.InputFlagsNavHidden.fromQualifiers(split));
    }
    private static void encodeKeyboard(ResConfig resConfig, String[] split){
        resConfig.setKeyboard(ResConfig.Keyboard.fromQualifiers(split));
    }
    private static void encodeDensity(ResConfig resConfig, String[] split){
        resConfig.setDensity(ResConfig.Density.fromQualifiers(split));
    }
    private static void encodeTouchscreen(ResConfig resConfig, String[] split){
        resConfig.setTouchscreen(ResConfig.Touchscreen.fromQualifiers(split));
    }
    private static void encodeUiMode(ResConfig resConfig, String[] split){
        resConfig.setUiModeNight(ResConfig.UiModeNight.fromQualifiers(split));
        resConfig.setUiModeType(ResConfig.UiModeType.fromQualifiers(split));
    }
    private static void encodeOrientation(ResConfig resConfig, String[] split){
        resConfig.setOrientation(ResConfig.Orientation.fromQualifiers(split));
    }
    private static void encodeScreenLayout(ResConfig resConfig, String[] split){
        resConfig.setScreenLayoutSize(ResConfig.ScreenLayoutSize.fromQualifiers(split));
        resConfig.setScreenLayoutLong(ResConfig.ScreenLayoutLong.fromQualifiers(split));
        resConfig.setScreenLayoutDir(ResConfig.ScreenLayoutDir.fromQualifiers(split));
    }
    private static void encodeScreenLayout2(ResConfig resConfig, String[] split){
        resConfig.setScreenLayoutRound(ResConfig.ScreenLayoutRound.fromQualifiers(split));
    }
    private static void encodeColorMode(ResConfig resConfig, String[] split){
        resConfig.setColorModeWide(ResConfig.ColorModeWide.fromQualifiers(split));
        resConfig.setColorModeHdr(ResConfig.ColorModeHdr.fromQualifiers(split));
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

    private static final Pattern PATTERN_SDK_VERSION=Pattern.compile("^-?v([0-9]+)$");

    private static final Pattern PATTERN_SCREEN_DP=Pattern.compile("^([wh])([0-9]+)dp$");

    private static final Pattern PATTERN_SCREEN_SMALLEST_DP=Pattern.compile("^sw([0-9]+)dp$");

    private static final Pattern PATTERN_LANG_NAME=Pattern.compile("^[a-z]{2}$");

    private static final Pattern PATTERN_COUNTRY_NAME=Pattern.compile("^[a-zA-Z]{2,3}$");

    private static final Pattern PATTERN_LOCALE_SCRIPT=Pattern.compile("^[a-zA-Z0-9]{3,4}$");

    private static final Pattern PATTERN_LOCALE_VARIANT=Pattern.compile("^[a-zA-Z0-9#]{5,8}$");

    private static final Pattern PATTERN_MCC_MNC=Pattern.compile("^(m[cn]c)([0-9]{2,3})$");

    private static final Pattern PATTERN_SCREEN_SIZE=Pattern.compile("^-?([0-9]+)x([0-9]+)$");

}

