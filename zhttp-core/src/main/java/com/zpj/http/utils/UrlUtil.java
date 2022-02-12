package com.zpj.http.utils;

import com.zpj.http.constant.Defaults;
import com.zpj.http.core.HttpConfig;
import com.zpj.http.core.IHttp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

public class UrlUtil {

    /**
     * Encodes the input URL into a safe ASCII URL string
     * @param url unescaped URL
     * @return escaped URL
     */
    public static String encodeUrl(String url) {
        try {
            URL u = new URL(url);
            return encodeUrl(u).toExternalForm();
        } catch (Exception e) {
            return url;
        }
    }

    public static URL encodeUrl(URL u) {
        try {
            //  odd way to encode urls, but it works!
            String urlS = u.toExternalForm(); // URL external form may have spaces which is illegal in new URL() (odd asymmetry)
            urlS = urlS.replaceAll(" ", "%20");
            final URI uri = new URI(urlS);
            return new URL(uri.toASCIIString());
        } catch (URISyntaxException | MalformedURLException e) {
            // give up and return the original input
            return u;
        }
    }

    /**
     * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
     * @param base the existing absolute base URL
     * @param relUrl the relative URL to resolve. (If it's already absolute, it will be returned)
     * @return the resolved absolute URL
     * @throws MalformedURLException if an error occurred generating the URL
     */
    public static URL resolve(URL base, String relUrl) throws MalformedURLException {
        // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
        if (relUrl.charAt(0) == '?') {
            relUrl = base.getPath() + relUrl;
        }
        // workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
        if (relUrl.charAt(0) == '.' && base.getFile().indexOf('/') != 0) {
            base = new URL(base.getProtocol(), base.getHost(), base.getPort(), "/" + base.getFile());
        }
        return encodeUrl(new URL(base, relUrl));
    }

    // for get url reqs, serialise the data map into the url
    public static void serialiseRequestUrl(HttpConfig config) throws IOException {
        URL in = config.url();
        StringBuilder url = new StringBuilder();
        boolean first = true;
        // reconstitute the query, ready for appends
        url.append(in.getProtocol())
                .append("://")
                .append(in.getAuthority()) // includes host, port
                .append(in.getPath())
                .append("?");
        if (in.getQuery() != null) {
            url.append(in.getQuery());
            first = false;
        }
        for (IHttp.KeyVal keyVal : config.data()) {
            Validate.isFalse(keyVal.hasInputStream(), "InputStream data not supported in URL query string.");
            if (!first)
                url.append('&');
            else
                first = false;
            url.append(URLEncoder.encode(keyVal.key(), Defaults.CHARSET))
                    .append('=')
                    .append(URLEncoder.encode(keyVal.value(), Defaults.CHARSET));
        }
        config.url(new URL(url.toString()));
        config.data().clear(); // moved into url as get params
    }

}
