package com.liaou.getrssidemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Callable;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class BlRssiCls extends AppCompatActivity implements Callable<BlDevice>,AutoCloseable {
    BluetoothAdapter RssiBa = BluetoothAdapter.getDefaultAdapter();
    BlDevice blDevice;

    public BlRssiCls(BlDevice blDevice) {
        this.blDevice = blDevice;
    }

    @Override
    public BlDevice call() throws Exception {
        BluetoothDevice currentDevice = RssiBa.getRemoteDevice(blDevice.mac);
        if (currentDevice == null) return blDevice;
        // Create a BroadcastReceiver for ACTION_FOUND.
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (!BluetoothDevice.ACTION_FOUND.equals(action)) return;
                BluetoothDevice bt = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //use MAC address to identify target
                if (!bt.getAddress().equals(blDevice.mac)) return;
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                blDevice.lstRssi.add(rssi);
                RssiBa.cancelDiscovery();//stop scanning after target device found
            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
        RssiBa.startDiscovery();//start scanning
        return blDevice;
    }

//    protected void finalize() {
//        if (RssiBa.isDiscovering()) {
//            RssiBa.cancelDiscovery();//stop scanning
//        }
//    }

    @Override
    public void close() throws Exception {
        if (RssiBa.isDiscovering()) {
            RssiBa.cancelDiscovery();//stop scanning
        }
    }
}
