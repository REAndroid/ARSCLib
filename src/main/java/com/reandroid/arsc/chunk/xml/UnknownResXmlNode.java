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

import com.reandroid.arsc.chunk.Chunk;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.refactor.ResourceMergeOption;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.HexUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.xml.XMLNode;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class UnknownResXmlNode extends ResXmlNode {

    public UnknownResXmlNode(Chunk<?> chunk) {
        super(chunk);
    }
    public UnknownResXmlNode() {
        this(new UnknownChunk());
    }

    @Override
    public Chunk<?> getChunk() {
        return (Chunk<?>) getBaseBlock();
    }

    @Override
    void onPreRemove() {

    }

    @Override
    void linkStringReferences() {

    }

    @Override
    Iterator<ResXmlEvent> getParserEvents() {
        return EmptyIterator.of();
    }

    @Override
    int autoSetLineNumber(int start) {
        return 0;
    }

    @Override
    String nodeTypeName() {
        return JSON_node_type_unknown;
    }

    @Override
    public boolean isUnknown() {
        return true;
    }

    public String getHexBytes() {
        return HexUtil.toHexString(getBytes());
    }
    public void setHexBytes(String hexBytes) throws NumberFormatException {
        byte[] bytes = HexUtil.fromHexSting(hexBytes);
        try {
            readBytes(new BlockReader(bytes));
        } catch (IOException exception) {
            throw new NumberFormatException("Invalid chunk hex bytes:"
                    + exception.getMessage());
        }
    }
    public int getChunkType() {
        return getChunk().getHeaderBlock()
                .getType() & 0xffff;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_node_type, nodeTypeName());
        jsonObject.put(JSON_type, getChunkType());
        jsonObject.put(JSON_value, getHexBytes());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) throws NumberFormatException {
        if (!nodeTypeName().equals(json.optString(JSON_node_type, null))) {
            throw new JSONException("Expecting: " + nodeTypeName() + ", but found: " +
                    json.optString(JSON_node_type, null));
        }
        if (json.has(JSON_value)) {
            setHexBytes(json.getString(JSON_value));
        }
    }

    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {

    }

    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {

    }

    @Override
    public XMLNode toXml(boolean decode) {
        return null;
    }

    @Override
    public void mergeWithName(ResourceMergeOption mergeOption, ResXmlNode xmlNode) {

    }

    @Override
    public void merge(ResXmlNode xmlNode) {

    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public void setLineNumber(int lineNumber) {

    }
}
