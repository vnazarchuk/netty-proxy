package com.collective.delayedproxy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;

public class Server implements Callable<Boolean> {

    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private final int port;
    private final boolean isRead;
    private final boolean isWrite;
    private final String msg;

    private Server(Builder builder) {
        this.port = builder.port;
        this.isRead = builder.isRead;
        this.isWrite = builder.isWrite;
        this.msg = builder.msg;
    }

    public static void read(Socket socket, String msg) throws IOException {
        socket.setSoTimeout(4000);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        log.trace("reading input stream");
        String readMsg = reader.readLine();
        log.debug("read message: {}", readMsg);
        if (readMsg == null || !readMsg.equals(msg)) {
            throw new IOException();
        }
        reader.close();
    }

    public static void write(Socket socket, String msg) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        log.trace("writing to output stream");
        writer.write(msg);
        writer.close();
    }

    public Boolean call() {
        try {
            ServerSocket socket = new ServerSocket(port);
            socket.setSoTimeout(1000);
            try {
                log.trace("accepting socket");
                Socket client = socket.accept();
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

        private final int port;

        private boolean isRead = false;
        private boolean isWrite = false;
        private String msg = null;

        public Builder(int port) {
            this.port = port;
        }

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