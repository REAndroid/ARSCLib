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
import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.json.JSONException;
import com.reandroid.json.JSONObject;

public class UnexpectedResXmlNode extends UnknownResXmlNode {

    public UnexpectedResXmlNode(ChunkType chunkType) {
        super(of(chunkType));
    }

    @Override
    public int getLineNumber() {
        return getBaseXmlChunk().getLineNumber();
    }

    private BaseXmlChunk getBaseXmlChunk() {
        return (BaseXmlChunk) getChunk();
    }
    @Override
    void linkStringReferences() {
        getBaseXmlChunk().linkStringReferences();
    }
    @Override
    void onPreRemove() {
        getBaseXmlChunk().onPreRemove();
    }

    public String getString() {
        BaseXmlChunk chunk = getBaseXmlChunk();
        return chunk.getString(chunk.getStringReference());
    }
    public void setString(String value) {
        getBaseXmlChunk().setString(value);
    }
    public String getUri() {
        return getBaseXmlChunk().getUri();
    }
    public void setUri(String uri) {
        BaseXmlChunk chunk = getBaseXmlChunk();
        ResXmlString xmlString = chunk.getOrCreateString(uri);
        int reference;
        if (xmlString != null) {
            reference = xmlString.getIndex();
        } else {
            reference = -1;
        }
        chunk.setNamespaceReference(reference);
    }

    public String getComment() {
        return getBaseXmlChunk().getComment();
    }
    public void setComment(String value) {
        getBaseXmlChunk().setComment(value);
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_node_type, nodeTypeName());
        jsonObject.put(JSON_type, getChunkType());
        jsonObject.put(JSON_comment, getComment());
        jsonObject.put(JSON_uri, getUri());
        jsonObject.put(JSON_value, getString());
        return jsonObject;
    }

    @Override
    public void fromJson(JSONObject json) throws NumberFormatException {
        if (!nodeTypeName().equals(json.optString(JSON_node_type, null))) {
            throw new JSONException("Expecting: " + nodeTypeName() + ", but found: " +
                    json.optString(JSON_node_type, null));
        }
        setComment(json.optString(JSON_comment, null));
        setUri(json.optString(JSON_uri, null));
        setString(json.optString(JSON_value, null));
    }

    public static boolean isSet(int chunkType) {
        return isSet(ChunkType.get((short) chunkType));
    }
    public static boolean isSet(ChunkType chunkType) {
        return chunkType == ChunkType.XML_END_ELEMENT ||
                chunkType == ChunkType.XML_END_NAMESPACE ||
                chunkType == ChunkType.XML_CDATA;
    }
    private static Chunk<?> of(ChunkType chunkType) {
        if (chunkType == ChunkType.XML_END_ELEMENT) {
            return new ResXmlEndElement();
        }
        if (chunkType == ChunkType.XML_END_NAMESPACE) {
            return new ResXmlEndNamespace();
        }
        if (chunkType == ChunkType.XML_CDATA) {
            return new ResXmlTextChunk();
        }
        throw new RuntimeException("Invalid chunk type: " + chunkType);
    }
}
