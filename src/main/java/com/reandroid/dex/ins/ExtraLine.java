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
package com.reandroid.dex.ins;

import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;
import java.util.Comparator;

public interface ExtraLine{
    void appendExtra(SmaliWriter writer) throws IOException;
    boolean isEqualExtraLine(Object obj);
    int getSortOrder();

    Comparator<ExtraLine> COMPARATOR = new Comparator<ExtraLine>() {
        @Override
        public int compare(ExtraLine extraLine1, ExtraLine extraLine2) {
            return Integer.compare(extraLine1.getSortOrder(), extraLine2.getSortOrder());
        }
    };

    int ORDER_DEBUG_LINE_NUMBER = 0;
    int ORDER_DEBUG_LINE = 1;
    int ORDER_INSTRUCTION_LABEL = 2;
    int ORDER_TRY_START = 3;
    int ORDER_TRY_END = 4;
    int ORDER_EXCEPTION_HANDLER = 5;
    int ORDER_CATCH = 6;
}
