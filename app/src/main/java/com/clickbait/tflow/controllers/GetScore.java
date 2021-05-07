package com.clickbait.tflow.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.clickbait.tflow.config.ClickBaitModel;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.exchange.LinkScoreRequest;
import com.clickbait.tflow.utilities.ClickBaitModelUtilities;
import com.sun.net.httpserver.HttpExchange;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

public class GetScore extends AbsRootController<String, LinkScoreRequest> {

    public GetScore(HttpExchange exchange, DBCPDataSource dbConn, SavedModelBundle model, ClickBaitModel config,
            String userId) {
        super(exchange, dbConn, model, config, userId);
    }

    @Override
    public void run() {
        try {
            String link = getBody(LinkScoreRequest.class).getName();
            LinkScoreRequest a = classify(link);
            exchange.sendResponseHeaders(200, a.toString().length());
            OutputStream os = exchange.getResponseBody();
            os.write(a.toString().getBytes());
            os.close();
        } catch (Exception e) {
            try {
                exchange.sendResponseHeaders(400, 0);
                exchange.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    @Override
    protected Tensor getInputTensor(String input) {
        return ClickBaitModelUtilities.get().getUrl(input);
    }

    @Override
    protected LinkScoreRequest convertToResult(Tensor tensor, String input) {
        ByteBuffer bbuf = ClickBaitModelUtilities.get().getBuffer(tensor);
        return new LinkScoreRequest(input, bbuf.asFloatBuffer().get(0));
    }
}