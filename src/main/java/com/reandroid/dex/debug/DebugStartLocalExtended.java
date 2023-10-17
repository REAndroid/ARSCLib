package com.reandroid.dex.debug;

import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.reference.Base1Ule128IdItemReference;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugStartLocalExtended extends DebugStartLocal {

    private final Base1Ule128IdItemReference<StringId> mSignature;

    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.mSignature = new Base1Ule128IdItemReference<>(SectionType.STRING_ID);

        addChild(4, mSignature);
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
        this.mSignature.setItem(StringKey.create(signature));
    }
    public void setSignature(StringKey key){
        this.mSignature.setItem(key);
    }

    @Override
    public void appendExtra(SmaliWriter writer) throws IOException {
        super.appendExtra(writer);
        writer.append(", ");
        mSignature.append(writer);
    }
    @Override
    public DebugElementType<DebugStartLocalExtended> getElementType() {
        return DebugElementType.START_LOCAL_EXTENDED;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + mSignature;
    }
}
