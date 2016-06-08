package com.github.pocmo.sensordashboard.network;

import android.util.Log;
import android.widget.Toast;

import com.github.pocmo.sensordashboard.PreferenceStorage;

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
        this.socket = null;
        this.ip = preferenceStorage.getSocketIp();
        this.port = preferenceStorage.getSocketPort();
        new Thread(new ClientThread()).start();
    }

    public void pushData(String data) throws Exception{
        String str = data;
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())),
                true);
        out.println(str);
        Log.d("socketmanager", "Data uploaded successfully !!");
    }

    public void pushDataAsyncronously(final String data) throws Exception{
        new Thread(new Runnable() {
            @Override
            public void run() {
                PrintWriter out = null;
                try {
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                out.println(data);
                Log.d("socketmanager", "Data uploaded successfully !!");
            }
        }).start();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            Log.d("socketmanager", "Socket initializing start !!");

            try {
                InetAddress serverAddr = InetAddress.getByName(ip);

                socket = new Socket(serverAddr, port);
                Log.d("socketmanager", "Socket initialized successfully !!");
            } catch (UnknownHostException e1) {
                Log.d("socketmanager", "Socket initialized FAILED unknown host !!");
                e1.printStackTrace();
            } catch (IOException e1) {
                Log.d("socketmanager", "Socket initialized FAILED ioexception !!");
                e1.printStackTrace();
            }
            Log.d("socketmanager", "Socket initializing end !!");


        }

    }
}
