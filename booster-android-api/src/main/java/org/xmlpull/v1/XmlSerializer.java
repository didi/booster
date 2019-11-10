package org.xmlpull.v1;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author neighbWang
 */
public interface XmlSerializer {

    void setFeature(String var1, boolean var2) throws IllegalArgumentException, IllegalStateException;

    boolean getFeature(String var1);

    void setProperty(String var1, Object var2) throws IllegalArgumentException, IllegalStateException;

    Object getProperty(String var1);

    void setOutput(OutputStream var1, String var2) throws IOException, IllegalArgumentException, IllegalStateException;

    void setOutput(Writer var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void startDocument(String var1, Boolean var2) throws IOException, IllegalArgumentException, IllegalStateException;

    void endDocument() throws IOException, IllegalArgumentException, IllegalStateException;

    void setPrefix(String var1, String var2) throws IOException, IllegalArgumentException, IllegalStateException;

    String getPrefix(String var1, boolean var2) throws IllegalArgumentException;

    int getDepth();

    String getNamespace();

    String getName();

    XmlSerializer startTag(String var1, String var2) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer attribute(String var1, String var2, String var3) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer endTag(String var1, String var2) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    XmlSerializer text(char[] var1, int var2, int var3) throws IOException, IllegalArgumentException, IllegalStateException;

    void cdsect(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void entityRef(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void processingInstruction(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void comment(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void docdecl(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void ignorableWhitespace(String var1) throws IOException, IllegalArgumentException, IllegalStateException;

    void flush() throws IOException;
}
