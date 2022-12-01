package com.reandroid.lib.arsc.chunk.xml;

import com.reandroid.lib.arsc.array.ResXmlIDArray;
import com.reandroid.lib.arsc.array.StringArray;
import com.reandroid.lib.arsc.item.ResXmlID;
import com.reandroid.lib.arsc.item.ResXmlString;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;

import java.util.*;

public class ResIdBuilder implements Comparator<Integer> {
    private final Map<Integer, String> mIdNameMap;
    public ResIdBuilder(){
        this.mIdNameMap=new HashMap<>();
    }
    public void buildTo(ResXmlIDMap resXmlIDMap){
        ResXmlStringPool stringPool = resXmlIDMap.getXmlStringPool();
        StringArray<ResXmlString> xmlStringsArray = stringPool.getStringsArray();
        ResXmlIDArray xmlIDArray = resXmlIDMap.getResXmlIDArray();
        List<Integer> idList=getSortedIds();
        int size = idList.size();
        xmlStringsArray.ensureSize(size);
        xmlIDArray.ensureSize(size);
        for(int i=0;i<size;i++){
            ResXmlString xmlString = xmlStringsArray.get(i);
            if(xmlString.getReferencedList().size()>0){
                ResXmlString replaceXmlString=new ResXmlString(xmlString.isUtf8(), xmlString.get());
                xmlStringsArray.setItem(i, replaceXmlString);
                xmlStringsArray.add(xmlString);
                xmlString=replaceXmlString;
            }
            ResXmlID xmlID = xmlIDArray.get(i);
            if(xmlID.getReferencedList().size()>0){
                ResXmlID replaceXmlId = new ResXmlID(xmlID.get());
                xmlIDArray.setItem(i, replaceXmlId);
                xmlIDArray.add(xmlID);
                xmlID=replaceXmlId;
            }
            int resourceId = idList.get(i);
            String name = mIdNameMap.get(resourceId);
            xmlID.set(resourceId);
            xmlString.set(name);
        }
    }
    public void add(int id, String name){
        if(id==0){
            return;
        }
        if(name==null){
            name="";
        }
        mIdNameMap.put(id, name);
    }
    private List<Integer> getSortedIds(){
        List<Integer> results=new ArrayList<>(mIdNameMap.keySet());
        results.sort(this);
        return results;
    }
    @Override
    public int compare(Integer i1, Integer i2) {
        return i1.compareTo(i2);
    }
}
