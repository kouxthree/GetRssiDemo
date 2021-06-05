package com.liaou.getrssidemo;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter BA;
    private Set<BluetoothDevice> scannedDevices;
    ListView lv;
    ResultView resultview;
    TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.lv_device);
        resultview = (ResultView) findViewById(R.id.result_view);
        textview = (TextView) findViewById(R.id.text_current);
    }

    public void on(View v) {
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(), "Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Already on", Toast.LENGTH_LONG).show();
        }
    }

    //    public void off(View v) {
//        BA.disable();
//        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
//    }
//    public void visible(View v) {
//        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        startActivityForResult(getVisible, 0);
//    }
    private void alert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("等一哈")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //do things
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private ExecutorService executor = Executors.newSingleThreadExecutor();
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getCurrentDeviceRssi(BlDevice device) {
//        BluetoothDevice currentDevice = BA.getRemoteDevice(device.mac);
//        if (currentDevice == null) return;
//        BluetoothGatt gatt = currentDevice.connectGatt(this, true, new BluetoothGattCallback() {
//            @Override
//            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//                super.onConnectionStateChange(gatt, status, newState);
//            }
//            @Override
//            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    textview.setText(Integer.toString(rssi));
//                }
//            }
//        });
//        gatt.readRemoteRssi();
    }
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void getRssiExecutor(BlDevice device) {
        if (resultview.isScanning) {
            alert();
            return;
        }
        resultview.isScanning = true;
        resultview.countDown = 3;
        resultview.currentDevice = device;
        textview.setText("");
        executor.submit(() -> {
//            device.getRssi();
            for (int i = resultview.countDown; i >= 0; i--) {
                resultview.invalidate();
                try {
                    getCurrentDeviceRssi(device);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resultview.countDown--;
            }
            resultview.isScanning = false;
//            textview.setText(Integer.toString(getCurrentDeviceRssi(device)));
        });
    }

    public void listScannedDevices(View v) {
        if (BA.isDiscovering()) {
            alert();
            return;
        }
        ArrayList<BlDevice> lstBlDevice = new ArrayList<BlDevice>();
        // Create ListView
        BlDeviceListAdapter bladapter = new BlDeviceListAdapter(this, lstBlDevice);
        lv.setAdapter(bladapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                getRssiExecutor(lstBlDevice.get(i));
            }
        });
        // Create a BroadcastReceiver for ACTION_FOUND.
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice bt = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //ParcelUuid[] uuids = bt.getUuids();
                    //lstBlDevice.add(new BlDevice(uuids[0].toString(), bt.getName(), bt.getAddress()));
                    lstBlDevice.add(new BlDevice("", bt.getName(), bt.getAddress()));
                    bladapter.notifyDataSetChanged();//refresh listview
                }
            }
        };
        Set<BluetoothDevice> pairedbts = BA.getBondedDevices();
        for (BluetoothDevice d : pairedbts) {
            ParcelUuid[] uu = d.getUuids();
            lstBlDevice.add(new BlDevice(uu[0].toString(), d.getName(), d.getAddress()));
        }
//        // GPS
//        final int GPS_REQUEST_CODE = 1;
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!isGpsEnabled) {
//            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST_CODE);
//        }
        // Scan
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
        boolean dis = BA.startDiscovery();
        int int1 = BA.getScanMode();

        Toast.makeText(getApplicationContext(), "Scan Bluetooth Devices", Toast.LENGTH_SHORT).show();
    }
}