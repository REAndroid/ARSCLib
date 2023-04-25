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

import com.reandroid.apk.RenamedInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive2.ZipSignature;
import com.reandroid.archive2.block.EndRecord;
import com.reandroid.archive2.io.ArchiveEntrySource;
import com.reandroid.archive2.io.ZipFileOutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApkWriter extends ZipFileOutput {
    private final Collection<? extends InputSource> sourceList;
    private ZipAligner zipAligner;

    public ApkWriter(File file, Collection<? extends InputSource> sourceList) throws IOException {
        super(file);
        this.sourceList = sourceList;
        this.zipAligner = ZipAligner.apkAligner();
    }
    public void write()throws IOException {
        List<OutputSource> outputList = buildOutputEntry();
        BufferFileInput buffer = writeBuffer(outputList);
        buffer.unlock();
        align(outputList);
        writeApk(outputList);
        writeCEH(outputList);
        buffer.close();
        this.close();
    }
    public ZipAligner getZipAligner() {
        return zipAligner;
    }
    public void setZipAligner(ZipAligner zipAligner) {
        this.zipAligner = zipAligner;
    }

    private void writeCEH(List<OutputSource> outputList) throws IOException{
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
        for(OutputSource outputSource:outputList){
            outputSource.writeApk( this);
        }
    }
    private BufferFileInput writeBuffer(List<OutputSource> outputList) throws IOException {
        File bufferFile = getBufferFile();
        BufferFileOutput output = new BufferFileOutput(bufferFile);
        BufferFileInput input = new BufferFileInput(bufferFile);
        for(OutputSource outputSource:outputList){
            outputSource.makeBuffer(input, output);
        }
        output.close();
        return input;
    }
    private void align(List<OutputSource> outputList){
        ZipAligner aligner = getZipAligner();
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

}
