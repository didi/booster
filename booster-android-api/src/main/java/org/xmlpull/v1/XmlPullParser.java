package org.xmlpull.v1;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author neighbWang
 */
public interface XmlPullParser {

    int END_DOCUMENT = 1;
    int END_TAG = 3;
    int START_TAG = 2;
    int TEXT = 4;

    void setInput(InputStream var1, String var2) throws XmlPullParserException;

    String getText();

    String getName();

    String getAttributeValue(String var1, String var2);

    int getEventType() throws XmlPullParserException;

    int next() throws IOException, XmlPullParserException;

}
