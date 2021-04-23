package com.clickbait.tflow;

public class ClickBaitTensorFlow {

    public static void main(String[] args) {

        ThreadPooledServer server = new ThreadPooledServer();
        new Thread(server).start();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Stopping Server");
        server.stop();
    }
}
