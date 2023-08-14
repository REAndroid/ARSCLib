package com.reandroid.dex.debug;

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.index.StringData;
import com.reandroid.dex.sections.SectionType;

public class DebugStartLocalExtended extends DebugStartLocal {
    private final Ule128Item signatureIndex;
    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.signatureIndex = new Ule128Item();

        addChild(4, signatureIndex);
    }
    public StringData getSignature(){
        return get(SectionType.STRING_DATA, signatureIndex.get() - 1);
    }

    @Override
    public String toString() {
        return super.toString() + ", \"" + getSignature() + "\"";
    }
}
