package com.clickbait.tflow.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.sun.net.httpserver.HttpExchange;

import org.tensorflow.SavedModelBundle;

import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

public abstract class RootController extends Thread {
    protected final SavedModelBundle nlpModel;
    protected final HttpExchange exchange;
    protected final DBCPDataSource connection;
    protected final Gson gson;

    protected RootController(HttpExchange exchange, DBCPDataSource connection, SavedModelBundle nlpModel) {
        this.nlpModel = nlpModel;
        this.exchange = exchange;
        this.connection = connection;
        this.gson = new Gson();
    }

    public abstract void run();

    private String readString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader sr = new InputStreamReader(is);
        char[] buf = new char[1024];
        int len;
        while ((len = sr.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }

    protected <T> T getBody(Class<T> a) throws JsonSyntaxException, IOException {
        return gson.fromJson(readString(exchange.getRequestBody()), a);
    }
}
