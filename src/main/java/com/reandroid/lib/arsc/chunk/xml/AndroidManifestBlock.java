package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.item.ResXmlString;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.arsc.value.ValueType;

import java.util.ArrayList;
import java.util.List;

public class AndroidManifestBlock extends ResXmlBlock{
    public AndroidManifestBlock(){
        super();
    }
    public List<String> getPermissions(){
        List<String> results=new ArrayList<>();
        ResXmlElement manifestElement=getManifestElement();
        if(manifestElement==null){
            return results;
        }
        List<ResXmlElement> permissionList = manifestElement.searchElementsByTagName(TAG_uses_permission);
        for(ResXmlElement permission:permissionList){
            ResXmlAttribute nameAttr = permission.searchAttributeByName(ATTR_android_name);
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
    public String getPackageName(){
        return getManifestAttributeString(ATTR_PACKAGE);
    }
    public boolean setPackageName(String packageName){
        return setManifestAttributeString(ATTR_PACKAGE, packageName);
    }
    public Integer getCompileSdkVersion(){
        return getManifestAttributeInt(ATTR_compileSdkVersion);
    }
    public boolean setCompileSdkVersion(int val){
        return setManifestAttributeInt(ATTR_compileSdkVersion, val);
    }
    public String getCompileSdkVersionCodename(){
        return getManifestAttributeString(ATTR_compileSdkVersionCodename);
    }
    public boolean setCompileSdkVersionCodename(String val){
        return setManifestAttributeString(ATTR_compileSdkVersionCodename, val);
    }
    public Integer getVersionCode(){
        return getManifestAttributeInt(ATTR_versionCode);
    }
    public boolean setVersionCode(int val){
        return setManifestAttributeInt(ATTR_versionCode, val);
    }
    public String getVersionName(){
        return getManifestAttributeString(ATTR_versionName);
    }
    public boolean setVersionName(String packageName){
        return setManifestAttributeString(ATTR_versionName, packageName);
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
    private ResXmlElement getManifestElement(){
        ResXmlElement manifestElement=getResXmlElement();
        if(manifestElement==null){
            return null;
        }
        if(!TAG_MANIFEST.equals(manifestElement.getTag())){
            return null;
        }
        return manifestElement;
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{");
        builder.append(ATTR_PACKAGE).append("=").append(getPackageName());
        builder.append(", ").append(ATTR_versionCode).append("=").append(getVersionCode());
        builder.append(", ").append(ATTR_versionName).append("=").append(getVersionName());
        builder.append(", ").append(ATTR_compileSdkVersion).append("=").append(getCompileSdkVersion());
        builder.append(", ").append(ATTR_compileSdkVersionCodename).append("=").append(getCompileSdkVersionCodename());

        List<String> allPermissions=getPermissions();
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
    private static final String TAG_MANIFEST="manifest";
    private static final String TAG_uses_permission="uses-permission";

    private static final String ATTR_compileSdkVersion="compileSdkVersion";
    private static final String ATTR_compileSdkVersionCodename="compileSdkVersionCodename";
    private static final String ATTR_installLocation="installLocation";
    private static final String ATTR_PACKAGE="package";
    private static final String ATTR_platformBuildVersionCode="platformBuildVersionCode";
    private static final String ATTR_platformBuildVersionName="platformBuildVersionName";
    private static final String ATTR_versionCode="versionCode";
    private static final String ATTR_versionName="versionName";

    private static final String ATTR_android_name="name";
}
