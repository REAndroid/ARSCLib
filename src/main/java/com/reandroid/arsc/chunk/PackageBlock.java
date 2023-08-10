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
package com.reandroid.arsc.chunk;

import com.reandroid.arsc.BuildInfo;
import com.reandroid.arsc.array.LibraryInfoArray;
import com.reandroid.arsc.array.SpecTypePairArray;
import com.reandroid.arsc.base.Block;
import com.reandroid.arsc.coder.CommonType;
import com.reandroid.arsc.coder.EncodeResult;
import com.reandroid.arsc.coder.ValueCoder;
import com.reandroid.arsc.coder.xml.XmlEncodeException;
import com.reandroid.arsc.container.BlockList;
import com.reandroid.arsc.container.PackageBody;
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.header.PackageHeader;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.list.OverlayableList;
import com.reandroid.arsc.list.StagedAliasList;
import com.reandroid.arsc.model.ResourceLibrary;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TableStringPool;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.arsc.value.*;
import com.reandroid.common.Namespace;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.*;
import com.reandroid.utils.collection.*;
import com.reandroid.utils.io.IOUtil;
import com.reandroid.xml.XMLElement;
import com.reandroid.xml.XMLFactory;
import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class PackageBlock extends Chunk<PackageHeader>
        implements ParentChunk,
        JSONConvert<JSONObject>,
        Comparable<PackageBlock>,
        ResourceLibrary {

    private final TypeStringPool mTypeStringPool;
    private final SpecStringPool mSpecStringPool;

    private final PackageBody mBody;

    private String mPrefix;
    private boolean mHasValidPrefix;

    private Object mTag;

    public PackageBlock() {
        super(new PackageHeader(), 3);
        PackageHeader header = getHeaderBlock();

        this.mTypeStringPool=new TypeStringPool(false, header.getTypeIdOffsetItem());
        this.mSpecStringPool=new SpecStringPool(true);

        this.mBody = new PackageBody();

        addChild(mTypeStringPool);
        addChild(mSpecStringPool);
        addChild(mBody);
    }

    public void changePackageId(int packageIdOld, int packageIdNew){
        Iterator<ValueItem> iterator = allValues();
        while (iterator.hasNext()){
            ValueItem valueItem = iterator.next();
            changePackageId(valueItem, packageIdOld, packageIdNew);
        }
        if(packageIdOld == getId()){
            setId(packageIdNew);
        }
    }
    public Iterator<ValueItem> allValues(){
        return new MergingIterator<>(new ComputeIterator<>(getSpecTypePairs(),
                SpecTypePair::allValues));
    }

    public Object getTag(){
        return mTag;
    }
    public void setTag(Object tag){
        this.mTag = tag;
    }
    public ResourceEntry getResource(int resourceId){
        int packageId = (resourceId >> 24 ) & 0xff;
        if(packageId == 0){
            return null;
        }
        if(packageId == getId()){
            int typeId = (resourceId >> 16 ) & 0xff;
            int entryId = resourceId & 0xffff;
            ResourceEntry resourceEntry = getResource(typeId, entryId);
            if(resourceEntry != null){
                return resourceEntry;
            }
        }
        StagedAliasEntry aliasEntry = searchByStagedResId(resourceId);
        if(aliasEntry == null){
            return null;
        }
        int alias = aliasEntry.getFinalizedResId();
        if(alias == 0 || alias == resourceId){
            return null;
        }
        packageId = (alias >> 24 ) & 0xff;
        if(packageId != getId()){
            return null;
        }
        int typeId = (alias >> 16 ) & 0xff;
        int entryId = alias & 0xffff;
        return getResource(typeId, entryId);
    }
    public ResourceEntry getResource(int typeId, int entryId){
        SpecTypePair specTypePair =
                getSpecTypePair(typeId);
        if(specTypePair == null){
            return null;
        }
        Entry entry = specTypePair.getAnyEntry((short) entryId);
        if(entry == null){
            return null;
        }
        return new ResourceEntry(this, entry.getResourceId());
    }
    public ResourceEntry getResource(String type, String name){
        SpecTypePair specTypePair =
                getSpecTypePair(type);
        if(specTypePair != null){
            return specTypePair.getResource(name);
        }
        return null;
    }
    public ResourceEntry getAttrResource(String name){
        Iterator<SpecTypePair> itr = getAttrSpecs();
        while (itr.hasNext()){
            ResourceEntry resourceEntry = itr.next()
                    .getResource(name);
            if(resourceEntry != null){
                return resourceEntry;
            }
        }
        return null;
    }
    public ResourceEntry getIdResource(String name){
        Iterator<SpecTypePair> itr = getIdSpecs();
        while (itr.hasNext()){
            ResourceEntry resourceEntry = itr.next()
                    .getResource(name);
            if(resourceEntry != null){
                return resourceEntry;
            }
        }
        return null;
    }
    public Iterator<ResourceEntry> getResources(){
        return new IterableIterator<SpecTypePair, ResourceEntry>(getSpecTypePairs()) {
            @Override
            public Iterator<ResourceEntry> iterator(SpecTypePair element) {
                return element.getResources();
            }
        };
    }
    public String buildDecodeDirectoryName(){
        int count = 0;
        TableBlock tableBlock = getTableBlock();
        if(tableBlock != null){
            count = tableBlock.countPackages();
        }
        return DIRECTORY_NAME_PREFIX + StringsUtil.formatNumber(getIndex() + 1, count);
    }
    public boolean hasValidTypeNames(){
        Set<String> unique = new HashSet<>();
        Iterator<SpecTypePair> iterator = getSpecTypePairs();
        while (iterator.hasNext()){
            SpecTypePair specTypePair = iterator.next();
            String typeName = specTypePair.getTypeName();
            if(!CommonType.isCommonTypeName(typeName) || unique.contains(typeName)){
                return false;
            }
            unique.add(typeName);
        }
        return true;
    }
    public int removeUnusedSpecs(){
        return getSpecStringPool().removeUnusedStrings().size();
    }
    public String refreshFull(){
        return refreshFull(true);
    }
    public String refreshFull(boolean elementsRefresh){
        int sizeOld = getHeaderBlock().getChunkSize();
        StringBuilder message = new StringBuilder();
        boolean appendOnce = false;
        int count = removeUnusedSpecs();
        if(count != 0){
            message.append("Removed unused spec strings = ");
            message.append(count);
            appendOnce = true;
        }
        getSpecStringPool().sort();
        sortTypes();
        if(!elementsRefresh){
            if(appendOnce){
                return message.toString();
            }
            return null;
        }
        refresh();
        int sizeNew = getHeaderBlock().getChunkSize();
        if(sizeOld != sizeNew){
            if(appendOnce){
                message.append("\n");
            }
            message.append("Package size changed = ");
            message.append(sizeOld);
            message.append(", ");
            message.append(sizeNew);
            appendOnce = true;
        }
        if(appendOnce){
            return message.toString();
        }
        return null;
    }
    public void linkTableStringsInternal(TableStringPool tableStringPool){
        Iterator<SpecTypePair> iterator = getSpecTypePairs();
        while (iterator.hasNext()){
            iterator.next().linkTableStringsInternal(tableStringPool);
        }
    }
    public void linkSpecStringsInternal(SpecStringPool specStringPool){
        Iterator<SpecTypePair> iterator = getSpecTypePairs();
        while (iterator.hasNext()){
            iterator.next().linkSpecStringsInternal(specStringPool);
        }
    }
    public void destroy(){
        getPackageBody().destroy();
        getTypeStringPool().destroy();
        getSpecStringPool().destroy();
        setId(0);
        setName("");
    }
    public int resolveResourceId(String type, String name){
        return getSpecStringPool().resolveResourceId(type, name);
    }
    public int resolveResourceId(int typeId, String name){
        return getSpecStringPool().resolveResourceId(typeId, name);
    }
    public Entry getEntry(String type, String name){
        Iterator<Entry> iterator = getEntries(type, name);
        Entry result = null;
        while (iterator.hasNext()){
            Entry entry = iterator.next();
            if(!entry.isNull()){
                return entry;
            }
            if(result == null){
                result = entry;
            }
        }
        return result;
    }
    public Iterator<Entry> getEntries(String type, String name){
        return getSpecStringPool().getEntries(type, name);
    }
    public Iterator<Entry> getEntries(int typeId, String name){
        return getSpecStringPool().getEntries(typeId, name);
    }
    public Iterator<Entry> getEntries(int resourceId){
        return getEntries(resourceId, true);
    }
    public Iterator<Entry> getEntries(int resourceId, boolean skipNull){
        int packageId = (resourceId >> 24) & 0xff;
        if(packageId != getId()){
            return EmptyIterator.of();
        }
        return getEntries((resourceId >> 16) & 0xff, resourceId & 0xffff, skipNull);
    }
    public Iterator<Entry> getEntries(int typeId, int entryId){
        return getEntries(typeId, entryId, true);
    }
    public Iterator<Entry> getEntries(int typeId, int entryId, boolean skipNull){
        SpecTypePair specTypePair = getSpecTypePair(typeId);
        if(specTypePair != null){
            return specTypePair.getEntries(entryId, skipNull);
        }
        return EmptyIterator.of();
    }
    public Entry getEntry(String qualifiers, String type, String name){
        return getSpecTypePairArray().getEntry(qualifiers, type, name);
    }
    public Entry getEntry(ResConfig resConfig, String type, String name){
        return getSpecTypePairArray().getEntry(resConfig, type, name);
    }
    public Entry getOrCreate(String qualifiers, String type, String name){
        return getOrCreate(ResConfig.parse(qualifiers), type, name);
    }
    public Entry getOrCreate(ResConfig resConfig, String typeName, String name){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        TypeBlock typeBlock = specTypePair.getOrCreateTypeBlock(resConfig);
        return typeBlock.getOrCreateEntry(name);
    }
    public TypeBlock getOrCreateTypeBlock(String qualifiers, String typeName){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        return specTypePair.getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(ResConfig resConfig, String typeName){
        SpecTypePair specTypePair = getOrCreateSpecTypePair(typeName);
        return specTypePair.getOrCreateTypeBlock(resConfig);
    }
    public SpecTypePair getOrCreateSpecTypePair(String typeName){
        return getSpecTypePairArray().getOrCreate(typeName);
    }
    public int getTypeIdOffset(){
        return getHeaderBlock().getTypeIdOffset();
    }
    public BlockList<UnknownChunk> getUnknownChunkList(){
        return mBody.getUnknownChunkList();
    }

    public StagedAliasEntry searchByStagedResId(int stagedResId){
        for(StagedAlias stagedAlias:listStagedAlias()){
            StagedAliasEntry aliasEntry = stagedAlias.searchByStagedResId(stagedResId);
            if(aliasEntry != null){
                return aliasEntry;
            }
        }
        return null;
    }
    public List<StagedAlias> listStagedAlias(){
        return getStagedAliasList().getChildren();
    }
    public StagedAliasList getStagedAliasList(){
        return mBody.getStagedAliasList();
    }
    public OverlayableList getOverlayableList(){
        return mBody.getOverlayableList();
    }
    public BlockList<OverlayablePolicy> getOverlayablePolicyList(){
        return mBody.getOverlayablePolicyList();
    }
    public void sortTypes(){
        getSpecTypePairArray().sort();
    }


    @Override
    protected void onPreRefreshRefresh() {
        removeEmpty();
        super.onPreRefreshRefresh();
    }
    public void removeEmpty(){
        getSpecTypePairArray().removeEmptyPairs();
    }
    public boolean isEmpty(){
        return getSpecTypePairArray().isEmpty();
    }
    @Override
    public int getId(){
        return getHeaderBlock().getPackageId().get();
    }
    public void setId(byte id){
        setId(0xff & id);
    }
    public void setId(int id){
        getHeaderBlock().getPackageId().set(id);
        mPrefix = null;
        mHasValidPrefix = false;
    }
    @Override
    public String getName(){
        return getHeaderBlock().getPackageName().get();
    }
    public void setName(String name){
        getHeaderBlock().getPackageName().set(name);
        mPrefix = null;
        mHasValidPrefix = false;
    }
    @Override
    public String getPrefix(){
        if(mPrefix != null){
            return mPrefix;
        }
        boolean hasValidPrefix;
        String prefix;
        if(getId() == 0x01){
            prefix = ResourceLibrary.PREFIX_ANDROID;
            hasValidPrefix = ResourceLibrary.PREFIX_ANDROID.equals(getName());
        }else {
            prefix = ResourceLibrary.toPrefix(getName());
            hasValidPrefix = Namespace.isValidPrefix(prefix)
                    && !ResourceLibrary.PREFIX_ANDROID.equals(prefix);
            if(!hasValidPrefix){
                prefix = ResourceLibrary.PREFIX_APP;
            }
        }
        mPrefix = prefix;
        mHasValidPrefix = hasValidPrefix;
        return prefix;
    }
    @Override
    public String getUri(){
        if(isAndroid()){
            return ResourceLibrary.URI_ANDROID;
        }
        return ResourceLibrary.URI_RES_AUTO;
    }
    @Override
    public boolean packageNameMatches(String packageName){
        if(packageName == null){
            return false;
        }
        if(packageName.equals(getName())){
            return true;
        }
        if(packageName.equals(getPrefix()) && mHasValidPrefix){
            return true;
        }
        return getLibraryBlock().containsLibraryInfo(packageName);
    }
    private boolean isAndroid(){
        return getId() == 0x01 && ResourceLibrary.PREFIX_ANDROID.equals(getName());
    }
    public TableBlock getTableBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof TableBlock){
                return (TableBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public String typeNameOf(int typeId){
        TypeString typeString = getTypeStringPool().getById(typeId);
        if(typeString != null){
            return typeString.get();
        }
        return null;
    }
    public int typeIdOf(String typeName){
        return getTypeStringPool().idOf(typeName);
    }
    public TypeString getOrCreateTypeString(int typeId, String typeName){
        return getTypeStringPool().getOrCreate(typeId, typeName);
    }
    public TypeStringPool getTypeStringPool(){
        return mTypeStringPool;
    }
    @Override
    public SpecStringPool getSpecStringPool(){
        return mSpecStringPool;
    }
    @Override
    public TableBlock getMainChunk(){
        return getTableBlock();
    }
    @Override
    public PackageBlock getPackageBlock(){
        return this;
    }
    public PackageBody getPackageBody() {
        return mBody;
    }
    public SpecTypePairArray getSpecTypePairArray(){
        return mBody.getSpecTypePairArray();
    }
    public void trimConfigSizes(int resConfigSize){
        getSpecTypePairArray().trimConfigSizes(resConfigSize);
    }
    public Collection<LibraryInfo> listLibraryInfo(){
        return getLibraryBlock().listLibraryInfo();
    }

    public void addLibrary(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        for(LibraryInfo info:libraryBlock.getLibraryInfoArray().listItems()){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        getLibraryBlock().addLibraryInfo(info);
    }
    public LibraryBlock getLibraryBlock(){
        return mBody.getLibraryBlock();
    }
    public Entry getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getOrCreateEntry(typeId, entryId, qualifiers);
    }
    public Entry getOrCreateEntry(byte typeId, short entryId, ResConfig resConfig){
        return getSpecTypePairArray().getOrCreateEntry(typeId, entryId, resConfig);
    }

    public Entry getAnyEntry(int resourceId){
        int packageId = (resourceId >> 24) & 0xff;
        if(packageId != getId()){
            return null;
        }
        byte typeId = (byte) ((resourceId >> 16) & 0xff);
        short entryId = (short) (resourceId & 0xffff);
        return getSpecTypePairArray().getAnyEntry(typeId, entryId);
    }
    public Entry getEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getEntry(typeId, entryId, qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getOrCreateTypeBlock(typeId, qualifiers);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getTypeBlock(typeId, qualifiers);
    }

    private Iterator<SpecTypePair> getAttrSpecs(){
        return getSpecTypePairArray().iterator(new Predicate<SpecTypePair>() {
            @Override
            public boolean test(SpecTypePair specTypePair) {
                return specTypePair != null && specTypePair.isTypeAttr();
            }
        });
    }
    private Iterator<SpecTypePair> getIdSpecs(){
        return getSpecTypePairArray().iterator(new Predicate<SpecTypePair>() {
            @Override
            public boolean test(SpecTypePair specTypePair) {
                return specTypePair != null && specTypePair.isTypeId();
            }
        });
    }
    public SpecTypePair getSpecTypePair(String typeName){
        return getSpecTypePair(typeIdOf(typeName));
    }
    public SpecTypePair getSpecTypePair(int typeId){
        return getSpecTypePairArray().getSpecTypePair((byte) typeId);
    }

    public Collection<SpecTypePair> listSpecTypePairs(){
        return getSpecTypePairArray().listItems();
    }
    public Iterator<ResConfig> getResConfigs(){
        return new MergingIterator<>(new ComputeIterator<>(getSpecTypePairs(),
                SpecTypePair::getResConfigs));
    }
    public Iterator<SpecTypePair> getSpecTypePairs(){
        return getSpecTypePairArray().iterator();
    }

    private void refreshTypeStringPoolOffset(){
        int pos=countUpTo(mTypeStringPool);
        getHeaderBlock().getTypeStringPoolOffset().set(pos);
    }
    private void refreshTypeStringPoolCount(){
        getHeaderBlock().getTypeStringPoolCount().set(mTypeStringPool.countStrings());
    }
    private void refreshSpecStringPoolOffset(){
        int pos=countUpTo(mSpecStringPool);
        getHeaderBlock().getSpecStringPoolOffset().set(pos);
    }
    private void refreshSpecStringCount(){
        getHeaderBlock().getSpecStringPoolCount().set(mSpecStringPool.countStrings());
    }
    @Override
    public void onChunkLoaded() {
    }

    @Override
    protected void onChunkRefreshed() {
        refreshTypeStringPoolOffset();
        refreshTypeStringPoolCount();
        refreshSpecStringPoolOffset();
        refreshSpecStringCount();
    }

    public void serializePublicXml(File file) throws IOException {
        XmlSerializer serializer = XMLFactory.newSerializer(file);
        serializePublicXml(serializer);
        IOUtil.close(serializer);
    }
    public void serializePublicXml(XmlSerializer serializer) throws IOException {
        serializePublicXml(serializer, true);
    }
    public void serializePublicXml(XmlSerializer serializer, boolean fullDocument) throws IOException {
        if(fullDocument){
            serializer.startDocument("utf-8", null);
            serializer.text("\n");
            serializer.startTag(null, TAG_resources);
            writePackageInfo(serializer);
        }
        serializePublicXmlTypes(serializer);
        if(fullDocument){
            serializer.text("\n");
            serializer.endTag(null, TAG_resources);
            serializer.endDocument();
            IOUtil.close(serializer);
        }
    }
    private void serializePublicXmlTypes(XmlSerializer serializer) throws IOException {
        Iterator<SpecTypePair> iterator = getSpecTypePairs();
        while (iterator.hasNext()){
            iterator.next().serializePublicXml(serializer);
        }
    }
    private void writePackageInfo(XmlSerializer serializer) throws IOException {
        String name = getName();
        if(name != null){
            serializer.attribute(null, ATTR_package, name);
        }
        int id = getId();
        if(id != 0){
            serializer.attribute(null, ATTR_id, HexUtil.toHex2((byte)id));
        }
    }
    public void parsePublicXml(XmlPullParser parser) throws IOException,
            XmlPullParserException {
        getPublicXmlParser()
                .parse(parser);
    }
    public PublicXmlParser getPublicXmlParser(){
        return new PublicXmlParser(this);
    }

    @Override
    public JSONObject toJson() {
        return toJson(true);
    }
    public JSONObject toJson(boolean addTypes) {
        JSONObject jsonObject=new JSONObject();

        jsonObject.put(BuildInfo.NAME_arsc_lib_version, BuildInfo.getVersion());

        jsonObject.put(NAME_package_id, getId());
        jsonObject.put(NAME_package_name, getName());
        jsonObject.put(NAME_specs, getSpecTypePairArray().toJson(!addTypes));
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        if(libraryInfoArray.getChildrenCount()>0){
            jsonObject.put(NAME_libraries,libraryInfoArray.toJson());
        }
        StagedAlias stagedAlias =
                StagedAlias.mergeAll(getStagedAliasList().getChildren());
        if(stagedAlias!=null){
            jsonObject.put(NAME_staged_aliases,
                    stagedAlias.getStagedAliasEntryArray().toJson());
        }
        JSONArray jsonArray = getOverlayableList().toJson();
        if(jsonArray!=null){
            jsonObject.put(NAME_overlaybles, jsonArray);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        int id = json.optInt(NAME_package_id, 0);
        if(id != 0){
            setId(id);
        }
        String name = json.optString(NAME_package_name, null);
        if(name != null){
            setName(name);
        }
        getSpecTypePairArray().fromJson(json.optJSONArray(NAME_specs));
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        libraryInfoArray.fromJson(json.optJSONArray(NAME_libraries));
        if(json.has(NAME_staged_aliases)){
            StagedAlias stagedAlias=new StagedAlias();
            stagedAlias.getStagedAliasEntryArray()
                    .fromJson(json.getJSONArray(NAME_staged_aliases));
            getStagedAliasList().add(stagedAlias);
        }
        if(json.has(NAME_overlaybles)){
            getOverlayableList().fromJson(json.getJSONArray(NAME_overlaybles));
        }
    }
    public void merge(PackageBlock packageBlock){
        if(packageBlock==null||packageBlock==this){
            return;
        }
        if(getId()!=packageBlock.getId()){
            throw new IllegalArgumentException("Can not merge different id packages: "
                    +getId()+"!="+packageBlock.getId());
        }
        setName(packageBlock.getName());
        getLibraryBlock().merge(packageBlock.getLibraryBlock());
        mergeSpecStringPool(packageBlock);
        getSpecTypePairArray().merge(packageBlock.getSpecTypePairArray());
        getOverlayableList().merge(packageBlock.getOverlayableList());
        getStagedAliasList().merge(packageBlock.getStagedAliasList());
    }
    private void mergeSpecStringPool(PackageBlock coming){
        this.getSpecStringPool().addStrings(
                coming.getSpecStringPool().toStringList());
    }

    @Override
    public int compareTo(PackageBlock pkg) {
        return Integer.compare(getId(), pkg.getId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(HexUtil.toHex2((byte) getId()));
        builder.append(", name=");
        builder.append(getName());
        int libCount=getLibraryBlock().getLibraryCount();
        if(libCount>0){
            builder.append(", libraries=");
            builder.append(libCount);
        }
        return builder.toString();
    }
    public static boolean isPackageId(int packageId){
        if(packageId == 0){
            return false;
        }
        return packageId > 0 && packageId <= 0xff;
    }
    public static boolean isResourceId(int resourceId){
        if(resourceId == 0){
            return false;
        }
        return (resourceId & 0x00ff0000) != 0
                && (resourceId & 0xff000000) != 0;
    }

    public static void changePackageId(ValueItem valueItem, int packageIdOld, int packageIdNew){
        if(valueItem instanceof AttributeValue){
            changePackageIdName(packageIdOld, packageIdNew, (AttributeValue)valueItem);
        }
        changePackageIdValue(packageIdOld, packageIdNew, valueItem);
    }
    private static void changePackageIdName(int packageIdOld, int packageIdNew, AttributeValue value){
        int resourceId = value.getNameResourceID();
        if(!isResourceId(resourceId)){
            return;
        }
        int id = (resourceId >> 24) & 0xff;
        if(id != packageIdOld){
            return;
        }
        resourceId = resourceId & 0xffffff;
        id = packageIdNew << 24;
        resourceId = id | resourceId;
        value.setNameResourceID(resourceId);
    }
    private static void changePackageIdValue(int packageIdOld, int packageIdNew, ValueItem valueItem){
        ValueType valueType = valueItem.getValueType();
        if(valueType == null || !valueType.isReference()){
            return;
        }
        int resourceId = valueItem.getData();
        if(!isResourceId(resourceId)){
            return;
        }
        int id = (resourceId >> 24) & 0xff;
        if(id != packageIdOld){
            return;
        }
        resourceId = resourceId & 0xffffff;
        id = packageIdNew << 24;
        resourceId = id | resourceId;
        valueItem.setData(resourceId);
    }

    public static int replacePackageId(int resourceId, int packageIdOld, int packageIdNew){
        if(!isResourceId(resourceId)){
            return resourceId;
        }
        int id = (resourceId >> 24) & 0xff;
        if(id != packageIdOld){
            return resourceId;
        }
        resourceId = resourceId & 0xffffff;
        id = packageIdNew << 24;
        resourceId = id | resourceId;
        return resourceId;
    }
    public static class PublicXmlParser{
        private final PackageBlock packageBlock;
        private boolean mInitializeIds = true;
        private PublicXmlParser(PackageBlock packageBlock){
            this.packageBlock = packageBlock;
        }
        public PublicXmlParser setInitializeIds(boolean initializeId) {
            this.mInitializeIds = initializeId;
            return this;
        }
        public PublicXmlParser parse(XmlPullParser parser) throws IOException, XmlPullParserException {
            parseResourcesAttributes(parser);
            parseElements(parser);
            IOUtil.close(parser);
            return this;
        }
        private boolean isInitializeIds(){
            return mInitializeIds;
        }
        private void parseElements(XmlPullParser parser) throws IOException, XmlPullParserException {
            while (XMLUtil.ensureStartTag(parser) == XmlPullParser.START_TAG){
                XMLElement element = XMLElement.parseElement(parser);
                parsePublicTag(element);
            }
        }
        private void parsePublicTag(XMLElement element) throws XmlEncodeException {
            if(!PackageBlock.TAG_public.equals(element.getName())){
                return;
            }
            String id = element.getAttributeValue(ATTR_id);
            String type = element.getAttributeValue(ATTR_type);
            String name = element.getAttributeValue(ATTR_name);
            if(id == null){
                throw new XmlEncodeException("Missing attribute: '" + ATTR_id + "', "
                        + element.getDebugText());
            }
            if(type == null){
                throw new XmlEncodeException("Missing attribute: '" + ATTR_type + "', "
                        + element.getDebugText());
            }
            if(name == null){
                throw new XmlEncodeException("Missing attribute: '" + ATTR_name + "', "
                        + element.getDebugText());
            }
            EncodeResult encodeResult = ValueCoder.encodeHexOrInteger(id.trim());
            if(encodeResult == null){
                throw new XmlEncodeException("Invalid id value: " + element.getDebugText());
            }
            int resourceId = encodeResult.value;
            int packageId = (resourceId >> 24) & 0xff;
            int i = packageBlock.getId();
            if(i == 0){
                packageBlock.setId(packageId);
            }else if(i != packageId){
                return;
            }
            int typeId = (resourceId >> 16) & 0xff;
            if(typeId == 0){
                throw new XmlEncodeException("Type id is zero: '" + id + "', "
                        + element.getDebugText());
            }
            TypeString typeString = packageBlock.getOrCreateTypeString(typeId, type);
            typeId = typeString.getId();
            TypeBlock typeBlock = packageBlock.getOrCreateTypeBlock((byte) typeId, "");
            int entryId = resourceId & 0xffff;
            Entry entry = typeBlock.getOrCreateEntry((short) entryId);
            entry.setName(name, true);
            if(isInitializeIds() && typeBlock.isTypeId()){
                entry.setValueAsBoolean(false);
                ValueHeader header = entry.getHeader();
                header.setPublic(true);
                header.setWeak(true);
            }
        }
        private void parseResourcesAttributes(XmlPullParser parser) throws IOException, XmlPullParserException {
            int event = parser.getEventType();
            boolean documentStarted = false;
            if(event == XmlPullParser.START_DOCUMENT){
                documentStarted = true;
                parser.next();
            }
            event = XMLUtil.ensureStartTag(parser);
            if(event != XmlPullParser.START_TAG){
                throw new XmlEncodeException("Expecting xml state START_TAG but found: "
                        + XMLUtil.toEventName(parser.getEventType()));
            }
            if(PackageBlock.TAG_resources.equals(parser.getName())){
                XMLElement element = new XMLElement(PackageBlock.TAG_resources);
                element.parseAttributes(parser);
                parseResourcesAttributes(element);
                parser.next();
            }else if(documentStarted){
                throw new XmlEncodeException("Expecting <resources> tag but found: " + parser.getName());
            }
        }
        private void parseResourcesAttributes(XMLElement element) throws IOException {
            String packageName = element.getAttributeValue(PackageBlock.ATTR_package);
            if(!StringsUtil.isWhiteSpace(packageName)){
                packageBlock.setName(packageName);
            }
            String id = element.getAttributeValue(PackageBlock.ATTR_id);
            if(!StringsUtil.isWhiteSpace(id) && packageBlock.getId() == 0){
                EncodeResult encodeResult = ValueCoder
                        .encodeHexOrInteger(id);
                if(encodeResult == null || !PackageBlock.isPackageId(encodeResult.value)){
                    throw new XmlEncodeException("Invalid id value: '" + element.getDebugText() + "'");
                }
                packageBlock.setId(encodeResult.value);
            }
        }
    }

    public static final String NAME_package_id = "package_id";
    public static final String NAME_package_name = "package_name";
    private static final String NAME_specs = "specs";
    public static final String NAME_libraries = "libraries";
    public static final String NAME_staged_aliases = "staged_aliases";
    public static final String NAME_overlaybles = "overlaybles";

    public static final String JSON_FILE_NAME = "package.json";
    public static final String DIRECTORY_NAME_PREFIX = "package_";
    public static final String RES_DIRECTORY_NAME = "res";
    public static final String VALUES_DIRECTORY_NAME = "values";

    public static final String PUBLIC_XML = "public.xml";

    public static final String TAG_public = "public";
    public static final String TAG_resources = "resources";
    public static final String ATTR_package = "package";
    public static final String ATTR_id = "id";
    public static final String ATTR_type = "type";
    public static final String ATTR_name = "name";

}
