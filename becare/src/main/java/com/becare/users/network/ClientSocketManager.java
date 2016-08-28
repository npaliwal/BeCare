package com.becare.users.network;
import android.util.Log;


import com.becare.users.PreferenceStorage;

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
    String error="";
    Callback callback;

    public ClientSocketManager(PreferenceStorage preferenceStorage) {
        restore(preferenceStorage);
    }

    public void refresh(String newIp, int newPort, Callback cbk) {
        this.socket = null;
        this.ip = newIp;
        this.port = newPort;
        this.callback = cbk;
        new Thread(new ClientThread()).start();
    }

    public void restore(PreferenceStorage preferenceStorage) {
        refresh(preferenceStorage.getSocketIp(), preferenceStorage.getSocketPort(), null);
    }

    public String getError(){
        return error;
    }

    public void pushData(String data) throws Exception{
        if(socket == null) {
            Exception e = new Exception("Socket error");
            throw e;
        }
        String str = data;
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())),
                true);
        out.println(str);
        Log.d("socketmanager", "Data uploaded successfully !!");
        if(callback != null){
            callback.onDataUploadSuccess();
        }
    }

    public void pushDataAsyncronously(final String data) throws Exception{
        if(socket == null) {
            Exception e = new Exception("Socket error");
            throw e;
        }

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
                if(callback != null){
                    callback.onDataUploadSuccess();
                }
            }
        }).start();
    }

    class ClientThread implements Runnable {
        @Override
        public void run() {
            Log.d("socketmanager", "Socket initializing start !!");
            error = "";
            try {
                InetAddress serverAddr = InetAddress.getByName(ip);

                socket = new Socket(serverAddr, port);
                Log.d("socketmanager", "Socket initialized successfully !!");
                if(callback != null){
                    callback.onSocketUpdateSuccess();
                }
            } catch (UnknownHostException e1) {
                Log.d("socketmanager", "Socket initialized FAILED unknown host !!");
                e1.printStackTrace();
                error = "Socket initialized FAILED unknown host !!";
                if(callback != null){
                    callback.onSocketUpdateError(error);
                }
            } catch (IOException e1) {
                Log.d("socketmanager", "Socket initialized FAILED ioexception !!");
                error = "Socket initialized FAILED ioexception !!";
                e1.printStackTrace();
                if(callback != null){
                    callback.onSocketUpdateError(error);
                }
            }
            Log.d("socketmanager", "Socket initializing end !!");


        }

    }

    public interface Callback{
        void onDataUploadSuccess();
        void onDataUploadError(String error);
        void onSocketUpdateSuccess();
        void onSocketUpdateError(String error);
    }
}
