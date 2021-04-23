package com.clickbait.tflow;

import java.io.IOException;
import java.util.Properties;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClientHttpHandler implements HttpHandler {
    DBCPDataSource dsource;

    ClientHttpHandler(Properties dbProperties) {
         dsource = DBCPDataSource.getInstance(dbProperties);
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        new ClientSocketThread(t, dsource).run();
    }
}