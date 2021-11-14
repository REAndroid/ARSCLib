package com.reandroid.lib.arsc.value;



import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResConfigHelper {
    public static ResConfig fromQualifiers(String qualifiers){
        ResConfig result=new ResConfig();
        result.setConfigSize(64);
        parseQualifiers(result, qualifiers);
        return result;
    }
    static String toQualifier(ResConfig resConfig){
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
        int len=split.length;
        Set<String> qSet=new HashSet<>();
        for(int i=0; i<len;i++){
            String q=split[i];
            if(q!=null){
                qSet.add(q);
            }
        }
        List<String> qList=new ArrayList<>(qSet);
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
    private static String decodeLanguageAndCountry(ResConfig resConfig) {
        StringBuilder builder = new StringBuilder();
        char[] localeVariant=resConfig.getLocaleVariant();
        char[] localeScript=resConfig.getLocaleScript();
        char[] region=resConfig.getRegion();
        char[] language=resConfig.getLanguage();
        if (localeVariant == null && localeScript == null && (region[0] != '\00' || language[0] != '\00') &&
                region.length != 3) {
            builder.append("-").append(language);
            if (region[0] != '\00') {
                builder.append("-r").append(region);
            }
        } else {
            if (language[0] == '\00' && region[0] == '\00') {
                return builder.toString();
            }
            builder.append("-b+");
            if (language[0] != '\00') {
                builder.append(language);
            }
            if (localeScript != null && localeScript.length == 4) {
                builder.append("+").append(localeScript);
            }
            if ((region.length == 2 || region.length == 3) && region[0] != '\00') {
                builder.append("+").append(region);
            }
            if (localeVariant != null && localeVariant.length >= 5) {
                builder.append("+").append(toUpper(localeVariant));
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
        switch (resConfig.getNavigation()) {
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
    private static String decodeInputFlags(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        int inputFlags=resConfig.getInputFlags();
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
    private static void encodeKeyboard(ResConfig resConfig, String[] split){
        byte keyboard=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            switch (s) {
                case "nokeys":
                    keyboard = KEYBOARD_NOKEYS;
                    break;
                case "qwerty":
                    keyboard = KEYBOARD_QWERTY;
                    break;
                case "12key":
                    keyboard = KEYBOARD_12KEY;
                    break;
                default:
                    continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setKeyboard(keyboard);
    }
    private static String decodeKeyboard(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        switch (resConfig.getKeyboard()) {
            case KEYBOARD_NOKEYS:
                ret.append("-nokeys");
                break;
            case KEYBOARD_QWERTY:
                ret.append("-qwerty");
                break;
            case KEYBOARD_12KEY:
                ret.append("-12key");
                break;
        }
        return ret.toString();
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
            String d=matcher.group("A");
            if("l".equals(d)){
                density = DENSITY_LOW;
            }else if("m".equals(d)){
                density = DENSITY_MEDIUM;
            }else if("h".equals(d)){
                density = DENSITY_HIGH;
            }else if("tv".equals(d)){
                density = DENSITY_TV;
            }else if("xh".equals(d)){
                density = DENSITY_XHIGH;
            }else if("xxh".equals(d)){
                density = DENSITY_XXHIGH;
            }else if("xxxh".equals(d)){
                density = DENSITY_XXXHIGH;
            }else if("any".equals(d)){
                density = DENSITY_ANY;
            }else if("no".equals(d)){
                density = DENSITY_NONE;
            }else if(isDecimal(d)){
                density = Integer.parseInt(d);
            }else {
                continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setDensity((short) density);
    }
    private static String decodeDensity(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        int density=resConfig.getDensity();
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
    private static void encodeTouchscreen(ResConfig resConfig, String[] split){
        byte touchscreen=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            switch (s) {
                case "notouch":
                    touchscreen = TOUCHSCREEN_NOTOUCH;
                    break;
                case "stylus":
                    touchscreen = TOUCHSCREEN_STYLUS;
                    break;
                case "finger":
                    touchscreen = TOUCHSCREEN_FINGER;
                    break;
                default:
                    continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setTouchscreen(touchscreen);
    }
    private static String decodeTouchscreen(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        byte touchscreen=resConfig.getTouchscreen();
        switch (touchscreen) {
            case TOUCHSCREEN_NOTOUCH:
                ret.append("-notouch");
                break;
            case TOUCHSCREEN_STYLUS:
                ret.append("-stylus");
                break;
            case TOUCHSCREEN_FINGER:
                ret.append("-finger");
                break;
        }
        return ret.toString();
    }
    private static void encodeUiMode(ResConfig resConfig, String[] split){
        int uiMode=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            switch (s) {
                case "car":
                    val = (UI_MODE_TYPE_CAR);
                    break;
                case "desk":
                    val = (UI_MODE_TYPE_DESK);
                    break;
                case "television":
                    val = (UI_MODE_TYPE_TELEVISION);
                    break;
                case "smallui":
                    val = (UI_MODE_TYPE_SMALLUI);
                    break;
                case "mediumui":
                    val = (UI_MODE_TYPE_MEDIUMUI);
                    break;
                case "largeui":
                    val = (UI_MODE_TYPE_LARGEUI);
                    break;
                case "godzillaui":
                    val = (UI_MODE_TYPE_GODZILLAUI);
                    break;
                case "hugeui":
                    val = (UI_MODE_TYPE_HUGEUI);
                    break;
                case "appliance":
                    val = (UI_MODE_TYPE_APPLIANCE);
                    break;
                case "watch":
                    val = (UI_MODE_TYPE_WATCH);
                    break;
                case "vrheadset":
                    val = (UI_MODE_TYPE_VR_HEADSET);
                    break;
                default:
                    continue;
            }
            uiMode=(uiMode | val);
            split[i]=null;
        }
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            if("night".equals(s)){
                val = (UI_MODE_NIGHT_YES);
            }else if("notnight".equals(s)){
                val = (UI_MODE_NIGHT_NO);
            }else {
                continue;
            }
            uiMode=(uiMode | val);
            split[i]=null;
        }
        resConfig.setUiMode((byte)uiMode);
    }
    private static String decodeUiMode(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        byte uiMode=resConfig.getUiMode();
        switch (uiMode & MASK_UI_MODE_TYPE) {
            case UI_MODE_TYPE_CAR:
                ret.append("-car");
                break;
            case UI_MODE_TYPE_DESK:
                ret.append("-desk");
                break;
            case UI_MODE_TYPE_TELEVISION:
                ret.append("-television");
                break;
            case UI_MODE_TYPE_SMALLUI:
                ret.append("-smallui");
                break;
            case UI_MODE_TYPE_MEDIUMUI:
                ret.append("-mediumui");
                break;
            case UI_MODE_TYPE_LARGEUI:
                ret.append("-largeui");
                break;
            case UI_MODE_TYPE_GODZILLAUI:
                ret.append("-godzillaui");
                break;
            case UI_MODE_TYPE_HUGEUI:
                ret.append("-hugeui");
                break;
            case UI_MODE_TYPE_APPLIANCE:
                ret.append("-appliance");
                break;
            case UI_MODE_TYPE_WATCH:
                ret.append("-watch");
                break;
            case UI_MODE_TYPE_VR_HEADSET:
                ret.append("-vrheadset");
                break;
        }
        switch (uiMode & MASK_UI_MODE_NIGHT) {
            case UI_MODE_NIGHT_YES:
                ret.append("-night");
                break;
            case UI_MODE_NIGHT_NO:
                ret.append("-notnight");
                break;
        }
        return ret.toString();
    }
    private static void encodeOrientation(ResConfig resConfig, String[] split){
        byte orientation=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            if("port".equals(s)){
                orientation = ORIENTATION_PORT;
            }else if("land".equals(s)){
                orientation = ORIENTATION_LAND;
            }else {
                continue;
            }
            split[i]=null;
            break;
        }
        resConfig.setOrientation(orientation);
    }
    private static String decodeOrientation(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        byte orientation=resConfig.getOrientation();
        switch (orientation) {
            case ORIENTATION_PORT:
                ret.append("-port");
                break;
            case ORIENTATION_LAND:
                ret.append("-land");
                break;
            case ORIENTATION_SQUARE:
                ret.append("-square");
                break;
        }
        return ret.toString();
    }
    private static void encodeScreenLayout(ResConfig resConfig, String[] split){
        int screenLayout=0;
        for(int i=0;i<split.length;i++){
            String s=split[i];
            if(s==null){
                continue;
            }
            int val;
            switch (s) {
                case "ldrtl":
                    val = (SCREENLAYOUT_LAYOUTDIR_RTL);
                    break;
                case "ldltr":
                    val = (SCREENLAYOUT_LAYOUTDIR_LTR);
                    break;
                case "small":
                    val = (SCREENSIZE_SMALL);
                    break;
                case "normal":
                    val = (SCREENSIZE_NORMAL);
                    break;
                case "large":
                    val = (SCREENSIZE_LARGE);
                    break;
                case "xlarge":
                    val = (SCREENSIZE_XLARGE);
                    break;
                case "long":
                    val = (SCREENLONG_YES);
                    break;
                case "notlong":
                    val = (SCREENLONG_NO);
                    break;
                default:
                    continue;
            }
            screenLayout = (screenLayout | val);
            split[i]=null;
        }
        resConfig.setScreenLayout((byte) screenLayout);
    }
    private static String decodeScreenLayout(ResConfig resConfig){
        StringBuilder ret=new StringBuilder();
        byte screenLayout=resConfig.getScreenLayout();
        switch (screenLayout & MASK_LAYOUTDIR) {
            case SCREENLAYOUT_LAYOUTDIR_RTL:
                ret.append("-ldrtl");
                break;
            case SCREENLAYOUT_LAYOUTDIR_LTR:
                ret.append("-ldltr");
        }
        switch (screenLayout & MASK_SCREENSIZE) {
            case SCREENSIZE_SMALL:
                ret.append("-small");
                break;
            case SCREENSIZE_NORMAL:
                ret.append("-normal");
                break;
            case SCREENSIZE_LARGE:
                ret.append("-large");
                break;
            case SCREENSIZE_XLARGE:
                ret.append("-xlarge");
                break;
        }
        switch (screenLayout & MASK_SCREENLONG) {
            case SCREENLONG_YES:
                ret.append("-long");
                break;
            case SCREENLONG_NO:
                ret.append("-notlong");
                break;
        }
        return ret.toString();
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
            String mccMnc=matcher.group("A");
            int val=Integer.parseInt(matcher.group("B"));
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
            String mccMnc=matcher.group("A");
            int val=Integer.parseInt(matcher.group("B"));
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
        short mcc=resConfig.getMcc();
        short mnc=resConfig.getMnc();
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
            String pre=matcher.group("A");
            if(!pre.equals("sw")){
                continue;
            }
            val=Integer.parseInt(matcher.group("B"));
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
            String pre=matcher.group("A");
            if(!pre.equals("h")){
                continue;
            }
            val=Integer.parseInt(matcher.group("B"));
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
            String pre=matcher.group("A");
            if(!pre.equals("w")){
                continue;
            }
            val=Integer.parseInt(matcher.group("B"));
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
            val=Integer.parseInt(matcher.group("A"));
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
        short width=resConfig.getScreenWidth();
        short height=resConfig.getScreenHeight();
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
            int a=Integer.parseInt(matcher.group("A"));
            int b=Integer.parseInt(matcher.group("B"));
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

    private static final Pattern PATTERN_SDK_VERSION=Pattern.compile("^-?v(?<A>[0-9]+)$");

    private static final Pattern PATTERN_SCREEN_DP=Pattern.compile("^(?<A>[wh])(?<B>[0-9]+)dp$");

    private static final Pattern PATTERN_SCREEN_SMALLEST_DP=Pattern.compile("^(?<A>(sw)|(sh))(?<B>[0-9]+)dp$");

    private static final Pattern PATTERN_LANG_NAME=Pattern.compile("^[a-z]{2}$");

    private static final Pattern PATTERN_COUNTRY_NAME=Pattern.compile("^[a-zA-Z]{2,3}$");

    private static final Pattern PATTERN_MCC_MNC=Pattern.compile("^(?<A>m[cn]c)(?<B>[0-9]{2,3})$");

    private static final Pattern PATTERN_DENSITY=Pattern.compile("^(?<A>[^\\s]+)dpi$");

    private static final Pattern PATTERN_NUMBER=Pattern.compile("^[0-9]+$");

    private static final Pattern PATTERN_SCREEN_SIZE=Pattern.compile("^-?(?<A>[0-9]+)x(?<B>[0-9]+)$");


    private final static short MASK_LAYOUTDIR = 0xc0;
    private final static short SCREENLAYOUT_LAYOUTDIR_ANY = 0x00;
    private final static short SCREENLAYOUT_LAYOUTDIR_LTR = 0x40;
    private final static short SCREENLAYOUT_LAYOUTDIR_RTL = 0x80;
    private final static short SCREENLAYOUT_LAYOUTDIR_SHIFT = 0x06;


    private final static byte MASK_SCREENSIZE = 0x0f;
    private final static byte SCREENSIZE_ANY = 0x00;
    private final static byte SCREENSIZE_SMALL = 0x01;
    private final static byte SCREENSIZE_NORMAL = 0x02;
    private final static byte SCREENSIZE_LARGE = 0x03;
    private final static byte SCREENSIZE_XLARGE = 0x04;

    private final static byte MASK_SCREENLONG = 0x30;
    private final static byte SCREENLONG_ANY = 0x00;
    private final static byte SCREENLONG_NO = 0x10;
    private final static byte SCREENLONG_YES = 0x20;


    private final static short MASK_SCREENROUND = 0x03;
    private final static short SCREENLAYOUT_ROUND_ANY = 0;
    private final static short SCREENLAYOUT_ROUND_NO = 0x1;
    private final static short SCREENLAYOUT_ROUND_YES = 0x2;


    private final static byte ORIENTATION_ANY = 0;
    private final static byte ORIENTATION_PORT = 1;
    private final static byte ORIENTATION_LAND = 2;
    private final static byte ORIENTATION_SQUARE = 3;

    private final static byte MASK_UI_MODE_TYPE = 0x0f;
    private final static byte UI_MODE_TYPE_ANY = 0x00;
    private final static byte UI_MODE_TYPE_NORMAL = 0x01;
    private final static byte UI_MODE_TYPE_DESK = 0x02;
    private final static byte UI_MODE_TYPE_CAR = 0x03;
    private final static byte UI_MODE_TYPE_TELEVISION = 0x04;
    private final static byte UI_MODE_TYPE_APPLIANCE = 0x05;
    private final static byte UI_MODE_TYPE_WATCH = 0x06;
    private final static byte UI_MODE_TYPE_VR_HEADSET = 0x07;


    private final static byte MASK_UI_MODE_NIGHT = 0x30;
    private final static byte UI_MODE_NIGHT_ANY = 0x00;
    private final static byte UI_MODE_NIGHT_NO = 0x10;
    private final static byte UI_MODE_NIGHT_YES = 0x20;


    private final static byte UI_MODE_TYPE_GODZILLAUI = 0x0b;
    private final static byte UI_MODE_TYPE_SMALLUI = 0x0c;
    private final static byte UI_MODE_TYPE_MEDIUMUI = 0x0d;
    private final static byte UI_MODE_TYPE_LARGEUI = 0x0e;
    private final static byte UI_MODE_TYPE_HUGEUI = 0x0f;


    private final static byte TOUCHSCREEN_ANY = 0;
    private final static byte TOUCHSCREEN_NOTOUCH = 1;
    private final static byte TOUCHSCREEN_STYLUS = 2;
    private final static byte TOUCHSCREEN_FINGER = 3;

    private final static byte MASK_KEYSHIDDEN = 0x3;
    private final static byte KEYSHIDDEN_ANY = 0x0;
    private final static byte KEYSHIDDEN_NO = 0x1;
    private final static byte KEYSHIDDEN_YES = 0x2;
    private final static byte KEYSHIDDEN_SOFT = 0x3;

    private final static byte KEYBOARD_ANY = 0;
    private final static byte KEYBOARD_NOKEYS = 1;
    private final static byte KEYBOARD_QWERTY = 2;
    private final static byte KEYBOARD_12KEY = 3;

    private final static byte MASK_NAVHIDDEN = 0xc;
    private final static byte NAVHIDDEN_ANY = 0x0;
    private final static byte NAVHIDDEN_NO = 0x4;
    private final static byte NAVHIDDEN_YES = 0x8;


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
    private final static int DENSITY_ANY = -2;
    private final static int DENSITY_NONE = -1;


}

