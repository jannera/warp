package com.rasanenj.warp;

import com.rasanenj.warp.messaging.MessageDelegator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author gilead
 */
public class WarpServer {

    public static void main( String[] args ) throws InterruptedException , IOException {
        int port = 8887; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }

        MessageDelegator delegator = new MessageDelegator();

        WSServer wsServer= new WSServer(port, delegator);

        ChatServer chatServer = new ChatServer(wsServer);
        chatServer.register(delegator);
        wsServer.start();

        System.out.println("server started on port: " + wsServer.getPort());

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
        }
    }

}
