package com.reandroid.lib.apk;


import com.reandroid.lib.arsc.chunk.PackageBlock;
import com.reandroid.lib.arsc.chunk.TableBlock;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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

    public static class Table{
        public final Map<Byte, Package> packageMap;
        public Table(){
            this.packageMap = new HashMap<>();
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
        public JSONObject toJson(){
            JSONObject jsonObject=new JSONObject();
            JSONArray jsonArray=new JSONArray();
            for(Package pkg: packageMap.values()){
                jsonArray.put(pkg.toJson());
            }
            jsonObject.put("packages", jsonArray);
            return jsonObject;
        }
        public static Table fromJson(JSONObject jsonObject){
            Table table=new Table();
            JSONArray jsonArray= jsonObject.optJSONArray("packages");
            if(jsonArray!=null){
                int length= jsonArray.length();
                for(int i=0;i<length;i++){
                    table.add(Package.fromJson(jsonArray.getJSONObject(i)));
                }
            }
            return table;
        }
        public static class Package {
            public final byte id;
            public String name;
            public final Map<Byte, Type> typeMap;
            public Package(byte id){
                this.id = id;
                this.typeMap = new HashMap<>();
            }
            public void merge(Package pkg){
                if(pkg==this||pkg==null){
                    return;
                }
                if(pkg.id!=this.id){
                    throw new IllegalArgumentException("Different package id: "+this.id+"!="+pkg.id);
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
                    throw new IllegalArgumentException("Different package id: "+entry);
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

            public static class Type{
                public final byte id;
                public String name;
                public Package mPackage;
                public final Map<Short, Entry> entryMap;
                public Type(byte id){
                    this.id = id;
                    this.entryMap = new HashMap<>();
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
                        throw new IllegalArgumentException("Different type ids: "+id+"!="+type.id);
                    }
                    if(type.name!=null){
                        this.name=type.name;
                    }
                    for(Entry entry:type.entryMap.values()){
                        Short entryId=entry.getEntryId();
                        Entry existEntry=this.entryMap.get(entryId);
                        if(existEntry != null && Objects.equals(existEntry.name, entry.name)){
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
                        throw new IllegalArgumentException("Different type id: "+entry);
                    }
                    short key=entry.getEntryId();
                    Entry exist=entryMap.get(key);
                    if(exist!=null){
                        if(Objects.equals(exist.name, entry.name)){
                            return;
                        }
                        throw new IllegalArgumentException("Duplicate entry exist: "+exist+", entry: "+entry);
                    }
                    if(name == null){
                        this.name = entry.typeName;
                    }
                    entry.type=this;
                    entryMap.put(key, entry);
                }

                public JSONObject toJson(){
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("id", id);
                    jsonObject.put("name", name);
                    JSONArray jsonArray=new JSONArray();
                    for(Entry entry: entryMap.values()){
                        jsonArray.put(entry.toJson());
                    }
                    jsonObject.put("entries", jsonArray);
                    return jsonObject;
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
                    if(name !=null){
                        builder.append(" ").append(name);
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
                    public Type type;
                    public Entry(int resourceId, String typeName, String name){
                        this.resourceId = resourceId;
                        this.typeName = typeName;
                        this.name = name;
                    }
                    public Entry(int resourceId, String name){
                        this(resourceId, null, name);
                    }
                    public String getTypeName(){
                        if(this.type!=null){
                            return this.type.name;
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
                        return Integer.compare(getResourceId(), entry.getResourceId());
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
                        jsonObject.put("name", name);
                        return jsonObject;
                    }
                    public String toXml(){
                        StringBuilder builder=new StringBuilder();
                        builder.append("<public");
                        builder.append(" id=\"").append(getHexId()).append("\"");
                        String tn=getTypeName();
                        if(tn !=null){
                            builder.append(" type=\"").append(tn).append("\"");
                        }
                        if(name!=null){
                            builder.append(" name=\"").append(name).append("\"");
                        }
                        builder.append("/>");
                        return builder.toString();
                    }
                    @Override
                    public String toString(){
                        return toJson().toString();
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
                            String attr=matcher.group("Attr").toLowerCase();
                            String value=matcher.group("Value");
                            element=matcher.group("Next");
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
                            throw new IllegalArgumentException("Missing id: "+xmlElement);
                        }
                        if(name==null){
                            throw new IllegalArgumentException("Missing name: "+xmlElement);
                        }
                        return new Entry(id, type, name);
                    }
                    private static final Pattern PATTERN=Pattern.compile("^\\s*(?<Attr>[^\\s=\"]+)\\s*=\\s*\"(?<Value>[^\"]+)\"(?<Next>.*)$");
                }
            }

        }
    }

    public static final String JSON_FILE_NAME ="resource-ids.json";
}
