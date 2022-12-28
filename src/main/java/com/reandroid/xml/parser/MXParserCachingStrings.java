/*
 *  This class is taken from org.xmlpull.*
 *
 *  Check license: http://xmlpull.org
 *
 */

/*This package is renamed from org.xmlpull.* to avoid conflicts*/
package com.reandroid.xml.parser;

public class MXParserCachingStrings extends MXParser implements Cloneable
{
    protected final static boolean CACHE_STATISTICS = false;
    protected final static boolean TRACE_SIZING = false;
    protected final static int INITIAL_CAPACITY = 13;
    protected int cacheStatCalls;
    protected int cacheStatWalks;
    protected int cacheStatResets;
    protected int cacheStatRehash;

    protected static final int CACHE_LOAD = 77;
    protected int cacheEntriesCount;
    protected int cacheEntriesThreshold;

    protected char[][] keys;
    protected String[] values;

    public MXParserCachingStrings() {
        super();
        allStringsInterned = true;
        initStringCache();
    }
    
    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException
    {
        if(FEATURE_NAMES_INTERNED.equals(name)) {
            if(eventType != START_DOCUMENT) throw new XmlPullParserException(
                    "interning names feature can only be changed before parsing", this, null);
            allStringsInterned = state;
            if(!state && keys != null) {
                resetStringCache();
            }
        } else {
            super.setFeature(name, state);
        }
    }

    public boolean getFeature(String name)
    {
        if(FEATURE_NAMES_INTERNED.equals(name)) {
            return allStringsInterned;
        } else {
            return super.getFeature(name);
        }
    }
    

    protected String newString(char[] cbuf, int off, int len) {
        if(allStringsInterned) {
            return newStringIntern(cbuf, off, len);
        } else {
            return super.newString(cbuf, off, len);
        }
    }

    protected String newStringIntern(char[] cbuf, int off, int len) {
        if(CACHE_STATISTICS) {
            ++cacheStatCalls;
        }
        if (cacheEntriesCount >= cacheEntriesThreshold) {
            rehash();
        }
        int offset = fastHash(cbuf, off, len) % keys.length;
        char[] k = null;
        while( (k = keys[offset]) != null
                  && !keysAreEqual(k, 0, k.length,
                                   cbuf, off, len))
        {
            offset = (offset + 1) % keys.length;
            if(CACHE_STATISTICS) ++cacheStatWalks;
        }
        if (k != null) {
            return  values[offset];
        } else {
            k = new char[len];
            System.arraycopy(cbuf, off, k, 0, len);
            final String v = new String(k).intern();
            keys[offset] = k;
            values[offset] = v;
            ++cacheEntriesCount;
            return  v;
        }
        
    }
    
    protected void initStringCache() {
        if(keys == null) {
            if(INITIAL_CAPACITY < 0) {
                throw  new IllegalArgumentException("Illegal initial capacity: " + INITIAL_CAPACITY);
            }
            if(CACHE_LOAD < 0 || CACHE_LOAD > 99) {
                throw  new IllegalArgumentException("Illegal load factor: " + CACHE_LOAD);
            }
            cacheEntriesThreshold = (int)((INITIAL_CAPACITY * CACHE_LOAD)/100);
            if(cacheEntriesThreshold >= INITIAL_CAPACITY) {
                throw new RuntimeException(
                        "internal error: threshold must be less than capacity: "+INITIAL_CAPACITY);
            }
            keys = new char[INITIAL_CAPACITY][];
            values = new String[INITIAL_CAPACITY];
            cacheEntriesCount = 0;
        }
    }
    
    protected void resetStringCache() {
        if(CACHE_STATISTICS) {
            ++cacheStatResets;
        }
        initStringCache();
    }
    
    private void rehash() {
        if(CACHE_STATISTICS) ++cacheStatRehash;
        final int newSize = 2 * keys.length + 1;
        cacheEntriesThreshold = (int)((newSize * CACHE_LOAD)/100);
        if(cacheEntriesThreshold >= newSize) throw new RuntimeException(
                "internal error: threshold must be less than capacity: "+newSize);

        final char[][] newKeys = new char[newSize][];
        final String[] newValues = new String[newSize];
        for(int i = 0; i < keys.length; i++) {
            final char[] k = keys[i];
            keys[i] = null;
            final String v = values[i];
            values[i] = null;
            if(k != null) {
                int newOffset = fastHash(k, 0, k.length) % newSize;
                char[] newk = null;
                while((newk = newKeys[newOffset]) != null) {
                    if(keysAreEqual(newk, 0, newk.length,
                                    k, 0, k.length)) {
                        throw new RuntimeException("internal cache error: duplicated keys: "+
                                                       new String(newk)+" and "+new String(k));
                    }
                    newOffset = (newOffset + 1) % newSize;
                }
                
                newKeys[newOffset] = k;
                newValues[newOffset] = v;
            }
        }
        keys = newKeys;
        values = newValues;
    }
    
    private static boolean keysAreEqual (char[] a, int astart, int alength,
                                         char[] b, int bstart, int blength) {
        if(alength != blength) {
            return  false;
        } else {
            for(int i = 0; i < alength; i++) {
                if(a[astart + i] != b[bstart + i]) {
                    return  false;
                }
            }
            return  true;
        }
    }
    
}
