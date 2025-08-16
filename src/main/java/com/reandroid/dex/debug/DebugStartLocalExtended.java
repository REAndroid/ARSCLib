package com.reandroid.dex.debug;

import com.reandroid.dex.id.IdItem;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliWriter;
import com.reandroid.dex.smali.model.Smali;
import com.reandroid.dex.smali.model.SmaliDebugLocal;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.SingleIterator;

import java.io.IOException;
import java.util.Iterator;

public class DebugStartLocalExtended extends DebugStartLocal {

    private final Base1Ule128IdItemReference<StringId> mSignature;

    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.mSignature = new Base1Ule128IdItemReference<>(SectionType.STRING_ID);

        addChild(4, mSignature);
    }

    @Override
    public boolean isValid(){
        return !isRemoved() && mSignature.getItem() != null;
    }
    public String getSignature(){
        StringId stringId = mSignature.getItem();
        if(stringId != null){
            return stringId.getString();
        }
        return null;
    }
    public StringKey getSignatureKey(){
        return (StringKey) mSignature.getKey();
    }
    public void setSignature(String signature){
        this.mSignature.setKey(StringKey.create(signature));
    }
    public void setSignature(StringKey key){
        this.mSignature.setKey(key);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        if(isValid()) {
            super.appendExtra(writer);
            writer.append(", ");
            mSignature.append(writer);
        }
    }
    @Override
    public DebugElementType<DebugStartLocalExtended> getElementType() {
        return DebugElementType.START_LOCAL_EXTENDED;
    }

    @Override
    public Iterator<IdItem> usedIds(){
        return CombiningIterator.two(super.usedIds(),
                SingleIterator.of(mSignature.getItem()));
    }
    @Override
    public void merge(DebugElement element){
        super.merge(element);
        DebugStartLocalExtended coming = (DebugStartLocalExtended) element;
        this.mSignature.setKey(coming.mSignature.getKey());
    }

    @Override
    public void fromSmali(Smali smali) {
        super.fromSmali(smali);
        setSignature(((SmaliDebugLocal)smali).getSignature());
    }

    @Override
    int compareDetailElement(DebugElement element) {
        int i = super.compareDetailElement(element);
        if (i != 0) {
            return i;
        }
        DebugStartLocalExtended debug = (DebugStartLocalExtended) element;
        i = CompareUtil.compare(getSignatureKey(), debug.getSignatureKey());
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugStartLocalExtended debug = (DebugStartLocalExtended) obj;
        return getFlag() == debug.getFlag() &&
                getRegisterNumber() == debug.getRegisterNumber() &&
                ObjectsUtil.equals(getName(), debug.getName()) &&
                ObjectsUtil.equals(getType(), debug.getType())&&
                ObjectsUtil.equals(getSignature(), debug.getSignature());
    }
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + getFlag();
        hash = hash * 31 + getRegisterNumber();
        hash = hash * 31 + ObjectsUtil.hash(getName(), getType(), getSignature());
        return hash;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + mSignature;
    }
}
