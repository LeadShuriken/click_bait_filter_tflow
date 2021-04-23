package com.clickbait.tflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpExchange;

class ClientSocketThread extends Thread {
    HttpExchange exchange;
    OutputStream os;

    public ClientSocketThread() {
        super();
    }

    ClientSocketThread(HttpExchange ex) {
        exchange = ex;
    }

    public void run() {
        try {
            String caller = exchange.getLocalAddress().getHostName();
            System.out.println("Ping From - " + caller);
            String response = "Echo " + caller;

            exchange.sendResponseHeaders(200, response.length());
            os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}