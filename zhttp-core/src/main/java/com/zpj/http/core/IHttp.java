package com.zpj.http.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public interface IHttp {

    interface Parser {

        boolean accept(Response response, Type type);

        Object parse(Response response, Type type) throws IOException;
    }

    interface ParserFactory {

        void register(Parser parser);

        Parser create(Response response, Type type);

    }

    /**
     * GET and POST http methods.
     */
    enum Method {

        GET(false), POST(true),
        PUT(true), DELETE(false),
        PATCH(true), HEAD(false),
        OPTIONS(false), TRACE(false);

        private final boolean hasBody;

        Method(boolean hasBody) {
            this.hasBody = hasBody;
        }

        /**
         * Check if this HTTP method has/needs a request body
         * @return if body needed
         */
        public final boolean hasBody() {
            return hasBody;
        }
    }

    interface OnRedirectListener {
        boolean onRedirect(int redirectCount, String redirectUrl);
    }

    interface OnStreamWriteListener {
        /**
         * Called every time that a bunch of bytes were written to the body
         * @param bytesWritten number of written bytes
         */
        void onBytesWritten(int bytesWritten);

        boolean shouldContinue();
    }

    interface HttpDispatcher {

        void enqueue(Connection connection, Callback callback);

    }

    interface HttpEngine {

        Request createRequest(HttpConfig config);

        Connection createConnection(Request request);

        Response createResponse(Connection connection);

        void initSSL(boolean isAllowAllSSL);

    }

    interface Connection {

        Request request();

        HttpConfig config();

        Response execute() throws IOException;

        void enqueue(Callback callback);

        InputStream getBodyStream() throws IOException;

        int getStatusCode();

        String getStatusMessage();

        String getContentType();

        long getContentLength();

        Map<String, String> getHeaders();

        void disconnect();

    }


    /**
     * Represents a HTTP request.
     */
    interface Request {

        HttpConfig config();

        Connection connect();

    }

    /**
     * Represents a HTTP response.
     */
    interface Response {

        HttpConfig config();

        boolean hasHeader(String name);

        boolean hasHeaderWithValue(String name, String value);

        String header(String name);

        Map<String, String> headers();

        Map<String, String> cookies();

        String cookieStr();

        String cookie(String key);

        Method method();

        int statusCode();

        String statusMessage();

        String charset();

        String contentType();

        long contentLength();

        /**
         * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
         * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
         * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
         * @return the response body input stream
         */
        InputStream bodyStream() throws IOException;

        <T> T parse(Parser parser) throws IOException;

        <T> T parse(Type type) throws IOException;

        <T> T parse(Class<T> clazz) throws IOException;

        String bodyString() throws IOException;

        void close();

    }

    /**
     * A Key:Value tuple(+), used for form data.
     */
    interface KeyVal {

        /**
         * Update the key of a keyval
         * @param key new key
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal key(String key);

        /**
         * Get the key of a keyval
         * @return the key
         */
        String key();

        /**
         * Update the value of a keyval
         * @param value the new value
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal value(String value);

        /**
         * Get the value of a keyval
         * @return the value
         */
        String value();

        /**
         * Add or update an input stream to this keyVal
         * @param inputStream new input stream
         * @return this KeyVal, for chaining
         */
        IHttp.KeyVal inputStream(InputStream inputStream);

        /**
         * Get the input stream associated with this keyval, if any
         * @return input stream if set, or null
         */
        InputStream inputStream();

        /**
         * Does this keyval have an input stream?
         * @return true if this keyval does indeed have an input stream
         */
        boolean hasInputStream();

        /**
         * Set the Content Type header used in the MIME body (aka mimetype) when uploading files.
         * Only useful if {@link #inputStream(InputStream)} is set.
         * <p>Will default to {@code application/octet-stream}.</p>
         * @param contentType the new content type
         * @return this KeyVal
         */
        IHttp.KeyVal contentType(String contentType);

        /**
         * Get the current Content Type, or {@code null} if not set.
         * @return the current Content Type.
         */
        String contentType();

        IHttp.KeyVal setListener(IHttp.OnStreamWriteListener listener);

        IHttp.OnStreamWriteListener getListener();

    }

    // TODO Cookie管理
    interface CookieJar {

        Map<String, String> loadCookies(URL url);

        void saveCookies(URL url, Map<String, String> cookieMap);

    }

    interface Callback {

        void onFailure(Connection conn, IOException e);

        void onResponse(Connection conn, Response response) throws IOException;

    }

}
