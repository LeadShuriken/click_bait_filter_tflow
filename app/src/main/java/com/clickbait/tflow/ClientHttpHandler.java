package com.clickbait.tflow;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClientHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange t) throws IOException {
        new ClientSocketThread(t).run();
    }
}