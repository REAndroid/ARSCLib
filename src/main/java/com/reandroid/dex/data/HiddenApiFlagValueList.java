package com.reandroid.dex.data;

import com.reandroid.arsc.base.Creator;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.dex.base.DexException;
import com.reandroid.utils.CompareUtil;

import java.io.IOException;
import java.io.OutputStream;

class HiddenApiFlagValueList extends BlockList<HiddenApiFlagValue>
        implements Iterable<HiddenApiFlagValue> {

    private DefArray<?> defArray;

    HiddenApiFlagValueList(Creator<? extends HiddenApiFlagValue> creator){
        super(creator);
    }
    public HiddenApiFlagValueList(){
        this(CREATOR);
    }


    public HiddenApiFlagValue get(Def<?> def) {
        if (def == null) {
            return null;
        }
        DefArray<?> defArray = this.defArray;
        if (defArray == null) {
            defArray = def.getParentInstance(DefArray.class);
            linkDefArray(defArray);
        }
        HiddenApiFlagValue flagValue = get(def.getIndex());
        if (defArray == null) {
            return flagValue;
        }
        if (flagValue != null && flagValue.getDef() == def) {
            return flagValue;
        }
        int size = size();
        for (int i = 0; i < size; i++) {
            flagValue = get(i);
            if (flagValue.getDef() == def) {
                return flagValue;
            }
        }
        flagValue = createNext();
        flagValue.linkDef(def);
        return flagValue;
    }

    HiddenApiFlagValueList newCompact(){
        return new Compact(this);
    }

    @Override
    protected void onPreRefresh() {
        super.onPreRefresh();
        ensureDefLinked();
    }

    private void ensureDefLinked() {
        DefArray<?> defArray = this.defArray;
        if(defArray == null || defArray.size() == 0){
            setSize(0);
            return;
        }
        removeIf(HiddenApiFlagValue::isEmpty);
        boolean needsSort = false;
        int size = defArray.size();
        for (int i = 0; i < size; i++) {
            Def<?> def = defArray.get(i);
            HiddenApiFlagValue flagValue = get(def);
            if (!needsSort) {
                needsSort = (def.getIndex() != flagValue.getIndex());
            }
        }
        if (needsSort) {
            sort(CompareUtil.getComparableComparator());
        }
    }

    void linkDefArray(DefArray<?> defArray) {
        if (this.defArray != null) {
            if (this.defArray == defArray) {
                return;
            }
            throw new DexException("Invalid link state");
        }
        this.defArray = defArray;
        if(defArray == null){
            setSize(0);
            return;
        }
        int size = defArray.size();
        setSize(size);
        for(int i = 0; i < size; i++){
            get(i).linkDef(defArray.get(i));
        }
    }
    boolean isAllNoRestrictions() {
        for (HiddenApiFlagValue flagValue : this) {
            if (!flagValue.isNoRestriction()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onReadBytes(BlockReader reader) throws IOException {
        readChildes(reader);
    }

    static class Compact extends HiddenApiFlagValueList{

        private final HiddenApiFlagValueList source;

        Compact(HiddenApiFlagValueList source){
            super(new CompactCreator(source));
            this.source = source;
        }
        @Override
        HiddenApiFlagValueList newCompact() {
            return new Compact(source);
        }

        @Override
        protected void onPreRefresh() {
        }
        @Override
        public int countBytes() {
            return 0;
        }
        @Override
        protected void onReadBytes(BlockReader reader) throws IOException {
        }
        @Override
        public void readChildes(BlockReader reader) throws IOException {
        }
        @Override
        protected int onWriteBytes(OutputStream stream) throws IOException {
            return 0;
        }
    }


    static class CompactCreator implements Creator<HiddenApiFlagValue>{

        private final HiddenApiFlagValueList source;

        CompactCreator(HiddenApiFlagValueList source){
            this.source = source;
        }

        @Override
        public HiddenApiFlagValue[] newArrayInstance(int length) {
            return new HiddenApiFlagValue[length];
        }
        @Override
        public HiddenApiFlagValue newInstance() {
            throw new RuntimeException("Call newInstanceAt()");
        }
        @Override
        public HiddenApiFlagValue newInstanceAt(int index) {
            return source.get(index).newCompact();
        }
    }

    private static final Creator<HiddenApiFlagValue> CREATOR = new Creator<HiddenApiFlagValue>() {
        @Override
        public HiddenApiFlagValue[] newArrayInstance(int length) {
            return new HiddenApiFlagValue[length];
        }
        @Override
        public HiddenApiFlagValue newInstance() {
            return new HiddenApiFlagValue();
        }
    };
}
