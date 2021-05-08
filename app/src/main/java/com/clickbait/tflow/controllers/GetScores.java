package com.clickbait.tflow.controllers;

import java.sql.Array;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    public GetScores(HttpExchange exchange, DBCPDataSource dbConn, SavedModelBundle model, ClickBaitModel config,
            String userId) {
        super(exchange, dbConn, model, config, userId);
    }

    @Override
    public void run() {
        try (Connection con = dbConn.getConnection()) {
            List<LinkScoreRequest> link = classify(getBody(LinkScoreBatchRequest.class).getLinks().stream()
                    .map(a -> a.getName()).toArray(String[]::new));
            LinkScoreBatchRequest a = new LinkScoreBatchRequest(link);
            exchange.sendResponseHeaders(200, a.toString().length());
            OutputStream os = exchange.getResponseBody();
            os.write(a.toString().getBytes());
            os.close();

            updateDB(con, link);
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
    protected void updateDB(Connection con, List<LinkScoreRequest> input) throws SQLException {
        PreparedStatement cs = con
                .prepareStatement("CALL tflow.insert_links(?::tflow.link_type[],?::tflow.bait_score[])");
        Array names = con.createArrayOf("tflow.link_type", input.stream().map(b -> b.getName()).toArray());
        Array scores = con.createArrayOf("tflow.bait_score", input.stream().map(b -> b.getScore()).toArray());
        cs.setArray(1, names);
        cs.setArray(2, scores);
        cs.executeUpdate();
        cs.close();
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
            found.add(new LinkScoreRequest(input[i], res[i]));
        }
        return found;
    }
}