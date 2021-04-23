package com.clickbait.tflow;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ClientHttpThreadPool implements HttpHandler {

    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Override
    public void handle(HttpExchange t) throws IOException {
        threadPool.execute(new ClientSocketThread(t));
    }

    public void stop() {
        threadPool.shutdown();
        this.stop();
    }
}