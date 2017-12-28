package com.example.apple.wireless_module_ad_hoc.Actiivties;

import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;

import com.example.apple.wireless_module_ad_hoc.DataProcess.CacheUtils;
import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LoginActivity extends AppCompatActivity implements OnClickListener {

    static String name,myID;
    BluetoothSocket _socket = null;
    Data getData;
    String TAG="LoginActivity";
    String toID;
    String message;
    String BROADCASTID="00000000";
    CacheUtils cacheUtils;
    EditText nameTextView;
    EditText IDTextView;

    private final static String RESCUE_INFORMATION ="3";


    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*try{
                _socket = getData.getSocket();

        }catch (Exception e){
            Toast.makeText(LoginActivity.this,"Please connect to the bluetooth module first.",Toast.LENGTH_SHORT).show();

        }*/

        //Intent intent=getIntent();

        Button SignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button location= (Button) findViewById(R.id.location);
        Button chat=(Button) findViewById(R.id.chat);
        Button home=(Button) findViewById(R.id.home);
        location.setOnClickListener(this);
        home.setOnClickListener(this);
        chat.setOnClickListener(this);
        SignInButton.setOnClickListener(this);

        nameTextView = (EditText) findViewById(R.id.email);
        IDTextView = (EditText) findViewById(R.id.password);

        getData=((Data)getApplicationContext());
        name=getData.getName();
        myID=getData.getFromID();

        cacheUtils=new CacheUtils();
        ArrayList<String> list=cacheUtils.readJson(LoginActivity.this,"login.txt");
        JSONObject jsonObject;

        String parsedData;
        for(String s:list){
            try{
                jsonObject=new JSONObject(s);
                myID=jsonObject.get("myID").toString();
                name=jsonObject.get("myName").toString();
                getData.setName(name);
                getData.setFromID(myID);
                nameTextView.setText(name);
                IDTextView.setText(myID);
                Log.d(TAG,"Login: "+name+"|"+myID);
            }catch (JSONException e){
                e.getStackTrace();
            }

        }


        //isWifiConnected(LoginActivity.this);


    }//onCreate end



    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.email_sign_in_button:

                name = nameTextView.getText().toString();
                myID = IDTextView.getText().toString();

                getData.setName(name);
                getData.setFromID(myID);
                JSONObject jsonObject=new JSONObject();
                try{
                    jsonObject.put("myName",name);
                    jsonObject.put("myID",myID);
                }catch (JSONException e){
                    e.getStackTrace();
                }
                cacheUtils.writeJson(LoginActivity.this,jsonObject.toString(),"login.txt",false);
                Toast.makeText(LoginActivity.this,"Login successfully!",Toast.LENGTH_SHORT).show();

/*
                Intent intent = new Intent(LoginActivity.this, Maptest.class);
                //Intent intent = new Intent(LoginActivity.this, MapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putCharSequence("name", name);
                bundle.putCharSequence("GroupNum", groupNum);
                intent.putExtras(bundle);
                startActivity(intent);*/
                break;

            case R.id.location:

                if(name.equals("FFFF")||myID.equals("FFFF")){
                    new AlertDialog.Builder(LoginActivity.this).setTitle("Warning")//设置对话框标题

                            .setMessage("请输入组号及用户名并点击登录")//设置显示的内容

                            .show();
                }
                else{
                    //Intent locationIntent = new Intent(LoginActivity.this, MapActivity.class);
                    Intent locationIntent = new Intent(LoginActivity.this, Maptest.class);
                    startActivity(locationIntent);
                }
                break;

            case R.id.home:
                Intent homeIntent=new Intent(LoginActivity.this,MainActivity.class);
                //Intent homeIntent=new Intent(LoginActivity.this,GPS.class);
                startActivity(homeIntent);
                break;

            case R.id.chat:
                //Intent intent=new Intent(LoginActivity.this,BluetoothChat.class);
                Intent chatIntent=new Intent(LoginActivity.this,BTClient.class);
                startActivity(chatIntent);
                break;
            /*case R.id.help_button_login:
                sendHelp();
                break;*/
            default:
                break;
        }
    }



}











