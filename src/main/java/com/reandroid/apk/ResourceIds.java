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
package com.reandroid.apk;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.util.HexUtil;
import com.reandroid.arsc.util.ResNameMap;
import com.reandroid.json.JSONArray;
import com.reandroid.json.JSONObject;
import com.reandroid.xml.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**Use {@link com.reandroid.identifiers.TableIdentifier} */
@Deprecated
 public class ResourceIds {
    private final Table mTable;
    public ResourceIds(Table table){
        this.mTable=table;
    }
    public ResourceIds(){
        this(new Table());
    }
    public Table getTable(){
        return mTable;
    }
    public int applyTo(TableBlock tableBlock){
        return mTable.applyTo(tableBlock);
    }
    public void fromJson(JSONObject jsonObject){
        mTable.fromJson(jsonObject);
    }
    public JSONObject toJson(){
        return mTable.toJson();
    }
    public void loadTableBlock(TableBlock tableBlock){
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            loadPackageBlock(packageBlock);
        }
    }
    public void loadPackageBlock(PackageBlock packageBlock){
        Collection<EntryGroup> entryGroupList = packageBlock.listEntryGroup();
        String name= packageBlock.getName();
        for(EntryGroup entryGroup:entryGroupList){
            Table.Package.Type.Entry entry= Table.Package.Type.Entry.fromEntryGroup(entryGroup);
            mTable.add(entry);
            if(name==null){
                continue;
            }
            Table.Package.Type type=entry.type;
            if(type!=null && type.mPackage!=null){
                type.mPackage.name=name;
                name=null;
            }
        }
    }

    public void writeXml(File file) throws IOException {
        mTable.writeXml(file);
    }
    public void writeXml(OutputStream outputStream) throws IOException {
        mTable.writeXml(outputStream);
    }
    public void fromXml(File file) throws IOException {
        mTable.fromXml(file);
    }
    public void fromXml(InputStream inputStream) throws IOException {
        mTable.fromXml(inputStream);
    }
    public void fromXml(XMLDocument xmlDocument) throws IOException {
        mTable.fromXml(xmlDocument);
    }

    public XMLDocument toXMLDocument(){
        return mTable.toXMLDocument();
    }

    public static class Table implements Comparator<Table.Package>{
        public final Map<Byte, Package> packageMap;
        public Table(){
            this.packageMap = new HashMap<>();
        }
        public int applyTo(TableBlock tableBlock){
            int renameCount=0;
            for(PackageBlock packageBlock : tableBlock.listPackages()){
                Package pkg=getPackage((byte) packageBlock.getId());
                if(pkg!=null){
                    renameCount+=pkg.applyTo(packageBlock);
                }
            }
            if(renameCount>0){
                tableBlock.refresh();
            }
            return renameCount;
        }
        public void add(Package pkg){
            Package exist=this.packageMap.get(pkg.id);
            if(exist!=null){
                exist.merge(pkg);
                return;
            }
            this.packageMap.put(pkg.id, pkg);
        }
        public Package add(Package.Type.Entry entry){
            if(entry==null){
                return null;
            }
            byte pkgId=entry.getPackageId();
            Package pkg = packageMap.get(pkgId);
            if(pkg==null){
                pkg=new Package(pkgId);
                packageMap.put(pkgId, pkg);
            }
            pkg.add(entry);
            return pkg;
        }
        public Package.Type.Entry getEntry(int resourceId){
            byte packageId = (byte) ((resourceId>>24) & 0xff);
            byte typeId = (byte) ((resourceId>>16) & 0xff);
            short entryId = (short) (resourceId & 0xff);
            Package pkg = getPackage(packageId);
            if(pkg == null){
                return null;
            }
            return getEntry(packageId, typeId, entryId);
        }
        public Package getPackage(byte packageId){
            return packageMap.get(packageId);
        }
        public Package.Type getType(byte packageId, byte typeId){
            Package pkg=getPackage(packageId);
            if(pkg==null){
                return null;
            }
            return pkg.getType(typeId);
        }
        public Package.Type.Entry getEntry(byte packageId, byte typeId, short entryId){
            Package pkg=getPackage(packageId);
            if(pkg==null){
                return null;
            }
            return pkg.getEntry(typeId, entryId);
        }
        public List<Package> listPackages(){
            List<Package> results=new ArrayList<>(packageMap.values());
            results.sort(this);
            return results;
        }
        public List<Package.Type.Entry> listEntries(){
            List<Package.Type.Entry> results=new ArrayList<>(countEntries());
            for(Package pkg:packageMap.values()){
                results.addAll(pkg.listEntries());
            }
            return results;
        }
        int countEntries(){
            int result=0;
            for(Package pkg:packageMap.values()){
                result+=pkg.countEntries();
            }
            return result;
        }
        public JSONObject toJson(){
            JSONObject jsonObject=new JSONObject();
            JSONArray jsonArray=new JSONArray();
            for(Package pkg: packageMap.values()){
                jsonArray.put(pkg.toJson());
            }
            jsonObject.put("packages", jsonArray);
            return jsonObject;
        }
        public void fromJson(JSONObject jsonObject){
            JSONArray jsonArray= jsonObject.optJSONArray("packages");
            if(jsonArray!=null){
                int length= jsonArray.length();
                for(int i=0;i<length;i++){
                    this.add(Package.fromJson(jsonArray.getJSONObject(i)));
                }
            }
        }
        public XMLDocument toXMLDocument(){
            XMLDocument xmlDocument = new XMLDocument();
            XMLElement documentElement = new XMLElement("resources");
            for(Package pkg:listPackages()){
                pkg.toXMLElements(documentElement);
            }
            xmlDocument.setDocumentElement(documentElement);
            return xmlDocument;
        }
        public void writeXml(File file) throws IOException {
            File dir=file.getParentFile();
            if(dir!=null && !dir.exists()){
                dir.mkdirs();
            }
            FileOutputStream outputStream=new FileOutputStream(file);
            writeXml(outputStream);
        }
        public void writeXml(OutputStream outputStream) throws IOException {
            String indent = "     ";
            writeXml(indent, outputStream);
        }
        public void writeXml(String indent, OutputStream outputStream) throws IOException {
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writeXml(indent, writer);
            writer.close();
            outputStream.close();
        }
        public void writeXml(String indent, Writer writer) throws IOException{
            writeBegin(writer);
            for(Package pkg:listPackages()){
                pkg.writeXml(indent, writer);
            }
            writeEnd(writer);
            writer.flush();
        }
        private void writeBegin(Writer writer) throws IOException{
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writer.write('\n');
            writer.write("<resources>");
        }
        private void writeEnd(Writer writer) throws IOException{
            writer.write('\n');
            writer.write("</resources>");
        }
        public void fromXml(File file) throws IOException {
            FileInputStream inputStream=new FileInputStream(file);
            fromXml(inputStream);
            inputStream.close();
        }
        public void fromXml(InputStream inputStream) throws IOException {
            try {
                fromXml(XMLDocument.load(inputStream));
            } catch (XMLException ex) {
                throw new IOException(ex.getMessage(), ex);
            }
        }
        public void fromXml(XMLDocument xmlDocument) {
            XMLElement documentElement = xmlDocument.getDocumentElement();
            int count=documentElement.getChildesCount();
            for(int i=0;i<count;i++){
                XMLElement element=documentElement.getChildAt(i);
                Package pkg = add(Package.Type.Entry.fromXml(element));
                pkg.setPackageName(element.getCommentAt(0));
            }
        }
        @Override
        public int compare(Package pkg1, Package pkg2) {
            return pkg1.compareTo(pkg2);
        }

        public static class Package implements Comparable<Package>, Comparator<Package.Type>{
            public final byte id;
            public String name;
            public final Map<Byte, Type> typeMap;
            private final ResNameMap<Integer> mEntryNameMap;
            public Package(byte id){
                this.id = id;
                this.typeMap = new HashMap<>();
                this.mEntryNameMap = new ResNameMap<>();
            }
            public void loadEntryMap(){
                mEntryNameMap.clear();
                for(Type type:typeMap.values()){
                    String typeName=type.getName();
                    for(Type.Entry entry: type.entryMap.values()){
                        mEntryNameMap.add(typeName, entry.getName(), entry.getResourceId());
                    }
                }
            }
            public Integer getResourceId(String typeName, String name){
                return mEntryNameMap.get(typeName, name);
            }
            public Type.Entry getEntry(String typeName, String name){
                Type type=getType(typeName);
                if(type==null){
                    return null;
                }
                return type.getEntry(name);
            }
            private Type getType(String typeName){
                for(Type type:typeMap.values()){
                    if(type.getName().equals(typeName)){
                        return type;
                    }
                }
                return null;
            }
            public int getIdInt(){
                return 0xff & id;
            }
            public int applyTo(PackageBlock packageBlock){
                int renameCount=0;
                Map<Integer, EntryGroup> map = packageBlock.getEntriesGroupMap();
                for(Map.Entry<Integer, EntryGroup> entry:map.entrySet()){
                    byte typeId=Table.toTypeId(entry.getKey());
                    Type type=typeMap.get(typeId);
                    if(type==null){
                        continue;
                    }
                    EntryGroup entryGroup=entry.getValue();
                    if(type.applyTo(entryGroup)){
                        renameCount++;
                    }
                }
                if(renameCount>0){
                    cleanSpecStringPool(packageBlock);
                }
                return renameCount;
            }
            private void cleanSpecStringPool(PackageBlock packageBlock){
                SpecStringPool specStringPool = packageBlock.getSpecStringPool();
                specStringPool.refreshUniqueIdMap();
                specStringPool.removeUnusedStrings();
                packageBlock.refresh();
            }
            public void merge(Package pkg){
                if(pkg==this||pkg==null){
                    return;
                }
                if(pkg.id!=this.id){
                    throw new DuplicateException("Different package id: "+this.id+"!="+pkg.id);
                }
                if(pkg.name!=null){
                    this.name = pkg.name;
                }
                for(Type type:pkg.typeMap.values()){
                    add(type);
                }
            }
            public Type getType(byte typeId){
                return typeMap.get(typeId);
            }
            public void add(Type type){
                Byte typeId= type.id;;
                Type exist=this.typeMap.get(typeId);
                if(exist!=null){
                    exist.merge(type);
                    return;
                }
                type.mPackage=this;
                this.typeMap.put(typeId, type);
            }
            public Package.Type.Entry getEntry(byte typeId, short entryId){
                Package.Type type=getType(typeId);
                if(type==null){
                    return null;
                }
                return type.getEntry(entryId);
            }
            public void add(Type.Entry entry){
                if(entry==null){
                    return;
                }
                if(entry.getPackageId()!=this.id){
                    throw new DuplicateException("Different package id: "+entry);
                }
                byte typeId=entry.getTypeId();
                Type type=typeMap.get(typeId);
                if(type==null){
                    type=new Type(typeId);
                    type.mPackage=this;
                    typeMap.put(typeId, type);
                }
                type.add(entry);
            }
            public String getHexId(){
                return HexUtil.toHex2(id);
            }
            public JSONObject toJson(){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("id", this.getIdInt());
                if(this.name!=null){
                    jsonObject.put("name", this.name);
                }
                JSONArray jsonArray=new JSONArray();
                for(Type type:typeMap.values()){
                    jsonArray.put(type.toJson());
                }
                jsonObject.put("types", jsonArray);
                return jsonObject;
            }
            @Override
            public int compareTo(Package pkg) {
                return Integer.compare(getIdInt(), pkg.getIdInt());
            }
            @Override
            public int compare(Type t1, Type t2) {
                return t1.compareTo(t2);
            }
            public void toXMLElements(XMLElement documentElement){
                int count = documentElement.getChildesCount();
                for(Type type:listTypes()){
                    type.toXMLElements(documentElement);
                }
                XMLElement firstElement = documentElement.getChildAt(count);
                if(firstElement!=null){
                    XMLComment comment = new XMLComment(
                            "packageName=\""+this.name+"\"");
                    firstElement.addComment(comment);
                }
            }
            void setPackageName(XMLComment xmlComment){
                if(xmlComment==null){
                    return;
                }
                String pkgName = xmlComment.getCommentText();
                if(pkgName==null || !pkgName.contains("packageName")){
                    return;
                }
                int i = pkgName.indexOf('"');
                if(i>0){
                    i++;
                    pkgName=pkgName.substring(i);
                }else {
                    return;
                }
                i = pkgName.indexOf('"');
                if(i>0){
                    pkgName=pkgName.substring(0, i);
                }else {
                    return;
                }
                this.name=pkgName.trim();
            }
            public List<Package.Type.Entry> listEntries(){
                List<Package.Type.Entry> results=new ArrayList<>(countEntries());
                for(Package.Type type:typeMap.values()){
                    results.addAll(type.listEntries());
                }
                return results;
            }
            public List<Type> listTypes(){
                List<Type> results=new ArrayList<>(typeMap.values());
                results.sort(this);
                return results;
            }
            int countEntries(){
                int results=0;
                for(Type type:typeMap.values()){
                    results+=type.countEntries();
                }
                return results;
            }
            public void writeXml(String indent, Writer writer) throws IOException{
                writeComment(indent, writer);
                for(Type type:listTypes()){
                    type.writeXml(indent, writer);
                }
            }
            private void writeComment(String indent, Writer writer) throws IOException{
                String name = this.name;
                if(name == null){
                    return;
                }
                writer.write('\n');
                writer.write(indent);
                writer.write("<!--packageName=\"");
                writer.write(name);
                writer.write("\"-->");
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Package aPackage = (Package) o;
                return id == aPackage.id;
            }
            @Override
            public int hashCode() {
                return Objects.hash(id);
            }
            @Override
            public String toString(){
                return getHexId() + ", types=" + typeMap.size();
            }

            public static Package fromJson(JSONObject jsonObject){
                Package pkg=new Package((byte) jsonObject.getInt("id"));
                pkg.name = jsonObject.optString("name", null);
                JSONArray jsonArray = jsonObject.optJSONArray("types");
                int length = jsonArray.length();
                for(int i=0;i<length;i++){
                    Type type=Type.fromJson(jsonArray.getJSONObject(i));
                    pkg.add(type);
                }
                return pkg;
            }

            public static class Type implements Comparable<Type>, Comparator<Type.Entry>{
                public final byte id;
                public String name;
                public String nameAlias;
                public Package mPackage;
                public final Map<Short, Entry> entryMap;
                public Type(byte id){
                    this.id = id;
                    this.entryMap = new HashMap<>();
                }
                public Entry getEntry(String entryName){
                    for(Entry entry:entryMap.values()){
                        if(entry.getName().equals(entryName)){
                            return entry;
                        }
                    }
                    return null;
                }
                public int getIdInt(){
                    return 0xff & id;
                }
                public boolean applyTo(EntryGroup entryGroup){
                    boolean renamed=false;
                    Entry entry=entryMap.get(entryGroup.getEntryId());
                    if(entry!=null){
                        if(entry.applyTo(entryGroup)){
                            renamed=true;
                        }
                    }
                    return renamed;
                }
                public String getName() {
                    if(nameAlias!=null){
                        return nameAlias;
                    }
                    return name;
                }

                public byte getId(){
                    return id;
                }
                public byte getPackageId(){
                    if(mPackage!=null){
                        return mPackage.id;
                    }
                    return 0;
                }
                public void merge(Type type){
                    if(type==this||type==null){
                        return;
                    }
                    if(this.id!= type.id){
                        throw new DuplicateException("Different type ids: "+id+"!="+type.id);
                    }
                    String n=type.getName();
                    if(n!=null){
                        this.name=n;
                    }
                    for(Entry entry:type.entryMap.values()){
                        Short entryId=entry.getEntryId();
                        Entry existEntry=this.entryMap.get(entryId);
                        if(existEntry != null && Objects.equals(existEntry.getName(), entry.getName())){
                            continue;
                        }
                        this.entryMap.remove(entryId);
                        entry.type=this;
                        this.entryMap.put(entryId, entry);
                    }
                }
                public Entry getEntry(short entryId){
                    return entryMap.get(entryId);
                }
                public String getHexId(){
                    return HexUtil.toHex2(id);
                }
                public void add(Entry entry){
                    if(entry==null){
                        return;
                    }
                    if(entry.getTypeId()!=this.id){
                        throw new DuplicateException("Different type id: "+entry);
                    }
                    short key=entry.getEntryId();
                    Entry exist=entryMap.get(key);
                    if(exist!=null){
                        if(Objects.equals(exist.getName(), entry.getName())){
                            return;
                        }
                        /* Developer may have a reason adding duplicate
                           resource ids , lets ignore rather than throw
                         */
                        // throw new DuplicateException("Duplicate entry exist: "+exist+", entry: "+entry);
                        return;
                    }
                    if(getName() == null){
                        this.name = entry.getTypeName();
                    }
                    entry.type=this;
                    entryMap.put(key, entry);
                }

                public JSONObject toJson(){
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("id", getIdInt());
                    jsonObject.put("name", getName());
                    JSONArray jsonArray=new JSONArray();
                    for(Entry entry: entryMap.values()){
                        jsonArray.put(entry.toJson());
                    }
                    jsonObject.put("entries", jsonArray);
                    return jsonObject;
                }
                public void toXMLElements(XMLElement documentElement){
                    for(Entry entry:listEntries()){
                        documentElement.addChild(entry.toXMLElement());
                    }
                }
                public List<Entry> listEntries(){
                    List<Entry> results=new ArrayList<>(entryMap.values());
                    results.sort(this);
                    return results;
                }
                int countEntries(){
                    return entryMap.size();
                }
                public void writeXml(String indent, Writer writer) throws IOException{
                    for(Entry entry:listEntries()){
                        entry.writeXml(indent, writer);
                    }
                }
                @Override
                public int compareTo(Type type) {
                    return Integer.compare(getIdInt(), type.getIdInt());
                }
                @Override
                public int compare(Entry entry1, Entry entry2) {
                    return entry1.compareTo(entry2);
                }
                @Override
                public boolean equals(Object o) {
                    if (this == o) return true;
                    if (o == null || getClass() != o.getClass()) return false;
                    Type that = (Type) o;
                    return id == that.id;
                }
                @Override
                public int hashCode() {
                    return Objects.hash(id);
                }
                @Override
                public String toString(){
                    StringBuilder builder=new StringBuilder();
                    builder.append(getHexId());
                    String n=getName();
                    if(n !=null){
                        builder.append(" ").append(n);
                    }
                    builder.append(", entries=").append(entryMap.size());
                    return builder.toString();
                }

                public static Type fromJson(JSONObject jsonObject){
                    Type type = new Type((byte) jsonObject.getInt("id"));
                    type.name = jsonObject.optString("name", null);
                    JSONArray jsonArray = jsonObject.optJSONArray("entries");
                    if(jsonArray!=null){
                        int length=jsonArray.length();
                        for(int i=0;i<length;i++){
                            Entry entry=Entry.fromJson(jsonArray.getJSONObject(i));
                            type.add(entry);
                        }
                    }
                    return type;
                }

                public static class Entry implements Comparable<Entry>{
                    public int resourceId;
                    public String typeName;
                    public String name;
                    public String nameAlias;
                    public Type type;
                    public Entry(int resourceId, String typeName, String name){
                        this.resourceId = resourceId;
                        this.typeName = typeName;
                        this.name = name;
                    }
                    public Entry(int resourceId, String name){
                        this(resourceId, null, name);
                    }
                    public boolean applyTo(EntryGroup entryGroup){
                        return entryGroup.renameSpec(this.getName());
                    }
                    public String getName() {
                        if(nameAlias!=null){
                            return nameAlias;
                        }
                        return name;
                    }
                    public String getTypeName(){
                        if(this.type!=null){
                            return this.type.getName();
                        }
                        return this.typeName;
                    }
                    public byte getPackageId(){
                        if(this.type!=null){
                            Package pkg=this.type.mPackage;
                            if(pkg!=null){
                                return pkg.id;
                            }
                        }
                        return (byte) ((resourceId>>24) & 0xff);
                    }
                    public byte getTypeId(){
                        if(this.type!=null){
                            return this.type.id;
                        }
                        return (byte) ((resourceId>>16) & 0xff);
                    }
                    public short getEntryId(){
                        return (short) (resourceId & 0xffff);
                    }
                    public int getEntryIdInt(){
                        return resourceId & 0xffff;
                    }
                    public int getResourceId(){
                        return ((getPackageId() & 0xff)<<24)
                                | ((getTypeId() & 0xff)<<16)
                                | (getEntryId() & 0xffff);
                    }
                    public String getHexId(){
                        return HexUtil.toHex8(getResourceId());
                    }

                    public void writeXml(String indent, Writer writer) throws IOException{
                        writer.write('\n');
                        writer.write(indent);
                        writer.write("<public id=\"");
                        writer.write(getHexId());
                        writer.write("\" type=\"");
                        String str = getTypeName();
                        if(str != null){
                            writer.write(str);
                        }
                        writer.write("\" name=\"");
                        str = getName();
                        if(str != null){
                            writer.write(str);
                        }
                        writer.write("\"/>");
                    }
                    @Override
                    public int compareTo(Entry entry) {
                        return Integer.compare(getEntryIdInt(), entry.getEntryIdInt());
                    }
                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        Entry that = (Entry) o;
                        return getResourceId() == that.getResourceId();
                    }
                    @Override
                    public int hashCode() {
                        return Objects.hash(getResourceId());
                    }
                    public JSONObject toJson(){
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("id", getResourceId());
                        jsonObject.put("name", getName());
                        return jsonObject;
                    }
                    public XMLElement toXMLElement(){
                        XMLElement element=new XMLElement("public");
                        element.setResourceId(getResourceId());
                        element.addAttribute(new XMLAttribute("id", getHexId()));
                        element.addAttribute(new XMLAttribute("type", getTypeName()));
                        element.addAttribute(new XMLAttribute("name", getName()));
                        return element;
                    }
                    @Override
                    public String toString(){
                        return toXMLElement().toText(false);
                    }
                    public static Entry fromEntryGroup(EntryGroup entryGroup){
                        return new Entry(entryGroup.getResourceId(),
                                entryGroup.getTypeName(),
                                entryGroup.getSpecName());
                    }
                    public static Entry fromJson(JSONObject jsonObject){
                        return new Entry(jsonObject.getInt("id"),
                                jsonObject.optString("type", null),
                                jsonObject.getString("name"));
                    }
                    public static Entry fromXml(XMLElement element){
                        return new Entry(
                                ApkUtil.parseHex(element.getAttributeValue("id")),
                                element.getAttributeValue("type"),
                                element.getAttributeValue("name"));
                    }
                }
            }

        }
        private static short toEntryId(int resourceId){
            int i=resourceId&0xffff;
            return (short) i;
        }
        static byte toTypeId(int resourceId){
            int i=resourceId>>16;
            i=i&0xff;
            return (byte) i;
        }
        static byte toPackageId(int resourceId){
            int i=resourceId>>24;
            i=i&0xff;
            return (byte) i;
        }
        static int toResourceId(byte pkgId, byte typeId, short entryId){
            return (pkgId & 0xff)<<24
                    | (typeId & 0xff)<<16
                    | (entryId & 0xffff);
        }
    }
    public static class DuplicateException extends IllegalArgumentException{
        public DuplicateException(String message){
            super(message);
        }
        public DuplicateException(String message, final Throwable cause) {
            super(message, cause);
        }
        public DuplicateException(Throwable cause) {
            super(cause.getMessage(), cause);
        }
    }

    public static final String JSON_FILE_NAME ="resource-ids.json";
}
