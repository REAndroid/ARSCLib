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
package com.reandroid.dex.program;

import java.util.Comparator;
import java.util.Iterator;

public interface ReferenceLabelSet {

    void addReferenceLabel(InstructionLabel label);
    void removeReferenceLabel(InstructionLabel label);
    void clearReferenceLabels();
    boolean hasReferenceLabels();
    Iterator<? extends InstructionLabel> getReferenceLabels();

    static String toStringLabels(ReferenceLabelSet set) {
        return toStringLabels(set, "    ");
    }
    static String toStringLabels(ReferenceLabelSet set, String tab) {
        if (set.hasReferenceLabels()) {
            StringBuilder builder = new StringBuilder();
            append(set, tab, builder);
            return builder.toString();
        }
        return "";
    }
    static void append(ReferenceLabelSet set, StringBuilder builder) {
        append(set, "    ", builder);
    }
    static void append(ReferenceLabelSet set, String tab, StringBuilder builder) {
        if (set.hasReferenceLabels()) {
            Iterator<? extends InstructionLabel> iterator = set.getReferenceLabels();
            Comparator<InstructionLabel> comparator = InstructionLabel.LABEL_COMPARATOR;
            InstructionLabel last = null;
            while (iterator.hasNext()) {
                InstructionLabel label = iterator.next();
                if (last != null && comparator.compare(last, label) == 0) {
                    continue;
                }
                builder.append(tab);
                builder.append(label.getLabelName());
                builder.append('\n');
                last = label;
            }
            if (last != null && last.getLabelType().isHandler()) {
                builder.append('\n');
            }
        }
    }

}
