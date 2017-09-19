package com.example.logqtainia.March;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private final String DEVICE_ADDRESS = "98:D3:32:70:E0:78";
    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;

    public static final int SIZE = 3;
    public static final String FACES_ORDER = "UDFBLR"; //"FRBLUD";

    private String cubeString = "UUUUUUUUU" + "RRRRRRRRR" + "FFFFFFFFF"
            + "DDDDDDDDD" + "LLLLLLLLL" + "BBBBBBBBB";  //展开图中0-5分别为URFDLB
    private String result;
    private String[] colorName = new String[]{"yellow", "white", "blue", "green", "orange", "red"};

    private BTHelper mBTHelper = null;

    public static final int WHAT_CONNECT = 0;
    public static final int WHAT_ERROR = 1;
    public static final int WHAT_RECV = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new ShowCubeFragment(), "ShowCube")
                    .commit();
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CONNECT:
                    boolean res = (boolean) msg.obj;
                    getFragmentManager().
                            findFragmentByTag("ShowCube").
                            getView().
                            findViewById(R.id.btn_open_blue_tooth).
                            setEnabled(!res);
                    getFragmentManager().
                            findFragmentByTag("ShowCube").
                            getView().
                            findViewById(R.id.btn_manual_control).
                            setEnabled(res);
                    getFragmentManager().
                            findFragmentByTag("ShowCube").
                            getView().
                            findViewById(R.id.btn_solve_cube).
                            setEnabled(res);
                    Log.i("BTconnect", res + "");
                    if (res) {
//                        setUIEnabled(true);
//                        msg("Connected to " + device.getName() + ".\n------------");
                    } else {
//                        msg("Can't connect to " + device.getName() + ".");
                    }
                    break;
                case WHAT_ERROR:
//                    msg("Lost connection.");
                    break;
                case WHAT_RECV:
//                    msg((String)msg.obj);
                    Log.i("BT Receive", msg.obj.toString());
                    break;
            }
        }
    };

    private boolean BTSearch() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesn't Support Bluetooth",
                    Toast.LENGTH_LONG).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first",
                    Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getName().equals("TAOLQ")) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        if (!found) Toast.makeText(getApplicationContext(), "Please Pair the Device first",
                Toast.LENGTH_SHORT).show();
        else Toast.makeText(getApplicationContext(), "Found Device Successfully",
                Toast.LENGTH_SHORT).show();
        return found;
    }

    public void connectToBT() {
        if (!BTSearch()) return;
        mBTHelper = new BTHelper(device, new BTHelper.BTListener() {
            @Override
            public void onConnect(boolean success) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_CONNECT;
                msg.obj = success;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onDataReceived(String data) {
                Message msg = mHandler.obtainMessage();
                msg.what = WHAT_RECV;
                msg.obj = data;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onError() {
                mHandler.sendEmptyMessage(WHAT_ERROR);
            }
        });
//        msg("Connecting to " + device.getName() + ".");
        mBTHelper.connect(PORT_UUID);
    }


//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 1000 && resultCode == 1001)
//        {
//            tv = (TextView)findViewById(R.id.textView2);
//            tv.setText("");
//            for (int i = 0; i < 6; i++) {
////                tv.append(FACES_ORDER.charAt(i) + " "
////                        + data.getStringExtra(FACES_ORDER.charAt(i) + "") + '\n');
//                colorName[i] = data.getStringExtra(FACES_ORDER.charAt(i) + "");
////                Log.i("color", 'A' + colorName[i]);
//            }
////            tv.append(data.getStringExtra("state"));
//            cubeString = data.getStringExtra("state");
//            drawCube();
//
//            //参数中魔方面顺序为URFDLB
//            result = search.solution(cubeString, maxDepth, 100, 0, mask);
//            tv.append("\n" + result);
//        }
//    }

    public void setColorName(String[] colorName) {
        this.colorName = colorName;
    }

    public void setCubeString(String cubeString) {
        this.cubeString = cubeString;
    }

    public String[] getColorName() {
        return colorName;
    }

    public String getCubeString() {
        return cubeString;
    }

    public BTHelper getBTHelper() {
        return mBTHelper;
    }

}
