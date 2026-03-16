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
package com.reandroid.apk;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.archive.block.ApkSignatureBlock;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ApkBundle implements Closeable {
    private final Map<String, ApkModule> mModulesMap;
    private AndroidManifestBlockMerger mManifestMerger;
    private APKLogger apkLogger;
    public ApkBundle(){
        this.mModulesMap=new HashMap<>();
        this.mManifestMerger = new AndroidManifestBlockMerger();
    }

    public ApkModule mergeModules() throws IOException {
        return mergeModules(false);
    }
    public ApkModule mergeModules(boolean force) throws IOException {
        List<ApkModule> moduleList=getApkModuleList();
        if(moduleList.size()==0){
            throw new FileNotFoundException("Nothing to merge, empty modules");
        }
        ApkModule result = new ApkModule(generateMergedModuleName(), new ZipEntryMap());
        result.setAPKLogger(apkLogger);
        result.setLoadDefaultFramework(false);

        ApkModule base=getBaseModule();
        if(base == null){
            base = getLargestTableModule();
        }
        result.merge(base, force);
        AndroidManifestBlockMerger manifestMerger = getManifestMerger();
        if (manifestMerger != null) {
            manifestMerger.reset();
            manifestMerger.initializeBase(result.getAndroidManifest());
        }
        ApkSignatureBlock signatureBlock = null;
        for(ApkModule module:moduleList){
            ApkSignatureBlock asb = module.getApkSignatureBlock();
            if(module==base){
                if(asb != null){
                    signatureBlock = asb;
                }
                continue;
            }
            if(signatureBlock == null){
                signatureBlock = asb;
            }
            result.merge(module, force);
            if (manifestMerger != null) {
                manifestMerger.merge(module.getAndroidManifest());
            }
        }
        if (manifestMerger != null) {
            manifestMerger.sanitize(result);
        }

        result.setApkSignatureBlock(signatureBlock);

        // Resolve resource overlays (@null references) from split modules
        resolveResourceOverlays(result, moduleList);

        if(result.hasTableBlock()){
            TableBlock tableBlock=result.getTableBlock();
            tableBlock.sortPackages();
            tableBlock.refresh();
        }
        result.getZipEntryMap().autoSortApkFiles();
        return result;
    }

    public AndroidManifestBlockMerger getManifestMerger() {
        return mManifestMerger;
    }
    public void setManifestMerger(AndroidManifestBlockMerger mManifestMerger) {
        this.mManifestMerger = mManifestMerger;
    }

    private String generateMergedModuleName(){
        Set<String> moduleNames=mModulesMap.keySet();
        String merged="merged";
        int i=1;
        String name=merged;
        while (moduleNames.contains(name)){
            name=merged+"_"+i;
            i++;
        }
        return name;
    }
    private ApkModule getLargestTableModule(){
        ApkModule apkModule=null;
        int chunkSize=0;
        for(ApkModule module:getApkModuleList()){
            if(!module.hasTableBlock()){
                continue;
            }
            TableBlock tableBlock=module.getTableBlock();
            int size=tableBlock.getHeaderBlock().getChunkSize();
            if(apkModule==null || size>chunkSize){
                chunkSize=size;
                apkModule=module;
            }
        }
        return apkModule;
    }
    public ApkModule getBaseModule(){
        for(ApkModule module:getApkModuleList()){
            if(module.isBaseModule()){
                return module;
            }
        }
        return null;
    }
    public List<ApkModule> getApkModuleList(){
        return new ArrayCollection<>(mModulesMap.values());
    }
    public void loadApkDirectory(File dir) throws IOException{
        loadApkDirectory(dir, false);
    }
    public void loadApkDirectory(File dir, boolean recursive) throws IOException {
        if(!dir.isDirectory()){
            throw new FileNotFoundException("No such directory: "+dir);
        }
        List<File> apkList;
        if(recursive){
            apkList = ApkUtil.recursiveFiles(dir, ".apk");
        }else {
            apkList = ApkUtil.listFiles(dir, ".apk");
        }
        if(apkList.size()==0){
            throw new FileNotFoundException("No '*.apk' files in directory: "+dir);
        }
        logMessage("Found apk files: "+apkList.size());
        for(File file:apkList){
            logVerbose("Loading: "+file.getName());
            String name = ApkUtil.toModuleName(file);
            ApkModule module = ApkModule.loadApkFile(file, name);
            module.setAPKLogger(apkLogger);
            addModule(module);
        }
    }
    public void addModule(ApkModule apkModule){
        apkModule.setLoadDefaultFramework(false);
        String name = apkModule.getModuleName();
        mModulesMap.remove(name);
        mModulesMap.put(name, apkModule);
    }
    public boolean containsApkModule(String moduleName){
        return mModulesMap.containsKey(moduleName);
    }
    public ApkModule removeApkModule(String moduleName){
        return mModulesMap.remove(moduleName);
    }
    public ApkModule getApkModule(String moduleName){
        return mModulesMap.get(moduleName);
    }
    public List<String> listModuleNames(){
        return new ArrayList<>(mModulesMap.keySet());
    }
    public int countModules(){
        return mModulesMap.size();
    }
    public Collection<ApkModule> getModules(){
        return mModulesMap.values();
    }
    private boolean hasOneTableBlock(){
        for(ApkModule apkModule:getModules()){
            if(apkModule.hasTableBlock()){
                return true;
            }
        }
        return false;
    }
    @Override
    public void close() throws IOException {
        for(ApkModule module : mModulesMap.values()) {
            module.close();
        }
        mModulesMap.clear();
    }
    public void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    private void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg){
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }

    /**
     * Resolves @null drawable references in merged APK by updating binary XML attributes
     * to point to actual PNG resources from density split modules.
     * 
     * Modern App Bundles use resource overlays where base APK contains XML with @null
     * references and density splits contain the actual PNG files. After merging, the PNGs
     * are present but XMLs still reference @null, causing Resources$NotFoundException at runtime.
     * 
     * This method:
     * 1. Scans drawable XMLs for @null references (type=REFERENCE, data=0)
     * 2. Finds corresponding PNG resources using fuzzy name matching
     * 3. Looks up resource IDs from the resource table
     * 4. Updates XML binary attributes with correct resource IDs
     */
    private void resolveResourceOverlays(ApkModule merged, List<ApkModule> splits) {
        try {
            if (!merged.hasTableBlock()) {
                return;
            }

            TableBlock tableBlock = merged.getTableBlock();
            PackageBlock packageBlock = null;
            Iterator<PackageBlock> packages = tableBlock.getPackages();
            while (packages.hasNext()) {
                PackageBlock pkg = packages.next();
                if (pkg.getName() != null) {
                    packageBlock = pkg;
                    break;
                }
            }
            if (packageBlock == null) {
                return;
            }

            ZipEntryMap mergedZip = merged.getZipEntryMap();
            int resolvedCount = 0;
            int totalNullCount = 0;
            List<String> unresolved = new ArrayList<>();

            // Scan drawable XMLs for @null references
            for (ResFile resFile : merged.listResFiles()) {
                String path = resFile.getFilePath();
                if (!path.startsWith("res/drawable/") || !path.endsWith(".xml")) {
                    continue;
                }

                InputSource source = resFile.getInputSource();
                if (source == null) continue;

                try {
                    ResXmlDocument xmlDoc = new ResXmlDocument();
                    xmlDoc.readBytes(source.openStream());

                    if (hasNullDrawableReferences(xmlDoc)) {
                        String baseName = getBaseName(path);
                        totalNullCount++;

                        // Find corresponding PNG resources
                        List<String> pngPaths = findOverlayPngs(baseName, mergedZip);

                        if (!pngPaths.isEmpty()) {
                            // Extract drawable names and look up resource IDs
                            Map<String, Integer> drawableIds = new HashMap<>();
                            for (String pngPath : pngPaths) {
                                String drawableName = extractDrawableName(pngPath);
                                ResourceEntry entry = packageBlock.getResource("drawable", drawableName);
                                if (entry != null) {
                                    drawableIds.put(drawableName, entry.getResourceId());
                                }
                            }

                            if (!drawableIds.isEmpty()) {
                                // Update XML with resource IDs
                                if (updateNullReferences(xmlDoc, drawableIds, baseName)) {
                                    // Write updated XML back to merged APK
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    xmlDoc.writeBytes(baos);
                                    byte[] newBytes = baos.toByteArray();

                                    mergedZip.remove(path);
                                    mergedZip.add(new ByteInputSource(newBytes, path));
                                    resolvedCount++;
                                }
                            } else {
                                unresolved.add(baseName);
                            }
                        } else {
                            unresolved.add(baseName);
                        }
                    }
                } catch (Exception e) {
                    // Skip files that can't be parsed
                }
            }

            if (totalNullCount > 0) {
                logMessage("Resolved " + resolvedCount + "/" + totalNullCount + " resource overlays");
                if (!unresolved.isEmpty() && unresolved.size() <= 5) {
                    logMessage("Unresolved: " + String.join(", ", unresolved));
                }
            }
        } catch (Exception e) {
            logError("Error resolving resource overlays", e);
        }
    }

    /**
     * Checks if XML document contains @null drawable references.
     * In binary XML, null references are represented as type=REFERENCE with data=0.
     */
    private boolean hasNullDrawableReferences(ResXmlDocument xmlDoc) {
        ResXmlElement root = xmlDoc.getDocumentElement();
        if (root == null) return false;

        Iterator<ResXmlElement> items = root.recursiveElements();
        while (items.hasNext()) {
            ResXmlElement item = items.next();
            if (!"item".equals(item.getName())) continue;

            ResXmlAttribute drawable = item.searchAttributeByName("drawable");
            if (drawable != null) {
                ValueType vtype = drawable.getValueType();
                int data = drawable.getData();
                if (vtype == ValueType.REFERENCE && data == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts base filename without extension.
     * Example: "res/drawable/abc_switch.xml" -> "abc_switch"
     */
    private String getBaseName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.substring(0, name.lastIndexOf('.'));
    }

    /**
     * Extracts drawable name from PNG path.
     * Example: "res/drawable-xxhdpi/abc_btn_switch.9.png" -> "abc_btn_switch"
     */
    private String extractDrawableName(String pngPath) {
        String fileName = pngPath.substring(pngPath.lastIndexOf('/') + 1);
        return fileName.replaceAll("\\.9?\\.png$", "");
    }

    /**
     * Finds PNG resources that match the XML base name using fuzzy keyword matching.
     * Material Design resources often have different naming patterns between XML and PNG.
     * Example: "abc_switch_thumb_material.xml" matches "abc_btn_switch_to_on_mtrl_*.png"
     */
    private List<String> findOverlayPngs(String baseName, ZipEntryMap zip) {
        List<String> pngs = new ArrayList<>();

        // Extract keywords for fuzzy matching (skip short words)
        String[] keywords = baseName.toLowerCase().split("_");

        for (InputSource inputSource : zip.listInputSources()) {
            String path = inputSource.getAlias();
            if (!path.endsWith(".png") || !path.startsWith("res/drawable-")) {
                continue;
            }

            String fileName = path.substring(path.lastIndexOf('/') + 1).toLowerCase();

            // Count keyword matches
            int matches = 0;
            for (String keyword : keywords) {
                if (keyword.length() > 3 && fileName.contains(keyword)) {
                    matches++;
                }
            }

            // Require at least 1 keyword match for fuzzy matching
            if (matches >= 1 && keywords.length > 2) {
                pngs.add(path);
            }
        }

        return pngs;
    }

    /**
     * Updates @null drawable references with actual resource IDs.
     * Uses heuristics to match states (checked/unchecked) based on drawable names.
     * 
     * @return true if any references were updated, false otherwise
     */
    private boolean updateNullReferences(ResXmlDocument xmlDoc, Map<String, Integer> drawableIds, String baseName) {
        ResXmlElement root = xmlDoc.getDocumentElement();
        if (root == null) return false;

        boolean updated = false;
        Map<String, Integer> stateToId = new HashMap<>();

        // Map drawable names to states based on naming patterns
        for (Map.Entry<String, Integer> entry : drawableIds.entrySet()) {
            String name = entry.getKey();
            Integer id = entry.getValue();

            // Heuristic: "on" or "00012" in name = checked state
            if (name.contains("_on_") || name.contains("00012")) {
                stateToId.put("checked", id);
            } else if (!stateToId.containsKey("default")) {
                stateToId.put("default", id);
            }
        }

        // Update @null references
        Iterator<ResXmlElement> items = root.recursiveElements();
        while (items.hasNext()) {
            ResXmlElement item = items.next();
            if (!"item".equals(item.getName())) continue;

            ResXmlAttribute drawable = item.searchAttributeByName("drawable");
            if (drawable != null && drawable.getValueType() == ValueType.REFERENCE && drawable.getData() == 0) {
                // Determine which state this item represents
                boolean isChecked = false;
                Iterator<ResXmlAttribute> attrs = item.getAttributes();
                while (attrs.hasNext()) {
                    ResXmlAttribute attr = attrs.next();
                    if ("state_checked".equals(attr.getName()) && attr.getValueAsBoolean()) {
                        isChecked = true;
                        break;
                    }
                }

                // Get appropriate resource ID
                Integer newResId = isChecked ?
                    stateToId.getOrDefault("checked", stateToId.get("default")) :
                    stateToId.get("default");

                if (newResId != null) {
                    drawable.setData(newResId);
                    updated = true;
                }
            }
        }

        return updated;
    }
}
