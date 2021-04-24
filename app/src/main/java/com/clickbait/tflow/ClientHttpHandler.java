package com.clickbait.tflow;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.google.common.base.Strings;
import com.clickbait.tflow.config.DataSource;
import com.clickbait.tflow.config.Encryption;
import com.clickbait.tflow.config.Endpoint;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.enumerators.HttpMethodType;
import com.clickbait.tflow.controllers.ClientSocketThread;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClientHttpHandler implements HttpHandler {
    DBCPDataSource dsource;
    Map<String, Endpoint> endpoints;
    Encryption encryption;

    ClientHttpHandler(DataSource dbProperties, Map<String, Endpoint> endP, Encryption enc) {
        dsource = DBCPDataSource.getInstance(dbProperties);
        endpoints = endP;
        encryption = enc;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        HttpMethodType type = HttpMethodType.valueOf(exchange.getRequestMethod());
        String auth = getAuth(exchange);

        if (compare(uri, type, auth, endpoints.get("getScore"))) {
            new ClientSocketThread(exchange, dsource).run();
        } else {
            exchange.close();
        }
    }

    private boolean compare(URI uri, HttpMethodType type, String auth, Endpoint end) {
        return uri.equals(end.getPath()) && type.equals(end.getType()) && (!end.isAuthenticated()
                || (!Strings.isNullOrEmpty(auth) && auth.startsWith(encryption.getPrefix() + " ")));
    }

    private String getAuth(HttpExchange exchange) {
        Headers eh = exchange.getRequestHeaders();
        return eh.getFirst(encryption.getAuthHeader());
    }
}