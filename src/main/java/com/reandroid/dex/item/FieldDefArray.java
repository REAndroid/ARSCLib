package com.reandroid.dex.item;

import com.reandroid.arsc.item.IntegerReference;
import com.reandroid.dex.value.DexValueBlock;

import java.util.Comparator;

public class FieldDefArray extends DefArray<FieldDef>{
    public FieldDefArray(IntegerReference itemCount){
        super(itemCount);
    }

    @Override
    public boolean sort(Comparator<? super FieldDef> comparator) {
        if(getCount() < 2){
            return false;
        }
        EncodedArray encodedArray = holdStaticValues();
        boolean changed = super.sort(comparator);
        if(changed){
            sortStaticValues(encodedArray);
        }else if(encodedArray != null){
            unHoldStaticValues();
        }
        return changed;
    }
    private EncodedArray holdStaticValues(){
        int count = getCount();
        if(count < 2){
            return null;
        }
        EncodedArray encodedArray = getStaticValues();
        if(encodedArray == null){
            return null;
        }
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = encodedArray.get(i);
            def.setTmpValue(valueBlock);
        }
        return encodedArray;
    }
    private void unHoldStaticValues(){
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            def.setTmpValue(null);
        }
    }
    private void sortStaticValues(EncodedArray encodedArray){
        if(encodedArray == null){
            return;
        }
        int count = getCount();
        for(int i = 0; i < count; i++){
            FieldDef def = get(i);
            DexValueBlock<?> valueBlock = def.getTmpValue();
            if(valueBlock != null && valueBlock.getIndex() != i){
                encodedArray.set(i, valueBlock);
            }
            def.setTmpValue(null);
        }
        encodedArray.trimNull();
    }
    private EncodedArray getStaticValues(){
        FieldDef fieldDef = get(0);
        if(fieldDef != null){
            return fieldDef.getStaticValues();
        }
        return null;
    }

    @Override
    public FieldDef[] newInstance(int length) {
        return new FieldDef[length];
    }
    @Override
    public FieldDef newInstance() {
        return new FieldDef();
    }
}
