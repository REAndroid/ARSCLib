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
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeKeyReference;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.*;

public class RenameTypes extends Rename<TypeKey, TypeKey>{

    private int arrayDepth;
    private boolean renameSource;
    private boolean skipSourceRenameRootPackageClass;
    private boolean fixAccessibility;
    private boolean fixInnerSimpleName;
    private Set<String> renamedStrings;

    public RenameTypes() {
        super();
        this.arrayDepth = DEFAULT_ARRAY_DEPTH;
        this.renameSource = true;
        this.skipSourceRenameRootPackageClass = true;
        this.fixAccessibility = true;
        this.fixInnerSimpleName = true;
        this.renamedStrings = new HashSet<>();
    }

    @Override
    public int apply(DexClassRepository classRepository) {
        Map<String, String> map = buildRenameMap();
        this.renamedStrings = new HashSet<>(map.size());
        renameStringIds(classRepository, map);
        renameAnnotationSignatures(classRepository, map);
        renameExternalTypeKeyReferences(classRepository, map);
        int size = renamedStrings.size();
        if(size != 0) {
            classRepository.clearPoolMap();
        }
        applyFix(classRepository);
        renamedStrings.clear();
        renamedStrings = null;
        return size;
    }
    private void renameStringIds(DexClassRepository classRepository, Map<String, String> map) {
        Iterator<StringId> iterator = classRepository.getClonedItems(SectionType.STRING_ID);
        while (iterator.hasNext()){
            StringId stringId = iterator.next();
            String text = map.get(stringId.getString());
            if (text != null) {
                setString(stringId, text);
            }
        }
    }
    private void renameAnnotationSignatures(DexClassRepository classRepository, Map<String, String> map) {
        Set<String> renamedStrings = this.renamedStrings;

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
            DalvikSignatureKey update = signatureKey;
            Iterator<TypeKey> types = CollectionUtil.copyOfUniqueOf(signatureKey.getTypes());
            while (types.hasNext()) {
                TypeKey search = types.next();
                String replace = map.get(search.getTypeName());
                if (replace == null) {
                    continue;
                }
                update = update.replaceKey(search, TypeKey.create(replace));
                renamedStrings.add(replace);
            }
            if (signatureKey != update) {
                dalvikSignature.setSignature(update);
            }
        }
    }

    private void renameExternalTypeKeyReferences(DexClassRepository classRepository, Map<String, String> map) {
        List<TypeKeyReference> referenceList = classRepository.getExternalTypeKeyReferenceList();
        for(TypeKeyReference reference : referenceList) {
            renameExternalTypeKeyReference(reference, map);
        }
    }
    private void renameExternalTypeKeyReference(TypeKeyReference reference, Map<String, String> map) {
        TypeKey typeKey = reference.getTypeKey();
        if(typeKey == null) {
            return;
        }
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
        if (renamedSet == null || renamedSet.isEmpty()) {
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
        if(arrayDepth < 0){
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

    private Map<String, String> buildRenameMap() {
        List<KeyPair<TypeKey, TypeKey>> list = toList();
        boolean renameSource = this.renameSource;
        boolean skipSourceFileForRootPackageClass = this.skipSourceRenameRootPackageClass;

        int estimatedSize = 1;
        if(renameSource) {
            estimatedSize = estimatedSize + 1;
        }
        if(arrayDepth > 0){
            estimatedSize = estimatedSize + arrayDepth + 1;
        }
        int size = list.size();

        estimatedSize = size * estimatedSize;

        Map<String, String> map = new HashMap<>(estimatedSize);

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
                if(!skipSourceFileForRootPackageClass || name1.indexOf('/') > 0){
                    name1 = first.getSourceName();
                    name2 = second.getSourceName();
                    map.put(name1, name2);
                }
            }
        }
        return map;
    }

    @Override
    public List<KeyPair<TypeKey, TypeKey>> toList() {
        return super.toList(CompareUtil.getInverseComparator());
    }

    public static final int DEFAULT_ARRAY_DEPTH = ObjectsUtil.of(3);
}
