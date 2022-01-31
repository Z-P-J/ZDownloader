package com.zpj.http.utils;

import android.text.TextUtils;

import com.zpj.http.constant.Defaults;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CharsetUtil {

    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*(?:[\"'])?([^\\s,;\"']*)");

    private CharsetUtil() {}

    /**
     * Parse out a charset from a content type header. If the charset is not supported, returns null (so the default
     * will kick in.)
     * @param contentType e.g. "text/html; charset=EUC-JP"
     * @return "EUC-JP", or null if not found. Charset is trimmed and uppercased.
     */
    public static String getCharsetFromContentType(String contentType) {
        if (contentType != null) {
            Matcher m = CHARSET_PATTERN.matcher(contentType);
            if (m.find()) {
                String charset = m.group(1).trim();
                charset = charset.replace("charset=", "");
                return validateCharset(charset);
            }
        }
        return Defaults.CHARSET;
    }

    public static String validateCharset(String cs) {
        if (!TextUtils.isEmpty(cs)) {
            cs = cs.trim().replaceAll("[\"']", "");
            try {
                if (Charset.isSupported(cs)) {
                    return cs;
                }
                cs = cs.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(cs)) {
                    return cs;
                }
            } catch (IllegalCharsetNameException e) {
                // if our this charset matching fails.... we just take the default
            }
        }
        return null;
    }

}
