package com.example.apple.wireless_module_ad_hoc.Actiivties;

import android.location.LocationListener;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;

import android.location.LocationManager;
import android.location.Location;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;

import java.util.ArrayList;
import java.util.*;

import com.baidu.mapapi.map.offline.MKOLUpdateElement;
import com.baidu.mapapi.map.offline.MKOfflineMap;
import com.baidu.mapapi.map.offline.MKOfflineMapListener;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.TextOptions;
import com.example.apple.wireless_module_ad_hoc.DataProcess.CacheUtils;
import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.DataProcess.SendMessage;
import com.example.apple.wireless_module_ad_hoc.R;

import org.json.JSONException;
import org.json.JSONObject;


public class Maptest extends AppCompatActivity implements OnClickListener{


    static double myLongitude;
    static double myLatitude;
    MapView mMapView = null;
    BaiduMap mBaiduMap=null;
    String TAG="MapTest";
    EditText condition;

    List<Marker> marker =new ArrayList<Marker>();


    private String smsg = "";
    String locationSend;
    String name;
    String group;

    LocationManager locationManager;
    String provider;
    CacheUtils cacheUtils;
    Data getData;

    private final static String ROUTE_DISCOVERY ="1";
    private final static String DIALOGUE ="2";
    private final static String RESCUE_INFORMATION ="3";
    private final static String ROAD_CONDITION ="4";
    String myID;
    String myName;
    Button helpButton;
    Location location;
    MKOfflineMap mkOfflineMap;
    private TextView stateView;
    //MKOLUpdateElement update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.maptestlayout);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        stateView = (TextView) findViewById(R.id.state);

        getData = ((Data)getApplicationContext());
        myName=getData.getName();
        myID=getData.getFromID();

        //update = mkOfflineMap.getUpdateInfo(state);


        condition=(EditText)findViewById(R.id.editText);
        Button location_map= (Button) findViewById(R.id.location_map);
        Button chat_map=(Button) findViewById(R.id.chat_map);
        Button home_map=(Button) findViewById(R.id.home_map);
        Button OK=(Button)findViewById(R.id.OK);
        Button refresh=(Button)findViewById(R.id.refresh);
        Button send=(Button)findViewById(R.id.send);
        helpButton=(Button)findViewById(R.id.help);


        location_map.setOnClickListener(this);
        home_map.setOnClickListener(this);
        chat_map.setOnClickListener(this);
        refresh.setOnClickListener(this);
        OK.setOnClickListener(this);//get others' location END
        send.setOnClickListener(this);
        helpButton.setOnClickListener(this);
        //getLongitudeAndLatitude();//地图部分，获取坐标值
        getGPSService();

        location = locationManager.getLastKnownLocation(provider);
        setLocation(location);
        offlineMap();

    }//onCreate END



    void getGPSService() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.getProviders(true);

        try {
            provider = LocationManager.GPS_PROVIDER;
            locationManager.requestLocationUpdates(provider, 5000, 1, locationListener);
            Toast.makeText(getApplicationContext(),"Update successfully.",Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "GPS is disable.", Toast.LENGTH_SHORT).show();
        }


    }


    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "GPS status is visible.");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "GPS is out of the service area.");//"当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "GPS service is stop.");//"当前GPS状态为暂停服务状态");
                    break;
            }
            Toast.makeText(getApplicationContext(),"onStatusChanged: "+status,Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(),"onProviderEnabled: GPS service connected successfully",Toast.LENGTH_SHORT).show();
            Log.e(TAG,"onProviderEnabled");

            /*if (Build.VERSION.SDK_INT >= 23) {
                int checkPermission = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
                if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                    //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    ActivityCompat.requestPermissions(getApplicationContext(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    Log.d(TAG, "动态获取定位权限");
                    Toast.makeText(getApplicationContext(),"动态获取权限",Toast.LENGTH_SHORT).show();
                }
            }*/
            Toast.makeText(getApplicationContext(),"onProviderEnable: GPS service started.",Toast.LENGTH_SHORT).show();
            Location location = locationManager.getLastKnownLocation(provider);
            setLocation(location);

        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(),"Failed to start GPS service. Please turn on GPS.",Toast.LENGTH_SHORT).show();

        }
    };


    public void setLocation(Location location){

        try{
            myLongitude = location.getLongitude();
            myLatitude = location.getLatitude();

            getData.setLongitude(String.valueOf(myLongitude));
            getData.setLatitude(String.valueOf(myLatitude));

            showLocation(myLongitude, myLatitude, name,"1","myNewLocation");
            Toast.makeText(getApplicationContext(),"Get new location.",Toast.LENGTH_SHORT).show();


        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Set fake location.",Toast.LENGTH_SHORT).show();
            myLatitude = 39.903185;
            myLongitude = 116.500264;

            try{
                String fakeAddress=getData.getFakeAddress();
                Double fake=Double.parseDouble(fakeAddress);
                myLongitude=myLongitude-fake;
                myLatitude=myLatitude+fake;
            }catch (Exception ex){
                System.out.println("Failed to update location.");
            }

            showLocation(myLongitude, myLatitude, name, "1","myLocation");
        }

    }





    public void showLocation(Double l,Double a,String rname,String rgroup,String message){

        LatLng point = new LatLng(a, l);
        String t="";

        //定义Maker坐标点
        // LatLng point = new LatLng(39.963175, 116.400244);北邮：39.966912,116.361968
        //宏福:116.381505,40.114778

        //getLongitudeAndLatitude();


        BitmapDescriptor bitmap;

        switch (rgroup) {
            //myLocation
            case "1": {
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka);
                t="You|"+l+"|"+a;
                break;
            }

            //Rescue station
            case "2": {
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markb);
                t="Rescue station|"+message;
                break;
            }

            //people ask for help
            case "3": {
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markc);
                t="SOS|"+message;
                break;
            }

            //blocked
            case "4":
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marknull);
                t="road|"+message;
                break;

            default:
                bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_markc);
                t="null";
                break;
        }


        OverlayOptions options = new MarkerOptions()
                .position(point)
                .icon(bitmap)
                .zIndex(9)
                .draggable(true);

        marker.add((Marker) mBaiduMap.addOverlay(options));

        //定义文字所显示的坐标点
        LatLng llText = new LatLng(a, l);
        //构建文字Option对象，用于在地图上添加文字



        OverlayOptions textOption = new TextOptions()
                .bgColor(0xAAFFFF00)
                .fontSize(24)
                .fontColor(0xFFFF00FF)
                .text(t)
                .rotate(-0)
                .position(llText);

        mBaiduMap.addOverlay(textOption);



    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.location_map:
                Intent intent_l = new Intent(Maptest.this, Maptest.class);
                startActivity(intent_l);
                break;
            case R.id.chat_map:
                Intent intent=new Intent(Maptest.this,BTClient.class);
                startActivity(intent);
                break;
            case R.id.home_map:
                Intent intent_h=new Intent(Maptest.this,LoginActivity.class);
                startActivity(intent_h);
                break;
            case R.id.refresh://clean

                mBaiduMap.clear();
                try{
                    cacheUtils.writeJson(Maptest.this,"","road.txt",false);
                    //Toast.makeText(Maptest.this,"The map record is cleaned.",Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    e.getStackTrace();
                }

                /*String fakeAddress=getData.getFakeAddress();
                Double makefake=Double.parseDouble(fakeAddress);
                makefake=makefake+0.005;
                String madefake=Double.toString(makefake);
                getData.setFakeAddress(madefake);*/
                setLocation(location);
                Toast.makeText(getApplicationContext(),"refreshed!",Toast.LENGTH_SHORT).show();
                break;
            case R.id.OK://refresh
                //mBaiduMap.clear();
                setLocation(location);
                getRoadCondition();
                break;
            case R.id.send:

                sendCondition();

                //locationSend = "$" + group + name + "#" + myLatitude + "&" + myLongitude + "*";

                /*String MYLatitude = Double.toString(myLatitude);
                String MYLongitude = Double.toString(myLongitude);//将double的坐标值转化为字符串

                Log.i("Edit", "MyLatitude" + myLatitude);//Show the name and group number in the log
                Log.i("Edit", "MyLongitude" + myLongitude);

                Data setLat = ((Data)getApplicationContext());
                setLat.setLatitude(MYLatitude);
                Data setLon=((Data)getApplicationContext());
                setLon.setLongitude(MYLongitude);*/
                break;
            case R.id.help:
                sendHelp();
                break;
            default:
                break;

        }


    }


    public void sendHelp() {

       /* try{
            if (_socket == null) {
                Log.d(TAG, "Socket is null");
                //Data applicationConstant = ((Data)getApplicationContext());
                _socket = getData.getSocket();
            }

        }catch (Exception e){
            Toast.makeText(LoginActivity.this,"Please connect to the bluetooth module first.",Toast.LENGTH_SHORT).show();
        }*/


        try{
            /*String name = getData.getName();
            String myID = getData.getFromID();*/
            Log.d(TAG, "Get data: " + myName + "/" + myID);

            //The help message will be broadcast.
            String m = "Help！";//+location
            String route;
            String broadcastCount="0";

            SendMessage sendMessage = new SendMessage(getApplicationContext());

            route = myID + "/";
            sendMessage.sendFormatMessage(RESCUE_INFORMATION, broadcastCount,myName, myID, "0000", route, m);
            Toast.makeText(Maptest.this,"Help message sent",Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            //Toast.makeText(LoginActivity.this,"Please connect to the bluetooth module first.",Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Send help failed.");
        }

    }


    public void sendCondition(){
        //|type/longitude/latitude/message/>
        //The ID of the rescue team is 999
        //Rescue team information report
        if(myID.equals("999")){
            String s="2"+"/"+myLongitude+"/"+myLatitude+"/"+"Rescue team report.";
            SendMessage sendMessage=new SendMessage(getApplicationContext());
            String route=myID+"/";
            sendMessage.sendFormatMessage(ROAD_CONDITION,"0",myName,myID,"0000",route,s);
            Toast.makeText(Maptest.this,"The rescue team information is reported.",Toast.LENGTH_SHORT).show();
        }
        //Road condition report.
        else{
            String m=condition.getText().toString();
            if(m.equals("")||m==null){
                Toast.makeText(Maptest.this,"Can not send an empty message.",Toast.LENGTH_SHORT).show();
                return;
            }
            String s="4"+"/"+myLongitude+"/"+myLatitude+"/"+m;
            SendMessage sendMessage=new SendMessage(getApplicationContext());
            String route=myID+"/";
            sendMessage.sendFormatMessage(ROAD_CONDITION,"0",myName,myID,"0000",route,s);
            condition.setText("");
        }

    }

    public void getRoadCondition(){

        ArrayList<String> list=cacheUtils.readJson(Maptest.this,"road.txt");
        JSONObject jsonObject;
        String parseName;
        String parseID;
        String parseMessage;
        String parsedData;
        Double l,a;
        String parseType;

        for(String s:list){
            try{
                jsonObject=new JSONObject(s);
                parseName=jsonObject.get("name").toString();
                parseID=jsonObject.get("fromID").toString();
                parsedData=jsonObject.get("message").toString();

                Log.d(TAG,parsedData);
                String[] info = parsedData.split("/");

                //|type/longitude/latitude/message/>
                //Beihang:116.354019,39.987288
                //Beijiao:116.349447,39.957935
                //Normal:116.373739,39.96712
                //hongfu:116.381659,40.114876

                parseType=info[0];
                Log.d(TAG,info[1]+"|"+info[2]);
                l=Double.parseDouble(info[1]);
                a=Double.parseDouble(info[2]);
                parseMessage=info[3];
                showLocation(l,a,parseName,parseType,parseMessage);

            }catch (JSONException e){
                e.getStackTrace();
            }

        }

    }

    public void offlineMap(){
        boolean wifi=isWifiConnected(Maptest.this);
        if(wifi){
            mkOfflineMap=new MKOfflineMap();
            MKOfflineMapListener listener=new MKOfflineMapListener() {
                @Override
                public void onGetOfflineMapState(int type, int state) {
                    switch (type) {
                        case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                            MKOLUpdateElement update = mkOfflineMap.getUpdateInfo(state);
                            // 处理下载进度更新提示,与百度地图官方的app有冲突
                            if (update != null) {
                                //stateView.setText("Downloading offline map of "+update.cityName+": "+update.ratio);
                                stateView.setText("Downloading offline map of Beijing: "+update.ratio+"%");
                                Log.d(TAG,update.cityName+": "+update.ratio);
                                //updateView();
                            }
                        }
                        break;
                        case MKOfflineMap.TYPE_NEW_OFFLINE:
                            // 有新离线地图安装
                            Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                            int cityID=131;//Beijing
                            try{
                                mkOfflineMap.start(cityID);
                                Log.d(TAG,"Start to download the offline map of Beijing.");
                                Toast.makeText(Maptest.this, "Start to download the offline map of Beijing.", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Toast.makeText(Maptest.this,"Please check your network connection.",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case MKOfflineMap.TYPE_VER_UPDATE:
                            // 版本更新提示
                            // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                            break;
                        default:
                            break;
                    }

                }
            };
            Boolean initial=mkOfflineMap.init(listener);
            if(initial){
                int cityID=131;//Beijing
                try{
                    mkOfflineMap.start(cityID);
                    Log.d(TAG,"Start to download the offline map of Beijing.");
                   // Toast.makeText(this, "Start to download the offline map of Beijing.", Toast.LENGTH_SHORT).show();
                }catch (Exception e){
                    Toast.makeText(this,"Please check your network connection.",Toast.LENGTH_SHORT).show();
                }

            }

        }
    }

    /*@Override
    public void onGetOfflineMapState(int type, int state) {
        switch (type) {
            case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
                MKOLUpdateElement update = mkOfflineMap.getUpdateInfo(state);
                // 处理下载进度更新提示
                if (update != null) {
                    stateView.setText(update.cityName+": "+update.ratio);
                    //updateView();
                }
            }
            break;
            case MKOfflineMap.TYPE_NEW_OFFLINE:
                // 有新离线地图安装
                Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
                break;
            case MKOfflineMap.TYPE_VER_UPDATE:
                // 版本更新提示
                // MKOLUpdateElement e = mOffline.getUpdateInfo(state);

                break;
            default:
                break;
        }

    }*/



    public boolean isWifiConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
           // Toast.makeText(context,"WiFi connected",Toast.LENGTH_SHORT).show();
            return true ;
        }
        Toast.makeText(context,"WiFi disconnected",Toast.LENGTH_SHORT).show();
        return false ;
    }


    /*
        Lifecycle
     */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
        /**
         * 退出时，销毁离线地图模块
         */
        //mkOfflineMap.destroy();
        mMapView.onDestroy();

    }
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }



    /*public void ok(){
        String rLatitude = "";
        String rLongitude = "";
        String smsg1 = smsg;
        String rname = "";
        String rgroup = "";

        //String smsg1="$2nino#39&116*";
        Log.d(TAG,"接收到的坐标值为："+smsg1);
        char a;
        int i, j, k;

				*//*
					Setting the string format to be: “$+groupnum+name+#+latitude+&+longitude+*”
				 *//*

        char b = smsg1.charAt(0);//Check whether the smsg is a location message or not.
        //If it is, just decode it into split Data.
        if (b == '$') {
            rgroup = rgroup + smsg1.charAt(1);
            for (k = 2; k < smsg1.length(); k++) {
                a = smsg1.charAt(k);
                if (a != '#') {
                    rname = rname + a;
                } else {
                    k++;
                    break;
                }
            }

            for (i = k; i < smsg1.length(); i++) {

                a = smsg1.charAt(i);
                if (a != '&') {
                    rLatitude = rLatitude + a;
                } else {
                    i++;
                    break;
                }
            }

            for (j = i; j < smsg1.length(); j++) {

                a = smsg1.charAt(j);
                if (a != '*') {
                    rLongitude = rLongitude + a;
                } else {
                    break;
                }
            }
        }

        Log.d(TAG,"latitude: " + rLatitude+"  longitude: " + rLongitude+"  name: "
                + rname+"  group: " + rgroup);

        double longitude = Double.parseDouble(rLongitude);
        double latitude = Double.parseDouble(rLatitude);

        showLocation(longitude, latitude, rname, rgroup);
        smsg="";
    }*/


}



