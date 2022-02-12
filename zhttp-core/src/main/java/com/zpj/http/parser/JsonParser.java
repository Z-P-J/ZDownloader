package com.zpj.http.parser;

import com.zpj.http.core.IHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Pattern;

public class JsonParser implements IHttp.Parser {

    private static final Pattern JSON_CONTENT_TYPE_RXP = Pattern.compile("(application|text)/\\w*\\+?json.*");

    @Override
    public boolean accept(IHttp.Response response, Type type) {
        return JSON_CONTENT_TYPE_RXP.matcher(response.contentType()).matches()
                && (type == JSONObject.class || type == JSONArray.class);
    }

    @Override
    public Object parse(IHttp.Response response, Type type) throws IOException {
        try {
            if (type == JSONObject.class) {
                return new JSONObject(response.bodyString());
            } else if (type == JSONArray.class) {
                return new JSONArray(response.bodyString());
            }
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } finally {
            response.close();
        }
    }

}
