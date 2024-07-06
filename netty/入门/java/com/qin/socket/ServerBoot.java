package com.qin.socket;

import java.io.IOException;

public class ServerBoot {

    public static void main(String[] args) throws IOException {
        Server server = new Server(8888);
        server.start();
    }

}
