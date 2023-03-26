/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)
package org.xmlpull.v1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XmlPullParserFactory {
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    protected ArrayList parserClasses;
    protected ArrayList serializerClasses;
    protected String classNamesLocation = null;
    protected HashMap<String, Boolean> features = new HashMap<String, Boolean>();
    protected XmlPullParserFactory() {
        parserClasses = new ArrayList<String>();
        serializerClasses = new ArrayList<String>();
        try {
            parserClasses.add(Class.forName("com.android.org.kxml2.io.KXmlParser"));
            serializerClasses.add(Class.forName("com.android.org.kxml2.io.KXmlSerializer"));
        } catch (ClassNotFoundException e) {
            throw new AssertionError();
        }
    }
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        features.put(name, state);
    }
    public boolean getFeature(String name) {
        Boolean value = features.get(name);
        return value != null ? value.booleanValue() : false;
    }
    public void setNamespaceAware(boolean awareness) {
        features.put (XmlPullParser.FEATURE_PROCESS_NAMESPACES, awareness);
    }
    public boolean isNamespaceAware() {
        return getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
    }
    public void setValidating(boolean validating) {
        features.put(XmlPullParser.FEATURE_VALIDATION, validating);
    }

    public boolean isValidating() {
        return getFeature(XmlPullParser.FEATURE_VALIDATION);
    }
    public XmlPullParser newPullParser() throws XmlPullParserException {
        final XmlPullParser pp = getParserInstance();
        for (Map.Entry<String, Boolean> entry : features.entrySet()) {
            if (entry.getValue()) {
                pp.setFeature(entry.getKey(), entry.getValue());
            }
        }
        return pp;
    }
    private XmlPullParser getParserInstance() throws XmlPullParserException {
        ArrayList<Exception> exceptions = null;
        if (parserClasses != null && !parserClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : parserClasses) {
                try {
                    if (o != null) {
                        Class<?> parserClass = (Class<?>) o;
                        return (XmlPullParser) parserClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }
        throw newInstantiationException("Invalid parser class list", exceptions);
    }
    private XmlSerializer getSerializerInstance() throws XmlPullParserException {
        ArrayList<Exception> exceptions = null;
        if (serializerClasses != null && !serializerClasses.isEmpty()) {
            exceptions = new ArrayList<Exception>();
            for (Object o : serializerClasses) {
                try {
                    if (o != null) {
                        Class<?> serializerClass = (Class<?>) o;
                        return (XmlSerializer) serializerClass.newInstance();
                    }
                } catch (InstantiationException e) {
                    exceptions.add(e);
                } catch (IllegalAccessException e) {
                    exceptions.add(e);
                } catch (ClassCastException e) {
                    exceptions.add(e);
                }
            }
        }
        throw newInstantiationException("Invalid serializer class list", exceptions);
    }
    private static XmlPullParserException newInstantiationException(String message,
                                                                    ArrayList<Exception> exceptions) {
        if (exceptions == null || exceptions.isEmpty()) {
            return new XmlPullParserException(message);
        } else {
            XmlPullParserException exception = new XmlPullParserException(message);
            for (Exception ex : exceptions) {
                exception.addSuppressed(ex);
            }
            return exception;
        }
    }

    public XmlSerializer newSerializer() throws XmlPullParserException {
        return getSerializerInstance();
    }
    public static XmlPullParserFactory newInstance () throws XmlPullParserException {
        return new XmlPullParserFactory();
    }
    public static XmlPullParserFactory newInstance (String unused, Class unused2)
            throws XmlPullParserException {
        return newInstance();
    }
}
