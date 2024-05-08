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

import com.reandroid.dex.common.AnnotationVisibility;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliAnnotationItem extends SmaliSet<SmaliAnnotationElement> implements SmaliRegion {

    private SmaliDirective smaliDirective;
    private AnnotationVisibility visibility;
    private TypeKey type;

    public SmaliAnnotationItem(){
        super();
        this.smaliDirective = SmaliDirective.ANNOTATION;
    }

    public AnnotationVisibility getVisibility() {
        return visibility;
    }
    public void setVisibility(AnnotationVisibility visibility) {
        this.visibility = visibility;
    }

    public TypeKey getType() {
        return type;
    }
    public void setType(TypeKey type) {
        this.type = type;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return smaliDirective;
    }

    public void setSmaliDirective(SmaliDirective annotationDirective) {
        if(annotationDirective == null) {
            throw new NullPointerException("Null annotation directive");
        }
        if(annotationDirective != SmaliDirective.ANNOTATION &&
                annotationDirective != SmaliDirective.SUB_ANNOTATION) {
            throw new IllegalArgumentException("Invalid annotation directive: "
                    + annotationDirective);
        }
        this.smaliDirective = annotationDirective;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        writer.appendOptional(getVisibility());
        writer.appendOptional(getType());
        writer.appendAllWithIndent(iterator());
        getSmaliDirective().appendEnd(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespacesOrComment();
        int position = reader.position();
        SmaliDirective directive = SmaliDirective.parse(reader);
        if(directive != SmaliDirective.ANNOTATION &&
                directive != SmaliDirective.SUB_ANNOTATION){
            reader.position(position);
            throw new SmaliParseException("Expecting " + SmaliDirective.ANNOTATION
                    + " || " + SmaliDirective.SUB_ANNOTATION, reader);
        }
        setSmaliDirective(directive);
        setVisibility(AnnotationVisibility.parse(reader));
        setType(TypeKey.read(reader));
        while (parseNext(reader) != null){
            reader.skipWhitespacesOrComment();
        }
        SmaliParseException.expect(reader, getSmaliDirective(), true);
    }
    @Override
    SmaliAnnotationElement createNext(SmaliReader reader) {
        reader.skipWhitespacesOrComment();
        if(reader.finished()) {
            return null;
        }
        if(getSmaliDirective().isEnd(reader)){
            return null;
        }
        return new SmaliAnnotationElement();
    }

    public static SmaliAnnotationItem read(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        if(reader.finished()) {
            return null;
        }
        SmaliAnnotationItem smali = new SmaliAnnotationItem();
        smali.parse(reader);
        if(!smali.isEmpty()) {
            return smali;
        }
        return null;
    }
}
