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
package com.reandroid.lib.arsc.value;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.base.BlockCounter;
import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.io.BlockReader;
import com.reandroid.lib.arsc.item.*;
import com.reandroid.lib.arsc.pool.SpecStringPool;
import com.reandroid.lib.arsc.pool.TableStringPool;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONObject;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EntryBlock extends Block implements JSONConvert<JSONObject> {
    private ShortItem mHeaderSize;
    private ByteItem mFlagEntryType;
    private ByteItem mByteFlagsB;
    private IntegerItem mSpecReference;
    private BaseResValue mResValue;
    private boolean mUnLocked;
    public EntryBlock() {
        super();
    }

    public ResValueInt setValueAsRaw(ValueType valueType, int rawValue){
        ResValueInt resValueInt;
        BaseResValue res = getResValue();
        if(res instanceof ResValueInt){
            resValueInt=(ResValueInt) res;
        }else {
            resValueInt=new ResValueInt();
            setResValue(resValueInt);
        }
        resValueInt.setType(valueType);
        resValueInt.setData(rawValue);
        return resValueInt;
    }
    public ResValueInt setValueAsBoolean(boolean val){
        ResValueInt resValueInt;
        BaseResValue res = getResValue();
        if(res instanceof ResValueInt){
            resValueInt=(ResValueInt) res;
        }else {
            resValueInt=new ResValueInt();
            setResValue(resValueInt);
        }
        resValueInt.setType(ValueType.INT_BOOLEAN);
        resValueInt.setData(val?0xffffffff:0);
        return resValueInt;
    }
    public ResValueInt setValueAsInteger(int val){
        ResValueInt resValueInt;
        BaseResValue res = getResValue();
        if(res instanceof ResValueInt){
            resValueInt=(ResValueInt) res;
        }else {
            resValueInt=new ResValueInt();
            setResValue(resValueInt);
        }
        resValueInt.setType(ValueType.INT_DEC);
        resValueInt.setData(val);
        return resValueInt;
    }
    public ResValueInt setValueAsReference(int resId){
        ResValueInt resValueInt;
        BaseResValue res = getResValue();
        if(res instanceof ResValueInt){
            resValueInt=(ResValueInt) res;
        }else {
            resValueInt=new ResValueInt();
            setResValue(resValueInt);
        }
        resValueInt.setType(ValueType.REFERENCE);
        resValueInt.setData(resId);
        return resValueInt;
    }
    public ResValueInt setValueAsString(String str){
        TableStringPool stringPool=getTableStringPool();
        if(stringPool==null){
            throw new IllegalArgumentException("TableStringPool = null, EntryBlock not added to parent TableBlock");
        }
        TableString tableString = stringPool.getOrCreate(str);
        ResValueInt resValueInt;
        BaseResValue res = getResValue();
        if(res instanceof ResValueInt){
            resValueInt=(ResValueInt) res;
        }else {
            resValueInt=new ResValueInt();
            setResValue(resValueInt);
        }
        resValueInt.setType(ValueType.STRING);
        resValueInt.setData(tableString.getIndex());
        return resValueInt;
    }
    public boolean getValueAsBoolean(){
        int data = ((ResValueInt)getResValue()).getData();
        return data!=0;
    }
    public String getValueAsString(){
        TableString tableString= getValueAsTableString();
        if(tableString==null){
            return null;
        }
        return tableString.getHtml();
    }
    public TableString getValueAsTableString(){
        TableStringPool stringPool=getTableStringPool();
        if(stringPool==null){
            return null;
        }
        BaseResValue res = getResValue();
        if(!(res instanceof ResValueInt)){
            return null;
        }
        ResValueInt resValueInt=(ResValueInt)res;
        TableString tableString= stringPool.get(resValueInt.getData());
        if(tableString==null){
            return null;
        }
        return tableString;
    }
    private TableStringPool getTableStringPool(){
        PackageBlock pkg=getPackageBlock();
        if(pkg==null){
            return null;
        }
        TableBlock table = pkg.getTableBlock();
        if(table==null){
            return null;
        }
        return table.getTableStringPool();
    }
    public boolean isDefault(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.isDefault();
        }
        return false;
    }
    public List<ReferenceItem> getTableStringReferences(){
        if(isNull()){
            return null;
        }
        List<ReferenceItem> results=null;
        BaseResValue resValue=getResValue();
        if(resValue instanceof ResValueInt){
            ResValueInt resValueInt=(ResValueInt)resValue;
            ReferenceItem ref=resValueInt.getTableStringReference();
            if(ref!=null){
                results=new ArrayList<>();
                results.add(ref);
            }
        }else if(resValue instanceof ResValueBag){
            ResValueBag resValueBag=(ResValueBag)resValue;
            results=resValueBag.getTableStringReferences();
        }
        return results;
    }
    boolean removeSpecReference(ReferenceItem ref){
        if(ref==null){
            return false;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return false;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        return specStringPool.removeReference(ref);
    }
    boolean removeTableReference(ReferenceItem ref){
        if(ref==null){
            return false;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return false;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return false;
        }
        TableStringPool tableStringPool=tableBlock.getTableStringPool();
        return tableStringPool.removeReference(ref);
    }

    void addSpecReference(ReferenceItem ref){
        if(ref==null){
            return;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        specStringPool.addReference(ref);
    }
    void addTableReference(ReferenceItem ref){
        if(ref==null){
            return;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return;
        }
        TableStringPool tableStringPool=tableBlock.getTableStringPool();
        tableStringPool.addReference(ref);
    }
    private void removeTableReferences(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        TableBlock tableBlock=packageBlock.getTableBlock();
        if(tableBlock==null){
            return;
        }
        TableStringPool tableStringPool=tableBlock.getTableStringPool();
        tableStringPool.removeReferences(getTableStringReferences());
    }
    private void removeSpecReferences(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        specStringPool.removeReference(getSpecReferenceBlock());
    }
    private void removeAllReferences(){
        removeTableReferences();
        removeSpecReferences();
    }
    public void setEntryTypeBag(boolean b){
        mFlagEntryType.putBit(0, b);
        refreshHeaderSize();
    }
    public boolean isEntryTypeBag(){
        return mFlagEntryType.getBit(0);
    }
    public void setEntryTypeShared(boolean b){
        mFlagEntryType.putBit(1, b);
    }
    public boolean isEntryTypeShared(){
        return mFlagEntryType.getBit(1);
    }
    public void setEntryTypePublic(boolean b){
        mFlagEntryType.putBit(2, b);
    }
    public boolean isEntryTypePublic(){
        return mFlagEntryType.getBit(2);
    }
    private void setByteFlagsB(byte b){
        mByteFlagsB.set(b);
    }
    private IntegerItem getSpecReferenceBlock(){
        return mSpecReference;
    }
    public int getSpecReference(){
        if(mSpecReference==null){
            return -1;
        }
        return mSpecReference.get();
    }
    public void setSpecReference(int ref){
        boolean created = createNullSpecReference();
        int old=mSpecReference.get();
        if(ref==old){
            return;
        }
        mSpecReference.set(ref);
        updateSpecRef(old, ref);
        if(created){
            updatePackage();
        }
    }
    public void setSpecReference(SpecString specString){
        removeSpecRef();
        if(specString==null){
            return;
        }
        boolean created = createNullSpecReference();
        mSpecReference.set(specString.getIndex());
        if(created){
            updatePackage();
        }
    }
    private boolean createNullSpecReference(){
        if(mSpecReference==null){
            mSpecReference = new IntegerItem();
            mSpecReference.setNull(true);
            return true;
        }
        return false;
    }
    public BaseResValue getResValue(){
        return mResValue;
    }
    public void setResValue(BaseResValue resValue){
        if(resValue==mResValue){
            return;
        }
        setResValueInternal(resValue);
        if(resValue==null){
            setNull(true);
        }else {
            setNull(false);
            boolean is_bag=(resValue instanceof ResValueBag);
            setEntryTypeBag(is_bag);
            updatePackage();
            updateSpecRef();
        }
    }
    private void setResValueInternal(BaseResValue resValue){
        if(resValue==mResValue){
            return;
        }
        if(resValue!=null){
            resValue.setIndex(4);
            resValue.setParent(this);
        }
        if(mResValue!=null){
            mResValue.setIndex(-1);
            mResValue.setParent(null);
            mResValue.onRemoved();
        }
        if(resValue!=null){
            setNull(false);
        }
        mResValue=resValue;
    }

    public ResConfig getResConfig(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getResConfig();
        }
        return null;
    }
    public String getName(){
        SpecString specString=getSpecString();
        if(specString==null){
            return null;
        }
        return specString.get();
    }
    public String getNameOrHex(){
        String name = getName();
        if(name==null){
            name = String.format("@0x%08x", getResourceId());
        }
        return name;
    }
    private void setName(String name){
        PackageBlock packageBlock=getPackageBlock();
        EntryGroup entryGroup = packageBlock.getEntryGroup(getResourceId());
        if(entryGroup!=null){
            entryGroup.renameSpec(name);
            return;
        }
        SpecStringPool specStringPool= packageBlock.getSpecStringPool();
        SpecString specString=specStringPool.getOrCreate(name);
        setSpecReference(specString.getIndex());
    }
    public String getTypeName(){
        TypeString typeString=getTypeString();
        if(typeString==null){
            return null;
        }
        return typeString.get();
    }
    public String getPackageName(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        return packageBlock.getName();
    }
    public SpecString getSpecString(){
        if(mSpecReference==null){
            return null;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        return specStringPool.get(getSpecReference());
    }
    public TypeString getTypeString(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getTypeString();
        }
        return null;
    }
    public String buildResourceName(int resourceId, char prefix, boolean includeType){
        if(resourceId==0){
            return null;
        }
        EntryBlock entryBlock=searchEntry(resourceId);
        return buildResourceName(entryBlock, prefix, includeType);
    }
    private EntryBlock searchEntry(int resourceId){
        if(resourceId==getResourceId()){
            return this;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return null;
        }
        TableBlock tableBlock= packageBlock.getTableBlock();
        if(tableBlock==null){
            return null;
        }
        EntryGroup entryGroup = tableBlock.search(resourceId);
        if(entryGroup!=null){
            return entryGroup.pickOne();
        }
        return null;
    }
    public String buildResourceName(EntryBlock entryBlock, char prefix, boolean includeType){
        if(entryBlock==null){
            return null;
        }
        String pkgName=entryBlock.getPackageName();
        if(getResourceId()==entryBlock.getResourceId()){
            pkgName=null;
        }else if(pkgName!=null){
            if(pkgName.equals(this.getPackageName())){
                pkgName=null;
            }
        }
        String type=null;
        if(includeType){
            type=entryBlock.getTypeName();
        }
        String name=entryBlock.getName();
        return buildResourceName(prefix, pkgName, type, name);
    }
    public String getResourceName(){
        return buildResourceName('@',null, getTypeName(), getName());
    }
    public String getResourceName(char prefix){
        return getResourceName(prefix, false, true);
    }
    public String getResourceName(char prefix, boolean includePackage, boolean includeType){
        String pkg=includePackage?getPackageName():null;
        String type=includeType?getTypeName():null;
        return buildResourceName(prefix,pkg, type, getName());
    }
    public int getResourceId(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock==null){
            return 0;
        }
        PackageBlock packageBlock=typeBlock.getPackageBlock();
        if(packageBlock==null){
            return 0;
        }
        int pkgId=packageBlock.getId();
        int typeId=typeBlock.getTypeIdInt();
        int entryId=getIndex();
        return ((pkgId << 24) | (typeId << 16) | entryId);
    }
    public PackageBlock getPackageBlock(){
        TypeBlock typeBlock=getTypeBlock();
        if(typeBlock!=null){
            return typeBlock.getPackageBlock();
        }
        return null;
    }
    public TypeBlock getTypeBlock(){
        Block parent=getParent();
        while (parent!=null){
            if(parent instanceof TypeBlock){
                return (TypeBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    private void unlockEntry(){
        if(mUnLocked){
            return;
        }
        mUnLocked =true;
        this.mHeaderSize =new ShortItem();
        this.mFlagEntryType =new ByteItem();
        this.mByteFlagsB=new ByteItem();
        if(mSpecReference==null){
            this.mSpecReference = new IntegerItem();
        }else if(mSpecReference.isNull()){
            mSpecReference.setNull(false);
        }

        mHeaderSize.setIndex(0);
        mFlagEntryType.setIndex(1);
        mByteFlagsB.setIndex(2);
        mSpecReference.setIndex(3);

        mHeaderSize.setParent(this);
        mFlagEntryType.setParent(this);
        mByteFlagsB.setParent(this);
        mSpecReference.setParent(this);

    }
    private void lockEntry(){
        if(!mUnLocked){
            return;
        }
        removeAllReferences();
        mUnLocked =false;
        mHeaderSize.setParent(null);
        mFlagEntryType.setParent(null);
        mByteFlagsB.setParent(null);
        mSpecReference.setParent(null);

        mHeaderSize.setIndex(-1);
        mFlagEntryType.setIndex(-1);
        mByteFlagsB.setIndex(-1);
        mSpecReference.setIndex(-1);
        removeResValue();

        this.mHeaderSize =null;
        this.mFlagEntryType =null;
        this.mByteFlagsB =null;
        this.mSpecReference =null;
    }
    private void removeResValue(){
        if(mResValue!=null){
            mResValue.setParent(null);
            mResValue.setIndex(-1);
            mResValue=null;
        }
    }
    private void refreshHeaderSize(){
        if(isEntryTypeBag()){
            mHeaderSize.set(HEADER_SIZE_BAG);
        }else {
            mHeaderSize.set(HEADER_SIZE_INT);
        }
    }
    private void createResValue(){
        if(getResValue()!=null){
            return;
        }
        BaseResValue resValue;
        if(isEntryTypeBag()){
            resValue=new ResValueBag();
        }else {
            resValue=new ResValueInt();
        }
        setResValueInternal(resValue);
    }
    @Override
    public void setNull(boolean is_null){
        if(is_null){
            lockEntry();
        }else {
            unlockEntry();
        }
        super.setNull(is_null);
    }
    @Override
    public boolean isNull(){
        if(super.isNull() || !mUnLocked){
            return true;
        }
        return mResValue==null;
    }
    @Override
    public byte[] getBytes() {
        if(isNull()){
            return null;
        }
        byte[] results=mHeaderSize.getBytes();
        results=addBytes(results, mFlagEntryType.getBytes());
        results=addBytes(results, mByteFlagsB.getBytes());
        results=addBytes(results, mSpecReference.getBytes());
        results=addBytes(results, mResValue.getBytes());
        return results;
    }
    @Override
    public int countBytes() {
        if(isNull()){
            return 0;
        }
        /*
           mHeaderSize -> 2 bytes
                mFlags -> 2 bytes
        mSpecReference -> 4 bytes
                          -------
                  Total = 8 bytes, thus this value is always fixed no need to re-count
        */
        return 8 + mResValue.countBytes();
    }
    @Override
    public void onCountUpTo(BlockCounter counter) {
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        if(isNull()){
            return;
        }
        counter.addCount(countBytes());
        mHeaderSize.onCountUpTo(counter);
        mFlagEntryType.onCountUpTo(counter);
        mByteFlagsB.onCountUpTo(counter);
        mSpecReference.onCountUpTo(counter);
        mResValue.onCountUpTo(counter);
    }
    @Override
    protected int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        int result=mHeaderSize.writeBytes(stream);
        result+= mFlagEntryType.writeBytes(stream);
        result+=mByteFlagsB.writeBytes(stream);
        result+= mSpecReference.writeBytes(stream);
        result+=mResValue.writeBytes(stream);
        return result;
    }

    private void updateSpecRef(){
        updateSpecRef(-1, getSpecReference());
    }
    private void updateSpecRef(int oldRef, int newNef){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        SpecString specString=specStringPool.get(oldRef);
        if(specString!=null){
            specString.removeReference(getSpecReferenceBlock());
        }
        SpecString specStringNew=specStringPool.get(newNef);
        if(specStringNew!=null){
            specStringNew.addReference(getSpecReferenceBlock());
        }
    }
    private void removeSpecRef(){
        if(mSpecReference==null){
            return;
        }
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        SpecStringPool specStringPool=packageBlock.getSpecStringPool();
        SpecString specString=specStringPool.get(getSpecReference());
        if(specString!=null){
            specString.removeReference(getSpecReferenceBlock());
        }
    }
    private void updatePackage(){
        PackageBlock packageBlock=getPackageBlock();
        if(packageBlock==null){
            return;
        }
        packageBlock.onEntryAdded(this);
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        setNull(false);
        removeResValue();
        mHeaderSize.readBytes(reader);
        mFlagEntryType.readBytes(reader);
        mByteFlagsB.readBytes(reader);
        mSpecReference.readBytes(reader);
        createResValue();
        mResValue.readBytes(reader);
        updatePackage();
        updateSpecRef();
        mResValue.onDataLoaded();
    }
    @Override
    public JSONObject toJson() {
        if(isNull()){
            return null;
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(NAME_entry_name, getSpecString().get());
        if(isEntryTypeBag()){
            jsonObject.put(NAME_is_bag, true);
        }
        if(isEntryTypePublic()){
            jsonObject.put(NAME_is_public, true);
        }
        if(isEntryTypeShared()){
            jsonObject.put(NAME_is_shared, true);
        }
        jsonObject.put(NAME_value, getResValue().toJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        if(json==null){
            setNull(true);
            return;
        }
        BaseResValue baseResValue;
        if(json.optBoolean(NAME_is_bag, false)){
            baseResValue=new ResValueBag();
        }else {
            baseResValue=new ResValueInt();
        }
        setResValue(baseResValue);
        setEntryTypeShared(json.optBoolean(NAME_is_shared, false));
        setEntryTypePublic(json.optBoolean(NAME_is_public, false));
        setName(json.getString(NAME_entry_name));
        baseResValue.fromJson(json.getJSONObject(NAME_value));
        mResValue.onDataLoaded();
    }
    public void merge(EntryBlock entryBlock){
        if(entryBlock==null||entryBlock==this||entryBlock.isNull()){
            return;
        }
        String name=entryBlock.getName();
        if(name==null){
            name="";
        }
        if(!entryBlock.isEntryTypeBag()){
            ResValueInt comingValue = (ResValueInt) entryBlock.getResValue();
            ResValueInt exist = getOrCreateResValueInt();
            setResValue(exist);
            exist.merge(comingValue);
        }else {
            ResValueBag comingValue = (ResValueBag)  entryBlock.getResValue();
            ResValueBag exist=getOrCreateResValueBag();
            setResValue(exist);
            exist.merge(comingValue);
        }
        SpecString spec = getPackageBlock()
                .getSpecStringPool().getOrCreate(name);
        setSpecReference(spec.getIndex());
        setEntryTypeShared(entryBlock.isEntryTypeShared());
        setEntryTypePublic(entryBlock.isEntryTypePublic());
    }
    private ResValueBag getOrCreateResValueBag(){
        if(mResValue instanceof ResValueBag){
            return (ResValueBag) mResValue;
        }
        return new ResValueBag();
    }
    private ResValueInt getOrCreateResValueInt(){
        if(mResValue instanceof ResValueInt){
            return (ResValueInt) mResValue;
        }
        return new ResValueInt();
    }
    public static String buildResourceName(char prefix, String packageName, String type, String name){
        if(name==null){
            return null;
        }
        StringBuilder builder=new StringBuilder();
        if(prefix!=0){
            builder.append(prefix);
        }
        if(packageName!=null){
            builder.append(packageName);
            builder.append(':');
        }
        if(type!=null){
            builder.append(type);
            builder.append('/');
        }
        builder.append(name);
        return builder.toString();
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(": ");
        ResConfig resConfig=getResConfig();
        if(resConfig!=null){
            builder.append(resConfig.toString());
            builder.append(", ");
        }
        String name=getResourceName();
        if(name==null){
            name=getNameOrHex();
        }else{
            builder.append(" id=");
            builder.append(String.format("0x%08x", getResourceId()));
        }
        builder.append('(');
        builder.append(name);
        builder.append(')');
        if(isNull()){
            builder.append(", null entry");
            return builder.toString();
        }
        BaseResValue baseResValue=getResValue();
        if(baseResValue instanceof ResValueInt){
            ResValueInt resValueInt=(ResValueInt)baseResValue;
            builder.append(" '");
            builder.append(resValueInt.toString());
            builder.append(" '");
        }
        return builder.toString();
    }

    private final static short HEADER_SIZE_BAG = 0x0010;
    private final static short HEADER_SIZE_INT = 0x0008;

    public static final String NAME_entry_name ="entry_name";
    private static final String NAME_is_bag="is_bag";
    private static final String NAME_is_shared="is_shared";
    private static final String NAME_is_public="is_public";
    private static final String NAME_value="value";

}
