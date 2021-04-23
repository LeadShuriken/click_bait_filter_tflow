package com.clickbait.tflow;

import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedHashMap;

import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable {

    private HttpServer server;
    private ClientHttpHandler threadpool;
    private boolean isStopped = false;

    private int serverPort;
    private int threadPoolSize;
    private Properties dbProperties;

    public ThreadPooledServer() {
        Map<String, LinkedHashMap<String, Object>> config = new Yaml()
                .load(this.getClass().getClassLoader().getResourceAsStream("application.yml"));
        this.serverPort = (int) config.get("server").get("port");
        this.threadPoolSize = (int) config.get("server").get("threadPoolSize");

        LinkedHashMap<String, Object> datasource = config.get("datasource");
        dbProperties = new Properties();
        for (Map.Entry<String, Object> me : datasource.entrySet()) {
            dbProperties.setProperty(me.getKey(), (String) me.getValue());
        }
    }

    public synchronized boolean isStopped() {
        return this.isStopped;
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
            threadpool = new ClientHttpHandler(dbProperties);
            server.createContext("/test", threadpool);
            server.setExecutor(Executors.newFixedThreadPool(threadPoolSize));
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
