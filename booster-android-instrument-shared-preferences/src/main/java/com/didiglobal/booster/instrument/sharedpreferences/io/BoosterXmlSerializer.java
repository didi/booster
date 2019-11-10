package com.didiglobal.booster.instrument.sharedpreferences.io;


import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @author neighbWang
 */
class BoosterXmlSerializer implements XmlSerializer {

    private static final String[] ESCAPE_TABLE = new String[]{
            null, null, null, null, null, null, null, null,  // 0-7
            null, null, null, null, null, null, null, null,  // 8-15
            null, null, null, null, null, null, null, null,  // 16-23
            null, null, null, null, null, null, null, null,  // 24-31
            null, null, "&quot;", null, null, null, "&amp;", null,  // 32-39
            null, null, null, null, null, null, null, null,  // 40-47
            null, null, null, null, null, null, null, null,  // 48-55
            null, null, null, null, "&lt;", null, "&gt;", null,  // 56-63
    };

    private static final int BUFFER_LEN = 8192;
    private final char[] mText = new char[BUFFER_LEN];
    private int mPos;
    private Writer mWriter;
    private OutputStream mOutputStream;
    private CharsetEncoder mCharset;
    private ByteBuffer mBytes = ByteBuffer.allocate(BUFFER_LEN);
    private boolean mInTag;

    @Override
    public XmlSerializer attribute(final String namespace, final String name, final String value) throws IOException, IllegalArgumentException, IllegalStateException {
        append(' ');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        append("=\"");
        escapeAndAppendString(value);
        append('"');
        return this;
    }

    @Override
    public void cdsect(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void comment(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void docdecl(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        flush();
    }

    @Override
    public XmlSerializer endTag(final String namespace, final String name) throws IOException, IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(" />\n");
        } else {
            append("</");
            if (namespace != null) {
                append(namespace);
                append(':');
            }
            append(name);
            append(">\n");
        }
        mInTag = false;
        return this;
    }

    @Override
    public void entityRef(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    private void flushBytes() throws IOException {
        int position;
        if ((position = mBytes.position()) > 0) {
            mBytes.flip();
            mOutputStream.write(mBytes.array(), 0, position);
            mBytes.clear();
        }
    }

    @Override
    public void flush() throws IOException {
        if (mPos > 0) {
            if (mOutputStream != null) {
                final CharBuffer charBuffer = CharBuffer.wrap(mText, 0, mPos);
                CoderResult result = mCharset.encode(charBuffer, mBytes, true);
                while (true) {
                    if (result.isError()) {
                        throw new IOException(result.toString());
                    } else if (result.isOverflow()) {
                        flushBytes();
                        result = mCharset.encode(charBuffer, mBytes, true);
                        continue;
                    }
                    break;
                }
                flushBytes();
                mOutputStream.flush();
            } else {
                mWriter.write(mText, 0, mPos);
                mWriter.flush();
            }
            mPos = 0;
        }
    }

    @Override
    public int getDepth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getFeature(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNamespace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPrefix(final String namespace, boolean generatePrefix) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getProperty(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ignorableWhitespace(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processingInstruction(final String text) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFeature(final String name, final boolean state) throws IllegalArgumentException, IllegalStateException {
        if (name.equals("http://xmlpull.org/v1/doc/features.html#indent-output")) {
            return;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOutput(final OutputStream os, final String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        if (os == null)
            throw new IllegalArgumentException();
        try {
            mCharset = Charset.forName(encoding).newEncoder();
        } catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
            throw (UnsupportedEncodingException) (new UnsupportedEncodingException(encoding).initCause(e));
        }
        mOutputStream = os;
    }

    @Override
    public void setOutput(final Writer writer) throws IllegalArgumentException, IllegalStateException {
        mWriter = writer;
    }

    @Override
    public void setPrefix(final String prefix, final String namespace) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(final String name, final Object value) throws IllegalArgumentException, IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startDocument(final String encoding, final Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        append("<?xml version='1.0' encoding='utf-8' standalone='" + (standalone ? "yes" : "no") + "' ?>\n");
    }

    @Override
    public XmlSerializer startTag(final String namespace, final String name) throws IOException, IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(">\n");
        }
        append('<');
        if (namespace != null) {
            append(namespace);
            append(':');
        }
        append(name);
        mInTag = true;
        return this;
    }

    @Override
    public XmlSerializer text(final char[] buf, final int start, final int len) throws IOException, IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(">");
            mInTag = false;
        }
        escapeAndAppendString(buf, start, len);
        return this;
    }

    @Override
    public XmlSerializer text(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        if (mInTag) {
            append(">");
            mInTag = false;
        }
        escapeAndAppendString(text);
        return this;
    }

    private void append(final char c) throws IOException {
        int pos = mPos;
        if (pos >= (BUFFER_LEN - 1)) {
            flush();
            pos = mPos;
        }
        mText[pos] = c;
        mPos = pos + 1;
    }

    private void append(final String str, int i, final int length) throws IOException {
        if (length > BUFFER_LEN) {
            final int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(str, i, next < end ? BUFFER_LEN : (end - i));
                i = next;
            }
            return;
        }
        int pos = mPos;
        if ((pos + length) > BUFFER_LEN) {
            flush();
            pos = mPos;
        }
        str.getChars(i, i + length, mText, pos);
        mPos = pos + length;
    }

    private void append(final char[] buf, int i, final int length) throws IOException {
        if (length > BUFFER_LEN) {
            final int end = i + length;
            while (i < end) {
                int next = i + BUFFER_LEN;
                append(buf, i, next < end ? BUFFER_LEN : (end - i));
                i = next;
            }
            return;
        }
        int pos = mPos;
        if ((pos + length) > BUFFER_LEN) {
            flush();
            pos = mPos;
        }
        System.arraycopy(buf, i, mText, pos, length);
        mPos = pos + length;
    }

    private void append(final String str) throws IOException {
        append(str, 0, str.length());
    }

    private void escapeAndAppendString(final String string) throws IOException {
        final int N = string.length();
        final char NE = (char) ESCAPE_TABLE.length;
        int lastPos = 0;
        int pos;
        for (pos = 0; pos < N; pos++) {
            char c = string.charAt(pos);
            if (c >= NE) continue;
            final String escape = ESCAPE_TABLE[c];
            if (escape == null) continue;
            if (lastPos < pos) append(string, lastPos, pos - lastPos);
            lastPos = pos + 1;
            append(escape);
        }
        if (lastPos < pos) append(string, lastPos, pos - lastPos);
    }

    private void escapeAndAppendString(final char[] buf, final int start, final int len) throws IOException {
        final char NE = (char) ESCAPE_TABLE.length;
        int end = start + len;
        int lastPos = start;
        int pos;
        for (pos = start; pos < end; pos++) {
            char c = buf[pos];
            if (c >= NE) continue;
            String escape = ESCAPE_TABLE[c];
            if (escape == null) continue;
            if (lastPos < pos) append(buf, lastPos, pos - lastPos);
            lastPos = pos + 1;
            append(escape);
        }
        if (lastPos < pos) append(buf, lastPos, pos - lastPos);
    }

}