package com.example.apple.wireless_module_ad_hoc.Actiivties;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.apple.wireless_module_ad_hoc.DataProcess.CacheUtils;
import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.DataProcess.ListAdapter;
import com.example.apple.wireless_module_ad_hoc.DataProcess.SendMessage;
import com.example.apple.wireless_module_ad_hoc.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RescueInfoActivity extends AppCompatActivity implements View.OnClickListener {

    String TAG="RescueInfoActivity";
    private final static String RESCUE_INFORMATION ="3";

    CacheUtils cacheUtils;
    ArrayList<String> rescueMessages;
    ListView resuceListView;
    ListAdapter adapter;
    Button cleanButton;
    Button refreshButton;
    Button sendButton;
    Data getData;
    String myName;
    String myID;
    EditText editText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rescueinfo);
        getData=((Data)getApplicationContext());
        myName=getData.getName();
        myID=getData.getFromID();

        cleanButton=(Button)findViewById(R.id.clean_rescue);
        refreshButton=(Button)findViewById(R.id.refresh_dialogue_rescue);
        sendButton=(Button)findViewById(R.id.send_rescue);
        editText=(EditText)findViewById(R.id.Edit0_resuce);
        refreshButton.setOnClickListener(this);
        cleanButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);


        cacheUtils=new CacheUtils();
        rescueMessages=new ArrayList<>();
        //dialogueMessages.add("1111");
        resuceListView=(ListView)findViewById(R.id.rescue_list);
        adapter=new ListAdapter(this,rescueMessages);
        resuceListView.setAdapter(adapter);
        getRescueList();

    }

    public void getRescueList(){
        ArrayList<String> dialogueList=new ArrayList<>();
        ArrayList<String> list=cacheUtils.readJson(RescueInfoActivity.this,"rescue_info.txt");
        JSONObject jsonObject;
        String parsedData;
        for(String s:list){
            try{
                jsonObject=new JSONObject(s);
               // Log.d(TAG,jsonObject.toString());
                parsedData="From: "+jsonObject.get("name").toString()+"\n"+jsonObject.get("message");
                dialogueList.add(parsedData);
            }catch (JSONException e){
                e.getStackTrace();
            }

        }
        adapter.setList(dialogueList);
        adapter.notifyDataSetChanged();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.refresh_dialogue_rescue:
                getRescueList();
                break;
            case R.id.clean_rescue:
                try{
                    cacheUtils.writeJson(RescueInfoActivity.this,"","rescue_info.txt",false);
                    Toast.makeText(RescueInfoActivity.this,"The rescue information record is cleaned.",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.getStackTrace();
                }
                break;
            case R.id.send_rescue:
                SendMessage sendMessage=new SendMessage(getApplicationContext());
                String route=myID+"/";
                String messeage=editText.getText().toString();
                if(messeage.equals("")||messeage==null){
                    Toast.makeText(RescueInfoActivity.this,"Can not send an empty message.",Toast.LENGTH_SHORT).show();
                    break;
                }
                sendMessage.sendFormatMessage(RESCUE_INFORMATION,"0",myName,myID,"0000",route,messeage);
                editText.setText("");
                break;
            default:
                break;
        }
    }

    public void onDestroy(){
        super.onDestroy();
		/*if(_socket!=null)
			try{
				_socket.close();
			}catch(IOException e){}*/
        Log.d(TAG,"RescueInfoActivity destroyed.");

    }
}
