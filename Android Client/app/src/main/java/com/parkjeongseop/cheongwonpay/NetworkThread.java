package com.parkjeongseop.cheongwonpay;


import android.net.Network;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by 정섭 on 2016-08-21.
 */
public class NetworkThread extends HandlerThread {

    public static NetworkThread instance = null;

    public Handler networkHandler = null;

    public Socket socket = null;
    public DataOutputStream dos = null;
    public DataInputStream dis = null;

    public static final int OP_LOGIN = 1, OP_PURCHASE = 2, OP_ADD_ITEM = 3, OP_RF_BAL = 4;

    public static final String SERVER_IP = "192.168.0.105";
    public static final int SERVER_PORT = 8888;

    public static void prepare(){
        if(instance!=null) return;
        instance = new NetworkThread("NetworkThread");
        instance.start();
    }

    private NetworkThread(String name) {
        super(name);
    }

    private void connectSocket(){
        try {
            Log.d("NetworkThread", "Try connect");
            socket = new Socket(SERVER_IP, SERVER_PORT);
            Log.d("NetworkThread", "Socket connected");
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.d("NetworkThread", e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void run() {
        Log.d("NetworkThread", "run()");

        connectSocket();
        Looper.prepare(); //건드리면 안댐

        networkHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d("NetworkThread", "Current instance : " + this.toString());
                Log.d("hello", "asjfdslafi");
                switch (msg.what) {
                    case OP_LOGIN:
                        Log.d("NetworkThread", "Start login");
                        if (socket==null || !socket.isConnected()) {
                            connectSocket();
                        }

                        if(socket==null || !socket.isConnected()){
                            //TODO: 인터넷이 꺼져있거나, 서버가 닫혀있는 경우
                            return;
                        }

                        try {

                            dos.writeInt(OP_LOGIN);
                            dos.writeUTF((String) msg.obj);

                            String result;

                            result = dis.readUTF();


                            Message res= new Message();
                            if (result.equals("Login Success!")) {
                                res.what = LoginActivity.LOGIN_OK;
                                MainActivity.CLUB_ID = msg.getData().getString("ID");
                            } else {
                                res.what = LoginActivity.LOGIN_FAIL;
                            }
                            LoginActivity.mHandler.sendMessage(res);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case OP_PURCHASE:

                        break;
                    case OP_RF_BAL:
                        try {
                            dos.writeInt(OP_RF_BAL);
                            dos.writeUTF((String) msg.obj);//바코드전송

                            String result;//잔액
                            result = dis.readUTF();

                            Message res= new Message();
                            res.what = 1;
                            res.obj = result;
                            MainActivity.mHandler.sendMessage(res);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }

        };
        Looper.loop();
    }
}