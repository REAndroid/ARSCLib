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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.*;

import java.io.IOException;

public class SmaliDebugRestartLocal extends SmaliDebug implements SmaliRegion {

    private final SmaliRegisterSet registerSet;

    public SmaliDebugRestartLocal(){
        super();
        this.registerSet = new SmaliRegisterSet();
        this.registerSet.setParent(this);
    }

    public SmaliRegisterSet getRegisterSet() {
        return registerSet;
    }

    @Override
    public SmaliDirective getSmaliDirective() {
        return SmaliDirective.RESTART_LOCAL;
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        getSmaliDirective().append(writer);
        getRegisterSet().append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException {
        SmaliParseException.expect(reader, getSmaliDirective());
        getRegisterSet().parse(reader);
    }
}
