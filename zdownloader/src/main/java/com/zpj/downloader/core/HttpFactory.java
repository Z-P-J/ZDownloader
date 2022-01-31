package com.zpj.downloader.core;

public interface HttpFactory<REQ extends Request, RESP extends Response> {

    REQ createRequest();

    RESP createResponse(REQ request);

    HttpEngine<REQ, RESP> createHttpEngine();

}
