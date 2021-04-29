package com.clickbait.tflow.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.clickbait.tflow.config.ClickBaitModel;
import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.exchange.LinkScoreBatchRequest;
import com.clickbait.tflow.exchange.LinkScoreRequest;
import com.clickbait.tflow.utilities.ClickBaitModelUtilities;
import com.sun.net.httpserver.HttpExchange;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;

public class GetScores extends AbsRootController<String[], List<LinkScoreRequest>, Float> {
    private final DecimalFormat df = new DecimalFormat("#.######");

    public GetScores(HttpExchange exchange, DBCPDataSource dbConn, SavedModelBundle model, ClickBaitModel config,
            String userId) {
        super(exchange, dbConn, model, config, userId);
    }

    @Override
    public void run() {
        try (Connection con = dbConn.getConnection()) {
            List<LinkScoreRequest> link = classify(getBody(LinkScoreBatchRequest.class).getLinks().stream()
                    .map(a -> a.getLink()).toArray(String[]::new));
            LinkScoreBatchRequest a = new LinkScoreBatchRequest(link);
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
    protected Tensor<Float> getInputTensor(String[] input) {
        return ClickBaitModelUtilities.get().getUrl(input);
    }

    @Override
    protected List<LinkScoreRequest> convertToResult(Tensor<Float> output, String[] input) {
        int axiz0 = (int) output.shape()[0];
        int axiz1 = (int) output.shape()[1];
        float[][] result = output.copyTo(new float[axiz0][axiz1]);
        return convertToScoredLinks(result, input);
    }

    private List<LinkScoreRequest> convertToScoredLinks(float[][] res, String[] input) {
        List<LinkScoreRequest> found = new ArrayList<>();
        for (int i = 0; i < res.length; ++i) {
            found.add(new LinkScoreRequest(input[i], df.format(res[i][0])));
        }
        return found;
    }
}