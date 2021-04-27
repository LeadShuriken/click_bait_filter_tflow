package com.clickbait.tflow.controllers;

import java.io.OutputStream;
import java.sql.Connection;

import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.exchange.LinkScoreRequest;
import com.sun.net.httpserver.HttpExchange;
import org.tensorflow.TensorFlow;

public class GetScore extends RootController {

    public GetScore(HttpExchange exchange, DBCPDataSource connection) {
        super(exchange, connection);
    }

    public void run() {
        try (Connection con = connection.getConnection()) {
            String response = TensorFlow.version() + " " + getBody(LinkScoreRequest.class);
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}