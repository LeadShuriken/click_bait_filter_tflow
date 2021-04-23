package com.clickbait.tflow;

import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable {

    protected int serverPort;
    protected HttpServer server;
    protected ClientHttpThreadPool threadpool;
    protected boolean isStopped = false;

    public ThreadPooledServer() {
        Map<String, LinkedHashMap<String, Object>> config = new Yaml()
                .load(this.getClass().getClassLoader().getResourceAsStream("application.yml"));
        this.serverPort = (int) config.get("server").get("port");
    }

    public synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void setStopped(Boolean stop) {
        this.isStopped = stop;
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            server.stop(0);
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Error Found stopping server socket");
            System.exit(-1);
        }
    }

    @Override
    public void run() {
        try {
            server = HttpServer.create(new InetSocketAddress(serverPort), 0);
            threadpool = new ClientHttpThreadPool();
            server.createContext("/test", threadpool);
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
        } catch (IOException ioe) {
            System.out.printf("Could not create server socket on port %d. Quitting.", serverPort);
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("It is now : " + formatter.format(now.getTime()));
    }
}
