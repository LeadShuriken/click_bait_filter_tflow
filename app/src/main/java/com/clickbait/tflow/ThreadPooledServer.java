package com.clickbait.tflow;

import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class ThreadPooledServer implements Runnable {

    protected int serverPort;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(10);

    public ThreadPooledServer() {
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml");
        Map<String, LinkedHashMap<String, Object>> obj = yaml.load(inputStream);
        this.serverPort = (int) obj.get("server").get("port");
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
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException ioe) {
            System.out.printf("Could not create server socket on port %d. Quitting.", serverPort);
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("It is now : " + formatter.format(now.getTime()));

        while (!isStopped()) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientServiceThread(clientSocket, this));
            } catch (IOException ioe) {
                System.out.println("Exception found on accept. Ignoring. Stack Trace :");
                ioe.printStackTrace();
            }
        }
        try {
            serverSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Error Found stopping server socket");
            System.exit(-1);
        }
    }

}
