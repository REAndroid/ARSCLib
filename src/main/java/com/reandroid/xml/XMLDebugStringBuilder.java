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
package com.reandroid.xml;

import java.util.Iterator;

public class XMLDebugStringBuilder {

    public static String build(XMLNode node) {
        StringBuilder indent = new StringBuilder();
        indent.append('\n');
        StringBuilder builder = new StringBuilder();
        appendNode(indent, builder, node);
        return builder.toString();
    }

    private static void appendNode(StringBuilder indent, StringBuilder builder, XMLNode node) {
        if (limitReached(builder)) {
            return;
        }
        if (node instanceof XMLDocument) {
            appendDocument(indent, builder, (XMLDocument) node);
        } else if (node instanceof XMLElement) {
            appendElement(indent, builder, (XMLElement) node);
        } else if (node instanceof XMLText) {
            appendText(builder, (XMLText) node);
        } else if (node instanceof XMLDocType) {
            appendDocDeclaration(builder, (XMLDocType) node);
        } else if(node != null){
            builder.append(node);
        }
    }
    private static void appendDocument(StringBuilder indent, StringBuilder builder, XMLDocument document) {
        if (limitReached(builder)) {
            return;
        }
        if (indent.length() > 1) {
            builder.append(addIndent(indent));
        }

        XMLDocDeclaration declaration = document.getDeclaration();
        if (declaration.isValid()) {
            builder.append(declaration.toString());
        }
        boolean hasChildes = false;
        Iterator<XMLNode> iterator = document.iterator();
        while (iterator.hasNext() && !limitReached(builder)) {
            appendNode(indent, builder, iterator.next());
            hasChildes = true;
        }
        String endIndent = subIndent(indent);
        if (hasChildes) {
            builder.append(endIndent);
        }
    }
    private static void appendElement(StringBuilder indent, StringBuilder builder, XMLElement element) {
        if (limitReached(builder)) {
            return;
        }
        builder.append(addIndent(indent));
        builder.append('<');
        String name = element.getName(true);
        if (name == null) {
            name = "null";
        }
        builder.append(name);
        Iterator<? extends XMLAttribute> attributes = element.getAttributes();
        while (attributes.hasNext() && !limitReached(builder)) {
            builder.append(' ');
            builder.append(attributes.next());
        }
        boolean hasChildes = false;
        boolean allText = false;
        Iterator<XMLNode> iterator = element.iterator();
        while (iterator.hasNext()) {
            XMLNode node = iterator.next();
            if (!hasChildes) {
                builder.append('>');
                hasChildes = true;
                allText = true;
            }
            if (allText) {
                allText = (node instanceof XMLText);
            }
            if (limitReached(builder)) {
                break;
            }
            appendNode(indent, builder, node);
        }
        String endIndent = subIndent(indent);
        if (!element.isVoidHtml()) {
            if (hasChildes) {
                if (!allText) {
                    builder.append(endIndent);
                }
                builder.append("</");
                builder.append(name);
                builder.append('>');
            } else {
                builder.append("/>");
            }
        } else {
            builder.append('>');
        }
    }
    private static void appendText(StringBuilder builder, XMLText xmlText) {
        if (limitReached(builder)) {
            return;
        }
        String text = xmlText.getText();
        if (text == null) {
            text = "null";
        }
        int i = STRING_LIMIT - builder.length();
        if (i <= 0) {
            return;
        }
        if (i < text.length()) {
            text = text.substring(0, i) + " ...";
        }
        builder.append(text);
    }
    private static void appendDocDeclaration(StringBuilder builder,
                                             XMLDocType declaration) {
        if (limitReached(builder)) {
            return;
        }
        builder.append("<!DOCTYPE ");
        builder.append(declaration.getName());
        builder.append(">\n");
    }
    private static boolean limitReached(StringBuilder builder) {
        return builder.length() >= STRING_LIMIT;
    }
    private static String addIndent(StringBuilder indent) {
        int length = indent.length();
        if (length == 0) {
            return "";
        }
        String str;
        if (length > 1) {
            str = indent.toString();
        } else {
            str = "";
        }
        if (length < MAX_INDENT) {
            indent.append(' ');
        }
        return str;
    }
    private static String subIndent(StringBuilder indent) {
        int length = indent.length();
        if (length == 0) {
            return "";
        }
        if (length > 1) {
            indent.deleteCharAt(length - 1);
        }
        return indent.toString();
    }

    private static final int STRING_LIMIT = 300;
    private static final int MAX_INDENT = 10;
}
