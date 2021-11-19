package com.reandroid.lib.arsc.decoder;

import com.reandroid.lib.arsc.value.ResValueBag;


public interface ResourceNameProvider {
    String getResourceFullName(int resId, boolean includePackageName);
    String getResourceName(int resId, boolean includePackageName);
    ResValueBag getAttributeBag(int resId);
}
