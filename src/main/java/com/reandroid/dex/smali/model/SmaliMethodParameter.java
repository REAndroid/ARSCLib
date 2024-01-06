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

import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliRegion;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliMethodParameter extends SmaliDebug implements SmaliRegion {

    private final SmaliRegisterSet registerSet;
    private StringKey name;
    private SmaliAnnotationSet annotationSet;

    public SmaliMethodParameter(){
        super();
        registerSet = new SmaliRegisterSet();
    }

    public SmaliRegisterSet getRegisterSet() {
        return registerSet;
    }

    public StringKey getName() {
        return name;
    }
    public void setName(StringKey name) {
        this.name = name;
    }

    public SmaliAnnotationSet getAnnotationSet() {
        return annotationSet;
    }
    public void setAnnotationSet(SmaliAnnotationSet annotationSet) {
        this.annotationSet = annotationSet;
        if(annotationSet != null){
            annotationSet.setParent(this);
        }
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.PARAM;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        SmaliDirective directive = getSmaliDirective();
        directive.append(writer);
        getRegisterSet().append(writer);
        StringKey name = getName();
        if(name != null){
            writer.append(", ");
            name.append(writer);
        }
        SmaliAnnotationSet annotationSet = getAnnotationSet();
        if(annotationSet != null){
            writer.indentPlus();
            writer.newLine();
            annotationSet.append(writer);
            writer.indentMinus();
            directive.appendEnd(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        SmaliDirective directive = SmaliDirective.parse(reader);
        if(directive != getSmaliDirective()){
            // throw
        }
        getRegisterSet().parse(reader);
        parseName(reader);
        parseAnnotationSet(reader);
    }
    private void parseName(SmaliReader reader) throws IOException {
        reader.skipSpaces();
        if(reader.get() == '"'){
            setName(StringKey.read(reader));
        }
    }
    private void parseAnnotationSet(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if(directive != SmaliDirective.ANNOTATION){
            return;
        }
        int position = reader.position();
        SmaliAnnotationSet annotationSet = new SmaliAnnotationSet();
        annotationSet.parse(reader);
        reader.skipWhitespacesOrComment();
        if(getSmaliDirective().isEnd(reader)){
            setAnnotationSet(annotationSet);
            SmaliDirective.parse(reader);
        }else {
            // put back, it is method annotation
            reader.position(position);
        }
    }
}
