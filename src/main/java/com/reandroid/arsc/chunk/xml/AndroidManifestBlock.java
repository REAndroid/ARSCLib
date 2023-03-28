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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.value.ValueType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AndroidManifestBlock extends ResXmlDocument {
    private int mGuessedPackageId;
    public AndroidManifestBlock(){
        super();
        super.getStringPool().setUtf8(false);
    }
    public ApkFile.ApkType guessApkType(){
        if(isSplit()){
            return ApkFile.ApkType.SPLIT;
        }
        Boolean core = isCoreApp();
        if(core!=null && core){
            return ApkFile.ApkType.CORE;
        }
        if(getMainActivity()!=null){
            return ApkFile.ApkType.BASE;
        }
        return null;
    }
    public Boolean isCoreApp(){
        ResXmlElement manifest = getManifestElement();
        if(manifest == null){
            return null;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByName(NAME_coreApp);
        if(attribute == null){
            return null;
        }
        if(attribute.getValueType() != ValueType.INT_BOOLEAN){
            return null;
        }
        return attribute.getValueAsBoolean();
    }
    public boolean isSplit(){
        ResXmlElement manifest = getManifestElement();
        if(manifest == null){
            return false;
        }
        return manifest.searchAttributeByName(NAME_split)!=null;
    }
    public String getSplit(){
        ResXmlElement manifest = getManifestElement();
        if(manifest == null){
            return null;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByName(NAME_split);
        if(attribute!=null){
            return attribute.getValueAsString();
        }
        return null;
    }
    public void setSplit(String split, boolean forceCreate){
        ResXmlElement manifest = getManifestElement();
        if(manifest == null){
            return;
        }
        ResXmlAttribute attribute;
        if(forceCreate){
            attribute = manifest.getOrCreateAttribute(NAME_split, 0);
        }else {
            attribute = manifest.searchAttributeByName(NAME_split);
            if(attribute==null){
                return;
            }
        }
        attribute.setValueAsString(split);
    }
    // TODO: find a better way
    public int guessCurrentPackageId(){
        if(mGuessedPackageId == 0){
            mGuessedPackageId = ((getIconResourceId()>>24) & 0xff);
        }
        return mGuessedPackageId;
    }
    public int getIconResourceId(){
        ResXmlElement applicationElement = getApplicationElement();
        if(applicationElement==null){
            return 0;
        }
        ResXmlAttribute iconAttribute=applicationElement.searchAttributeByResourceId(ID_icon);
        if(iconAttribute==null || iconAttribute.getValueType() != ValueType.REFERENCE){
            return 0;
        }
        return iconAttribute.getData();
    }
    public void setIconResourceId(int resourceId){
        ResXmlElement applicationElement = getApplicationElement();
        if(applicationElement==null){
            return;
        }
        ResXmlAttribute iconAttribute =
                applicationElement.getOrCreateAndroidAttribute(NAME_icon, ID_icon);
        iconAttribute.setValueType(ValueType.REFERENCE);
        iconAttribute.setData(resourceId);
    }
    public boolean isDebuggable(){
        ResXmlElement application=getApplicationElement();
        if(application==null){
            return false;
        }
        ResXmlAttribute attribute = application
                .searchAttributeByResourceId(ID_debuggable);
        if(attribute==null){
            return false;
        }
        return attribute.getValueAsBoolean();
    }
    public void setDebuggable(boolean debuggable){
        ResXmlElement application=getApplicationElement();
        if(application==null){
            return;
        }
        ResXmlAttribute attribute = application
                .searchAttributeByResourceId(ID_debuggable);
        if(debuggable){
            if(attribute==null){
                attribute=application.createAndroidAttribute(NAME_debuggable, ID_debuggable);
            }
            attribute.setValueAsBoolean(true);
        }else if(attribute!=null) {
            application.removeAttribute(attribute);
        }
    }
    public ResXmlElement getMainActivity(){
        for(ResXmlElement activity:listActivities()){
            for(ResXmlElement intentFilter:activity.listElements(TAG_intent_filter)){
                for(ResXmlElement action:intentFilter.listElements(TAG_action)){
                    ResXmlAttribute attribute = action.searchAttributeByResourceId(ID_name);
                    if(attribute==null){
                        continue;
                    }
                    if(VALUE_android_intent_action_MAIN.equals(attribute.getValueAsString())){
                        return activity;
                    }
                }
            }
        }
        return null;
    }
    public List<ResXmlElement> listActivities(){
        return listActivities(true);
    }
    public List<ResXmlElement> listActivities(boolean includeActivityAlias){
        ResXmlElement application=getApplicationElement();
        if(application==null){
            return new ArrayList<>();
        }
        List<ResXmlElement> results = application.listElements(TAG_activity);
        if(includeActivityAlias){
            results.addAll(application.listElements(TAG_activity_alias));
        }
        return results;
    }
    public List<ResXmlElement> listApplicationElementsByTag(String tag){
        ResXmlElement application=getApplicationElement();
        if(application==null){
            return new ArrayList<>();
        }
        return application.listElements(tag);
    }
    public List<String> getUsesPermissions(){
        List<String> results=new ArrayList<>();
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return results;
        }
        List<ResXmlElement> permissionList = manifestElement.listElements(TAG_uses_permission);
        for(ResXmlElement permission:permissionList){
            ResXmlAttribute nameAttr = permission.searchAttributeByResourceId(ID_name);
            if(nameAttr==null||nameAttr.getValueType()!=ValueType.STRING){
                continue;
            }
            String val=nameAttr.getValueAsString();
            if(val!=null){
                results.add(val);
            }
        }
        return results;
    }
    public ResXmlElement getUsesPermission(String permissionName){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        List<ResXmlElement> permissionList = manifestElement.listElements(TAG_uses_permission);
        for(ResXmlElement permission:permissionList){
            ResXmlAttribute nameAttr = permission.searchAttributeByResourceId(ID_name);
            if(nameAttr==null || nameAttr.getValueType()!=ValueType.STRING){
                continue;
            }
            String val=nameAttr.getValueAsString();
            if(val==null){
                continue;
            }
            if(val.equals(permissionName)){
                return permission;
            }
        }
        return null;
    }
    public ResXmlElement addUsesPermission(String permissionName){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        ResXmlElement exist = getUsesPermission(permissionName);
        if(exist!=null){
            return exist;
        }
        ResXmlElement result = manifestElement.createChildElement(TAG_uses_permission);
        ResXmlAttribute attr = result.getOrCreateAndroidAttribute(NAME_name, ID_name);
        attr.setValueAsString(permissionName);
        int i = manifestElement.lastIndexOf(TAG_uses_permission);
        i++;
        manifestElement.changeIndex(result, i);
        return result;
    }
    public String getPackageName(){
        ResXmlElement manifest=getManifestElement();
        if(manifest==null){
            return null;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByName(NAME_PACKAGE);
        if(attribute==null || attribute.getValueType()!=ValueType.STRING){
            return null;
        }
        return attribute.getValueAsString();
    }
    public boolean setPackageName(String packageName){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return false;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByName(NAME_PACKAGE);
        if(attribute==null){
            return false;
        }
        attribute.setValueAsString(packageName);
        return true;
    }
    public Integer getPlatformBuildVersionCode(){
        ResXmlElement manifest = getManifestElement();
        if(manifest==null){
            return null;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByName(NAME_platformBuildVersionCode);
        if(attribute==null || attribute.getValueType()!=ValueType.INT_DEC){
            return null;
        }
        return attribute.getData();
    }
    public Integer getTargetSdkVersion(){
        ResXmlElement manifest = getManifestElement();
        if(manifest==null){
            return null;
        }
        ResXmlElement usesSdk = manifest.getElementByTagName(TAG_uses_sdk);
        if(usesSdk==null){
            return null;
        }
        ResXmlAttribute attribute = usesSdk.searchAttributeByResourceId(ID_targetSdkVersion);
        if(attribute==null || attribute.getValueType()!=ValueType.INT_DEC){
            return null;
        }
        return attribute.getData();
    }
    public Integer getCompileSdkVersion(){
        return getManifestAttributeInt(ID_compileSdkVersion);
    }
    public void setCompileSdkVersion(int val){
        setManifestAttributeInt(NAME_compileSdkVersion, ID_compileSdkVersion, val);
    }
    public String getCompileSdkVersionCodename(){
        return getManifestAttributeString(ID_compileSdkVersionCodename);
    }
    public boolean setCompileSdkVersionCodename(String val){
        ResXmlElement manifest=getManifestElement();
        if(manifest==null){
            return false;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByResourceId(ID_compileSdkVersionCodename);
        if(attribute==null){
            return false;
        }
        attribute.setValueAsString(val);
        return true;
    }
    public Integer getVersionCode(){
        return getManifestAttributeInt(ID_versionCode);
    }
    public void setVersionCode(int val){
        setManifestAttributeInt(NAME_versionCode, ID_versionCode, val);
    }
    public String getVersionName(){
        return getManifestAttributeString(ID_versionName);
    }
    public boolean setVersionName(String packageName){
        return setManifestAttributeString(NAME_versionName,  ID_versionName, packageName);
    }
    private String getManifestAttributeString(int resourceId){
        ResXmlElement manifest=getManifestElement();
        if(manifest==null){
            return null;
        }
        ResXmlAttribute attribute = manifest.searchAttributeByResourceId(resourceId);
        if(attribute==null || attribute.getValueType()!=ValueType.STRING){
            return null;
        }
        return attribute.getValueAsString();
    }
    private boolean setManifestAttributeString(String attributeName, int resourceId, String value){
        ResXmlElement manifestElement=getOrCreateManifestElement();
        ResXmlAttribute attribute = manifestElement
                .getOrCreateAndroidAttribute(attributeName, resourceId);
        attribute.setValueAsString(value);
        return true;
    }
    private void setManifestAttributeInt(String attributeName, int resourceId, int value){
        ResXmlElement manifestElement=getOrCreateManifestElement();
        ResXmlAttribute attribute = manifestElement
                .getOrCreateAndroidAttribute(attributeName, resourceId);
        attribute.setTypeAndData(ValueType.INT_DEC, value);
    }
    private Integer getManifestAttributeInt(int resourceId){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByResourceId(resourceId);
        if(attribute==null || attribute.getValueType()!=ValueType.INT_DEC){
            return null;
        }
        return attribute.getData();
    }
    public ResXmlElement getApplicationElement(){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        return manifestElement.getElementByTagName(TAG_application);
    }
    public ResXmlElement getManifestElement(){
        ResXmlElement manifestElement=getResXmlElement();
        if(manifestElement==null){
            return null;
        }
        if(!TAG_manifest.equals(manifestElement.getTag())){
            return null;
        }
        return manifestElement;
    }
    private ResXmlElement getOrCreateManifestElement(){
        ResXmlElement manifestElement=getResXmlElement();
        if(manifestElement==null){
            manifestElement=createRootElement(TAG_manifest);
        }
        if(!TAG_manifest.equals(manifestElement.getTag())){
            manifestElement.setTag(TAG_manifest);
        }
        return manifestElement;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{");
        builder.append(NAME_PACKAGE).append("=").append(getPackageName());
        builder.append(", ").append(NAME_versionCode).append("=").append(getVersionCode());
        builder.append(", ").append(NAME_versionName).append("=").append(getVersionName());
        builder.append(", ").append(NAME_compileSdkVersion).append("=").append(getCompileSdkVersion());
        builder.append(", ").append(NAME_compileSdkVersionCodename).append("=").append(getCompileSdkVersionCodename());

        List<String> allPermissions= getUsesPermissions();
        builder.append(", PERMISSIONS[");
        boolean appendOnce=false;
        for(String permissions:allPermissions){
            if(appendOnce){
                builder.append(", ");
            }
            builder.append(permissions);
            appendOnce=true;
        }
        builder.append("]");
        builder.append("}");
        return builder.toString();
    }
    public static boolean isAndroidManifestBlock(ResXmlDocument xmlBlock){
        if(xmlBlock==null){
            return false;
        }
        ResXmlElement root = xmlBlock.getResXmlElement();
        if(root==null){
            return false;
        }
        return TAG_manifest.equals(root.getTag());
    }
    public static AndroidManifestBlock load(File file) throws IOException {
        return load(new FileInputStream(file));
    }
    public static AndroidManifestBlock load(InputStream inputStream) throws IOException {
        AndroidManifestBlock manifestBlock=new AndroidManifestBlock();
        manifestBlock.readBytes(inputStream);
        return manifestBlock;
    }
    public static final String TAG_action = "action";
    public static final String TAG_activity = "activity";
    public static final String TAG_activity_alias = "activity-alias";
    public static final String TAG_application = "application";
    public static final String TAG_category = "category";
    public static final String TAG_data = "data";
    public static final String TAG_intent_filter = "intent-filter";
    public static final String TAG_manifest = "manifest";
    public static final String TAG_meta_data = "meta-data";
    public static final String TAG_package = "package";
    public static final String TAG_permission = "permission";
    public static final String TAG_provider = "provider";
    public static final String TAG_receiver = "receiver";
    public static final String TAG_service = "service";
    public static final String TAG_uses_feature = "uses-feature";
    public static final String TAG_uses_library = "uses-library";
    public static final String TAG_uses_permission = "uses-permission";
    public static final String TAG_uses_sdk = "uses-sdk";

    public static final String NAME_compileSdkVersion = "compileSdkVersion";
    public static final String NAME_compileSdkVersionCodename = "compileSdkVersionCodename";
    public static final String NAME_installLocation="installLocation";
    public static final String NAME_PACKAGE = "package";
    public static final String NAME_split = "split";
    public static final String NAME_coreApp = "coreApp";
    public static final String NAME_platformBuildVersionCode = "platformBuildVersionCode";
    public static final String NAME_platformBuildVersionName = "platformBuildVersionName";
    public static final String NAME_versionCode = "versionCode";
    public static final String NAME_versionName = "versionName";
    public static final String NAME_name = "name";
    public static final String NAME_extractNativeLibs = "extractNativeLibs";
    public static final String NAME_isSplitRequired = "isSplitRequired";
    public static final String NAME_value = "value";
    public static final String NAME_resource = "resource";
    public static final String NAME_debuggable = "debuggable";
    public static final String NAME_icon = "icon";
    public static final String NAME_label = "label";
    public static final String NAME_theme = "theme";
    public static final String NAME_id = "id";

    public static final int ID_name = 0x01010003;
    public static final int ID_compileSdkVersion = 0x01010572;
    public static final int ID_targetSdkVersion = 0x01010270;
    public static final int ID_compileSdkVersionCodename = 0x01010573;
    public static final int ID_authorities = 0x01010018;
    public static final int ID_host = 0x01010028;
    public static final int ID_configChanges = 0x0101001f;
    public static final int ID_screenOrientation = 0x0101001e;
    public static final int ID_extractNativeLibs = 0x010104ea;
    public static final int ID_isSplitRequired = 0x01010591;
    public static final int ID_value = 0x01010024;
    public static final int ID_resource = 0x01010025;
    public static final int ID_versionCode = 0x0101021b;
    public static final int ID_versionName = 0x0101021c;
    public static final int ID_debuggable = 0x0101000f;
    public static final int ID_icon = 0x01010002;
    public static final int ID_label = 0x01010001;
    public static final int ID_theme = 0x01010000;
    public static final int ID_id = 0x010100d0;

    public static final String VALUE_android_intent_action_MAIN = "android.intent.action.MAIN";

    public static final String FILE_NAME="AndroidManifest.xml";
}
