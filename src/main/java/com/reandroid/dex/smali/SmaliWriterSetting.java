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
package com.reandroid.dex.smali;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.model.DexClassRepository;

import java.io.IOException;

public class SmaliWriterSetting {

    private ResourceIdComment resourceIdComment;
    private MethodComment methodComment;

    public SmaliWriterSetting(){
    }

    public void writeResourceIdComment(SmaliWriter writer, long l) throws IOException {
        ResourceIdComment resourceIdComment = getResourceIdComment();
        if(resourceIdComment != null){
            resourceIdComment.writeResourceIdComment(writer, (int)l);
        }
    }
    public void writeResourceIdComment(SmaliWriter writer, int i) throws IOException {
        ResourceIdComment resourceIdComment = getResourceIdComment();
        if(resourceIdComment != null){
            resourceIdComment.writeResourceIdComment(writer, i);
        }
    }
    public ResourceIdComment getResourceIdComment() {
        return resourceIdComment;
    }
    public void setResourceIdComment(ResourceIdComment resourceIdComment) {
        this.resourceIdComment = resourceIdComment;
    }
    public void setResourceIdComment(PackageBlock packageBlock) {
        this.setResourceIdComment(new ResourceIdComment.ResourceTableComment(packageBlock));
    }

    public void writeMethodComment(SmaliWriter writer, MethodKey methodKey) throws IOException {
        MethodComment methodComment = getMethodComment();
        if(methodComment != null){
            methodComment.writeMethodComment(writer, methodKey);
        }
    }
    public MethodComment getMethodComment() {
        return methodComment;
    }
    public void setMethodComment(MethodComment methodComment) {
        this.methodComment = methodComment;
    }
    public void setMethodComment(DexClassRepository classRepository) {
        setMethodComment(new MethodComment.MethodHierarchyComment(classRepository));
    }
}
