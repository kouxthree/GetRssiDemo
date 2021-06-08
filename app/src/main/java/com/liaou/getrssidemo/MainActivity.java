package com.liaou.getrssidemo;

import android.Manifest;
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
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    final int REQUEST_CODE = 1;

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
        //Bluetooth Permission
        if (!BA.isEnabled()) {
            Intent turnOnBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBluetooth, 0);
            Toast.makeText(getApplicationContext(), "Bluetooth Turned on", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth Already on", Toast.LENGTH_LONG).show();
        }
//        // GPS
//        final int GPS_REQUEST_CODE = 1;
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!isGpsEnabled) {
//            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_REQUEST_CODE);
//        }
        //Location Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Location Permission Already Granted", Toast.LENGTH_LONG).show();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE);
//                Toast.makeText(getApplicationContext(), "Location Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Location Permission Is Necessary For This App", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
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

    private ExecutorService executorTimer = Executors.newSingleThreadExecutor();
    private ExecutorService executorGetRssi = Executors.newFixedThreadPool(2);//thread pools
//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getCurrentDeviceRssi(BlDevice device) {
        for (int i = 0; i < 4; i++) {
            executorGetRssi.submit(new BlRssiCls(device));
        }
//        GATT is only for BLE
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void runGetRssi(BlDevice device) {
        if (resultview.isScanning) {
            alert();
            return;
        }
        resultview.isScanning = true;
        resultview.countDown = 3;//scanning time(seconds)
        resultview.currentDevice = device;
        textview.setText("");
        executorTimer.submit(() -> {
//            device.getRssi();
            getCurrentDeviceRssi(device);
            for (int i = resultview.countDown; i >= 0; i--) {
                resultview.invalidate();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                resultview.countDown--;
            }
            resultview.isScanning = false;
            executorGetRssi.shutdownNow();//shutdown all scanning thread
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
                runGetRssi(lstBlDevice.get(i));
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
        //paired devices
//        Set<BluetoothDevice> pairedbts = BA.getBondedDevices();
//        for (BluetoothDevice d : pairedbts) {
//            ParcelUuid[] uu = d.getUuids();
//            lstBlDevice.add(new BlDevice("Paired".concat(uu[0].toString()), d.getName(), d.getAddress()));
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