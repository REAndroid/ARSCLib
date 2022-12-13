package com.reandroid.lib.arsc.array;

import com.reandroid.lib.arsc.base.BlockArray;
import com.reandroid.lib.arsc.chunk.TypeBlock;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResConfig;
import com.reandroid.lib.json.JSONConvert;
import com.reandroid.lib.json.JSONArray;
import com.reandroid.lib.json.JSONObject;

import java.util.*;

public class SpecTypePairArray extends BlockArray<SpecTypePair>
        implements JSONConvert<JSONArray>, Comparator<SpecTypePair> {
    public SpecTypePairArray(){
        super();
    }

    public void sort(){
        for(SpecTypePair specTypePair:listItems()){
            specTypePair.sortTypes();
        }
        sort(this);
    }
    public void removeEmptyPairs(){
        List<SpecTypePair> allPairs=new ArrayList<>(listItems());
        boolean foundEmpty=false;
        for(SpecTypePair typePair:allPairs){
            typePair.removeEmptyTypeBlocks();
            if(typePair.isEmpty()){
                super.remove(typePair, false);
                foundEmpty=true;
            }
        }
        if(foundEmpty){
            trimNullBlocks();
        }
    }
    public boolean isEmpty(){
        Iterator<SpecTypePair> iterator=iterator(true);
        while (iterator.hasNext()){
            SpecTypePair pair=iterator.next();
            if(!pair.isEmpty()){
                return false;
            }
        }
        return true;
    }
    public EntryBlock getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getOrCreateTypeBlock(typeId, qualifiers);
        return typeBlock.getOrCreateEntry(entryId);
    }
    public EntryBlock getEntry(byte typeId, short entryId, String qualifiers){
        TypeBlock typeBlock=getTypeBlock(typeId, qualifiers);
        if(typeBlock==null){
            return null;
        }
        return typeBlock.getEntry(entryId);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getOrCreateTypeBlock(qualifiers);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        SpecTypePair pair=getPair(typeId);
        if(pair==null){
            return null;
        }
        return pair.getTypeBlock(qualifiers);
    }
    public TypeBlock getOrCreate(byte typeId, ResConfig resConfig){
        SpecTypePair pair=getOrCreate(typeId);
        return pair.getTypeBlockArray().getOrCreate(resConfig);
    }
    public SpecTypePair getOrCreate(byte typeId){
        SpecTypePair pair=getPair(typeId);
        if(pair!=null){
            return pair;
        }
        pair=createNext();
        pair.setTypeId(typeId);
        return pair;
    }
    public SpecTypePair getPair(byte typeId){
        SpecTypePair[] items=getChildes();
        if(items==null){
            return null;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            SpecTypePair pair=items[i];
            if(pair==null){
                continue;
            }
            if(pair.getTypeId()==typeId){
                return pair;
            }
        }
        return null;
    }
    public byte getTypeId(){
        SpecTypePair[] items=getChildes();
        if(items==null){
            return 0;
        }
        int max=items.length;
        for(int i=0;i<max;i++){
            SpecTypePair pair=items[i];
            if(pair!=null){
                return pair.getTypeId();
            }
        }
        return 0;
    }
    @Override
    public SpecTypePair newInstance() {
        SpecTypePair pair=new SpecTypePair();
        pair.setTypeId(getTypeId());
        return pair;
    }
    @Override
    public SpecTypePair[] newInstance(int len) {
        return new SpecTypePair[len];
    }
    @Override
    protected void onRefreshed() {

    }
    @Override
    protected void onPreRefreshRefresh(){
        validateEntryCounts();
    }
    // For android API < 26, it is required to have equal entry count on all SpecTypePair
    private void validateEntryCounts(){
        Map<Byte, Integer> entryCountMap=mapHighestEntryCount();
        for(Map.Entry<Byte, Integer> entry:entryCountMap.entrySet()){
            byte id=entry.getKey();
            int count=entry.getValue();
            SpecTypePair pair=getPair(id);
            pair.getSpecBlock().setEntryCount(count);
            pair.getTypeBlockArray().setEntryCount(count);
        }
    }
    private Map<Byte, Integer> mapHighestEntryCount(){
        Map<Byte, Integer> results=new HashMap<>();
        SpecTypePair[] childes=getChildes();
        for (SpecTypePair pair:childes){
            int count=pair.getHighestEntryCount();
            byte id=pair.getTypeId();
            Integer exist=results.get(id);
            if(exist==null || count>exist){
                results.put(id, count);
            }
        }
        return results;
    }
    public byte getSmallestTypeId(){
        SpecTypePair[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        boolean firstFound=false;
        for (int i=0;i<childes.length;i++){
            SpecTypePair pair=childes[i];
            if(pair==null){
                continue;
            }
            int id=pair.getTypeId();
            if(!firstFound){
                result=id;
            }
            firstFound=true;
            if(id<result){
                result=id;
            }
        }
        return (byte) result;
    }
    public byte getHighestTypeId(){
        SpecTypePair[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        boolean firstFound=false;
        for (int i=0;i<childes.length;i++){
            SpecTypePair pair=childes[i];
            if(pair==null){
                continue;
            }
            int id=pair.getTypeId();
            if(!firstFound){
                result=id;
            }
            firstFound=true;
            if(id<result){
                result=id;
            }
        }
        return (byte) result;
    }
    @Override
    public JSONArray toJson() {
        JSONArray jsonArray=new JSONArray();
        int i=0;
        for(SpecTypePair specTypePair:listItems()){
            JSONObject jsonObject= specTypePair.toJson();
            if(jsonObject==null){
                continue;
            }
            jsonArray.put(i, jsonObject);
            i++;
        }
        return jsonArray;
    }
    @Override
    public void fromJson(JSONArray json) {
        int length= json.length();
        clearChildes();
        ensureSize(length);
        for (int i=0;i<length;i++){
            JSONObject jsonObject=json.getJSONObject(i);
            SpecTypePair specTypePair=get(i);
            specTypePair.fromJson(jsonObject);
        }
    }
    @Override
    public int compare(SpecTypePair typePair1, SpecTypePair typePair2) {
        return typePair1.compareTo(typePair2);
    }
}
