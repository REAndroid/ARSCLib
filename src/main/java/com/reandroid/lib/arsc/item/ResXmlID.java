package com.reandroid.lib.arsc.item;

import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.arsc.pool.ResXmlStringPool;
import com.reandroid.lib.json.JsonItem;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ResXmlID extends IntegerItem implements JsonItem<JSONObject> {
    private final List<ReferenceItem> mReferencedList;
    public ResXmlID(int resId){
        super(resId);
        this.mReferencedList=new ArrayList<>();
    }
    public ResXmlID(){
        this(0);
    }
    public boolean removeReference(ReferenceItem ref){
        return mReferencedList.remove(ref);
    }
    public boolean removeAllReference(Collection<ReferenceItem> referenceItems){
        return mReferencedList.removeAll(referenceItems);
    }
    public void removeAllReference(){
        mReferencedList.clear();
    }
    public List<ReferenceItem> getReferencedList(){
        return mReferencedList;
    }
    public void addReference(ReferenceItem ref){
        if(ref!=null){
            mReferencedList.add(ref);
        }
    }
    public void addReference(Collection<ReferenceItem> refList){
        if(refList==null){
            return;
        }
        for(ReferenceItem ref:refList){
            addReference(ref);
        }
    }
    private void reUpdateReferences(int newIndex){
        for(ReferenceItem ref:mReferencedList){
            ref.set(newIndex);
        }
    }
    @Override
    public void onIndexChanged(int oldIndex, int newIndex){
        reUpdateReferences(newIndex);
    }

    private ResXmlStringPool getXmlStringPool(){
        Block parent=this;
        while (parent!=null){
            if(parent instanceof ResXmlBlock){
                return ((ResXmlBlock)parent).getStringPool();
            }
            parent=parent.getParent();
        }
        return null;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id", get());
        jsonObject.put("name", getXmlStringPool().get(getIndex()).getHtml());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        //TODO
        throw new IllegalArgumentException("Not implemented yet");
    }
    @Override
    public String toString(){
        return getIndex()+": "+String.format("0x%08x", get());
    }
}
