package com.zpj.downloader.utils;

import android.util.Log;

import com.zpj.http.core.IHttp;
import com.zpj.http.io.ConstrainableInputStream;
import com.zpj.http.utils.StringUtil;
import com.zpj.http.utils.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Internal static utilities for handling data.
 *
 */
public final class DataUtil {
    private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)");
    public static final String defaultCharset = "UTF-8"; // used if not found in header or meta charset
    private static final int firstReadBufferSize = 1024 * 5;
    public static final int bufferSize = 1024 * 512; // 1024 * 32
    private static final char[] mimeBoundaryChars =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    static final int boundaryLength = 32;

    private DataUtil() {}

//    /**
//     * Writes the input stream to the output stream. Doesn't close them.
//     * @param in input stream to read from
//     * @param out output stream to write to
//     * @throws IOException on IO error
//     */
//    public static void crossStreams(final InputStream in, final OutputStream out) throws IOException {
//        crossStreams(in, out, null);
//    }

    /**
     * Writes the input stream to the output stream. Doesn't close them.
     * @param in input stream to read from
     * @param out output stream to write to
     * @throws IOException on IO error
     */
    public static void crossStreams(final InputStream in, final OutputStream out, IHttp.OnStreamWriteListener listener) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
            if (listener == null || listener.shouldContinue()) {
                Log.d("crossStreams", "len=" + len);
                out.write(buffer, 0, len);
                Log.d("crossStreams", "write");
                out.flush();
                Log.d("crossStreams", "flush");
                Log.d("crossStreams", "onBytesWritten");
                if (listener != null) {
                    listener.onBytesWritten(len);
                }
                Log.d("crossStreams", "----------------------------------------------");
            } else {
                Log.d("crossStreams", "stop");
                throw new IOException("Stop Post by User!");
            }

        }
    }

    /**
     * Read the input stream into a byte buffer. To deal with slow input streams, you may interrupt the thread this
     * method is executing on. The data read until being interrupted will be available.
     * @param inStream the input stream to read from
     * @param maxSize the maximum size in bytes to read from the stream. Set to 0 to be unlimited.
     * @return the filled byte buffer
     * @throws IOException if an exception occurs whilst reading from the input stream.
     */
    public static ByteBuffer readToByteBuffer(InputStream inStream, long maxSize) throws IOException {
        Validate.isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");
        final ConstrainableInputStream input = ConstrainableInputStream.wrap(inStream, bufferSize, maxSize);
        return input.readToByteBuffer(maxSize);
    }

    public static ByteBuffer emptyByteBuffer() {
        return ByteBuffer.allocate(0);
    }

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default
     * will kick in.)
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */
    public static String getCharsetFromContentType(String contentType) {
        if (contentType == null) return null;
        Matcher m = charsetPattern.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            charset = charset.replace("charset=", "");
            return validateCharset(charset);
        }
        return defaultCharset;
    }

    private static String validateCharset(String cs) {
        if (cs == null || cs.length() == 0) return null;
        cs = cs.trim().replaceAll("[\"']", "");
        try {
            if (Charset.isSupported(cs)) return cs;
            cs = cs.toUpperCase(Locale.ENGLISH);
            if (Charset.isSupported(cs)) return cs;
        } catch (IllegalCharsetNameException e) {
            // if our this charset matching fails.... we just take the default
        }
        return null;
    }

    /**
     * Creates a random string, suitable for use as a mime boundary
     */
    public static String mimeBoundary() {
        final StringBuilder mime = StringUtil.borrowBuilder();
        final Random rand = new Random();
        for (int i = 0; i < boundaryLength; i++) {
            mime.append(mimeBoundaryChars[rand.nextInt(mimeBoundaryChars.length)]);
        }
        return StringUtil.releaseBuilder(mime);
    }

    private static BomCharset detectCharsetFromBom(final ByteBuffer byteData) {
        final Buffer buffer = byteData; // .mark and rewind used to return Buffer, now ByteBuffer, so cast for backward compat
        buffer.mark();
        byte[] bom = new byte[4];
        if (byteData.remaining() >= bom.length) {
            byteData.get(bom);
            buffer.rewind();
        }
        if (bom[0] == 0x00 && bom[1] == 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF || // BE
            bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == 0x00 && bom[3] == 0x00) { // LE
            return new BomCharset("UTF-32", false); // and I hope it's on your system
        } else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF || // BE
            bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
            return new BomCharset("UTF-16", false); // in all Javas
        } else if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            return new BomCharset("UTF-8", true); // in all Javas
            // 16 and 32 decoders consume the BOM to determine be/le; utf-8 should be consumed here
        }
        return null;
    }

    private static class BomCharset {
        private final String charset;
        private final boolean offset;

        public BomCharset(String charset, boolean offset) {
            this.charset = charset;
            this.offset = offset;
        }
    }
}
