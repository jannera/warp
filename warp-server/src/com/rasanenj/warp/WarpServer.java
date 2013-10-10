package com.rasanenj.warp;

import com.rasanenj.warp.messaging.MessageDelegator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.rasanenj.warp.Log.log;

/**
 * @author gilead
 */
public class WarpServer {

    public static void main( String[] args ) throws InterruptedException , IOException {
        int port = 9091; // 843 flash policy port
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }

        MessageDelegator delegator = new MessageDelegator();

        WSServer wsServer = new WSServer(port, delegator);

        ChatServer chatServer = new ChatServer(wsServer, delegator);
        new Thread(chatServer).start();
        wsServer.start();

        BattleLoop battleLoop = new BattleLoop(delegator, wsServer);
        new Thread(battleLoop).start();


        log("server started on port: " + wsServer.getPort());

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {
            String in = sysin.readLine();
        }
    }

}
