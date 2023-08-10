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
package com.reandroid.arsc.chunk.xml;

import com.reandroid.arsc.base.BlockCounter;
import com.reandroid.arsc.container.FixedBlockContainer;
import com.reandroid.json.JSONConvert;
import com.reandroid.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

public abstract class ResXmlNode extends FixedBlockContainer  implements JSONConvert<JSONObject> {
    ResXmlNode(int childrenCount) {
        super(childrenCount);
    }
    abstract void onRemoved();
    abstract void linkStringReferences();
    public abstract int getDepth();
    abstract void addEvents(ParserEventList parserEventList);
    abstract void calculateLineNumber(BlockCounter counter, boolean startLine);
    public abstract void autoSetLineNumber();
    public abstract void serialize(XmlSerializer serializer) throws IOException;
    public abstract void parse(XmlPullParser parser) throws IOException, XmlPullParserException;

    public static final String NAME_node_type = "node_type";
}
