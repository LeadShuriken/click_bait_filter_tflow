package com.clickbait.tflow;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.clickbait.tflow.config.DataSource;
import com.clickbait.tflow.config.Encryption;
import com.clickbait.tflow.config.Endpoint;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.enumerators.HttpMethodType;
import com.clickbait.tflow.security.JWTUtils;
import com.clickbait.tflow.controllers.GetScore;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.jsonwebtoken.JwtException;

public class ClientHttpHandler implements HttpHandler {
    private final DBCPDataSource dsource;
    private final Map<String, Endpoint> endpoints;
    private final Encryption encryption;
    private final JWTUtils jwt;

    ClientHttpHandler(DataSource dbProperties, Map<String, Endpoint> endP, Encryption enc) {
        jwt = new JWTUtils(enc.getJwtConfig());
        dsource = DBCPDataSource.getInstance(dbProperties);
        endpoints = endP;
        encryption = enc;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        HttpMethodType type = HttpMethodType.valueOf(exchange.getRequestMethod());
        String auth = getAuth(exchange);
        if (is(uri, type, auth, endpoints.get("getScore"))) {
            new GetScore(exchange, dsource).run();
        } else {
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

    private boolean is(URI uri, HttpMethodType type, String auth, Endpoint end) {
        return isType(uri, type, end) && isAuthenticated(auth, end);
    }

    private boolean isType(URI uri, HttpMethodType type, Endpoint end) {
        return uri.equals(end.getPath()) && type.equals(end.getType());
    }

    private boolean isAuthenticated(String auth, Endpoint end) {
        if (end.isAuthenticated() && (auth != null && auth.length() > encryption.getPrefix().length()
                && auth.startsWith(encryption.getPrefix() + " "))) {
            String token = auth.substring(encryption.getPrefix().length() + 1);
            try {
                String username = jwt.extractUsername(token);
                if (jwt.validateToken(token, username)) {
                    return true;
                }
            } catch (JwtException e) {
                return false;
            }
        }
        return true;
    }

    private String getAuth(HttpExchange exchange) {
        Headers eh = exchange.getRequestHeaders();
        return eh.getFirst(encryption.getAuthHeader());
    }
}