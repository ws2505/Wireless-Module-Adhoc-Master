package com.example.apple.wireless_module_ad_hoc.DataProcess;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Easy_MAI on 2017/4/17.
 * This class is used to build the sending information.
 */

public class SendMessage {

    private final static String ROUTE_DISCOVERY ="1";
    private final static String DIALOGUE ="2";
    private final static String RESCUE_INFORMATION ="3";
    private final static String ROAD_CONDITION ="4";
    private final static String Acknowledgement="5";
    private final static String IMAGE="6";

    private BluetoothSocket _socket = null;
    Context applicationContext;
    Context acitivityContext;
    String TAG="SendMessage";

    Data getData;
    String fileName="json.txt";
    JSONObject jsonObject;
    String name;
    String type;
    String message;
    String toID;
    String fromID;
    CacheUtils cacheUtils;
    Double start,end;
    int unsentCount=0;
    int testNUm=1000;


    public SendMessage(Context applicationContext){
        //call by: getApplicationContext();
        this.applicationContext =applicationContext;
        start=(double)System.currentTimeMillis();
    }


    public void sendFormatMessage(String type, String broadcastCount, String name, String fromID, String toID,String route, String message){
        int i;
        int n=0;
        String sendingMessage;
        getData = ((Data) applicationContext);
        jsonObject=new JSONObject();
        this.name=name;
        this.type=type;
        this.message=message;
        this.toID=toID;
        this.fromID=fromID;


        /*getData = ((Data)applicationContext);
        String name=getData.getName();
        String fromID=getData.getFromID();
        Log.d(TAG,"Get data: "+name+"/"+fromID);*/



        /*
          *     1.Route Discovery
          *     2.Dialogue
          *     3.Rescue team broadcasting information
          *     4.Road condition broadcasting information
         */

        cacheUtils=new CacheUtils();
        switch (type){
            case ROUTE_DISCOVERY:
                //Log.d(TAG,"Start route discovery.");
                sendingMessage=type+"|"+broadcastCount+"|"+name+"|"+fromID+"|"+toID+"|"+route+"|"+message+"/>";
                /*try{
                    jsonObject.put("message",message);
                    jsonObject.put("toID",toID);
                    jsonObject.put("fromID",fromID);
                    jsonObject.put("name",name);
                   // jsonObject.put("broadcastCount",broadcastCount);
                    jsonObject.put("type",type);

                }catch (JSONException e){
                    Log.d(TAG,"Failed to set JSON.");
                }
                cacheUtils.writeJson(applicationContext,jsonObject.toString(),"dialogue.txt",true);*/
                break;

            case Acknowledgement:
                //Log.d(TAG,"Start to send Ack.");
                sendingMessage=type+"|"+broadcastCount+"|"+name+"|"+fromID+"|"+toID+"|"+route+"|"+"Ack"+"/>";
                break;

            case DIALOGUE:
                //Log.d(TAG,"Start send dialogue.");
                sendingMessage=type+"|"+broadcastCount+"|"+name+"|"+fromID+"|"+toID+"|"+route+"|"+message+"/>";
                /*if(broadcastCount.equals("0")){
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("toID",toID);
                        jsonObject.put("fromID",fromID);
                        jsonObject.put("name",name);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(applicationContext,jsonObject.toString(),"dialogue.txt",true);
                }*/
                break;

            case RESCUE_INFORMATION:
                sendingMessage=type+'|'+broadcastCount+'|'+name+'|'+fromID+'|'+route+"|"+message+"/>";
                //Broadcasting the message.
                if(broadcastCount.equals("0")){
                    try{
                        jsonObject.put("message",message);
                      //  jsonObject.put("toID",toID);
                        jsonObject.put("fromID",fromID);
                        jsonObject.put("name",name);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(applicationContext,jsonObject.toString(),"rescue_info.txt",true);
                }
                break;

            case ROAD_CONDITION:
                sendingMessage=type+'|'+broadcastCount+'|'+name+'|'+fromID+'|'+route+'|'+message+"/>";
                //Broadcasting the message.
                break;

            case IMAGE:
                sendingMessage=type+'|'+broadcastCount+'|'+name+'|'+fromID+'|'+route+'|'+message+"/>";
                break;

            default:
                //Toast.makeText(applicationContext,"The data format is invalid.",Toast.LENGTH_SHORT).show();
                Log.d(TAG,"The data format is invalid");
                return;

        }

        _socket=getData.getSocket();

        try{
            OutputStream os = _socket.getOutputStream();

            byte[] bos=sendingMessage.getBytes();
           // byte[] bos="ttttt".getBytes();

            for(i=0;i<bos.length;i++){
                if(bos[i]==0x0a)n++;
            }
            byte[] bos_new = new byte[bos.length+n];
            n=0;
            for(i=0;i<bos.length;i++){
                if(bos[i]==0x0a){
                    bos_new[n]=0x0d;
                    n++;
                    bos_new[n]=0x0a;
                }else{
                    bos_new[n]=bos[i];
                }
                n++;
            }
            int bCount=Integer.parseInt(broadcastCount);
            //It is unnecessary to get ack when relaying the massages.
            if(bCount==0){
                if(type.equals(ROUTE_DISCOVERY)||type.equals(DIALOGUE)){
                    //getAckThread.start();
                    GetAckThread getAckThread=new GetAckThread();
                    getAckThread.start();
                }

            }

            os.write(bos_new);

           // os.close();
            Log.d(TAG,sendingMessage);
        }catch(IOException e){
            e.getStackTrace();
        }
    }


    private class GetAckThread extends Thread{
        @Override
        public void run(){
            super.run();

            doTask();

        }
    }

    public void doTask(){
        int ackFlag=0;
        long t2;
        getData.setAckFlag(ackFlag);
        long t1 = System.currentTimeMillis();

        while(ackFlag==0){
            t2 = System.currentTimeMillis();
            //Log.d(TAG,"Ack Thread running...");
            if(t2-t1 > 5*1000){//1ms

                //waiting for response... /5s
                Log.e(TAG,"Failed to reach to the destination.");
                // Start route discovery again
               // getData.setRoute("0000");
                getData.setAckFlag(-1);
                //for test:
                /*unsentCount++;
                test();*/


                break;
            }else{
                ackFlag=getData.getAckFlag();
                if(ackFlag==1){
                    Log.d(TAG,"Sent");
                    //Toast.makeText(acitivityContext,"Sent",Toast.LENGTH_SHORT).show();
                    JSONObject jsonObject=new JSONObject();
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("toID",toID);
                        jsonObject.put("fromID",fromID);
                        jsonObject.put("name",name);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(applicationContext,jsonObject.toString(),"dialogue.txt",true);

                    //for test:
                    //test();

                    break;
                }

            }

        }
    }

    public void test(){

        int newCounter=getData.getCounter()+1;
        String me=Integer.toString(newCounter);
        if(newCounter<=testNUm){
            getData.setCounter(newCounter);
            String route=getData.getRoute();
            sendFormatMessage(DIALOGUE,"0","Nino","18811111111","18833333333",route,me);
        }else{
            end=(double)System.currentTimeMillis();
            Double time=end-start;
            Double ave=time/testNUm;
            float u=(float)unsentCount;
            float t=(float)testNUm;
            float loss=u/t;
            Log.e(TAG,"The total sending time is: "+time);
            Log.e(TAG,"The average sending time is: "+ave);
            Log.e(TAG,"The number of messages that fail to reach to the destination is: "+unsentCount);
            Log.e(TAG,"The package loss rate is: "+loss);
        }
    }

    /*public Thread getAckThread = new Thread(){
        public void run(){
            int ackFlag=0;
            long t2;
            getData.setAckFlag(ackFlag);
            long t1 = System.currentTimeMillis();

            while(ackFlag==0){
                t2 = System.currentTimeMillis();
                //Log.d(TAG,"Ack Thread running...");
                if(t2-t1 > 5*1000){//1ms

                    //waiting for response... /5s
                    //Toast.makeText(applicationContext,"Failed to reach to the destination.",Toast.LENGTH_SHORT).show();
                    //Toast.makeText(acitivityContext,"Unsent",Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"Failed to reach to the destination.");
                    // Start route discovery again
                    getData.setRoute("0000");
                    getData.setAckFlag(-1);

                    break;
                }else{
                    ackFlag=getData.getAckFlag();
                    if(ackFlag==1){
                        Log.d(TAG,"Sent");
                        //Toast.makeText(acitivityContext,"Sent",Toast.LENGTH_SHORT).show();
                        JSONObject jsonObject=new JSONObject();
                        try{
                            jsonObject.put("message",message);
                            jsonObject.put("toID",toID);
                            jsonObject.put("fromID",fromID);
                            jsonObject.put("name",name);
                            // jsonObject.put("broadcastCount",broadcastCount);
                            jsonObject.put("type",type);

                        }catch (JSONException e){
                            Log.d(TAG,"Failed to set JSON.");
                        }
                        cacheUtils.writeJson(applicationContext,jsonObject.toString(),"dialogue.txt",true);
                        break;
                    }

                }

            }



        }

    };*/


}
