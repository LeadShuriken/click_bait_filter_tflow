package com.clickbait.tflow;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

import com.clickbait.tflow.config.ApplicationConfig;
import com.clickbait.tflow.config.ClickBaitModel;
import com.clickbait.tflow.config.Encryption;
import com.clickbait.tflow.config.Endpoint;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.enumerators.HttpMethodType;
import com.clickbait.tflow.security.JWTUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.clickbait.tflow.controllers.GetScore;
import com.clickbait.tflow.controllers.GetScores;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.tensorflow.SavedModelBundle;

import io.jsonwebtoken.JwtException;

public class ClientHttpHandler implements HttpHandler {
    private final DBCPDataSource dsource;
    private final Map<String, Endpoint> endpoints;
    private final Encryption encryption;
    private final SavedModelBundle nlpModel;
    private final ClickBaitModel nlpConfig;
    private final JWTUtils jwt;

    ClientHttpHandler(ApplicationConfig config) throws InvalidProtocolBufferException {
        jwt = new JWTUtils(config.getEncryption().getJwtConfig());
        dsource = DBCPDataSource.getInstance(config.getDatasource());
        nlpModel = SavedModelBundle.load(config.getModels().getClickBaitModel().getModelPath(), "serve");
        var signatureDef = nlpModel.metaGraphDef().getSignatureDefMap().get("serving_default");

        nlpConfig = config.getModels().getClickBaitModel();

        nlpConfig.setInputTensorInfo(
                signatureDef.getInputsMap().values().stream().filter(Objects::nonNull).findFirst().orElse(null));
        nlpConfig.setOutputTensorInfo(
                signatureDef.getOutputsMap().values().stream().filter(Objects::nonNull).findFirst().orElse(null));

        endpoints = config.getEndpoints();
        encryption = config.getEncryption();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        URI uri = exchange.getRequestURI();
        HttpMethodType type = HttpMethodType.valueOf(exchange.getRequestMethod());
        String auth = getAuth(exchange);
        String userID = null;
        if (is(uri, type, auth, endpoints.get("getScore"), userID)) {
            new GetScore(exchange, dsource, nlpModel, nlpConfig, userID).run();
        } else if (is(uri, type, auth, endpoints.get("getScores"), userID)) {
            new GetScores(exchange, dsource, nlpModel, nlpConfig, userID).run();
        } else {
            exchange.sendResponseHeaders(400, 0);
            exchange.close();
        }
    }

    private boolean is(URI uri, HttpMethodType type, String auth, Endpoint end, String userID) {
        return isType(uri, type, end) && isAuthenticated(auth, end, userID);
    }

    private boolean isType(URI uri, HttpMethodType type, Endpoint end) {
        return uri.equals(end.getPath()) && type.equals(end.getType());
    }

    private boolean isAuthenticated(String auth, Endpoint end, String userID) {
        if (end.isAuthenticated() && (auth != null && auth.length() > encryption.getPrefix().length()
                && auth.startsWith(encryption.getPrefix() + " "))) {
            String token = auth.substring(encryption.getPrefix().length() + 1);
            try {
                String username = jwt.extractUsername(token);
                if (jwt.validateToken(token, username)) {
                    userID = username;
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