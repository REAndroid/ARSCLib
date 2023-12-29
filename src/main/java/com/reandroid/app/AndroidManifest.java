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
package com.reandroid.app;

import com.reandroid.utils.ObjectsUtil;

public interface AndroidManifest {

    String getPackageName();
    void setPackageName(String packageName);
    Integer getVersionCode();
    void setVersionCode(int version);
    String getVersionName();
    void setVersionName(String name);
    Integer getPlatformBuildVersionCode();
    void setPlatformBuildVersionCode(int version);
    String getPlatformBuildVersionName();
    void setPlatformBuildVersionName(String name);

    Integer getCompileSdkVersion();
    void setCompileSdkVersion(int version);
    String getCompileSdkVersionCodename();
    void setCompileSdkVersionCodename(String name);

    Integer getMinSdkVersion();
    void setMinSdkVersion(int version);

    Integer getTargetSdkVersion();
    void setTargetSdkVersion(int version);

    default AndroidApiLevel getPlatformBuild(){
        Integer api = getPlatformBuildVersionCode();
        if(api != null){
            return AndroidApiLevel.forApi(api);
        }
        return null;
    }
    default void setPlatformBuild(AndroidApiLevel apiLevel){
        setPlatformBuildVersionCode(apiLevel.getApi());
        setPlatformBuildVersionName(apiLevel.getName());
    }
    default AndroidApiLevel getCompileSdk(){
        Integer api = getCompileSdkVersion();
        if(api != null){
            return AndroidApiLevel.forApi(api);
        }
        return null;
    }
    default void setCompileSdk(AndroidApiLevel apiLevel){
        setCompileSdkVersion(apiLevel.getApi());
        setCompileSdkVersionCodename(apiLevel.getName());
    }

    public static final int ID_name = ObjectsUtil.of(0x01010003);
    public static final int ID_compileSdkVersion = ObjectsUtil.of(0x01010572);
    public static final int ID_minSdkVersion = ObjectsUtil.of(0x0101020c);
    public static final int ID_maxSdkVersion = ObjectsUtil.of(0x01010271);
    public static final int ID_targetSdkVersion = ObjectsUtil.of(0x01010270);
    public static final int ID_compileSdkVersionCodename = ObjectsUtil.of(0x01010573);
    public static final int ID_authorities = ObjectsUtil.of(0x01010018);
    public static final int ID_host = ObjectsUtil.of(0x01010028);
    public static final int ID_configChanges = ObjectsUtil.of(0x0101001f);
    public static final int ID_screenOrientation = ObjectsUtil.of(0x0101001e);
    public static final int ID_extractNativeLibs = ObjectsUtil.of(0x010104ea);
    public static final int ID_isSplitRequired = ObjectsUtil.of(0x01010591);
    public static final int ID_isFeatureSplit = ObjectsUtil.of(0x0101055b);
    public static final int ID_value = ObjectsUtil.of(0x01010024);
    public static final int ID_resource = ObjectsUtil.of(0x01010025);
    public static final int ID_versionCode = ObjectsUtil.of(0x0101021b);
    public static final int ID_versionName = ObjectsUtil.of(0x0101021c);
    public static final int ID_debuggable = ObjectsUtil.of(0x0101000f);
    public static final int ID_icon = ObjectsUtil.of(0x01010002);
    public static final int ID_roundIcon = ObjectsUtil.of(0x0101052c);
    public static final int ID_label = ObjectsUtil.of(0x01010001);
    public static final int ID_theme = ObjectsUtil.of(0x01010000);
    public static final int ID_id = ObjectsUtil.of(0x010100d0);
    public static final int ID_exported = ObjectsUtil.of(0x01010010);


    public static final String NAME_compileSdkVersion = ObjectsUtil.of("compileSdkVersion");
    public static final String NAME_compileSdkVersionCodename = ObjectsUtil.of("compileSdkVersionCodename");
    public static final String NAME_installLocation = ObjectsUtil.of("installLocation");
    public static final String NAME_PACKAGE = ObjectsUtil.of("package");
    public static final String NAME_split = ObjectsUtil.of("split");
    public static final String NAME_coreApp = ObjectsUtil.of("coreApp");
    public static final String NAME_platformBuildVersionCode = ObjectsUtil.of("platformBuildVersionCode");
    public static final String NAME_platformBuildVersionName = ObjectsUtil.of("platformBuildVersionName");
    public static final String NAME_versionCode = ObjectsUtil.of("versionCode");
    public static final String NAME_versionName = ObjectsUtil.of("versionName");
    public static final String NAME_minSdkVersion = ObjectsUtil.of("minSdkVersion");
    public static final String NAME_targetSdkVersion = ObjectsUtil.of("targetSdkVersion");
    public static final String NAME_name = ObjectsUtil.of("name");
    public static final String NAME_extractNativeLibs = ObjectsUtil.of("extractNativeLibs");
    public static final String NAME_isSplitRequired = ObjectsUtil.of("isSplitRequired");
    public static final String NAME_isFeatureSplit = ObjectsUtil.of("isFeatureSplit");
    public static final String NAME_value = ObjectsUtil.of("value");
    public static final String NAME_resource = ObjectsUtil.of("resource");
    public static final String NAME_debuggable = ObjectsUtil.of("debuggable");
    public static final String NAME_icon = ObjectsUtil.of("icon");
    public static final String NAME_roundIcon = ObjectsUtil.of("roundIcon");
    public static final String NAME_label = ObjectsUtil.of("label");
    public static final String NAME_theme = ObjectsUtil.of("theme");
    public static final String NAME_id = ObjectsUtil.of("id");
    public static final String NAME_configChanges = ObjectsUtil.of("configChanges");
    public static final String NAME_host = ObjectsUtil.of("host");
    public static final String NAME_authorities = ObjectsUtil.of("authorities");
    public static final String NAME_screenOrientation = ObjectsUtil.of("screenOrientation");
    public static final String NAME_exported = ObjectsUtil.of("exported");
    public static final String NAME_maxSdkVersion = ObjectsUtil.of("maxSdkVersion");


    public static final String NAME_requiredSplitTypes = ObjectsUtil.of("requiredSplitTypes");
    public static final String NAME_splitTypes = ObjectsUtil.of("splitTypes");



    public static final String TAG_action = ObjectsUtil.of("action");
    public static final String TAG_activity = ObjectsUtil.of("activity");
    public static final String TAG_activity_alias = ObjectsUtil.of("activity-alias");
    public static final String TAG_application = ObjectsUtil.of("application");
    public static final String TAG_category = ObjectsUtil.of("category");
    public static final String TAG_data = ObjectsUtil.of("data");
    public static final String TAG_intent_filter = ObjectsUtil.of("intent-filter");
    public static final String TAG_manifest = ObjectsUtil.of("manifest");
    public static final String TAG_meta_data = ObjectsUtil.of("meta-data");
    public static final String TAG_package = ObjectsUtil.of("package");
    public static final String TAG_permission = ObjectsUtil.of("permission");
    public static final String TAG_provider = ObjectsUtil.of("provider");
    public static final String TAG_receiver = ObjectsUtil.of("receiver");
    public static final String TAG_service = ObjectsUtil.of("service");
    public static final String TAG_uses_feature = ObjectsUtil.of("uses-feature");
    public static final String TAG_uses_library = ObjectsUtil.of("uses-library");
    public static final String TAG_uses_permission = ObjectsUtil.of("uses-permission");
    public static final String TAG_uses_sdk = ObjectsUtil.of("uses-sdk");


    public static final String VALUE_android_intent_action_MAIN = ObjectsUtil.of("android.intent.action.MAIN");

    public static final String FILE_NAME = ObjectsUtil.of("AndroidManifest.xml");
    public static final String FILE_NAME_BIN = ObjectsUtil.of("AndroidManifest.xml.bin");
    public static final String FILE_NAME_JSON = ObjectsUtil.of("AndroidManifest.xml.json");

    public static final String EMPTY_MANIFEST_TAG = ObjectsUtil.of("x");

}
