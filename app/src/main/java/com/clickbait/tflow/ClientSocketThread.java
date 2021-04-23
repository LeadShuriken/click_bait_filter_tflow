package com.clickbait.tflow;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import com.sun.net.httpserver.HttpExchange;

class ClientSocketThread extends Thread {
    HttpExchange exchange;
    DBCPDataSource dsource;

    public ClientSocketThread() {
        super();
    }

    ClientSocketThread(HttpExchange ex, DBCPDataSource ds) {
        exchange = ex;
        dsource = ds;
    }

    public void run() {
        try (Connection con = dsource.getConnection()) {
            String caller = exchange.getLocalAddress().getHostName();
            System.out.println("Ping From - " + caller);
            String response = "Echo " + caller;

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}