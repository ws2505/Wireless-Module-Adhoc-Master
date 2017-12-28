package com.example.apple.wireless_module_ad_hoc.Actiivties;

import java.io.InputStream;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.example.apple.wireless_module_ad_hoc.DataProcess.CacheUtils;
import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.DataProcess.ListAdapter;
import com.example.apple.wireless_module_ad_hoc.DataProcess.SendMessage;
import com.example.apple.wireless_module_ad_hoc.R;

import org.json.JSONException;
import org.json.JSONObject;

public class BTClient extends AppCompatActivity implements OnClickListener{

	private final static int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_INVOKE_IMAGE = 3;

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
	BluetoothSocket _socket = null;
	boolean _discoveryFinished = false;
	boolean bRun = true;
	boolean bThread = false;
	String TAG="BTClient：";

	private BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter();

	Data getData;
	EditText toUser;
	String toID;
	String message;
	private final static String ROUTE_DISCOVERY ="1";
	private final static String DIALOGUE ="2";
	private final static String ROAD_CONDITION ="4";

	//final static String smsg1;

	CacheUtils cacheUtils;
	ArrayList<String> dialogueMessages;
	ListView dialogueListView;
	ListAdapter adapter;
	Button sendButton;
	Button cleanButton;
	Button refreshButton;
	Button rescueButton;
	Button imgButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_btclient);


		//text0 = (TextView)findViewById(R.id.Text0);
		edit0 = (EditText)findViewById(R.id.Edit0);
		//sv = (ScrollView)findViewById(R.id.ScrollView01);
		//dis = (TextView) findViewById(R.id.in);

		iv = (ImageView)findViewById(R.id.image);

		//pic=(Button)findViewById(R.id.Button_pic);
		//pic.setOnClickListener(listener);
		//pic.setText("pic");


		getData = ((Data)getApplicationContext());
		toUser=(EditText)findViewById(R.id.to_user);
		sendButton=(Button)findViewById(R.id.send);
		cleanButton=(Button)findViewById(R.id.clean);
		refreshButton=(Button)findViewById(R.id.refresh_dialogue);
		rescueButton=(Button)findViewById(R.id.rescue_info);
		imgButton=(Button)findViewById(R.id.img);
		sendButton.setOnClickListener(this);
		cleanButton.setOnClickListener(this);
		refreshButton.setOnClickListener(this);
		rescueButton.setOnClickListener(this);
		imgButton.setOnClickListener(this);

		/*
			ListView
		 */
		cacheUtils=new CacheUtils();
		dialogueMessages=new ArrayList<>();
		//dialogueMessages.add("1111");
		dialogueListView=(ListView)findViewById(R.id.dialogue_list);
		adapter=new ListAdapter(this,dialogueMessages);
		dialogueListView.setAdapter(adapter);
		getDialogueList();

	}

	public void getDialogueList(){
		ArrayList<String> dialogueList=new ArrayList<>();
		ArrayList<String> list=cacheUtils.readJson(BTClient.this,"dialogue.txt");
		JSONObject jsonObject;
		String parsedData;
		for(String s:list){
			try{
				jsonObject=new JSONObject(s);
				//Log.d(TAG,jsonObject.toString());
				parsedData="From: "+jsonObject.get("name").toString()+" | To: "+jsonObject.get("toID")+"\n"+jsonObject.get("message");
				dialogueList.add(parsedData);
			}catch (JSONException e){
				e.getStackTrace();
			}

		}

		adapter.setList(dialogueList);
		adapter.notifyDataSetChanged();

	}


	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(BTClient.this, Image.class);
			startActivityForResult(intent, REQUEST_INVOKE_IMAGE);

		}
	};




	@Override
	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.send:
				/*//for test:
				getData.setCounter(1);*/

				send();
				break;

			case R.id.clean:
				try{
					cacheUtils.writeJson(BTClient.this,"","dialogue.txt",false);
					Toast.makeText(BTClient.this,"The chatting record is cleaned.",Toast.LENGTH_SHORT).show();
				}catch (Exception e){
					e.getStackTrace();
				}
				break;

			case R.id.refresh_dialogue:
				if(getData.getMessageType()==1){
					//Log.d(TAG,"Show image.");
					//Show image
					bm=getData.getImage();
					iv.setImageBitmap(bm);

					getData.setMessageType(0);
					Log.d(TAG,"Show image.");

				}else {
					getDialogueList();
				}

				break;

			case R.id.rescue_info:
				Intent rescueIntent=new Intent(BTClient.this,RescueInfoActivity.class);
				startActivity(rescueIntent);
				break;

			case R.id.img:

				/*Intent imgIntent = new Intent(BTClient.this, Image.class);
				startActivity(imgIntent);*/
				Intent intent=new Intent();
				intent.setClass(BTClient.this, Image.class);
				startActivityForResult(intent, REQUEST_INVOKE_IMAGE);


			default:
				break;
		}
	}


	public void send(){


		String broadCastCount="0";
		toID=toUser.getText().toString();
		message=edit0.getText().toString();
		if (message.equals("")){
			Toast.makeText(BTClient.this,"Can not send an empty message.",Toast.LENGTH_SHORT).show();
			return;
		}
		if(toID.equals("")){
			Toast.makeText(BTClient.this,"Please input the destination ID.",Toast.LENGTH_SHORT).show();
			return;
		}

		String route;

		String name=getData.getName();
		String fromID=getData.getFromID();
		Log.d(TAG,"Get data: "+name+"/"+fromID);

		SendMessage sendMessage=new SendMessage(getApplicationContext());

		//Check the stored routing table.
		if(!getData.getRoute().equals("0000")){
			String[] s=getData.getRoute().split("/");
			String storeDest=s[s.length-1];
			if(storeDest.equals(toID)){
				route=getData.getRoute();
				Log.d(TAG,"Get stored route: "+route);
				sendMessage.sendFormatMessage(DIALOGUE,broadCastCount,name,fromID,toID,route,message);
			}else {
				route=getData.getFromID()+"/";
				sendMessage.sendFormatMessage(ROUTE_DISCOVERY,broadCastCount,name,fromID,toID,route,message);
				edit0.setText("");
			}
		}
		else {
			route=getData.getFromID()+"/";
			sendMessage.sendFormatMessage(ROUTE_DISCOVERY,broadCastCount,name,fromID,toID,route,message);
			edit0.setText("");
		}


	}






	public void onDestroy(){
		super.onDestroy();
		/*if(_socket!=null)
			try{
				_socket.close();
			}catch(IOException e){}*/
		Log.d(TAG,"BTClient destroyed.");

	}



}