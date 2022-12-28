/*
 *  This class is taken from org.xmlpull.*
 *
 *  Check license: http://xmlpull.org
 *
 */

/*This package is renamed from org.xmlpull.* to avoid conflicts*/
package com.reandroid.xml.parser;

import java.io.IOException;

public class MXParserNonValidating extends MXParserCachingStrings
{
    private boolean processDocDecl;

    public MXParserNonValidating() {
        super();
    }

   @Override
    public void setFeature(String name,
                           boolean state) throws XmlPullParserException
    {
        if(FEATURE_PROCESS_DOCDECL.equals(name)) {
            if(eventType != START_DOCUMENT) throw new XmlPullParserException(
                    "process DOCDECL feature can only be changed before parsing", this, null);
            processDocDecl = state;
        } else {
            super.setFeature(name, state);
        }
    }

    @Override
    public boolean getFeature(String name)
    {
        if(FEATURE_PROCESS_DOCDECL.equals(name)) {
            return processDocDecl;
        } else {
            return super.getFeature(name);
        }
    }


    @Override
    protected char more() throws IOException, XmlPullParserException {
        return super.more();
    }

    @Override
    protected char[] lookuEntityReplacement(int entitNameLen) throws XmlPullParserException, IOException

    {
        if(!allStringsInterned) {
            final int hash = fastHash(buf, posStart, posEnd - posStart);
            LOOP:
            for (int i = entityEnd - 1; i >= 0; --i)
            {
                if(hash == entityNameHash[ i ] && entitNameLen == entityNameBuf[ i ].length) {
                    final char[] entityBuf = entityNameBuf[ i ];
                    for (int j = 0; j < entitNameLen; j++)
                    {
                        if(buf[posStart + j] != entityBuf[j]) continue LOOP;
                    }
                    if(tokenize) text = entityReplacement[ i ];
                    return entityReplacementBuf[ i ];
                }
            }
        } else {
            entityRefName = newString(buf, posStart, posEnd - posStart);
            for (int i = entityEnd - 1; i >= 0; --i)
            {
                // take advantage that interning for newStirng is enforced
                if(entityRefName == entityName[ i ]) {
                    if(tokenize) {
                        text = entityReplacement[ i ];
                    }
                    return entityReplacementBuf[ i ];
                }
            }
        }
        return null;
    }


    @Override
    protected void parseDocdecl()
        throws XmlPullParserException, IOException
    {
        //make sure that tokenize flag is disabled temporarily!!!!
        final boolean oldTokenize = tokenize;
        try {
            //ASSUMPTION: seen <!D
            char ch = more();
            if(ch != 'O') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            ch = more();
            if(ch != 'C') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            ch = more();
            if(ch != 'T') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            ch = more();
            if(ch != 'Y') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            ch = more();
            if(ch != 'P') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            ch = more();
            if(ch != 'E') throw new XmlPullParserException(
                    "expected <!DOCTYPE", this, null);
            posStart = pos;
            // do simple and crude scanning for end of doctype

            // [28]  doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S? ('['
            //                      (markupdecl | DeclSep)* ']' S?)? '>'
            ch = requireNextS();
            int nameStart = pos;
            ch = readName(ch);
            int nameEnd = pos;
            ch = skipS(ch);
            // [75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S PubidLiteral S SystemLiteral
            if(ch == 'S' || ch == 'P') {
                ch = processExternalId(ch);
                ch = skipS(ch);
            }
            if(ch == '[') {
                processInternalSubset();
            }
            ch = skipS(ch);
            if(ch != '>') {
                throw new XmlPullParserException(
                    "expected > to finish <[DOCTYPE but got "+printable(ch), this, null);
            }
            posEnd = pos - 1;
        } finally {
            tokenize = oldTokenize;
        }
    }
    protected char processExternalId(char ch)
        throws XmlPullParserException, IOException
    {
        // [75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S PubidLiteral S SystemLiteral
        // [11] SystemLiteral ::= ('"' [^"]* '"') | ("'" [^']* "'")
        // [12] PubidLiteral ::= '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
        // [13] PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]

        //TODO

        return ch;
    }

    protected void processInternalSubset()
        throws XmlPullParserException, IOException
    {
        // [28]  ... (markupdecl | DeclSep)* ']'  // [WFC: External Subset]
        // [28a] DeclSep ::= PEReference | S       // [WFC: PE Between Declarations]

        // [69] PEReference ::= '%' Name ';'  //[WFC: No Recursion]  [WFC: In DTD]
        while(true) {
            char ch = more();  // firs ttime called it will skip initial "["
            if(ch == ']') break;
            if(ch == '%') {
                processPEReference();
            } else if(isS(ch)) {
                ch = skipS(ch);
            } else {
                processMarkupDecl(ch);
            }
        }
    }

    protected void processPEReference()
        throws XmlPullParserException, IOException
    {
        //TODO
    }
    protected void processMarkupDecl(char ch)
        throws XmlPullParserException, IOException
    {
        // [29]  markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI | Comment
        //                                                     [WFC: PEs in Internal Subset]


        //BIG SWITCH statement
        if(ch != '<') {
            throw new XmlPullParserException("expected < for markupdecl in DTD not "+printable(ch),
                                             this, null);
        }
        ch = more();
        if(ch == '?') {
            parsePI();
        } else if(ch == '!') {
            ch = more();
            if(ch == '-') {
                // note: if(tokenize == false) posStart/End is NOT changed!!!!
                parseComment();
            } else {
                ch = more();
                if(ch == 'A') {
                    processAttlistDecl(ch);  //A-TTLIST
                } else if(ch == 'E') {
                    ch = more();
                    if(ch == 'L') {
                        processElementDecl(ch);  //EL-EMENT
                    } else if(ch == 'N') {
                        processEntityDecl(ch);  // EN-TITY
                    } else {
                        throw new XmlPullParserException(
                            "expected ELEMENT or ENTITY after <! in DTD not "+printable(ch),
                            this, null);
                    }
                } else if(ch == 'N') {
                    processNotationDecl(ch);  //N-OTATION
                } else {
                    throw new XmlPullParserException(
                        "expected markupdecl after <! in DTD not "+printable(ch),this, null);
                }
            }

        } else {
            throw new XmlPullParserException("expected markupdecl in DTD not "+printable(ch),
                                             this, null);
        }
    }

    protected void processElementDecl(char ch)
        throws XmlPullParserException, IOException
    {
        //[45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'
        //???? [VC: Unique Element Type Declaration]
        // [46] contentspec ::= 'EMPTY' | 'ANY' | Mixed | children
        // [47] children ::= (choice | seq) ('?' | '*' | '+')?
        // [48] cp ::= (Name | choice | seq) ('?' | '*' | '+')?
        // [49] choice ::= '(' S? cp ( S? '|' S? cp )+ S? ')'
        // [50] seq ::= '(' S? cp ( S? ',' S? cp )* S? ')'
        // [51] Mixed ::=  '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
        //             | '(' S? '#PCDATA' S? ')'

        //assert ch == 'L'
        ch = requireNextS();
        readName(ch);
        ch = requireNextS();
        // readContentSpec(ch);
    }

    protected void processAttlistDecl(char ch)
        throws XmlPullParserException, IOException
    {
        // [52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
        // [53] AttDef ::= S Name S AttType S DefaultDecl
        // [54] AttType ::= StringType | TokenizedType | EnumeratedType
        // [55] StringType ::= 'CDATA'
        // [56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES' | 'NMTOKEN'
        //                        | 'NMTOKENS'
        // [57] EnumeratedType ::= NotationType | Enumeration
        // [58] NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
        // [59] Enumeration ::= '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
        // [60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED' | (('#FIXED' S)? AttValue)
        //                     [WFC: No < in Attribute Values]

        //assert ch == 'A'

    }


    protected void processEntityDecl(char ch)
        throws XmlPullParserException, IOException
    {

        // [70] EntityDecl ::= GEDecl | PEDecl
        // [71] GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
        // [72] PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
        // [73] EntityDef ::= EntityValue | (ExternalID NDataDecl?)
        // [74] PEDef ::= EntityValue | ExternalID
        // [75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S PubidLiteral S SystemLiteral

        //[9] EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
        //                     |  "'" ([^%&'] | PEReference | Reference)* "'"

        //assert ch == 'N'

    }

    protected void processNotationDecl(char ch)
        throws XmlPullParserException, IOException
    {

        // [82] NotationDecl ::= '<!NOTATION' S Name S (ExternalID | PublicID) S? '>'
        // [83] PublicID ::= 'PUBLIC' S PubidLiteral

        //assert ch == 'N'
    }



    protected char readName(char ch)
        throws XmlPullParserException, IOException
    {
        if(isNameStartChar(ch)) {
            throw new XmlPullParserException(
                "XML name must start with name start character not "+printable(ch), this, null);
        }
        while(isNameChar(ch)) {
            ch = more();
        }
        return ch;
    }
}