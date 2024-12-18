package com.example.user.bt05;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter myBluetooth;
    private Set<BluetoothDevice> paireDevices;

    public static  String EXTRA_ADRESS = "device_address";

    ArrayAdapter <String> adapter;


    Button openbutton, scanbutton,closebutton;

    ListView bluelist;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get a handle to the default local Bluetooth adapter.
        myBluetooth = BluetoothAdapter.getDefaultAdapter();


        openbutton = (Button) findViewById(R.id.open_button);
        scanbutton = (Button) findViewById(R.id.scan_button);
        closebutton = (Button) findViewById(R.id.close_button);


        bluelist = (ListView)  findViewById(R.id.bluetoothlist);

        //按下按鈕發生事件
        openbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openBluetooth();
            }
        });

        scanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listdevice();
            }
        });
        closebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBluetooth();
            }
        });
    }



    //顯示目前收尋到的藍芽裝置
    private void listdevice() {
        paireDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();
        if(paireDevices.size() > 0){
            for(BluetoothDevice bt:paireDevices){
                list.add(bt.getName()+"\n"+bt.getAddress()+"\n"+bt.getUuids());
                Log.d("Bluetooth_Name-", bt.getName());
                Log.d("Bluetooth_Mac:", bt.getAddress());
//                Log.d("Bluetooth_Uuids:", bt.getUuids().toString());
                Log.d("Bluetooth_Class:", bt.getBluetoothClass().toString());


                System.out.print("Bluetooth_Name:"+bt.getName());
                System.out.print("Bluetooth_Mac:"+bt.getAddress());
                System.out.print("Bluetooth_Uuids:"+bt.getUuids());
                System.out.print("Bluetooth_Class"+bt.getBluetoothClass());
            }
            Log.d("Bluetooth-", paireDevices.toString());
        }else{
            Toast.makeText(getApplicationContext(),"沒有配對設備", Toast.LENGTH_SHORT).show();
            Log.d("Nothing!","No Device!");
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, list);
        bluelist.setAdapter(adapter);
        bluelist.setOnItemClickListener(selectDevice);
    }
    //點集清單
    public AdapterView.OnItemClickListener selectDevice = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length()-17);

                    Intent comintent = new Intent(MainActivity.this, Comunication.class);
                    comintent.putExtra(EXTRA_ADRESS, address);
                    startActivity(comintent);
        }
    };

    //掃描藍芽功能
    private void openBluetooth(){
        if(myBluetooth == null){
            Toast.makeText(getApplicationContext(), "Bluetooth Null!",  Toast.LENGTH_SHORT).show();
        }
        if(!myBluetooth.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);//開啟藍芽
        }
    }

    private void closeBluetooth(){
        if(myBluetooth.isEnabled()){
            myBluetooth.disable();//關閉藍芽(再按一下關閉藍芽)
        }
    }
}
