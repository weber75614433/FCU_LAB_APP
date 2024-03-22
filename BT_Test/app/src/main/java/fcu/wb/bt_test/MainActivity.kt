package fcu.wb.bt_test

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import fcu.wb.bt_test.ui.theme.BT_TestTheme
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val TAG = "BluetoothTest"
    private val UUID_SERIAL_PORT_SERVICE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        this.bluetoothDevice = bluetoothAdapter.getRemoteDevice("HM-10 MAC")

        connectBluetoothDevice()

    }

    private fun connectBluetoothDevice() {
        Thread {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@Thread
                }
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID_SERIAL_PORT_SERVICE)
                bluetoothSocket.connect()
                Log.d(TAG, "Bluetooth device connected")

                inputStream = bluetoothSocket.inputStream
                outputStream = bluetoothSocket.outputStream

                readData()

                val testData = "Hello HM-10"
                outputStream.write(testData.toByteArray())


            } catch (e : Exception){
                Log.e(TAG, "Failed to connect to HM-10 : ${e.message}")
            }
        }.start()
    }

    private fun readData() {
        Thread {
            val buffer = ByteArray(1024)
            var bytes : Int

            while (true){
                try{
                    bytes = inputStream.read(buffer)
                    val receivedData = String(buffer, 0, bytes)
                    Log.d(TAG, "Received Data : $receivedData")

                    handler.post{
                        textView.text = receivedData
                    }

                }catch (e : Exception){
                    Log.e(TAG, "Errpr reading from HM-10 : ${e.message}")
                }
            }

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BT_TestTheme {
        Greeting("Android")
    }
}