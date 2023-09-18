package com.reandroid.dex.debug;

import com.reandroid.dex.index.StringId;
import com.reandroid.dex.item.StringData;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class DebugStartLocalExtended extends DebugStartLocal {

    private final Base1Ule128Item<StringId> signatureData;

    public DebugStartLocalExtended(){
        super(1, DebugElementType.START_LOCAL_EXTENDED);
        this.signatureData = new Base1Ule128Item<>(SectionType.STRING_ID);

        addChild(4, signatureData);
    }
    public StringData getSignature(){
        StringId stringId = signatureData.getItem();
        if(stringId != null){
            return stringId.getStringData();
        }
        return null;
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
