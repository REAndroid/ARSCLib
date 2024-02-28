package com.reandroid.dex.model;

import com.reandroid.dex.data.AnnotationElement;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.value.DexValueType;

import java.io.IOException;

public class DexAnnotationElement extends Dex {

    private final DexAnnotation dexAnnotation;
    private final AnnotationElement annotationElement;

    public DexAnnotationElement(DexAnnotation dexAnnotation, AnnotationElement annotationElement){
        super();
        this.dexAnnotation = dexAnnotation;
        this.annotationElement = annotationElement;
    }

    public String getName(){
        return getAnnotationElement().getName();
    }
    public void setName(String name){
        getAnnotationElement().setName(name);
    }

    public DexValue getValue(){
        return DexValue.create(this,
                getAnnotationElement().getValue());
    }
    public DexValue getOrCreateValue(DexValueType<?> valueType){
        return DexValue.create(this,
                getAnnotationElement().getOrCreateValue(valueType));
    }

    public AnnotationElement getAnnotationElement() {
        return annotationElement;
    }
    public DexAnnotation getDexAnnotation() {
        return dexAnnotation;
    }
    @Override
    public DexClassRepository getClassRepository() {
        return getDexAnnotation().getClassRepository();
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getAnnotationElement().append(writer);
    }

    public static DexAnnotationElement create(DexAnnotation dexAnnotation, AnnotationElement annotationElement){
        if(dexAnnotation != null && annotationElement != null){
            return new DexAnnotationElement(dexAnnotation, annotationElement);
        }
        return null;
    }
}
