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

import com.reandroid.dex.program.InstructionLabelType;
import com.reandroid.dex.smali.*;

import java.io.IOException;

public abstract class SmaliCodeExceptionHandler extends SmaliCode implements SmaliRegion {

    private final SmaliLabel start;
    private final SmaliLabel end;
    private final SmaliLabel catchLabel;

    public SmaliCodeExceptionHandler() {
        super();
        this.start = new SmaliHandlerBoundLabel(InstructionLabelType.TRY_START);
        this.end = new SmaliHandlerBoundLabel(InstructionLabelType.TRY_END);
        this.catchLabel = new SmaliHandlerCatchLabel(this);

        this.start.setParent(this);
        this.end.setParent(this);
        this.catchLabel.setParent(this);
    }

    public int getTargetAddress() {
        SmaliCodeTryItem tryItem = getTryItem();
        if (tryItem == null) {
            return -1;
        }
        return tryItem.getAddress();
    }
    public SmaliLabel getStart() {
        return start;
    }
    public SmaliLabel getEnd() {
        return end;
    }
    public SmaliLabel getCatchLabel() {
        return catchLabel;
    }

    public abstract InstructionLabelType getLabelType();

    public void setLabelNames(String start, String end, String catchLabel) {
        getStart().setLabelName(start);
        getEnd().setLabelName(end);
        getCatchLabel().setLabelName(catchLabel);
    }
    public void setLabelNames(SmaliLabel start, SmaliLabel end, SmaliLabel catchLabel) {
        setLabelNames(start.getLabelName(), end.getLabelName(), catchLabel.getLabelName());
    }

    SmaliCodeTryItem getTryItem() {
        return getParentInstance(SmaliCodeTryItem.class);
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        appendType(writer);
        writer.append('{');
        getStart().append(writer);
        writer.append(" .. ");
        getEnd().append(writer);
        writer.append('}');
        writer.append(' ');
        getCatchLabel().append(writer);
    }
    protected void appendType(SmaliWriter writer) throws IOException {

    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, getSmaliDirective());
        parseType(reader);
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '{');
        reader.skipWhitespaces();
        getStart().parse(reader);
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '.');
        SmaliParseException.expect(reader, '.');
        getEnd().parse(reader);
        reader.skipWhitespaces();
        SmaliParseException.expect(reader, '}');
        getCatchLabel().parse(reader);
    }
    protected void parseType(SmaliReader reader) throws IOException {
    }

    static class SmaliHandlerBoundLabel extends SmaliLabel {

        private final InstructionLabelType labelType;

        public SmaliHandlerBoundLabel(InstructionLabelType labelType) {
            super();
            this.labelType = labelType;
        }

        @Override
        public InstructionLabelType getLabelType() {
            return labelType;
        }
        @Override
        public SmaliLabel getDestinationLabel() {
            SmaliLabel label = super.getDestinationLabel();
            if (label != null) {
                label.setLabelType(labelType);
            }
            return label;
        }

        @Override
        public boolean isSourceLabel() {
            return true;
        }
        @Override
        public boolean isDestinationLabel() {
            return false;
        }
    }

    static class SmaliHandlerCatchLabel extends SmaliLabel {

        private final SmaliCodeExceptionHandler handler;

        public SmaliHandlerCatchLabel(SmaliCodeExceptionHandler handler) {
            super();
            this.handler = handler;
        }

        @Override
        public InstructionLabelType getLabelType() {
            if (handler instanceof SmaliCodeCatch) {
                return InstructionLabelType.CATCH;
            }
            return InstructionLabelType.CATCH_ALL;
        }
        @Override
        public SmaliLabel getDestinationLabel() {
            SmaliLabel label = super.getDestinationLabel();
            if (label != null) {
                label.setLabelType(getLabelType());
            }
            return label;
        }

        @Override
        public boolean isSourceLabel() {
            return true;
        }
        @Override
        public boolean isDestinationLabel() {
            return false;
        }
    }
}
