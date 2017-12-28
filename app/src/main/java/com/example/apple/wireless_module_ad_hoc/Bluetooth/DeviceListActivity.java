/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.apple.wireless_module_ad_hoc.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.example.apple.wireless_module_ad_hoc.R;


public class DeviceListActivity extends Activity {

    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;


    //public static String EXTRA_DEVICE_ADDRESS = "设备地址：";
    public static String EXTRA_DEVICE_ADDRESS = "Device address：";

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);


        setResult(Activity.RESULT_CANCELED);


        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });


        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);


        
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);


        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);


        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);


        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }


        this.unregisterReceiver(mReceiver);
    }
    
    public void OnCancel(View v){
    	finish();
    }


    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");


        setProgressBarIndeterminateVisibility(true);
        //setTitle("查找设备...");
        setTitle("Scanning...");


        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        mBtAdapter.startDiscovery();
    }


    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            mBtAdapter.cancelDiscovery();


            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);


            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);


            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }else{
                	mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                //setTitle("查找结果：");
                setTitle("Scanning Result：");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    //String noDevices = "没有发现蓝牙设备";
                    String noDevices = "Not found";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
             //   if(mPairedDevicesArrayAdapter.getCount() > 0)
              //  	findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            }
        }
    };


}
