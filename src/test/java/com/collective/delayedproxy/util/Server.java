package com.collective.delayedproxy.util;

import com.collective.delayedproxy.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class Server implements Callable<Boolean> {

    private final boolean isRead;
    private final boolean isWrite;
    private final String msg;

    private Server(Builder builder) {
        this.isRead = builder.isRead;
        this.isWrite = builder.isWrite;
        this.msg = builder.msg;
    }

    public static void read(Socket socket, String msg) throws IOException {
        socket.setSoTimeout(1000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("SERVER: reading");
        String readMsg = reader.readLine();
        System.out.println(readMsg);
        if (readMsg == null || !readMsg.equals(msg)) {
            throw new IOException();
        }
        reader.close();
    }

    public static void write(Socket socket, String msg) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        System.out.println("SERVER: writing");
        writer.write(msg);
        System.out.println("SERVER: closing");
        writer.close();
    }

    public Boolean call() {
        try {
            ServerSocket socket = new ServerSocket(Config.REMOTE_PORT);
            socket.setSoTimeout(1000);
            try {
                System.out.println("SERVER: waiting to accept");
                Socket client = socket.accept();
                System.out.println("SERVER: accepted");
                if (isRead)
                    read(client, msg);
                if (isWrite)
                    write(client, msg);
            } finally {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public static class Builder {

        private boolean isRead = false;
        private boolean isWrite = false;
        private String msg = null;

        public Server build() {
            return new Server(this);
        }

        public Builder read(String msg) {
            this.isRead = true;
            this.msg = msg;
            return this;
        }

        public Builder write(String msg) {
            this.isWrite = true;
            this.msg = msg;
            return this;
        }
    }
}