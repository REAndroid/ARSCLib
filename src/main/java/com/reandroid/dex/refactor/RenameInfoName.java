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

import com.reandroid.arsc.base.Block;
import com.reandroid.utils.collection.EmptyList;

import java.util.List;

public abstract class RenameInfoName<T extends Block> extends RenameInfo<T> {

    private final String typeName;
    private final String parameters;

    public RenameInfoName(String typeName, String parameters, String search, String replace) {
        super(search, replace);
        this.typeName = typeName;
        this.parameters = parameters;
    }

    public String getTypeName() {
        return typeName;
    }
    public String getParameters() {
        return parameters;
    }

    @Override
    public abstract String getKey();
    @Override
    List<RenameInfo<?>> createChildRenames() {
        return EmptyList.of();
    }
}
