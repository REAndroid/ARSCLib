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
package com.reandroid.arsc.coder.xml;

import com.reandroid.xml.XMLUtil;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;

public class XmlEncodeException extends IOException {

    public XmlEncodeException(String message) {
        super(message);
    }

    public XmlEncodeException(XmlPullParser parser, String message) {
        this(XMLUtil.getSimplePositionDescription(parser) + "\n" + message);
    }
}
