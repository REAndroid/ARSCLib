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

// Copied and modified from AOSP
package com.reandroid.common;

import com.reandroid.utils.collection.CollectionUtil;

import java.util.Set;

public class SymbolGenerator {

    private final char[] characters;
    private final int totalCharacters;
    private final int lowercaseAndSuffix;
    private final int suffixLength;

    private final Set<String> reservedNames;

    public SymbolGenerator(char[] characters, int lowercaseLength, int suffixLength, Set<String> reservedNames) {
        this.characters = characters;
        this.totalCharacters = characters.length;
        this.lowercaseAndSuffix = lowercaseLength + suffixLength;
        this.suffixLength = suffixLength;
        this.reservedNames = reservedNames;
    }
    public SymbolGenerator(char[] characters, int lowercaseLength, int suffixLength) {
        this(characters, lowercaseLength, suffixLength, loadDefaultReservedNames());
    }

    public String generateLowercase(int index) {
        return generate(index, false);
    }
    public String generateMixedCase(int index) {
        return generate(index, true);
    }
    public String generate(int index, boolean mixedCase) {
        int size = 1;
        int number = index + 1;
        int maximumNumberOfCharacters = mixedCase ? totalCharacters : lowercaseAndSuffix;
        int firstNumberOfCharacters = maximumNumberOfCharacters - suffixLength;

        int availableCharacters;
        for(availableCharacters = firstNumberOfCharacters; number > availableCharacters; ++size) {
            number = (number - 1) / availableCharacters;
            availableCharacters = maximumNumberOfCharacters;
        }

        char[] characters = new char[size];
        number = index + 1;
        int i = 0;
        availableCharacters = firstNumberOfCharacters;

        int firstLetterPadding;
        for(firstLetterPadding = suffixLength; number > availableCharacters; firstLetterPadding = 0) {
            characters[i++] = this.characters[(number - 1) % availableCharacters + firstLetterPadding];
            number = (number - 1) / availableCharacters;
            availableCharacters = maximumNumberOfCharacters;
        }

        characters[i] = this.characters[number - 1 + firstLetterPadding];

        String symbol = new String(characters);
        if(isReserved(symbol)) {
            return generate(index + 1, mixedCase);
        }
        return symbol;
    }
    public void addReserved(String symbol) {
        reservedNames.add(symbol);
    }
    public boolean removeReserved(String symbol) {
        return reservedNames.remove(symbol);
    }
    public void clearReserved() {
        reservedNames.clear();
    }
    public boolean isReserved(String symbol) {
        return reservedNames.contains(symbol);
    }

    private static Set<String> loadDefaultReservedNames() {
        return CollectionUtil.newHashSet(
                "boolean", "byte", "char",
                "double", "float", "int",
                "long", "short", "void", "it",
                "by", "class");
    }
}
