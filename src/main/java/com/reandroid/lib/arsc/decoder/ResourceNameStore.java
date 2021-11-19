package com.reandroid.lib.arsc.decoder;

import com.reandroid.lib.arsc.value.ResValueBag;

import java.util.HashSet;
import java.util.Set;

public class ResourceNameStore implements ResourceNameProvider {
    private final Set<ResourceNameProvider> resourceNameProviders;
    public ResourceNameStore(){
        this.resourceNameProviders=new HashSet<>();
    }
    public void addResourceNameProvider(ResourceNameProvider provider){
        if(provider!=null){
            resourceNameProviders.add(provider);
        }
    }
    public Set<ResourceNameProvider> getResourceNameProviders(){
        return resourceNameProviders;
    }
    @Override
    public String getResourceFullName(int resId, boolean includePackageName) {
        for(ResourceNameProvider provider:this.resourceNameProviders){
            String name=provider.getResourceFullName(resId, includePackageName);
            if(name!=null){
                return name;
            }
        }
        return null;
    }

    @Override
    public String getResourceName(int resId, boolean includePackageName) {
        for(ResourceNameProvider provider:this.resourceNameProviders){
            String name=provider.getResourceName(resId, includePackageName);
            if(name!=null){
                return name;
            }
        }
        return null;
    }
    @Override
    public ResValueBag getAttributeBag(int resId) {
        for(ResourceNameProvider provider:this.resourceNameProviders){
            ResValueBag resValueBag=provider.getAttributeBag(resId);
            if(resValueBag!=null){
                return resValueBag;
            }
        }
        return null;
    }
}
