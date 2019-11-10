package com.didiglobal.booster.instrument.sharedpreferences.io;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author neighbWang
 */
public final class XmlUtils {

    public static void writeMapXml(final Map val, final OutputStream out) throws XmlPullParserException, java.io.IOException {
        final XmlSerializer serializer = new BoosterXmlSerializer();
        serializer.setOutput(out, "utf-8");
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        writeMapXml(val, null, serializer);
        serializer.endDocument();
    }

    private static void writeMapXml(final Map val, final String name, final XmlSerializer out) throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }
        final Set s = val.entrySet();
        final Iterator i = s.iterator();
        out.startTag(null, "map");
        if (name != null) {
            out.attribute(null, "name", name);
        }
        while (i.hasNext()) {
            final Map.Entry e = (Map.Entry) i.next();
            writeValueXml(e.getValue(), (String) e.getKey(), out);
        }
        out.endTag(null, "map");
    }

    private static void writeListXml(final List val, final String name, final XmlSerializer out) throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }
        out.startTag(null, "list");
        if (name != null) {
            out.attribute(null, "name", name);
        }
        final int N = val.size();
        int i = 0;
        while (i < N) {
            writeValueXml(val.get(i), null, out);
            i++;
        }
        out.endTag(null, "list");
    }

    private static void writeSetXml(Set val, String name, XmlSerializer out) throws XmlPullParserException, java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }
        out.startTag(null, "set");
        if (name != null) {
            out.attribute(null, "name", name);
        }
        for (Object v : val) {
            writeValueXml(v, null, out);
        }
        out.endTag(null, "set");
    }

    private static void writeByteArrayXml(final byte[] val, final String name, final XmlSerializer out) throws java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }
        out.startTag(null, "byte-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }
        out.attribute(null, "num", Integer.toString(val.length));
        final StringBuilder sb = new StringBuilder(val.length * 2);
        for (int b : val) {
            int h = b >> 4;
            sb.append(h >= 10 ? ('a' + h - 10) : ('0' + h));
            h = b & 0xff;
            sb.append(h >= 10 ? ('a' + h - 10) : ('0' + h));
        }
        out.text(sb.toString());
        out.endTag(null, "byte-array");
    }

    private static void writeIntArrayXml(final int[] val, final String name, final XmlSerializer out) throws java.io.IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }
        out.startTag(null, "int-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }
        out.attribute(null, "num", Integer.toString(val.length));
        for (int value : val) {
            out.startTag(null, "item");
            out.attribute(null, "value", Integer.toString(value));
            out.endTag(null, "item");
        }
        out.endTag(null, "int-array");
    }

    private static void writeValueXml(final Object v, final String name, final XmlSerializer out) throws XmlPullParserException, java.io.IOException {
        String typeStr;
        if (v == null) {
            out.startTag(null, "null");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.endTag(null, "null");
            return;
        } else if (v instanceof String) {
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else if (v instanceof Integer) {
            typeStr = "int";
        } else if (v instanceof Long) {
            typeStr = "long";
        } else if (v instanceof Float) {
            typeStr = "float";
        } else if (v instanceof Double) {
            typeStr = "double";
        } else if (v instanceof Boolean) {
            typeStr = "boolean";
        } else if (v instanceof byte[]) {
            writeByteArrayXml((byte[]) v, name, out);
            return;
        } else if (v instanceof int[]) {
            writeIntArrayXml((int[]) v, name, out);
            return;
        } else if (v instanceof Map) {
            writeMapXml((Map) v, name, out);
            return;
        } else if (v instanceof List) {
            writeListXml((List) v, name, out);
            return;
        } else if (v instanceof Set) {
            writeSetXml((Set) v, name, out);
            return;
        } else if (v instanceof CharSequence) {
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else {
            throw new RuntimeException("writeValueXml: unable to write value " + v);
        }
        out.startTag(null, typeStr);
        if (name != null) {
            out.attribute(null, "name", name);
        }
        out.attribute(null, "value", v.toString());
        out.endTag(null, typeStr);
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> readMapXml(final InputStream in) throws XmlPullParserException, java.io.IOException {
        final XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, null);
        return (HashMap<String, Object>) readValueXml(parser, new String[1]);
    }

    private static Map<String, Object> readThisMapXml(XmlPullParser parser, String[] name) throws XmlPullParserException, java.io.IOException {
        final Map<String, Object> map = new HashMap<>();
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                if (name[0] != null) {
                    map.put(name[0], val);
                } else {
                    throw new XmlPullParserException("Map value without name attribute: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if ("map".equals(parser.getName())) {
                    return map;
                }
                throw new XmlPullParserException("Expected map end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);
        throw new XmlPullParserException("Document ended before map end tag");
    }

    private static List<Object> readThisListXml(final XmlPullParser parser, final String[] name)
            throws XmlPullParserException, java.io.IOException {
        final List<Object> list = new ArrayList<>();
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                list.add(val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals("list")) {
                    return list;
                }
                throw new XmlPullParserException("Expected list end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);
        throw new XmlPullParserException("Document ended before list end tag");
    }

    private static Set<Object> readThisSetXml(XmlPullParser parser, String[] name) throws XmlPullParserException, java.io.IOException {
        final Set<Object> set = new HashSet<>();
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name);
                set.add(val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals("set")) {
                    return set;
                }
                throw new XmlPullParserException("Expected set end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before set end tag");
    }

    private static int[] readThisIntArrayXml(final XmlPullParser parser) throws XmlPullParserException, java.io.IOException {
        int num;
        try {
            num = Integer.parseInt(parser.getAttributeValue(null, "num"));
        } catch (NullPointerException e) {
            throw new XmlPullParserException("Need num attribute in byte-array");
        } catch (NumberFormatException e) {
            throw new XmlPullParserException("Not a number in num attribute in byte-array");
        }
        final int[] array = new int[num];
        int i = 0;
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    try {
                        array[i] = Integer.parseInt(parser.getAttributeValue(null, "value"));
                    } catch (NullPointerException e) {
                        throw new XmlPullParserException("Need value attribute in item");
                    } catch (NumberFormatException e) {
                        throw new XmlPullParserException("Not a number in value attribute in item");
                    }
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if ("int-array".equals(parser.getName())) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected `int-array` end tag at: " + parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);
        throw new XmlPullParserException("Document ended before `int-array` end tag");
    }

    private static Object readValueXml(XmlPullParser parser, String[] name)
            throws XmlPullParserException, java.io.IOException {
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                return readThisValueXml(parser, name);
            } else if (eventType == parser.END_TAG) {
                throw new XmlPullParserException("Unexpected end tag at: " + parser.getName());
            } else if (eventType == parser.TEXT) {
                throw new XmlPullParserException("Unexpected text: " + parser.getText());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);
        throw new XmlPullParserException("Unexpected end of document");
    }

    private static Object readThisValueXml(final XmlPullParser parser, final String[] name) throws XmlPullParserException, java.io.IOException {
        final String valueName = parser.getAttributeValue(null, "name");
        final String tagName = parser.getName();
        Object res;
        switch (tagName) {
            case "null":
                res = null;
                break;
            case "string":
                final StringBuilder value = new StringBuilder();
                int eventType;
                while ((eventType = parser.next()) != parser.END_DOCUMENT) {
                    if (eventType == parser.END_TAG) {
                        if (parser.getName().equals("string")) {
                            name[0] = valueName;
                            return value.toString();
                        }
                        throw new XmlPullParserException("Unexpected end tag in <string>: " + parser.getName());
                    } else if (eventType == parser.TEXT) {
                        value.append(parser.getText());
                    } else if (eventType == parser.START_TAG) {
                        throw new XmlPullParserException("Unexpected start tag in <string>: " + parser.getName());
                    }
                }
                throw new XmlPullParserException("Unexpected end of document in <string>");
            case "int":
                res = Integer.parseInt(parser.getAttributeValue(null, "value"));
                break;
            case "long":
                res = Long.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "float":
                res = new Float(parser.getAttributeValue(null, "value"));
                break;
            case "double":
                res = new Double(parser.getAttributeValue(null, "value"));
                break;
            case "boolean":
                res = Boolean.valueOf(parser.getAttributeValue(null, "value"));
                break;
            case "int-array":
                parser.next();
                res = readThisIntArrayXml(parser);
                name[0] = valueName;
                return res;
            case "map":
                parser.next();
                res = readThisMapXml(parser, name);
                name[0] = valueName;
                return res;
            case "list":
                parser.next();
                res = readThisListXml(parser, name);
                name[0] = valueName;
                return res;
            case "set":
                parser.next();
                res = readThisSetXml(parser, name);
                name[0] = valueName;
                return res;
            default:
                throw new XmlPullParserException("Unknown tag: " + tagName);
        }
        int eventType;
        while ((eventType = parser.next()) != parser.END_DOCUMENT) {
            if (eventType == parser.END_TAG) {
                if (parser.getName().equals(tagName)) {
                    name[0] = valueName;
                    return res;
                }
                throw new XmlPullParserException("Unexpected end tag in <" + tagName + ">: " + parser.getName());
            } else if (eventType == parser.TEXT) {
                throw new XmlPullParserException("Unexpected text in <" + tagName + ">: " + parser.getName());
            } else if (eventType == parser.START_TAG) {
                throw new XmlPullParserException("Unexpected start tag in <" + tagName + ">: " + parser.getName());
            }
        }
        throw new XmlPullParserException("Unexpected end of document in <" + tagName + ">");
    }
}