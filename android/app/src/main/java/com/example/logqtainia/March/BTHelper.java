package com.example.logqtainia.March;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * bluetooth device接受与发送消息
 * Created by tarjan on 17-3-9.
 */

public class BTHelper extends Thread {
    private UUID mUUID;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;

    private BTListener mListener;

    private boolean connected = false;

    public BTHelper(BluetoothDevice device, BTListener listener) {
        this.mDevice = device;
        this.mListener = listener;
    }

    public void connect(final UUID uuid) {
        this.mUUID = uuid;
        this.start();
    }

    public void disconnect() {
        mListener = null;
        connected = false;
        try {
            if (mInputStream != null) mInputStream.close();
            if (mOutputStream != null) mOutputStream.close();
            if (mSocket != null) mSocket.close();
        } catch (IOException e) {}
    }

    public void send(final byte[] data) {
        if (!connected) throw new IllegalStateException("no connection");
        new Thread() {
            @Override
            public void run() {
                try {
                    mOutputStream.write(data);
                    mOutputStream.flush();
                } catch (IOException e) {
                    if (mListener != null) mListener.onError();
                }
            }
        }.start();
    }

    @Override
    public void run() {
        try {
            mSocket = mDevice.createRfcommSocketToServiceRecord(mUUID);
            if (mSocket == null) {
                if (mListener != null) mListener.onConnect(false);
                return;
            }

            mSocket.connect();
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            connected = true;

            if (mListener != null) mListener.onConnect(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(mInputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                if (mListener != null) mListener.onDataReceived(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (connected) {
                if (mListener != null) mListener.onError();
            } else {
                if (mListener != null) mListener.onConnect(false);
            }
        }
    }

    public boolean getConnected() { return connected; }

    public interface BTListener {
        void onConnect(boolean success);
        void onDataReceived(String data);
        void onError();
    }
}
