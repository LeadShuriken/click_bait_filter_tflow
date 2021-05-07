package com.clickbait.tflow.controllers;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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

public class GetScores extends AbsRootController<String[], List<LinkScoreRequest>> {
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
    protected Tensor getInputTensor(String[] input) {
        return ClickBaitModelUtilities.get().getUrl(input);
    }

    @Override
    protected List<LinkScoreRequest> convertToResult(Tensor tensor, String[] input) {
        int axiz0 = (int) tensor.shape().asArray()[0];
        float[] res = new float[axiz0];

        ByteBuffer bbuf = ClickBaitModelUtilities.get().getBuffer(tensor);

        FloatBuffer b = bbuf.asFloatBuffer();
        for (int i = 0; i < res.length; i++) {
            res[i] = b.get(i);
        }

        return convertToScoredLinks(res, input);
    }

    private List<LinkScoreRequest> convertToScoredLinks(float[] res, String[] input) {
        List<LinkScoreRequest> found = new ArrayList<>();
        for (int i = 0; i < res.length; ++i) {
            found.add(new LinkScoreRequest(input[i], df.format(res[i])));
        }
        return found;
    }
}