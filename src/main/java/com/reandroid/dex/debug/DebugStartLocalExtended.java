package com.reandroid.dex.debug;

import com.reandroid.dex.index.StringData;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugStartLocalExtended extends DebugStartLocal {

    private final Base1Ule128Item<StringData> signatureData;

    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.signatureData = new Base1Ule128Item<>(SectionType.STRING_DATA);

        addChild(4, signatureData);
    }
    public StringData getSignature(){
        return signatureData.getItem();
    }

    public void appendExtra(SmaliWriter writer) throws IOException {
        super.appendExtra(writer);
        writer.append(", ");
        getSignature().append(writer);
    }
    @Override
    public String toString() {
        return super.toString() + ", \"" + getSignature() + "\"";
    }
}
