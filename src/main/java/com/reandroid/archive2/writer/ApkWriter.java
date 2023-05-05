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
package com.reandroid.archive2.writer;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.RenamedInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.WriteProgress;
import com.reandroid.archive2.ZipSignature;
import com.reandroid.archive2.block.ApkSignatureBlock;
import com.reandroid.archive2.block.EndRecord;
import com.reandroid.archive2.io.ArchiveEntrySource;
import com.reandroid.archive2.io.ZipFileOutput;
import com.reandroid.arsc.chunk.TableBlock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApkWriter extends ZipFileOutput {
    private final Object mLock = new Object();
    private final Collection<? extends InputSource> sourceList;
    private ZipAligner zipAligner;
    private ApkSignatureBlock apkSignatureBlock;
    private APKLogger apkLogger;
    private WriteProgress writeProgress;

    public ApkWriter(File file, Collection<? extends InputSource> sourceList) throws IOException {
        super(file);
        this.sourceList = sourceList;
        this.zipAligner = ZipAligner.apkAligner();
    }
    public void write()throws IOException {
        synchronized (mLock){
            List<OutputSource> outputList = buildOutputEntry();
            logMessage("Buffering compress changed files ...");
            BufferFileInput buffer = writeBuffer(outputList);
            buffer.unlock();
            align(outputList);
            writeApk(outputList);
            buffer.close();

            writeSignatureBlock();

            writeCEH(outputList);
            this.close();
            logMessage("Written to: " + getFile().getName());
        }
    }
    public void setApkSignatureBlock(ApkSignatureBlock apkSignatureBlock) {
        this.apkSignatureBlock = apkSignatureBlock;
    }
    public ZipAligner getZipAligner() {
        return zipAligner;
    }
    public void setZipAligner(ZipAligner zipAligner) {
        this.zipAligner = zipAligner;
    }

    private void writeCEH(List<OutputSource> outputList) throws IOException{
        logMessage("Writing CEH ...");
        EndRecord endRecord = new EndRecord();
        endRecord.setSignature(ZipSignature.END_RECORD);
        long offset = position();
        endRecord.setOffsetOfCentralDirectory((int) offset);
        endRecord.setNumberOfDirectories(outputList.size());
        endRecord.setTotalNumberOfDirectories(outputList.size());
        for(OutputSource outputSource:outputList){
            outputSource.writeCEH(this);
        }
        long len = position() - offset;
        endRecord.setLengthOfCentralDirectory(len);
        endRecord.writeBytes(getOutputStream());
    }
    private void writeApk(List<OutputSource> outputList) throws IOException{
        logMessage("Writing files: " + outputList.size());
        for(OutputSource outputSource:outputList){
            outputSource.writeApk( this);
        }
    }
    private void writeSignatureBlock() throws IOException {
        ApkSignatureBlock signatureBlock = this.apkSignatureBlock;
        if(signatureBlock == null){
            return;
        }
        logMessage("Writing signature block ...");
        long offset = position();
        int alignment = 4096;
        int filesPadding = (int) ((alignment - (offset % alignment)) % alignment);
        OutputStream outputStream = getOutputStream();
        if(filesPadding > 0){
            outputStream.write(new byte[filesPadding]);
        }
        logMessage("files padding = " + filesPadding);
        signatureBlock.updatePadding();
        signatureBlock.writeBytes(outputStream);
    }
    private BufferFileInput writeBuffer(List<OutputSource> outputList) throws IOException {
        File bufferFile = getBufferFile();
        BufferFileOutput output = new BufferFileOutput(bufferFile);
        BufferFileInput input = new BufferFileInput(bufferFile);
        OutputSource tableSource = null;
        for(OutputSource outputSource:outputList){
            InputSource inputSource = outputSource.getInputSource();
            if(tableSource == null && TableBlock.FILE_NAME.equals(inputSource.getAlias())){
                tableSource = outputSource;
                continue;
            }
            onCompressFileProgress(inputSource.getAlias(),
                    inputSource.getMethod(),
                    output.position());
            outputSource.makeBuffer(input, output);
        }
        if(tableSource != null){
            tableSource.makeBuffer(input, output);
        }
        output.close();
        return input;
    }
    private void align(List<OutputSource> outputList){
        ZipAligner aligner = getZipAligner();
        if(aligner!=null){
            aligner.reset();
            logMessage("Zip align ...");
        }
        for(OutputSource outputSource:outputList){
            outputSource.align(aligner);
        }
    }
    private File getBufferFile(){
        File file = getFile();
        File dir = file.getParentFile();
        String name = file.getAbsolutePath();
        name = "tmp" + name.hashCode();
        File bufFile;
        if(dir != null){
            bufFile = new File(dir, name);
        }else {
            bufFile = new File(name);
        }
        bufFile.deleteOnExit();
        return bufFile;
    }
    private List<OutputSource> buildOutputEntry(){
        Collection<? extends InputSource> sourceList = this.sourceList;
        List<OutputSource> results = new ArrayList<>(sourceList.size());
        for(InputSource inputSource:sourceList){
            results.add(toOutputSource(inputSource));
        }
        return results;
    }
    private OutputSource toOutputSource(InputSource inputSource){
        if(inputSource instanceof ArchiveEntrySource){
            return new ArchiveOutputSource(inputSource);
        }
        if(inputSource instanceof RenamedInputSource){
            InputSource renamed = ((RenamedInputSource<?>) inputSource).getInputSource();
            if(renamed instanceof ArchiveEntrySource){
                return new RenamedArchiveSource((RenamedInputSource<?>) inputSource);
            }
        }
        return new OutputSource(inputSource);
    }

    public void setWriteProgress(WriteProgress writeProgress){
        this.writeProgress = writeProgress;
    }
    private void onCompressFileProgress(String path, int mode, long writtenBytes) {
        if(writeProgress!=null){
            writeProgress.onCompressFile(path, mode, writtenBytes);
        }
    }

    APKLogger getApkLogger(){
        return apkLogger;
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
    private void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }

}
