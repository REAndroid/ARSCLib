package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public abstract class SmaliDefSet<T extends SmaliDef> extends SmaliSet<T>
        implements SmaliRegion {

    public SmaliDefSet(){
        super();
    }

    public abstract T createNew();

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAllWithDoubleNewLine(iterator());
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        while (parseNext(reader)){
            reader.skipWhitespaces();
        }
    }
    private boolean parseNext(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != getSmaliDirective()){
            return false;
        }
        T item = createNew();
        add(item);
        item.parse(reader);
        return true;
    }
}
