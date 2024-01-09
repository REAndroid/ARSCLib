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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.common.AccessFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliRegion;

public abstract class SmaliDef extends Smali implements SmaliRegion {

    private String name;
    private AccessFlag[] accessFlags;
    private SmaliAnnotationSet annotation;

    public SmaliDef(){
        super();
    }

    public abstract Key getKey();
    public AccessFlag[] getAccessFlags() {
        return accessFlags;
    }
    public void setAccessFlags(AccessFlag[] accessFlags) {
        this.accessFlags = accessFlags;
    }
    public int getAccessFlagsValue(){
        return Modifier.combineValues(getAccessFlags());
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public SmaliAnnotationSet getAnnotation() {
        return annotation;
    }
    public SmaliAnnotationSet getOrCreateAnnotation() {
        SmaliAnnotationSet directory = getAnnotation();
        if(directory == null){
            directory = new SmaliAnnotationSet();
            setAnnotation(directory);
        }
        return directory;
    }
    public void setAnnotation(SmaliAnnotationSet annotation) {
        this.annotation = annotation;
        if(annotation != null){
            annotation.setParent(this);
        }
    }
    public boolean hasAnnotation(){
        SmaliAnnotationSet annotation = getAnnotation();
        return annotation != null && !annotation.isEmpty();
    }
    @Override
    public SmaliDirective getSmaliDirective() {
        return null;
    }

    public boolean isStatic(){
        return Modifier.contains(getAccessFlags(), AccessFlag.STATIC);
    }
    public boolean isPrivate(){
        return Modifier.contains(getAccessFlags(), AccessFlag.PRIVATE);
    }

    public SmaliClass getSmaliClass(){
        return getParentInstance(SmaliClass.class);
    }
}
