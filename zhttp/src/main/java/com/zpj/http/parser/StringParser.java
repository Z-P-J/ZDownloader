package com.zpj.http.parser;

import android.support.annotation.NonNull;
import android.util.Log;

import com.zpj.http.core.IHttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * 解析为字符串
 * @author Z-P-J
 */
public class StringParser implements IHttp.Parser<String> {

    private static final String TAG = "StringParser";

    @Override
    public String parse(IHttp.Response response) throws IOException {
        try (InputStream is = response.bodyStream()) {
            String body = streamToString(is, Charset.forName(response.charset()));
            if (response.config().debug()) {
                Log.d(TAG, "parse body=" + body);
            }
            return body;
        } finally {
            response.close();
        }

    }

    private static String streamToString(InputStream is, @NonNull Charset charset) throws IOException {
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
