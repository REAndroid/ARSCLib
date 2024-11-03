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
package com.reandroid.dex.data;

import com.reandroid.arsc.base.BlockRefresh;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.common.HiddenApiFlag;
import com.reandroid.dex.common.Modifier;
import com.reandroid.dex.common.SectionTool;
import com.reandroid.dex.smali.SmaliFormat;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class HiddenApiFlagValue extends Ule128Item implements
        BlockRefresh, Iterable<HiddenApiFlag>, SmaliFormat, Comparable<HiddenApiFlagValue> {

    private Def<?> def;

    public HiddenApiFlagValue() {
        super();
        set(HiddenApiFlag.NO_RESTRICTION);
    }

    Def<?> getDef() {
        return def;
    }

    HiddenApiFlagValue newCompact() {
        return new Compact(this);
    }
    void linkDef(Def<?> def) {
        this.def = def;
        def.linkHiddenApiFlagValueInternal(this);
    }

    public void removeSelf() {
        HiddenApiFlagValueList flagValueList = getParentInstance(HiddenApiFlagValueList.class);
        if (flagValueList != null) {
            flagValueList.remove(this);
        }
    }

    public boolean isEmpty() {
        Def<?> def = getDef();
        return def == null || def.isRemoved();
    }
    public boolean isRemoved() {
        return getParentInstance(HiddenApiRestrictions.class) == null;
    }
    public boolean isNoRestriction() {
        return isEmpty() || get() == HiddenApiFlag.NO_RESTRICTION;
    }

    public HiddenApiFlag getRestriction() {
        return HiddenApiFlag.restrictionOf(get());
    }
    public HiddenApiFlag getDomain() {
        return HiddenApiFlag.domainOf(get());
    }
    public void add(HiddenApiFlag flag) {
        int restrict = HiddenApiFlag.NO_RESTRICTION;
        int domain = 0;
        if (flag.isDomainFlag()) {
            HiddenApiFlag keep = getRestriction();
            if (keep != null) {
                restrict = keep.getValue();
            }
            domain = flag.getValue();
        } else {
            HiddenApiFlag keep = getDomain();
            if (keep != null) {
                domain = keep.getValue();
            }
            restrict = flag.getValue();
        }
        set(domain | restrict);
    }
    public void remove(HiddenApiFlag flag) {
        int restrict = HiddenApiFlag.NO_RESTRICTION;
        int domain = 0;
        if (flag.isDomainFlag()) {
            HiddenApiFlag keep = getRestriction();
            if (keep != null) {
                restrict = keep.getValue();
            }
        } else {
            HiddenApiFlag keep = getDomain();
            if (keep != null) {
                domain = keep.getValue();
            }
        }
        set(domain | restrict);
    }
    public void clear() {
        set(HiddenApiFlag.NO_RESTRICTION);
    }

    @Override
    public Iterator<HiddenApiFlag> iterator() {
        return HiddenApiFlag.valuesOf(get());
    }

    @Override
    public void refresh() {
        Def<?> def = this.def;
        if (def == null) {
            set(HiddenApiFlag.NO_RESTRICTION);
        }
    }
    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.appendModifiers(iterator());
    }

    @Override
    public int compareTo(HiddenApiFlagValue hiddenApiFlagValue) {
        if(hiddenApiFlagValue == null){
            return -1;
        }
        if(hiddenApiFlagValue == this){
            return 0;
        }
        return SectionTool.compareIndex(def, hiddenApiFlagValue.def);
    }


    @Override
    public int hashCode() {
        return get();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HiddenApiFlagValue)) {
            return false;
        }
        HiddenApiFlagValue flagValue = (HiddenApiFlagValue) obj;
        return get() == flagValue.get();
    }

    @Override
    public String toString() {
        return Modifier.toString(iterator());
    }

    static class Compact extends HiddenApiFlagValue {
        private final HiddenApiFlagValue source;

        Compact(HiddenApiFlagValue source){
            super();
            this.source = source;
        }
        @Override
        public void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        public int get() {
            return source.get();
        }
        @Override
        public void set(int value) {
            source.set(value);
        }
        @Override
        protected void writeValue(int value) {
        }
        @Override
        protected int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
        @Override
        public boolean isNull() {
            return true;
        }
        @Override
        public byte[] getBytes() {
            return null;
        }
    }

}
