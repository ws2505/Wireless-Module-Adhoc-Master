package com.example.apple.wireless_module_ad_hoc.Actiivties;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.example.apple.wireless_module_ad_hoc.DataProcess.Data;
import com.example.apple.wireless_module_ad_hoc.DataProcess.SendMessage;
import com.example.apple.wireless_module_ad_hoc.R;


public class Image extends Activity{

    String TAG="ImageActivity";
    private final static String IMAGE="6";
    Data getData;
    String myName,myID;
    //ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_image);
        setContentView(R.layout.activity_btclient);
        Log.v("tag","message");

        //imageView=(ImageView)findViewById(R.id.imgview);

        getData=((Data)getApplicationContext());
        myID=getData.getFromID();
        myName=getData.getName();

        Intent intent = new Intent();

        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, 1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            ContentResolver cr = this.getContentResolver();
            try {


                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

                String a= bitmap2String(bitmap);
                Log.d(TAG,"The pic string is: "+a);
//                String a = "test";
                Intent i = new Intent();
                String b="";
                i.putExtra("Data",b);
                Log.d(TAG,"The a is: "+a);
                SendMessage sendMessage=new SendMessage(getApplicationContext());
                sendMessage.sendFormatMessage(IMAGE,"0",myName,myID,"0","0",a);

                setResult(1,i);
                finish();

//                TextView t= (TextView) findViewById(R.id.textview_test);
//                t.setText(""+a.length());


            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static String bitmap2String(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();
        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }





}
