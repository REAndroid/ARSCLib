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
package com.reandroid.arsc.pool.builder;

import com.reandroid.arsc.array.StringArray;
import com.reandroid.arsc.array.StyleArray;
import com.reandroid.arsc.item.StyleItem;
import com.reandroid.arsc.item.TableString;
import com.reandroid.arsc.model.StyleSpanInfo;
import com.reandroid.arsc.pool.TableStringPool;

import java.util.*;

public class StringPoolMerger implements Comparator<String> {
    private final Set<TableStringPool> mPools;
    private int mMergedPools;
    private int mMergedStrings;
    private int mMergedStyleStrings;
    public StringPoolMerger(){
        this.mPools=new HashSet<>();
    }
    public void mergeTo(TableStringPool destination){
        mMergedPools=0;
        mMergedStrings=0;
        mMergedStyleStrings=0;
        if(destination.countStrings()>0 || destination.countStyles()>0){
            throw new IllegalArgumentException("Destination string pool is not empty");
        }
        mergeStyledStrings(destination);
        mergeNonStyledStrings(destination);
        mMergedPools = mPools.size();
        mPools.clear();
        destination.refresh();
    }
    public void add(TableStringPool stringPool){
        mPools.add(stringPool);
    }
    public int getMergedPools() {
        return mMergedPools;
    }
    public int getMergedStyleStrings() {
        return mMergedStyleStrings;
    }
    public int getMergedStrings() {
        return mMergedStrings;
    }

    private void mergeStyledStrings(TableStringPool destination){
        List<TableString> styledStrings = getStyledStrings();
        Map<String, TableString> mapTableStrings =
                destination.insertStrings(toStringList(styledStrings));
        Map<String, TableString> mapTags =
                destination.insertStrings(listStyleTags(styledStrings));

        StyleArray styleArray = destination.getStyleArray();
        styleArray.setChildesCount(styledStrings.size());

        for(TableString tableString:styledStrings){
            TableString createdString = mapTableStrings.get(tableString.get());
            StyleItem createdStyle = styleArray.get(createdString.getIndex());

            StyleItem styleItem = tableString.getStyle();
            for(StyleSpanInfo spanInfo:styleItem.getSpanInfoList()){
                if(spanInfo!=null && createdStyle!=null){
                    int tagReference = mapTags.get(spanInfo.getTag())
                            .getIndex();
                    createdStyle.addStylePiece(
                            tagReference,
                            spanInfo.getFirst(),
                            spanInfo.getLast());
                }
            }
        }
        mMergedStyleStrings=styledStrings.size();
    }
    private void mergeNonStyledStrings(TableStringPool destination){
        List<String> nonStyledStrings=getNonStyledStrings();
        destination.insertStrings(nonStyledStrings);
        mMergedStrings=nonStyledStrings.size();
    }
    private List<TableString> getStyledStrings(){
        Map<String, TableString> mapUniqueHtml = new HashMap<>();
        for(TableStringPool pool:mPools){
            int styleCount = pool.countStyles();
            StringArray<TableString> stringArray = pool.getStringsArray();
            for(int i=0;i<styleCount;i++){
                TableString tableString = stringArray.get(i);
                if(tableString==null || !tableString.hasStyle()){
                    continue;
                }
                mapUniqueHtml.put(tableString.getHtml(), tableString);
            }
        }
        return new ArrayList<>(mapUniqueHtml.values());
    }
    private List<String> getNonStyledStrings(){
        Set<String> uniqueSet = new HashSet<>();
        for(TableStringPool pool:mPools){
            TableString[] tableStrings = pool.getStrings();
            if(tableStrings==null){
                continue;
            }
            for(int i=0;i<tableStrings.length;i++){
                TableString tableString=tableStrings[i];
                if(tableString==null || tableString.hasStyle()){
                    continue;
                }
                uniqueSet.add(tableString.get());
            }
        }
        List<String> results=new ArrayList<>(uniqueSet);
        results.sort(this);
        return results;
    }
    private List<String> toStringList(Collection<TableString> tableStringList){
        List<String> results=new ArrayList<>(tableStringList.size());
        for(TableString tableString:tableStringList){
            String str=tableString.get();
            if(str!=null){
                results.add(str);
            }
        }
        results.sort(this);
        return results;
    }
    private List<String> listStyleTags(List<TableString> styledStrings){
        Set<String> resultSet=new HashSet<>();
        for(TableString tableString:styledStrings){
            StyleItem style = tableString.getStyle();
            if(style==null){
                continue;
            }
            for(StyleSpanInfo spanInfo:style.getSpanInfoList()){
                if(spanInfo!=null){
                    resultSet.add(spanInfo.getTag());
                }
            }
        }
        List<String> results=new ArrayList<>(resultSet);
        results.sort(this);
        return results;
    }
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
}
