package com.clickbait.tflow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.common.base.Strings;

class ClientServiceThread extends Thread {
    ThreadPooledServer server;
    Socket myClientSocket;
    boolean currThread = true;

    public ClientServiceThread() {
        super();
    }

    ClientServiceThread(Socket s, ThreadPooledServer s2) {
        server = s2;
        myClientSocket = s;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        System.out.println("Accepted Client Address - " + myClientSocket.getInetAddress().getHostName());
        try {
            in = new BufferedReader(new InputStreamReader(myClientSocket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(myClientSocket.getOutputStream()));

            while (currThread) {
                String clientCommand = in.readLine();
                System.out.println("Client Says : " + clientCommand);

                if (server.isStopped()) {
                    System.out.print("Server has already stopped");
                    out.println("Server has already stopped");
                    out.flush();
                    currThread = false;
                }

                if (!Strings.isNullOrEmpty(clientCommand)) {
                    if (clientCommand.equalsIgnoreCase("quit")) {
                        currThread = false;
                        System.out.print("Stopping client thread for client : ");
                    } else if (clientCommand.equalsIgnoreCase("end")) {
                        currThread = false;
                        System.out.print("Stopping client thread for client : ");
                        server.setStopped(true);
                    } else {
                        out.println("Server Says : " + clientCommand);
                        out.flush();
                    }
                } else {
                    out.flush();
                    throw new NullPointerException("No command");
                }
            }
        } catch (Exception e) {
        } finally {
            flush(in, out);
        }
    }

    void flush(BufferedReader in, PrintWriter out) {
        try {
            in.close();
            out.close();
            myClientSocket.close();
            System.out.println("...Stopped");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}