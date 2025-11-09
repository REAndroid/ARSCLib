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

import com.reandroid.dex.common.DexUtils;
import com.reandroid.dex.dalvik.DalvikSignature;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.DalvikSignatureKey;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.KeyPair;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.key.TypeKeyReference;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.dex.smali.SmaliDirective;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RenameTypes extends Rename<TypeKey, TypeKey> {

    private final Set<String> renamedStrings;
    private final Map<String, String> stringMap;

    private int arrayDepth;
    private boolean renameSourceClassName;
    private boolean skipSourceRenameRootPackageClass;
    private boolean fixAccessibility;
    private boolean fixInnerSimpleName;
    private boolean fixSourceFileName;
    private boolean renameInnerClasses;

    private boolean mChanged;

    public RenameTypes() {
        super();
        this.renamedStrings = new HashSet<>();
        this.stringMap = new HashMap<>();
        this.arrayDepth = DEFAULT_ARRAY_DEPTH;
        this.renameSourceClassName = true;
        this.skipSourceRenameRootPackageClass = true;
        this.fixAccessibility = true;
        this.fixInnerSimpleName = true;
        this.fixSourceFileName = false;
        this.renameInnerClasses = false;

        this.mChanged = true;
    }

    public void addPackage(DexClassRepository classRepository, String search, String replace, boolean includeSubPackages) {
        if (ObjectsUtil.equals(search, replace)) {
            return;
        }
        validatePackageName(search);
        validatePackageName(replace);
        Iterator<TypeId> iterator = classRepository.getItemsIfKey(SectionType.TYPE_ID,
                key -> ((TypeKey) key).isPackage(search, includeSubPackages));

        List<KeyPair<TypeKey, TypeKey>> keyPairList = new ArrayCollection<>();

        while (iterator.hasNext()) {
            TypeKey typeSearch = iterator.next().getKey();
            TypeKey typeReplace = typeSearch.renamePackage(search, replace);
            KeyPair<TypeKey, TypeKey> keyPair = new KeyPair<>(typeSearch, typeReplace);
            keyPairList.add(keyPair);
            if (classRepository.containsClass(typeReplace)) {
                lockAll(keyPairList);
                keyPairList.clear();
                break;
            }
        }

        addAll(keyPairList);
    }
    private void validatePackageName(String packageName) {
        if (StringsUtil.isEmpty(packageName)) {
            throw new IllegalArgumentException("Empty package name: " + packageName);
        }
        if (packageName.charAt(0) != 'L') {
            throw new IllegalArgumentException(
                    "Package name should start with 'L': " + packageName);
        }
        int i = packageName.length() - 1;
        if (i != 0 && packageName.charAt(i) != '/') {
            throw new IllegalArgumentException(
                    "Non root package name should end with '/': " + packageName);
        }
    }
    @Override
    public void add(DexClassRepository classRepository, KeyPair<TypeKey, TypeKey> keyPair) {
        if (validateAndAdd(classRepository, keyPair)) {
            if (renameInnerClasses) {
                TypeKey search = keyPair.getFirst();
                TypeKey replace = keyPair.getSecond();
                Iterator<TypeId> iterator = classRepository.getItemsIfKey(SectionType.TYPE_ID,
                        key -> search.isOuterOf(key.getDeclaring()));
                String replacePrefix = replace.getTypeName().replace(';', '$');
                int prefixLength = search.getTypeName().length();
                while (iterator.hasNext()) {
                    TypeKey typeSearch = iterator.next()
                            .getKey().getDeclaring();
                    TypeKey typeReplace = TypeKey.create(
                            replacePrefix + typeSearch.getTypeName().substring(prefixLength));
                    validateAndAdd(classRepository, new KeyPair<>(typeSearch, typeReplace));
                }
            }
        }
    }
    private boolean validateAndAdd(DexClassRepository classRepository, KeyPair<TypeKey, TypeKey> keyPair) {
        if (keyPair == null || !keyPair.isValid()) {
            return false;
        }
        KeyPair<TypeKey, TypeKey> existing = get(keyPair.getFirst());
        if (existing != null && existing.equalsBoth(keyPair)) {
            return true;
        }
        if (classRepository.containsClass(keyPair.getSecond())) {
            lock(keyPair);
            return false;
        } else {
            add(keyPair);
            return true;
        }
    }


    @Override
    public int apply(DexClassRepository classRepository) {
        if (isEmpty()) {
            return 0;
        }
        this.renamedStrings.clear();
        buildRenameMap();
        if (stringMap.isEmpty()) {
            return 0;
        }
        renameStringIds(classRepository);
        renameAnnotationSignatures(classRepository);
        renameExternalTypeKeyReferences(classRepository);
        int size = renamedStrings.size();
        if (size != 0) {
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
        while (iterator.hasNext()) {
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
        for (TypeKeyReference reference : referenceList) {
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
        if (replace == null) {
            replace = map.get(typeKey.getSourceName());
        }
        TypeKey replaceKey = TypeKey.parse(replace);
        if (replaceKey != null) {
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
        boolean fixSourceFileName = this.fixSourceFileName;

        if (!fixAccessibility && !fixInnerSimpleName && !fixSourceFileName) {
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
            if (fixSourceFileName) {
                TypeKey typeKey = dexClass.getKey();
                if (isSimpleNameChanged(typeKey)) {
                    dexClass.setSourceFile(DexUtils.toSourceFileName(typeKey.getTypeName()));
                }
            }
        }
    }
    private boolean isSimpleNameChanged(TypeKey typeKey) {
        KeyPair<TypeKey, TypeKey> keyPair = getFlipped(typeKey);
        if (keyPair == null) {
            return false;
        }
        return !keyPair.getFirst().getSimpleName().equals(
                keyPair.getSecond().getSimpleName());
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
    public void setRenameSourceClassName(boolean renameSourceClassName) {
        this.renameSourceClassName = renameSourceClassName;
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
    public void setRenameInnerClasses(boolean renameInnerClasses) {
        this.renameInnerClasses = renameInnerClasses;
    }
    public void setFixSourceFileName(boolean fixSourceFileName) {
        this.fixSourceFileName = fixSourceFileName;
    }

    private void buildRenameMap() {
        if (!mChanged) {
            return;
        }
        mChanged = false;
        List<KeyPair<TypeKey, TypeKey>> list = toList();
        boolean renameSource = this.renameSourceClassName;
        boolean skipSourceRenameRootPackageClass = this.skipSourceRenameRootPackageClass;

        int size = list.size();

        Map<String, String> map = this.stringMap;

        int arrayDepth = this.arrayDepth + 1;

        for (int i = 0; i < size; i++) {

            KeyPair<TypeKey, TypeKey> keyPair = list.get(i);
            TypeKey first = keyPair.getFirst();
            TypeKey second = keyPair.getSecond();

            String name1 = first.getTypeName();
            String name2 = second.getTypeName();
            map.put(name1, name2);

            for (int j = 1; j < arrayDepth; j++) {
                name1 = first.getArrayType(j);
                name2 = second.getArrayType(j);
                map.put(name1, name2);
            }
            if (renameSource) {
                name1 = first.getTypeName();
                if (!skipSourceRenameRootPackageClass || name1.indexOf('/') > 0) {
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

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.CLASS;
    }

    public static final int DEFAULT_ARRAY_DEPTH = ObjectsUtil.of(3);
}
