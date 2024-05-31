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
package com.reandroid.graph;

import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.archive.InputSource;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClass;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.sections.SectionType;
import com.reandroid.utils.StringsUtil;
import com.reandroid.utils.io.IOUtil;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class RequiredClassesScanner extends BaseApkModuleProcessor {

    private final Set<TypeKey> requiredTypes;

    private boolean lookInStrings = true;

    public RequiredClassesScanner(ApkModule apkModule, DexClassRepository classRepository) {
        super(apkModule, classRepository);
        this.requiredTypes = new HashSet<>();
    }

    public void setLookInStrings(boolean lookInStrings) {
        this.lookInStrings = lookInStrings;
    }
    public void scan() {
        verbose("Scanning required classes ...");
        scanOnXml(getApkModule().getAndroidManifest());
        scanOnServicesMeta();
        scanOnStrings();
        scanOnResFiles();
        scanHavingNativeMethods();
    }
    public void keepClasses(Predicate<? super TypeKey> filter) {
        if(filter == null) {
            return;
        }
        Iterator<DexClass> iterator = getClassRepository().getDexClasses(filter);
        while (iterator.hasNext()) {
            addType(iterator.next().getKey());
        }
    }
    public Set<TypeKey> getResults() {
        return requiredTypes;
    }

    private void scanHavingNativeMethods() {
        if(keptAll()) {
            return;
        }
        if(isVerboseEnabled()) {
            verbose("Searching required classes on native methods ...");
        }
        Iterator<DexClass> iterator = getClassRepository().getDexClasses(
                typeKey -> !requiredTypes.contains(typeKey));
        while (iterator.hasNext()) {
            DexClass dexClass = iterator.next();
            if(dexClass.usesNative()) {
                addUsed(dexClass);
            }
        }
    }
    private void scanOnServicesMeta() {
        if(keptAll()) {
            return;
        }
        if(isVerboseEnabled()) {
            verbose("Searching required classes META-INF/services/ ...");
        }
        Iterator<InputSource> iterator = getZipEntryMap()
                .withinDirectory("META-INF/services/");
        while (iterator.hasNext()) {
            scanOnServicesMeta(iterator.next());
        }
    }
    private void scanOnServicesMeta(InputSource inputSource) {
        String content;
        try {
            content = IOUtil.readUtf8(inputSource.openStream());
        } catch (IOException exception) {
            warn("Failed to process '" + inputSource.getAlias() + "', error = "
                    + exception.getMessage());
            return;
        }
        String[] lines = StringsUtil.split(content, '\n', true);
        for(String line : lines) {
            line = line.trim();
            addType(TypeKey.parse(line));
        }
    }
    private void scanOnResFiles() {
        if(keptAll()) {
            return;
        }
        List<ResFile> resFileList = getApkModule().listResFiles();
        if(isVerboseEnabled()) {
            verbose("Searching required classes on res files: " + resFileList.size());
        }
        for(ResFile resFile : resFileList) {
            scanOnXml(resFile.getResXmlDocument());
        }
    }
    private void scanOnXml(ResXmlDocument resXmlDocument) {
        if(keptAll()) {
            return;
        }
        if(resXmlDocument == null) {
            return;
        }
        Iterator<String> iterator = resXmlDocument.getStringPool().getStrings();
        while (iterator.hasNext()) {
            addType(TypeKey.parse(iterator.next()));
        }
    }
    private void scanOnStrings() {
        if(!this.lookInStrings) {
            return;
        }
        scanOnDexStrings();
    }
    private void scanOnDexStrings() {
        if(keptAll()) {
            return;
        }
        verbose("Searching on dex strings ...");
        int usage = UsageMarker.USAGE_INSTRUCTION | UsageMarker.USAGE_STATIC_VALUES;
        DexClassRepository repository = getClassRepository();
        Iterator<StringId> iterator = repository.getItems(SectionType.STRING_ID);
        while (iterator.hasNext()) {
            StringId stringId = iterator.next();
            if(stringId.containsUsage(usage) ) {
                scanOnSourceString(stringId.getString());
            }
        }
    }
    private void scanOnSourceString(String sourceString) {
        if(isValidSourceType(sourceString)) {
            if(addType(TypeKey.parse(sourceString))) {
                if(isVerboseEnabled()) {
                    verbose("Kept: " + sourceString);
                }
            }
        }
    }
    private boolean addType(TypeKey typeKey) {
        if(typeKey == null) {
            return false;
        }
        return addUsed(getClassRepository().getDexClass(typeKey));
    }
    private boolean addUsed(DexClass dexClass) {
        if(dexClass == null) {
            return false;
        }
        TypeKey typeKey = dexClass.getKey();
        Set<TypeKey> requiredTypes = this.requiredTypes;
        if(!requiredTypes.add(typeKey)) {
            return false;
        }
        Set<DexClass> requiredSet = dexClass.getRequired();
        for(DexClass dex : requiredSet) {
            requiredTypes.add(dex.getKey());
        }
        return true;
    }

    private boolean keptAll() {
        return requiredTypes.size() == getClassRepository().getDexClassesCount();
    }
    public void reset() {
        this.requiredTypes.clear();
    }

    private boolean isValidSourceType(String type) {
        if(StringsUtil.isEmpty(type)) {
            return false;
        }
        int length = type.length();
        if(length < 3 || type.indexOf('.') < 0) {
            return false;
        }
        String[] splits = StringsUtil.split(type, '.', false);
        for(String s : splits) {
            if(!isValidSimpleName(s)){
                return false;
            }
        }
        return splits.length > 1;
    }
    private boolean isValidSimpleName(String name) {
        int length = name.length();
        for(int i = 0; i < length; i++) {
            if(!isValidSimpleName(name.charAt(i))){
                return false;
            }
        }
        return length != 0;
    }
    private boolean isValidSimpleName(char ch) {
        switch (ch) {
            case ' ':
            case '\n':
            case '\r':
            case '\t':
            case '(':
            case ')':
            case '<':
            case '>':
            case ',':
            case '/':
            case '\\':
            case '!':
            case '@':
            case '#':
            case '%':
            case '^':
            case '&':
            case '*':
            case '+':
            case '=':
            case '|':
            case '\'':
            case '"':
            case ';':
            case ':':
                return false;
            default:
                return true;
        }
    }
}
