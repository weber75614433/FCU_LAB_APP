package com.example.user.bt05;

import static android.os.Build.VERSION_CODES.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;


public class Comunication extends AppCompatActivity {


    String address = "98:D3:31:20:68:1F"; //直接給就好讓我搞好久 Fuck!!!

    private OutputStream outputStream = null;
    public InputStream inputStream = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    String message = "";
    TextView textVeiw;
    private String _recieveData = "";



    BluetoothSocket btSocket = null;
    BluetoothDevice remoteDevice;
    BluetoothServerSocket mmServer;

    private boolean isBtConnected = false;
    //// SPP UUID 尋找它
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    Button LedOn, LedOFF;
    protected void  onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);
        //Intent newint = getIntent();

        //address = newint.getStringExtra(MainActivity.EXTRA_ADRESS);
        LedOn = (Button) findViewById(R.id.led_on);
        LedOFF = (Button) findViewById(R.id.led_off);
        textVeiw = (TextView) findViewById(R.id.text_view_infro);

        LedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket!=null){
                    try {
                            btSocket.getOutputStream().write("f".toString().getBytes());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        LedOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btSocket!=null){
                    try {
                        btSocket.getOutputStream().write("b".toString().getBytes());
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        //執行連結藍芽
        new ConnectBT().execute();




    }

    private void Disconnect(){
        if(btSocket!= null){
            try {
                btSocket.close();
            }catch (IOException e){
                Log.e("Error!","XXXX");
            }
        }
        finish();
    }

    public void onBackPressed(){
        super.onBackPressed();
        Disconnect();
    }
    // UI thread
    private class ConnectBT extends AsyncTask<Void, Void, Void>{
        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(Comunication.this,"Connecting...", "Please wait!!"); // show progress dialog
        }
        @Override
        //https://gelecegiyazanlar.turkcell.com.tr/konu/android/egitim/android-301/asynctask
        protected Void doInBackground(Void... devices) {
            connect();
            return null;
        }
        protected void onPostExecute(Void result){
            super.onPostExecute(result);
            if(!ConnectSuccess){
                Toast.makeText(getApplicationContext(),"Connect Error",Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(),"Connect Access!",Toast.LENGTH_SHORT).show();
                isBtConnected =true;
            }
            progress.dismiss();
        }
    }
    public  void connect(){
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice deviceid = myBluetooth.getRemoteDevice(address);
        try {
            btSocket = deviceid.createInsecureRfcommSocketToServiceRecord(myUUID);
        }catch (IOException e){
            e.printStackTrace();
        }
        myBluetooth.cancelDiscovery();
        try {
            btSocket.connect();
            outputStream = btSocket.getOutputStream();
            inputStream = btSocket.getInputStream();
            ConnectedThread connectedThread = new ConnectedThread(btSocket);
            connectedThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            //btSocket.close();
        }catch (Exception e2){
            Log .e("Exception-","ON RESUME: Unable to close socket during connection failure", e2);
        }
    }

    /**
     * 在接受不到数据
     * **/
    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket btSocket) {
            this.socket = btSocket;
            InputStream input = null;
            OutputStream output = null;
            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }
        public void run(){
            byte[] buff = new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    //System.out.println("bytes lenght is:" + bytes);
                    String str = new String(buff, "UTF-8");
                    //System.out.println("Str_1->" + str);
                    str = str.substring(0,bytes);
                    //System.out.println("Str_2->" + str);
                    Log.e("RecieveData", str);
                    Message message=handler.obtainMessage();
                    message.obj=str;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    Handler handler = new Handler(){
        public void handleMessage(Message msg) {

            String s= (String) msg.obj;
            System.out.print("msg.obj->"+s);
            String ma = s;
            //檢查是否被抱走情況
            if(ma.contains("falleddown") | ma.contains("Falleddown")){
                System.out.print("Inter->"+s);
                AlertDialog.Builder dialog = new AlertDialog.Builder(Comunication.this);
                dialog.setTitle("警告!!");
                dialog.setMessage("你的朋友跌倒了!");
                dialog.setPositiveButton("我知道了!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });//結束對話框!!
                dialog.show();
            }
            // message= message+ s; //重疊上次傳的內容
            textVeiw.setText(s);
        }
    };
}
