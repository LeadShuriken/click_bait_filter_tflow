package com.clickbait.tflow.controllers;

import java.io.OutputStream;

import com.clickbait.tflow.dataSource.DBCPDataSource;
import com.clickbait.tflow.exchange.LinkScoreRequest;
import com.clickbait.tflow.utilities.ClickBaitModelUtilities;
import com.sun.net.httpserver.HttpExchange;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

public class GetScore extends RootController {

    public GetScore(HttpExchange exchange, DBCPDataSource connection, SavedModelBundle nlpModel) {
        super(exchange, connection, nlpModel);
    }

    @Override
    public void run() {
        try (Session sess = nlpModel.session()) {
            Tensor<Integer> x = ClickBaitModelUtilities.get().getUrl(getBody(LinkScoreRequest.class).getLink());
            float[] y = sess.runner().feed("serving_default", x).fetch("dense_2").run().get(0)
                    .copyTo(new float[x.numElements()]);

            String response = y.toString();
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}