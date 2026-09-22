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
package com.reandroid.dex.smali.fix;

import com.reandroid.dex.smali.model.SmaliCodeTryItem;
import com.reandroid.dex.smali.model.SmaliMethod;

import java.util.Iterator;

public class SmaliOverlappingTryFix extends SmaliMethodFix {

    public static final SmaliOverlappingTryFix INSTANCE = new SmaliOverlappingTryFix();

    public SmaliOverlappingTryFix() {
        super();
    }

    @Override
    public Boolean apply(SmaliMethod smaliMethod) {
        boolean result = false;
        if (haveNestedTryItems(smaliMethod)) {
            flattenAll(smaliMethod);
            result = fixOverlappingStart(smaliMethod);
            result = fixOverlappingHandler(smaliMethod) || result;
        }
        return result;
    }

    private boolean fixOverlappingStart(SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (fixOverlappingStart(tryItem, smaliMethod)) {
                fixOverlappingStart(smaliMethod);
                return true;
            }
        }
        return false;
    }
    private boolean fixOverlappingStart(SmaliCodeTryItem previous, SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        int startPrev = previous.getStartAddress();
        int position = previous.getAddress();
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (tryItem == previous) {
                continue;
            }
            if (tryItem.getStartAddress() < position) {
                if (startPrev < tryItem.getStartAddress()) {
                    previous.splitAtIndex(tryItem.getStartLabel());
                    return true;
                }
            }
        }
        return false;
    }


    private boolean fixOverlappingHandler(SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (fixOverlappingHandler(tryItem, smaliMethod)) {
                fixOverlappingHandler(smaliMethod);
                return true;
            }
        }
        return false;
    }
    private boolean fixOverlappingHandler(SmaliCodeTryItem previous, SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        int startPrev = previous.getStartAddress();
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (tryItem == previous) {
                continue;
            }
            if (startPrev == tryItem.getStartAddress() && previous.getAddress() > tryItem.getAddress()) {
                previous.splitAtIndex(tryItem);
                return true;
            }
        }
        return false;
    }

    private boolean haveNestedTryItems(SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        int position = -2;
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (position == -2) {
                position = tryItem.getAddress();
                continue;
            }
            if (tryItem.getStartAddress() < position) {
                return true;
            }
            position = tryItem.getAddress();
        }
        return false;
    }

    private void flattenAll(SmaliMethod smaliMethod) {
        Iterator<SmaliCodeTryItem> iterator = smaliMethod.getTryItems();
        while (iterator.hasNext()) {
            SmaliCodeTryItem tryItem = iterator.next();
            if (tryItem.flatten()) {
                flattenAll(smaliMethod);
            }
        }
    }
}
