package com.example.apple.wireless_module_ad_hoc.DataProcess;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;

/**
 * Created by Easy_MAI on 2017/4/17.
 * This class is used to parse the receiving information.
 */

public class ReceiveMessage {

    private Context context;
    private String type;
    private String broadcastCount;
    private String rName;
    private String rFromID;
    private String toID;
    private String route;
    private String message;
    private Data getData;
    private JSONObject jsonObject;
    private int bCount;
    private final static String ROUTE_DISCOVERY ="1";
    private final static String DIALOGUE ="2";
    private final static String RESCUE_INFORMATION ="3";
    private final static String ROAD_CONDITION ="4";
    private final static String Acknowledgement="5";
    private final static String IMAGE="6";

    String TAG="ReceiveMessage";



    public ReceiveMessage(Context context){

        this.context=context;//called by getApplicationContext();
        getData=((Data)context);
        jsonObject=new JSONObject();

    }

    public void receiveMessageHandler(String receiveInfo){
       // Log.d(TAG,"Entering receiveMessageHandler.");
        String name=getData.getName();
        String fromID=getData.getFromID();
       // Log.d(TAG,"Get data: "+name+"/"+fromID);

        SendMessage sendMessage=new SendMessage(context);
        String myID=getData.getFromID();

        String[] infoArray=receiveInfo.split("\\|");
        //Check the validity of receiving message.
        if(infoArray.length==6||infoArray.length==7){
            type=infoArray[0];

            //try {
                broadcastCount=infoArray[1];
                bCount=Integer.parseInt(broadcastCount);
                rName =infoArray[2];
                rFromID=infoArray[3];
                /*jsonObject.put("type",infoArray[0]);
                jsonObject.put("broadcastCount",infoArray[1]);
                jsonObject.put("rName",infoArray[2]);
                jsonObject.put("fromID",infoArray[3]);*/

                //'1' is a char;"1" is a String.
                if(type.equals("1")||type.equals("2")||type.equals("5")){
                    toID=infoArray[4];
                    route=infoArray[5];
                    message=infoArray[6];
                   /* jsonObject.put("toID",infoArray[4]);
                    jsonObject.put("route",infoArray[5]);
                    jsonObject.put("message",infoArray[6]);*/
                }
                else {
                    route=infoArray[4];
                    message=infoArray[5];
                   // jsonObject.put("message",infoArray[4]);
                }
                getData.setrJson(jsonObject);
            /*}catch (JSONException e){
                e.getStackTrace();
            }*/

        }
        else {
            //When receive an invalid message format.
            Log.d(TAG,"The new message is invalid. It will be ignored.");
            return;
        }

        CacheUtils cacheUtils=new CacheUtils();
        switch(type){
            case ROUTE_DISCOVERY:
                //  myID=destination
                if(toID.equals(myID)){
                   // Log.d(TAG,"I'm the destination.");

                    //display the message to the user here.
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("toID",toID);
                        jsonObject.put("fromID",rFromID);
                        jsonObject.put("name",rName);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(context,jsonObject.toString(),"dialogue.txt",true);


                    //Send acknowledgement and complete route back to the source.
                    //hypothesis: There is no delay for the Ack.
                    route=route+myID;
                    sendMessage.sendFormatMessage(Acknowledgement,"0",name,fromID,rFromID,route,"Ack");
                    Log.d(TAG,"Send back ack and show message.");
                    Log.d(TAG,"The received message is: "+message);

                    //TODO: Handle th delay of the Ack.

                }
                //Relay the route discovery  information.
                else if(bCount<8){
                    route=route+myID+"/";
                    bCount++;
                    broadcastCount=Integer.toString(bCount);
                    sendMessage.sendFormatMessage(ROUTE_DISCOVERY,broadcastCount,rName,rFromID,toID,route,message);
                    Log.d(TAG,"Relay route discovery.");
                }
                else{
                    Log.d(TAG,"This is the end of this broadcasting route discovery.");
                }
                break;

            case Acknowledgement:
                //toID=myID
                if(toID.equals(myID)){
                    getData.setRoute(route);
                   // Log.d(TAG,"The new route is: "+route);

                    getData.setAckFlag(1);
                    //Log.d(TAG,"The new route is: "+getData.getRoute());
                }
                else{
                    String[] s=route.split("/");
                    int i;
                    int flag=0;
                    //Search whether myID is in the route.
                    for(i=0;i<s.length;i++){
                        if(s[i].equals(myID)){
                            //Avoid transmission loop
                            int calculate=s.length-1;
                            calculate=calculate-i;
                            calculate=calculate-1;
                            if(bCount==calculate)
                            {
                                bCount++;
                                broadcastCount=Integer.toString(bCount);
                                sendMessage.sendFormatMessage(Acknowledgement,broadcastCount,rName,rFromID,toID,route,message);
                                flag=1;
                            }
                            else {
                                flag=2;
                            }
                            break;
                        }
                    }
                    switch (flag){
                        case 0:
                            Log.d(TAG,"MyID is not in the route. Ignore the message.");
                            break;
                        case 1:
                            Log.d(TAG,"Relay the acknowledgement.");
                            break;
                        case 2:
                            Log.d(TAG,"This ack has been relayed before. Ignore the message.");
                            break;
                        default:
                            break;
                    }

                }

                break;

            case DIALOGUE:
                //toID=myID
                if(toID.equals(myID)){

                    Log.d(TAG,"The new message is: "+message);
                    sendMessage.sendFormatMessage(Acknowledgement,"0",name,fromID,rFromID,route,"Ack");
                    Log.d(TAG,"Send back dialogue ack.");

                    //display the message to the user here.
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("toID",toID);
                        jsonObject.put("fromID",rFromID);
                        jsonObject.put("name",rName);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(context,jsonObject.toString(),"dialogue.txt",true);

                }
                else{
                    String[] s=route.split("/");
                    int i;
                    int flag=0;
                    //Search whether myID is in the route. Do relay.
                    for(i=0;i<s.length;i++){
                        if(s[i].equals(myID)){
                            //Avoid transmission loop
                            int calculate=s.length-1;
                            calculate=calculate-i;
                            calculate=calculate-1;
                            if(bCount==calculate)
                            {
                                bCount++;
                                broadcastCount=Integer.toString(bCount);
                                sendMessage.sendFormatMessage(DIALOGUE,broadcastCount,rName,rFromID,toID,route,message);
                                flag=1;
                            }
                            else {
                                flag=2;
                            }
                            break;
                        }
                    }
                    switch (flag){
                        case 0:
                            Log.d(TAG,"MyID is not in the route. Ignore the dialogue message.");
                            break;
                        case 1:
                            Log.d(TAG,"Relay the dialogue message.");
                            break;
                        case 2:
                            Log.d(TAG,"This dialogue message has been relayed before. Ignore the it.");
                            break;
                        default:
                            break;
                    }

                }

                break;

            case RESCUE_INFORMATION:
                String[] s = route.split("/");
                int i;
                int flag=0;
                //find whether the message has reached this node.
                for(i=0;i<s.length;i++) {
                    if (s[i].equals(myID)) {
                        flag=1;
                    }
                }
                if (flag==0){

                    Log.d(TAG,"The new rescue information is: "+message);

                    //display the message to the user here.
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("fromID",rFromID);
                        jsonObject.put("name",rName);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(context,jsonObject.toString(),"rescue_info.txt",true);

                    if (bCount<7){
                        bCount++;
                        broadcastCount=Integer.toString(bCount);
                        toID="0000";
                        route=route+myID+"/";
                        sendMessage.sendFormatMessage(RESCUE_INFORMATION,broadcastCount,rName,rFromID,toID,route,message);
                    }

                }
                else{
                    Log.d(TAG,"The message has reached to this node before. Ignore.");
                }
                break;

            case ROAD_CONDITION:
                String[] ids = route.split("/");
                int j;
                int f=0;
                //find whether the message has reached this node.
                for(j=0;j<ids.length;j++) {
                    if (ids[j].equals(myID)) {
                        f=1;
                    }
                }
                if (f==0){

                    Log.d(TAG,"The new road condition is: "+message);

                    //TODO: display the road condition
                    //TODO: never send the road condition
                    //display the message to the user here.
                    try{
                        jsonObject.put("message",message);
                        jsonObject.put("fromID",rFromID);
                        jsonObject.put("name",rName);
                        // jsonObject.put("broadcastCount",broadcastCount);
                        jsonObject.put("type",type);

                    }catch (JSONException e){
                        Log.d(TAG,"Failed to set JSON.");
                    }
                    cacheUtils.writeJson(context,jsonObject.toString(),"road.txt",true);

                    if (bCount<7){
                        bCount++;
                        broadcastCount=Integer.toString(bCount);
                        toID="0000";
                        route=route+myID+"/";
                        sendMessage.sendFormatMessage(ROAD_CONDITION,broadcastCount,rName,rFromID,toID,route,message);
                    }

                }
                else{
                    Log.d(TAG,"The message has reached to this node before. Ignore.");
                }
                break;

            case IMAGE:
                Log.d(TAG,"image string: "+message);
                //Bitmap bm = string2Bitmap(message.substring(0, message.length() - 5));
                Bitmap bm = string2Bitmap(message);
                getData.setImage(bm);
                getData.setMessageType(1);
                break;
            default:
                Log.d(TAG,"The message type is invalid. Ignore.");
                break;

        }
    }


    public static Bitmap string2Bitmap(String st)
    {
        // OutputStream out;
        Bitmap bitmap = null;
        try
        {
            byte[] bitmapArray= Base64.decode(st, Base64.DEFAULT);
            ByteArrayInputStream baos = new ByteArrayInputStream(bitmapArray);// inputstream
            bitmap = BitmapFactory.decodeStream(baos);
            return bitmap;
        }
        catch (Exception e)
        {
            return null;
        }
    }


}
