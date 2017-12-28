package com.example.apple.wireless_module_ad_hoc.Bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.DataProcess.ReceiveMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Easy_MAI on 2017/4/13.
 */

public class BTService extends Service {

    private String TAG="BTService";
    private final static int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_INVOKE_IMAGE = 3;
    private final static String ROUTE_DISCOVERY ="1";
    private final static String DIALOGUE ="2";
    private final static String RESCUE_INFORMATION ="3";
    private final static String ROAD_CONDITION ="4";
    private final static String Acknowledgement="5";
    private final static String IMAGE="6";

    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private InputStream is;
    private TextView text0;
    private EditText edit0;
    private TextView dis;
    private ScrollView sv;
    private String smsg = "";
    private String fmsg = "";

    private Button pic;

    private ImageView iv;
    private Bitmap bm;
    private String imageString;
    private String accept="";


    public String filename="";
    BluetoothDevice _device = null;
    BluetoothSocket _socket;
    boolean _discoveryFinished = false;
    boolean bRun = true;
    boolean bThread = false;

    private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();


    @Override
    public void onCreate(){

        Data applicationConstant = ((Data)getApplicationContext());
        _socket=applicationConstant.getSocket();

        Log.d(TAG,"Bluetooth service starting.");
        try{
            is = _socket.getInputStream();
        }catch(IOException e){
            //Toast.makeText(this, "接收数据失败", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Failed to receive the data.", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!bThread){
            ReadThread.start();
            //Intent intent=new Intent(BTClient.this, BTService.class);
            //startService(intent);
            bThread=true;
        }else{
            bRun = true;
        }

    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    Thread ReadThread=new Thread(){

        public void run(){
            int num = 0;
            byte[] buffer = new byte[1024];
            byte[] buffer_new = new byte[1024];
            int i = 0;
            int n = 0;
            bRun = true;
            Log.d(TAG,"read thread start.");

            while(true){
                try{
                    while(is.available()==0){
                        while(!bRun){}
                    }
                    while(true){
                        num = is.read(buffer);
                       // Log.d(TAG,"Reading...");
                        n=0;

                        String s0 = new String(buffer,0,num);
                        fmsg+=s0;
                        for(i=0;i<num;i++){
                            if((buffer[i] == 0x0d)&&(buffer[i+1]==0x0a)){
                                buffer_new[n] = 0x0a;
                                i++;
                            }else{
                                buffer_new[n] = buffer[i];
                            }
                            n++;
                        }
                        String s = new String(buffer_new,0,n);
                        smsg+=s;
                      //  Toast.makeText(BTService.this,"ReadThread receive: "+smsg,Toast.LENGTH_SHORT).show();
                       // Log.d(TAG,"ReadThread receive: "+smsg);
                        if(is.available()==0)break;
                    }

                    /*if(smsg.equals("1234"))
                        text0.setText("123");*/
                    handler.sendMessage(handler.obtainMessage());
                    //Log.d(TAG,"read thread end.");
                }catch(IOException e){
                    e.getStackTrace();
                }
            }
        }
    };


    Handler handler= new Handler(){

        public void handleMessage(Message msg){
            super.handleMessage(msg);
            /*if(smsg.charAt(0)=='i') {
                if ((smsg.charAt(smsg.length() - 1) == '5')
                        && (smsg.charAt(smsg.length() - 2) == '4')
                        && (smsg.charAt(smsg.length() - 3) == '3')
                        && (smsg.charAt(smsg.length() - 4) == '2')
                        && (smsg.charAt(smsg.length() - 5) == '1'))
                {
                    bm = string2Bitmap(smsg.substring(0, smsg.length() - 5));
                    iv.setImageBitmap(bm);
                }
            }*/

            if(smsg.length()>2){
               // Log.d(TAG,"The length is right.");
                if (smsg.substring((smsg.length()-2),smsg.length()).equals("/>")){
                    String[] s=smsg.split("/>");
                   // Log.d(TAG,"The length is: "+s.length);

                    smsg=s[s.length-1];
                    Log.d(TAG,"Handler receive: "+smsg);
                    ReceiveMessage receiveMessage=new ReceiveMessage(getApplicationContext());
                    receiveMessage.receiveMessageHandler(smsg);

                    String[] info=smsg.split("\\|");
                    switch (info[0]){
                        case ROUTE_DISCOVERY:
                            Toast.makeText(BTService.this,"New chatting message!",Toast.LENGTH_SHORT).show();
                            break;
                        case DIALOGUE:
                            Toast.makeText(BTService.this,"New chatting message!",Toast.LENGTH_SHORT).show();
                            break;
                        case RESCUE_INFORMATION:
                            Toast.makeText(BTService.this,"New rescue message!",Toast.LENGTH_SHORT).show();
                            break;
                        case ROAD_CONDITION:
                            Toast.makeText(BTService.this,"New road condition message!",Toast.LENGTH_SHORT).show();
                            break;
                        case IMAGE:
                            Toast.makeText(BTService.this,"New image message!",Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }

                    smsg="";

                }
                else {
                    //TODO: invalid handling
                   // Log.d(TAG,"Invalid ignore.2");
                }
            }else {
               // Log.d(TAG,"Invalid ignore.1");
            }


        }
    };



    public static Bitmap string2Bitmap(String st)
    {
        // OutputStream out;
        Bitmap bitmap = null;
        try
        {
            byte[] bitmapArray= Base64.decode(st, Base64.DEFAULT);
            ByteArrayInputStream baos = new ByteArrayInputStream(bitmapArray);
            bitmap = BitmapFactory.decodeStream(baos);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        try{
            is.close();
        }catch(IOException e){
            Log.d(TAG,"Failed to close the input stream.");
        }

        if(_socket!=null)
            try{
                _socket.close();
            }catch(IOException e){
                Log.d(TAG,"Failed to close the socket.");
            }

        Log.d(TAG,"Bluetooth service is destroyed");


    }


}
