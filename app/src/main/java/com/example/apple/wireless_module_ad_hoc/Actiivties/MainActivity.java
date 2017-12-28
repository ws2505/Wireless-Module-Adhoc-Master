package com.example.apple.wireless_module_ad_hoc.Actiivties;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.apple.wireless_module_ad_hoc.Bluetooth.BTService;
import com.example.apple.wireless_module_ad_hoc.Bluetooth.DeviceListActivity;
import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    Data getData;
    String TAG="MainActivity";

    /*
        BT
     */
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private final static int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_INVOKE_IMAGE = 3;
    BluetoothDevice _device = null;
    BluetoothSocket _socket = null;
    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();
    boolean bRun = true;
    private InputStream is;
    private String imageString;


    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        //ImageButton touchscreen = (ImageButton) findViewById(R.id.touch);
        View home=(LinearLayout)findViewById(R.id.LinearLayout_home);


        getData=((Data)getApplicationContext());
        getData.setRoute("0000");
        getData.setFromID("FFFF");
        getData.setName("FFFF");
        getData.setMessageType(0);
        Log.d(TAG,"Finished setting values.");

        home.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


        //Set the original diviation of the fake location.
        String original="0.000";
        Data fAddress=((Data)getApplicationContext());
        fAddress.setFakeAddress(original);



        /*
        Bluetooth
     */

        if (_bluetooth == null){
            Toast.makeText(this, "Failed to turn on the bluetooth. Please check your bluetooth setting.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        new Thread(){
            public void run(){
                if(_bluetooth.isEnabled()==false){
                    _bluetooth.enable();
                }
            }
        }.start();



        /*
            BT
         */

        if(_bluetooth.isEnabled()==false){
            Toast.makeText(MainActivity.this, "Starting Bluetooth Service...", Toast.LENGTH_LONG).show();
            return;
        }


        //DeviceListActivity
        //Button btn = (Button) findViewById(R.id.Button03);
        if(_socket==null){
            Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
        else{

            try{

                is.close();
                _socket.close();
                _socket = null;
                bRun = false;
              //  btn.setText("连接");
            }catch(IOException e){}
        }




    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){

            case REQUEST_CONNECT_DEVICE:

                if (resultCode == Activity.RESULT_OK) {

                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                    _device = _bluetooth.getRemoteDevice(address);

                    try{
                        _socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                    }catch(IOException e){
                        Toast.makeText(this, "连接失败，请连接蓝牙模块", Toast.LENGTH_SHORT).show();
                    }

                   // Button btn = (Button) findViewById(R.id.Button03);
                    try{
                        _socket.connect();
                        Toast.makeText(this, "连接"+_device.getName()+"成功", Toast.LENGTH_SHORT).show();
                        //btn.setText("断开");
                    }catch(IOException e){
                        try{
                            Toast.makeText(this, "连接失败，请连接蓝牙模块", Toast.LENGTH_SHORT).show();
                            _socket.close();
                            _socket = null;
                        }catch(IOException ee){
                            Toast.makeText(this, "连接失败，请连接蓝牙模块", Toast.LENGTH_SHORT).show();
                        }

                        return;
                    }

/*
					try{
						is = _socket.getInputStream();
					}catch(IOException e){
						Toast.makeText(this, "接收数据失败", Toast.LENGTH_SHORT).show();
						return;
					}
					if(bThread==false){
						ReadThread.start();
						bThread=true;
					}else{
						bRun = true;
					}*/

                    getData.setSocket(_socket);
                    Log.d(TAG,"Setting socket.");

                    Intent intent=new Intent(MainActivity.this, BTService.class);
                    startService(intent);

                }
                break;

            case REQUEST_INVOKE_IMAGE:
                // get a image from the gallery and store it in string type
                imageString = data.getStringExtra("Data")+"12345";
                break;

            default:break;
        }
    }

    public void onDestroy(){
        super.onDestroy();
		/*if(_socket!=null)
			try{
				_socket.close();
			}catch(IOException e){}*/
        Log.d(TAG,"MainActivity destroyed.");

    }


    /*public void onConnectButtonClicked(View v){
        if(_bluetooth.isEnabled()==false){
            Toast.makeText(this, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
            return;
        }


        //DeviceListActivity
        Button btn = (Button) findViewById(R.id.Button03);
        if(_socket==null){
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
        }
        else{

            try{

                is.close();
                _socket.close();
                _socket = null;
                bRun = false;
                btn.setText("连接");
            }catch(IOException e){}
        }
        return;
    }*/





}
