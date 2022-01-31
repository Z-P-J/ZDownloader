package com.zpj.http.parser;

import android.util.Log;

import com.zpj.http.core.IHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * 解析为字符串
 * @author Z-P-J
 */
public class StringParser implements IHttp.Parser {

    private static final String TAG = "StringParser";

    @Override
    public boolean accept(IHttp.Response response, Type type) {
        return type == String.class;
    }

    @Override
    public String parse(IHttp.Response response, Type type) throws IOException {
        try (InputStream is = response.bodyStream()) {
            String body = StringParser.streamToString(is, Charset.forName(response.charset()));
            if (response.config().debug()) {
                Log.d(TAG, "parse body=" + body);
            }
            return body;
        } finally {
            response.close();
        }
    }

    public static String streamToString(InputStream is, Charset charset) throws IOException {
        String buf;
        try (InputStreamReader inputStreamReader = new InputStreamReader(is, charset);
             BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            buf = sb.toString();
            return buf;
        }
    }

}
