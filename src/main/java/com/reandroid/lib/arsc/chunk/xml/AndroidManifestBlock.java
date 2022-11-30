package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.item.ResXmlString;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.arsc.value.ValueType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AndroidManifestBlock extends ResXmlBlock{
    public AndroidManifestBlock(){
        super();
    }
    public List<String> getUsesPermissions(){
        List<String> results=new ArrayList<>();
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return results;
        }
        List<ResXmlElement> permissionList = manifestElement.searchElementsByTagName(TAG_uses_permission);
        for(ResXmlElement permission:permissionList){
            ResXmlAttribute nameAttr = permission.searchAttributeById(ID_name);
            if(nameAttr==null){
                continue;
            }
            String val=nameAttr.getValueString();
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
        List<ResXmlElement> permissionList = manifestElement.searchElementsByTagName(TAG_uses_permission);
        for(ResXmlElement permission:permissionList){
            ResXmlAttribute nameAttr = permission.searchAttributeById(ID_name);
            if(nameAttr==null){
                continue;
            }
            String val=nameAttr.getValueString();
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
        ResXmlElement exist=getUsesPermission(permissionName);
        if(exist!=null){
            return exist;
        }
        ResXmlElement result=manifestElement.createChildElement(TAG_uses_permission);
        ResXmlAttribute attr = result.createAndroidAttribute(NAME_name, ID_name);
        attr.setValueAsString(permissionName);
        return result;
    }
    public String getPackageName(){
        return getManifestAttributeString(NAME_PACKAGE);
    }
    public boolean setPackageName(String packageName){
        return setManifestAttributeString(NAME_PACKAGE, packageName);
    }
    public Integer getCompileSdkVersion(){
        return getManifestAttributeInt(NAME_compileSdkVersion);
    }
    public boolean setCompileSdkVersion(int val){
        return setManifestAttributeInt(ID_compileSdkVersion, val);
    }
    public String getCompileSdkVersionCodename(){
        return getManifestAttributeString(NAME_compileSdkVersionCodename);
    }
    public boolean setCompileSdkVersionCodename(String val){
        return setManifestAttributeString(ID_compileSdkVersionCodename, val);
    }
    public Integer getVersionCode(){
        return getManifestAttributeInt(NAME_versionCode);
    }
    public boolean setVersionCode(int val){
        return setManifestAttributeInt(NAME_versionCode, val);
    }
    public String getVersionName(){
        return getManifestAttributeString(NAME_versionName);
    }
    public boolean setVersionName(String packageName){
        return setManifestAttributeString(NAME_versionName, packageName);
    }
    private String getManifestAttributeString(String name){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByName(name);
        if(attribute==null){
            return null;
        }
        int raw=attribute.getRawValue();
        ResXmlStringPool pool = getStringPool();
        ResXmlString resXmlString = pool.get(raw);
        if(resXmlString==null){
            return null;
        }
        return resXmlString.getHtml();
    }
    private boolean setManifestAttributeString(int resId, String value){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return false;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeById(resId);
        if(attribute==null){
            return false;
        }
        attribute.setValueType(ValueType.STRING);
        ResXmlString resXmlString=attribute.setValueString(value);
        return resXmlString!=null;
    }
    private boolean setManifestAttributeString(String name, String value){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return false;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByName(name);
        if(attribute==null){
            return false;
        }
        attribute.setValueType(ValueType.STRING);
        ResXmlString resXmlString=attribute.setValueString(value);
        return resXmlString!=null;
    }
    private boolean setManifestAttributeInt(int resId, int value){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return false;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeById(resId);
        if(attribute==null){
            return false;
        }
        attribute.setValueType(ValueType.INT_DEC);
        attribute.setValueString(String.valueOf(value));
        attribute.setRawValue(value);
        return true;
    }
    private boolean setManifestAttributeInt(String name, int value){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return false;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByName(name);
        if(attribute==null){
            return false;
        }
        attribute.setValueType(ValueType.INT_DEC);
        attribute.setValueString(String.valueOf(value));
        attribute.setRawValue(value);
        return true;
    }
    private Integer getManifestAttributeInt(String name){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        ResXmlAttribute attribute= manifestElement.searchAttributeByName(name);
        if(attribute==null){
            return null;
        }
        return attribute.getRawValue();
    }
    private ResXmlElement getApplicationElement(){
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return null;
        }
        return manifestElement.getElementByTagName(TAG_application);
    }
    private ResXmlElement getManifestElement(){
        ResXmlElement manifestElement=getResXmlElement();
        if(manifestElement==null){
            return null;
        }
        if(!TAG_manifest.equals(manifestElement.getTag())){
            return null;
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
    public static AndroidManifestBlock load(File file) throws IOException {
        return load(new FileInputStream(file));
    }
    public static AndroidManifestBlock load(InputStream inputStream) throws IOException {
        AndroidManifestBlock manifestBlock=new AndroidManifestBlock();
        manifestBlock.readBytes(inputStream);
        return manifestBlock;
    }
    public static final String TAG_manifest ="manifest";
    public static final String TAG_uses_permission="uses-permission";
    public static final String TAG_application ="application";

    public static final String NAME_compileSdkVersion ="compileSdkVersion";
    public static final String NAME_compileSdkVersionCodename ="compileSdkVersionCodename";
    public static final String NAME_installLocation="installLocation";
    public static final String NAME_PACKAGE ="package";
    public static final String NAME_platformBuildVersionCode="platformBuildVersionCode";
    public static final String NAME_platformBuildVersionName ="platformBuildVersionName";
    public static final String NAME_versionCode ="versionCode";
    public static final String NAME_versionName ="versionName";
    public static final String NAME_name ="name";

    public static final int ID_name = 0x01010003;
    public static final int ID_compileSdkVersion = 0x01010572;
    public static final int ID_compileSdkVersionCodename = 0x01010573;
    public static final int ID_authorities = 0x01010018;
    public static final int ID_host = 0x01010028;
    public static final int ID_configChanges = 0x0101001f;
    public static final int ID_screenOrientation = 0x0101001e;

    public static final String FILE_NAME="AndroidManifest.xml";
}
