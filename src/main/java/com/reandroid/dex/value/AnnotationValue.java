package com.reandroid.dex.value;

import com.reandroid.dex.data.AnnotationItem;
import com.reandroid.dex.key.AnnotationItemKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueAnnotation;

public class AnnotationValue extends DexValueBlock<AnnotationItem> {

    public AnnotationValue() {
        super(new AnnotationItem(true), DexValueType.ANNOTATION);
    }
    public AnnotationItem get(){
        return getValueContainer();
    }
    public AnnotationItemKey getKey(){
        return get().getKey();
    }

    @Override
    public void setKey(Key key) {
        get().setKey(key);
    }

    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.ANNOTATION;
    }

    @Override
    public void merge(DexValueBlock<?> valueBlock){
        super.merge(valueBlock);
        AnnotationValue value = (AnnotationValue) valueBlock;
        AnnotationItem coming = value.get();
        AnnotationItem item = get();
        item.setType(coming.getType());
        item.merge(coming);
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueAnnotation smaliValueAnnotation = (SmaliValueAnnotation) smaliValue;
        AnnotationItem annotationItem = get();
        annotationItem.fromSmali(smaliValueAnnotation.getValue());
    }
}
