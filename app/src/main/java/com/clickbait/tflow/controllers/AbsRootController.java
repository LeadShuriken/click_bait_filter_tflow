package com.clickbait.tflow.controllers;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import com.google.gson.Gson;
import com.clickbait.tflow.config.ClickBaitModel;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.interfaces.Classifier;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

public abstract class AbsRootController<I, O> extends Thread implements Classifier<I, O> {

    protected final HttpExchange exchange;
    protected final DBCPDataSource dbConn;
    protected final String userId;

    private final Gson gson;
    private final SavedModelBundle model;
    private final ClickBaitModel config;

    public abstract void run();

    protected abstract Tensor getInputTensor(I input);

    protected abstract O convertToResult(Tensor output, I input);

    protected abstract void updateDB(Connection con, O input) throws SQLException;

    protected AbsRootController(HttpExchange exchange, DBCPDataSource dbConn, SavedModelBundle model,
            ClickBaitModel config, String userId) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        this.gson = new Gson();
        this.model = model;
        this.config = config;
        this.exchange = exchange;
        this.dbConn = dbConn;
        this.userId = userId;
    }

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

    protected <N> N getBody(Class<N> n) throws JsonSyntaxException, IOException {
        return gson.fromJson(readString(exchange.getRequestBody()), n);
    }

    public O classify(I input) {
        Session sees = model.session();
        Tensor inputTensor = getInputTensor(input);
        List<Tensor> outputs = sees.runner().feed(config.getInputTensorInfo().getName(), inputTensor)
                .fetch(config.getOutputTensorInfo().getName()).run();

        try (Tensor output = (Tensor) outputs.get(0)) {
            return convertToResult(output, input);
        }
    }

    @Override
    public void close() {
        model.close();
    }
}
