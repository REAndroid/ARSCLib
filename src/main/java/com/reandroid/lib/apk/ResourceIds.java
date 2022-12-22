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
package com.reandroid.lib.apk;

import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.pool.SpecStringPool;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private void loadPackageBlock(PackageBlock packageBlock){
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
    public void writeXml(Writer writer) throws IOException {
        mTable.writeXml(writer);
    }
    public void fromXml(File file) throws IOException {
        mTable.fromXml(file);
    }
    public void fromXml(InputStream inputStream) throws IOException {
        mTable.fromXml(inputStream);
    }
    public void fromXml(Reader reader) throws IOException {
        mTable.fromXml(reader);
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
        public void add(Package.Type.Entry entry){
            if(entry==null){
                return;
            }
            byte pkgId=entry.getPackageId();
            Package pkg = packageMap.get(pkgId);
            if(pkg==null){
                pkg=new Package(pkgId);
                packageMap.put(pkgId, pkg);
            }
            pkg.add(entry);
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
        public void writeXml(File file) throws IOException {
            File dir=file.getParentFile();
            if(dir!=null && !dir.exists()){
                dir.mkdirs();
            }
            FileOutputStream outputStream=new FileOutputStream(file);
            writeXml(outputStream);
        }
        public void writeXml(OutputStream outputStream) throws IOException {
            OutputStreamWriter writer=new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            writeXml(writer);
            outputStream.close();
        }
        public void writeXml(Writer writer) throws IOException {
            writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
            writer.write("\n<resources>");
            for(Package pkg:listPackages()){
                pkg.writeXml(writer);
            }
            writer.write("\n</resources>");
            writer.flush();
        }
        public void fromXml(File file) throws IOException {
            FileInputStream inputStream=new FileInputStream(file);
            fromXml(inputStream);
        }
        public void fromXml(InputStream inputStream) throws IOException {
            InputStreamReader reader=new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            fromXml(reader);
            inputStream.close();
        }
        public void fromXml(Reader reader) throws IOException {
            BufferedReader bufferedReader;
            if(reader instanceof BufferedReader){
                bufferedReader=(BufferedReader) reader;
            }else {
                bufferedReader=new BufferedReader(reader);
            }
            String line;
            while ((line=bufferedReader.readLine())!=null){
                add(Package.Type.Entry.fromXml(line));
            }
            bufferedReader.close();
        }
        @Override
        public int compare(Package pkg1, Package pkg2) {
            return pkg1.compareTo(pkg2);
        }

        public static class Package implements Comparable<Package>, Comparator<Package.Type>{
            public final byte id;
            public String name;
            public final Map<Byte, Type> typeMap;
            public Package(byte id){
                this.id = id;
                this.typeMap = new HashMap<>();
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
                return String.format("0x%02x", id);
            }
            public JSONObject toJson(){
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("id", this.id);
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
                return Integer.compare(id, pkg.id);
            }
            @Override
            public int compare(Type t1, Type t2) {
                return t1.compareTo(t2);
            }
            public void writeXml(Writer writer) throws IOException {
                writer.write("\n");
                if(this.name!=null){
                    writer.write("     <!-- packageName=\"");
                    writer.write(this.name);
                    writer.write("\" -->");
                }
                for(Type type:listTypes()){
                    type.writeXml(writer);
                }
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
                    return String.format("0x%02x", id);
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
                        throw new DuplicateException("Duplicate entry exist: "+exist+", entry: "+entry);
                    }
                    if(getName() == null){
                        this.name = entry.getTypeName();
                    }
                    entry.type=this;
                    entryMap.put(key, entry);
                }

                public JSONObject toJson(){
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("id", getId());
                    jsonObject.put("name", getName());
                    JSONArray jsonArray=new JSONArray();
                    for(Entry entry: entryMap.values()){
                        jsonArray.put(entry.toJson());
                    }
                    jsonObject.put("entries", jsonArray);
                    return jsonObject;
                }
                public void writeXml(Writer writer) throws IOException {
                    for(Entry entry:listEntries()){
                        writer.write("\n     ");
                        entry.writeXml(writer);
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
                @Override
                public int compareTo(Type type) {
                    return Integer.compare(id, type.id);
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
                    public int getResourceId(){
                        return ((getPackageId() & 0xff)<<24)
                                | ((getTypeId() & 0xff)<<16)
                                | (getEntryId() & 0xffff);
                    }
                    public String getHexId(){
                        return String.format("0x%08x", getResourceId());
                    }
                    @Override
                    public int compareTo(Entry entry) {
                        return Integer.compare(getEntryId(), entry.getEntryId());
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
                    public String toXml(){
                        StringWriter writer=new StringWriter();
                        try {
                            writeXml(writer);
                            writer.flush();
                            writer.close();
                        } catch (IOException ignored) {
                        }
                        return writer.toString();
                    }
                    public void writeXml(Writer writer) throws IOException {
                        writer.write("<public");
                        writer.write(" id=\"");
                        writer.write(getHexId());
                        writer.write("\"");
                        String tn=getTypeName();
                        if(tn !=null){
                            writer.append(" type=\"");
                            writer.append(tn);
                            writer.append("\"");
                        }
                        String n=getName();
                        if(n!=null){
                            writer.write(" name=\"");
                            writer.append(n);
                            writer.append("\"");
                        }
                        writer.append("/>");
                    }
                    @Override
                    public String toString(){
                        return toXml();
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
                    public static Entry fromXml(String xmlElement){
                        String element=xmlElement;
                        element=element.replaceAll("[\n\r\t]+", " ");
                        element=element.trim();
                        String start="<public ";
                        if(!element.startsWith(start) || !element.endsWith(">")){
                            return null;
                        }
                        element=element.substring(start.length()).trim();
                        Pattern pattern=PATTERN;
                        int id=0;
                        String type=null;
                        String name=null;
                        Matcher matcher=pattern.matcher(element);
                        while (matcher.find()){
                            String attr=matcher.group(1).toLowerCase();
                            String value=matcher.group(2);
                            element=matcher.group(3);
                            if(attr.equals("id")){
                                id=Integer.decode(value);
                            }else if(attr.equals("name")){
                                name=value;
                            }else if(attr.equals("type")){
                                type=value;
                            }
                            matcher= pattern.matcher(element);
                        }
                        if(id==0){
                            throw new DuplicateException("Missing id: "+xmlElement);
                        }
                        if(name==null){
                            throw new DuplicateException("Missing name: "+xmlElement);
                        }
                        return new Entry(id, type, name);
                    }
                    private static final Pattern PATTERN=Pattern.compile("^\\s*([^\\s=\"]+)\\s*=\\s*\"([^\"]+)\"(.*)$");
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
