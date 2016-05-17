package com.github.pocmo.sensordashboard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by neerajpaliwal on 18/05/16.
 */
public class ClientSocketManager {
    private Socket socket;
    String ip;
    int port;

    public ClientSocketManager(PreferenceStorage preferenceStorage){
        refresh(preferenceStorage);
    }

    public void refresh(PreferenceStorage preferenceStorage){
        this.ip = preferenceStorage.getSocketIp();
        this.port = preferenceStorage.getSocketPort();
        new Thread(new ClientThread()).start();
    }

    public void pushData(String data) throws Exception{
        if(socket == null)
            return;

        String str = data;
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())),
                true);
        out.println(str);
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(ip);

                socket = new Socket(serverAddr, port);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }
}
