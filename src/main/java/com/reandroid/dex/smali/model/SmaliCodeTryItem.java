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
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.util.Iterator;

public class SmaliCodeTryItem extends SmaliCode{

    private final SmaliSet<SmaliCodeCatch> catchSet;
    private SmaliCodeCatchAll catchAll;

    public SmaliCodeTryItem() {
        super();
        this.catchSet = new SmaliSet<>();
        this.catchSet.setParent(this);
    }


    public int getStartAddress() {
        SmaliLabel label = pickBoundLabel(true);
        if (label != null) {
            return label.getTargetAddress();
        }
        return -1;
    }
    public int getAddress() {
        SmaliCodeSet codeSet = getCodeSet();
        if (codeSet != null) {
            SmaliInstruction instruction = codeSet.getNextInstruction(getIndex());
            if (instruction != null) {
                return instruction.getAddress();
            }
        }
        return -1;
    }

    public SmaliCodeCatchAll getCatchAll() {
        return catchAll;
    }
    public void setCatchAll(SmaliCodeCatchAll catchAll) {
        this.catchAll = catchAll;
        if (catchAll != null) {
            catchAll.setParent(this);
        }
    }
    public SmaliSet<SmaliCodeCatch> getCatchSet() {
        return catchSet;
    }

    public SmaliLabel getStartLabel() {
        SmaliLabel label = pickBoundLabel(true);
        if (label != null) {
            return label.getDestinationLabel();
        }
        return null;
    }
    public SmaliLabel getEndLabel() {
        SmaliLabel label = pickBoundLabel(false);
        if (label != null) {
            return label.getDestinationLabel();
        }
        return null;
    }
    private SmaliLabel pickBoundLabel(boolean start) {
        SmaliCodeCatch codeCatch = getCatchSet().getFirst();
        if (codeCatch != null) {
            if (start) {
                return codeCatch.getStart();
            }
            return codeCatch.getEnd();
        }
        SmaliCodeCatchAll catchAll = getCatchAll();
        if (catchAll != null) {
            if (start) {
                return catchAll.getStart();
            }
            return catchAll.getEnd();
        }
        return null;
    }

    /** Separates multiple exception handlers
     *  and keeps only one handler per try item
     */
    public boolean flatten() {
        SmaliSet<SmaliCodeCatch> catchSet = getCatchSet();
        SmaliCodeCatchAll catchAll = getCatchAll();
        int count = catchSet.size();
        if (catchAll != null) {
            count = count + 1;
        }
        if (count < 2) {
            return false;
        }
        SmaliCodeTryItem previous = this;
        while (catchSet.size() > 1) {
            int i = catchSet.size() - 1;
            SmaliCodeCatch handler = catchSet.get(i);
            previous = flatten(previous, handler);
            catchSet.remove(i);
        }
        if (catchAll != null) {
            flatten(previous, catchAll);
            setCatchAll(null);
        }
        return true;
    }
    private SmaliCodeTryItem flatten(SmaliCodeTryItem previous, SmaliCodeExceptionHandler handler) {
        SmaliCodeSet codeSet = getCodeSet();

        SmaliCodeTryItem tryItem = new SmaliCodeTryItem();
        SmaliLabel start = previous.getStartLabel();
        SmaliLabel end = previous.getEndLabel();
        SmaliLabel catchLabel = handler.getCatchLabel()
                .getDestinationLabel();

        start = codeSet.newLabelAtIndex(start.getIndex() + 1, start.getLabelType());
        end = codeSet.newLabelAtIndex(end.getIndex() + 1, end.getLabelType());
        catchLabel = codeSet.newLabelAtIndex(catchLabel.getIndex() + 1,
                catchLabel.getLabelType());

        SmaliCodeExceptionHandler resultHandler;
        if (handler instanceof SmaliCodeCatchAll) {
            SmaliCodeCatchAll catchAll = new SmaliCodeCatchAll();
            tryItem.setCatchAll(catchAll);
            resultHandler = catchAll;
        } else {
            SmaliCodeCatch codeCatch = new SmaliCodeCatch();
            codeCatch.setType(((SmaliCodeCatch) handler).getType());
            tryItem.getCatchSet().add(codeCatch);
            resultHandler = codeCatch;
        }
        resultHandler.setLabelNames(start, end, catchLabel);

        codeSet.add(previous.getIndex() + 1, tryItem);
        return tryItem;
    }

    public SmaliCodeTryItem splitAtIndex(Smali indexEnd) {
        SmaliCodeSet codeSet = getCodeSet();

        SmaliLabel startLabel = getStartLabel();

        SmaliCodeTryItem splitItem = new SmaliCodeTryItem();

        SmaliLabel startLabelSplit = codeSet.newLabelAtIndex(
                startLabel.getIndex(), InstructionLabelType.TRY_START);

        SmaliLabel endLabelSplit = codeSet.newLabelAtIndex(
                indexEnd.getIndex(), InstructionLabelType.TRY_END);

        Iterator<SmaliCodeCatch> iterator = getCatchSet().iterator();
        while (iterator.hasNext()) {
            SmaliCodeCatch codeCatch = iterator.next();

            SmaliCodeCatch codeCatchSplit = new SmaliCodeCatch();
            codeCatchSplit.setLabelNames(startLabelSplit, endLabelSplit,
                    codeCatch.getCatchLabel());
            codeCatchSplit.setType(codeCatch.getType());

            splitItem.getCatchSet().add(codeCatchSplit);
        }

        SmaliCodeCatchAll catchAll = getCatchAll();
        if (catchAll != null) {
            SmaliCodeCatchAll catchAllSplit = new SmaliCodeCatchAll();
            catchAllSplit.setLabelNames(startLabelSplit,
                    endLabelSplit, catchAll.getCatchLabel());
            splitItem.setCatchAll(catchAllSplit);
        }
        codeSet.add(lastIndexOfHandler(codeSet, endLabelSplit.getIndex() + 1), splitItem);
        codeSet.moveTo(startLabel, splitItem.getIndex());

        return splitItem;
    }
    private int lastIndexOfHandler(SmaliCodeSet codeSet, int fromIndex) {
        int index = fromIndex;
        int size = codeSet.size();
        for (int i = fromIndex; i < size; i++) {
            SmaliCode code = codeSet.get(i);
            if (code instanceof SmaliInstruction) {
                break;
            }
            if (code instanceof SmaliCodeTryItem) {
                index ++;
            }
        }
        return index;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendAll(getCatchSet().iterator());
        SmaliCodeCatchAll catchAll = getCatchAll();
        if (catchAll != null) {
            writer.newLine();
            catchAll.append(writer);
        }
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        parseCatches(reader);
        parseCatchAll(reader);
    }
    private void parseCatches(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliSet<SmaliCodeCatch> catchSet = getCatchSet();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        while (directive == SmaliDirective.CATCH) {
            int position = reader.position();
            SmaliCodeCatch codeCatch = new SmaliCodeCatch();
            codeCatch.parse(reader);
            if (isDifferentGroup(codeCatch)) {
                reader.position(position);
                break;
            }
            catchSet.add(codeCatch);
            reader.skipWhitespacesOrComment();
            directive = SmaliDirective.parse(reader, false);
        }
    }
    private void parseCatchAll(SmaliReader reader) throws IOException {
        reader.skipWhitespacesOrComment();
        SmaliDirective directive = SmaliDirective.parse(reader, false);
        if (directive == SmaliDirective.CATCH_ALL) {
            SmaliCodeCatchAll catchAll = new SmaliCodeCatchAll();
            int position = reader.position();
            catchAll.parse(reader);
            if (isDifferentGroup(catchAll)) {
                reader.position(position);
            }else {
                setCatchAll(catchAll);
            }
        }
    }
    private boolean isDifferentGroup(SmaliCodeExceptionHandler exceptionHandler) {
        SmaliLabel smaliLabel = pickBoundLabel(true);
        return smaliLabel != null && !smaliLabel.equals(exceptionHandler.getStart());
    }
}
