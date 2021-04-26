package com.clickbait.tflow;

import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.Calendar;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.net.InetSocketAddress;

import com.clickbait.tflow.config.ApplicationConfig;
import com.clickbait.tflow.config.Server;
import com.sun.net.httpserver.HttpServer;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable {

    private HttpServer server;
    private ApplicationConfig config;
    private boolean isStopped = false;

    public ThreadPooledServer() {
        Yaml yaml = new Yaml(new Constructor(ApplicationConfig.class));
        config = yaml.load(getClass().getClassLoader().getResourceAsStream("application.yml"));
    }

    public synchronized boolean isStopped() {
        return isStopped;
    }

    public synchronized void stop() {
        isStopped = true;
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
        Server a = config.getServer();
        try {
            server = HttpServer.create(new InetSocketAddress(a.getPort()), 0);
            server.createContext(a.getApi(), new ClientHttpHandler(config));
            server.setExecutor(Executors.newFixedThreadPool(a.getThreadPoolSize()));
            server.start();
        } catch (IOException ioe) {
            System.out.printf("Could not create server socket on port %d. Quitting.", a.getPort());
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("It is now : " + formatter.format(now.getTime()));
    }
}
