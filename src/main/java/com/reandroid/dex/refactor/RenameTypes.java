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
package com.reandroid.dex.refactor;

import com.reandroid.dex.dalvik.DalvikSignature;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.DalvikSignatureKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeKeyReference;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.*;

public class RenameTypes extends Rename<TypeKey, TypeKey>{

    private final Set<String> renamedStrings;
    private final Map<String, String> stringMap;

    private int arrayDepth;
    private boolean renameSource;
    private boolean skipSourceRenameRootPackageClass;
    private boolean fixAccessibility;
    private boolean fixInnerSimpleName;

    private boolean mChanged;

    public RenameTypes() {
        super();
        this.renamedStrings = new HashSet<>();
        this.stringMap = new HashMap<>();
        this.arrayDepth = DEFAULT_ARRAY_DEPTH;
        this.renameSource = true;
        this.skipSourceRenameRootPackageClass = true;
        this.fixAccessibility = true;
        this.fixInnerSimpleName = true;

        this.mChanged = true;
    }

    @Override
    public int apply(DexClassRepository classRepository) {
        this.renamedStrings.clear();
        buildRenameMap();
        if (stringMap.isEmpty()) {
            return 0;
        }
        renameStringIds(classRepository);
        renameAnnotationSignatures(classRepository);
        renameExternalTypeKeyReferences(classRepository);
        int size = renamedStrings.size();
        if(size != 0) {
            classRepository.clearPoolMap();
        }
        applyFix(classRepository);
        return size;
    }
    private void renameStringIds(DexClassRepository classRepository) {
        Map<String, String> map = this.stringMap;
        if (map.isEmpty()) {
            return;
        }
        Iterator<StringId> iterator = classRepository.getClonedItems(SectionType.STRING_ID);
        while (iterator.hasNext()){
            StringId stringId = iterator.next();
            String text = map.get(stringId.getString());
            if (text != null) {
                setString(stringId, text);
            }
        }
    }
    private void renameAnnotationSignatures(DexClassRepository classRepository) {

        Iterator<DalvikSignature> iterator = ComputeIterator.of(
                classRepository.getItems(SectionType.ANNOTATION_ITEM),
                annotationItem -> DalvikSignature.of(annotationItem.asAnnotated()));

        while (iterator.hasNext()) {
            DalvikSignature dalvikSignature = iterator.next();
            DalvikSignatureKey signatureKey = dalvikSignature.getSignature();
            if (signatureKey == null) {
                // unlikely
                continue;
            }
            DalvikSignatureKey update = replaceInKey(signatureKey);
            if (signatureKey != update) {
                dalvikSignature.setSignature(update);
            }
        }
    }

    private void renameExternalTypeKeyReferences(DexClassRepository classRepository) {
        List<TypeKeyReference> referenceList = classRepository.getExternalTypeKeyReferenceList();
        for(TypeKeyReference reference : referenceList) {
            renameExternalTypeKeyReference(reference);
        }
    }
    private void renameExternalTypeKeyReference(TypeKeyReference reference) {
        TypeKey typeKey = reference.getTypeKey();
        if (typeKey == null) {
            return;
        }
        Map<String, String> map = this.stringMap;
        String replace = map.get(typeKey.getTypeName());
        if(replace == null) {
            replace = map.get(typeKey.getSourceName());
        }
        TypeKey replaceKey = TypeKey.parse(replace);
        if(replaceKey != null) {
            reference.setTypeKey(replaceKey);
            renamedStrings.add(replace);
        }
    }
    private void applyFix(DexClassRepository classRepository) {
        Set<String> renamedSet = this.renamedStrings;
        if (renamedSet.isEmpty()) {
            return;
        }

        boolean fixAccessibility = this.fixAccessibility;
        boolean fixInnerSimpleName = this.fixInnerSimpleName;

        if (!fixAccessibility && !fixInnerSimpleName) {
            return;
        }

        Iterator<DexClass> iterator = classRepository.getDexClasses(
                typeKey -> renamedSet.contains(typeKey.getTypeName()));

        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            if (fixAccessibility) {
                dexClass.fixAccessibility();
            }
            if (fixInnerSimpleName) {
                dexClass.fixDalvikInnerClassName();
            }
        }
    }
    private void setString(StringId stringId, String value) {
        stringId.setString(value);
        renamedStrings.add(value);
    }

    public void setArrayDepth(int arrayDepth) {
        if (arrayDepth < 0) {
            arrayDepth = DEFAULT_ARRAY_DEPTH;
        }
        this.arrayDepth = arrayDepth;
    }
    public void setRenameSource(boolean renameSource) {
        this.renameSource = renameSource;
    }
    public void setSkipSourceRenameRootPackageClass(boolean skipSourceRenameRootPackageClass) {
        this.skipSourceRenameRootPackageClass = skipSourceRenameRootPackageClass;
    }
    public void setFixAccessibility(boolean fixAccessibility) {
        this.fixAccessibility = fixAccessibility;
    }
    public void setFixInnerSimpleName(boolean fixInnerSimpleName) {
        this.fixInnerSimpleName = fixInnerSimpleName;
    }

    private void buildRenameMap() {
        if (!mChanged) {
            return;
        }
        mChanged = false;
        List<KeyPair<TypeKey, TypeKey>> list = toList();
        boolean renameSource = this.renameSource;
        boolean skipSourceRenameRootPackageClass = this.skipSourceRenameRootPackageClass;

        int size = list.size();

        Map<String, String> map = this.stringMap;

        int arrayDepth = this.arrayDepth + 1;

        for(int i = 0; i < size; i++){

            KeyPair<TypeKey, TypeKey> keyPair = list.get(i);
            TypeKey first = keyPair.getFirst();
            TypeKey second = keyPair.getSecond();

            String name1 = first.getTypeName();
            String name2 = second.getTypeName();
            map.put(name1, name2);

            for(int j = 1; j < arrayDepth; j++){
                name1 = first.getArrayType(j);
                name2 = second.getArrayType(j);
                map.put(name1, name2);
            }
            if(renameSource){
                name1 = first.getTypeName();
                if(!skipSourceRenameRootPackageClass || name1.indexOf('/') > 0){
                    name1 = first.getSourceName();
                    name2 = second.getSourceName();
                    map.put(name1, name2);
                }
            }
        }
    }

    @Override
    public TypeKey getReplace(Key search) {
        TypeKey result = null;
        if (search instanceof TypeKey) {
            result = super.getReplace(search);
            if (result == null) {
                String replace = stringMap.get(search.toString());
                if (replace != null) {
                    result = TypeKey.create(replace);
                }
            }
        }
        return result;
    }

    @Override
    public void close() {
        super.close();
        stringMap.clear();
        renamedStrings.clear();
        mChanged = true;
    }
    @Override
    protected void onChanged() {
        super.onChanged();
        mChanged = true;
    }

    @Override
    public List<KeyPair<TypeKey, TypeKey>> toList() {
        return super.toList(CompareUtil.getInverseComparator());
    }

    public static final int DEFAULT_ARRAY_DEPTH = ObjectsUtil.of(3);
}
