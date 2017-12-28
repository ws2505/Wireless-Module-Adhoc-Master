package com.example.apple.wireless_module_ad_hoc.DataProcess;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by apple on 16/5/17.
 */

public class Data extends Application {


    /*
     * Socket
     */
    private BluetoothSocket _socket;
    public void setSocket(BluetoothSocket _socket){
        this._socket=_socket;
    }
    public BluetoothSocket getSocket(){
        return _socket;
    }



    /*
     * send name and id
     */
    private String name;
    public void setName(String name){
        this.name=name;
    }
    public String getName(){
        return name;
    }

    private String groNum;
    public void setFromID(String groNum ){
        this.groNum=groNum;
    }
    public String getFromID(){
        return groNum;
    }

    //route format: source/...passing nodes... /destination
    private String route;
    private String routeDest;
    public void setRoute(String route){
        this.route=route;
        /*String[] s=route.split("/");
        routeDest=s[s.length-1];*/
    }
    public String getRoute(){
        return route;
    }
    public String getRouteDest(){
        return routeDest;
    }

    private int ackFlag;
    public void setAckFlag(int ackFlag){
        this.ackFlag=ackFlag;
    }
    public int getAckFlag(){
        return ackFlag;
    }



    /*
     * receive name and id
     */
    private String rname;
    public void setrName(String rname){
        this.rname=rname;
    }
    public String getrName(){
        return rname;
    }

    private String rFromID;
    public void setrFromID(String rFromID ){
        this.rFromID =rFromID;
    }
    public String getrFromID(){
        return rFromID;
    }
    private JSONObject rJson;
    public void setrJson(JSONObject rJson){
        this.rJson=rJson;
    }
    public JSONObject getrJson(){
        return rJson;
    }




    /*
     * Data about location.
     */

    private String FakeAddress;
    public void setFakeAddress(String FakeAddress){
        this.FakeAddress=FakeAddress;
    }
    public String getFakeAddress(){
        return FakeAddress;
    }


    private String longitude;
    public void setLongitude(String data){
        this.longitude=data;
    }
    public String getLongitude(){
        return longitude;
    }

    private String latitude;
    public void setLatitude(String latitude){
        this.latitude=latitude;
    }
    public String getLatitude(){
        return latitude;
    }

    private String rLatitude;
    public void setrLatitude(String rLatitude){
        this.rLatitude=rLatitude;
    }
    public String getrLatitude(){
        return rLatitude;
    }
    private String rLongitude;
    public void setrLongitude(String rLongitude){
        this.rLongitude=rLongitude;
    }
    public String getrLongitude(){
        return rLongitude;
    }

    /*
        Image
     */
    private Bitmap image;
    public void setImage(Bitmap image){
        this.image=image;
    }
    public Bitmap getImage(){
        return image;
    }

    private int messageType;
    public void setMessageType(int messageType){
        this.messageType=messageType;
    }
    public int getMessageType(){
        return messageType;
    }


    /*
        Test
     */
    private int counter;
    public void setCounter(int counter){
        this.counter=counter;
    }
    public int getCounter(){
        return counter;
    }

}
