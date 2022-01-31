package com.zpj.http.parser;

import com.zpj.http.core.IHttp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Z-P-J
 */
public class HttpParserFactory implements IHttp.ParserFactory {

    private final List<IHttp.Parser> parserList = new ArrayList<>();

    public HttpParserFactory() {
        parserList.add(new StringParser());
        parserList.add(new JsonParser());
    }

    @Override
    public void register(IHttp.Parser parser) {
        synchronized (parserList) {
            parserList.add(parser);
        }
    }

    @Override
    public IHttp.Parser create(IHttp.Response response, Type type) {
        synchronized (parserList) {
            for (IHttp.Parser parser : parserList) {
                if (parser.accept(response, type)) {
                    return parser;
                }
            }
        }
        return null;
    }

}
