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
package com.reandroid.dex.item;

import com.reandroid.dex.base.*;
import com.reandroid.dex.index.ClassId;
import com.reandroid.dex.writer.SmaliFormat;
import com.reandroid.dex.writer.SmaliWriter;

import java.io.IOException;

public class ClassData extends DexItem
        implements SmaliFormat {

    private final Ule128Item staticFieldsCount;
    private final Ule128Item instanceFieldCount;
    private final Ule128Item directMethodCount;
    private final Ule128Item virtualMethodCount;

    private final FieldDefArray staticFields;
    private final FieldDefArray instanceFields;
    private final MethodDefArray directMethods;
    private final MethodDefArray virtualMethods;

    public ClassData() {
        super(8);
        this.staticFieldsCount = new Ule128Item();
        this.instanceFieldCount = new Ule128Item();
        this.directMethodCount = new Ule128Item();
        this.virtualMethodCount = new Ule128Item();


        this.staticFields = new FieldDefArray(staticFieldsCount);
        this.instanceFields = new FieldDefArray(instanceFieldCount);
        this.directMethods = new MethodDefArray(directMethodCount);
        this.virtualMethods = new MethodDefArray(virtualMethodCount);


        addChild(0, staticFieldsCount);
        addChild(1, instanceFieldCount);
        addChild(2, directMethodCount);
        addChild(3, virtualMethodCount);

        addChild(4, staticFields);
        addChild(5, instanceFields);
        addChild(6, directMethods);
        addChild(7, virtualMethods);
    }

    public FieldDefArray getStaticFields() {
        return staticFields;
    }
    public FieldDefArray getInstanceFields() {
        return instanceFields;
    }

    public MethodDefArray getDirectMethods() {
        return directMethods;
    }
    public MethodDefArray getVirtualMethods() {
        return virtualMethods;
    }

    public void setClassId(ClassId classId) {
        staticFields.setClassId(classId);
        instanceFields.setClassId(classId);
        directMethods.setClassId(classId);
        virtualMethods.setClassId(classId);
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        staticFields.append(writer);
        instanceFields.append(writer);
        directMethods.append(writer);
        virtualMethods.append(writer);
    }
    @Override
    public String toString() {
        return "staticFieldsCount=" + staticFieldsCount +
                ", instanceFieldCount=" + instanceFieldCount +
                ", directMethodCount=" + directMethodCount +
                ", virtualMethodCount=" + virtualMethodCount;
    }

}
