package com.liaou.getrssidemo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BlDevice {
    public String uuid;
    public String name;
    public String mac;
    public List<Double> lstRssi;
    public BlDevice(String uuid, String name, String mac) {
        this.uuid = uuid;
        this.name = name;
        this.mac = mac;
        lstRssi = new ArrayList<>();
    }
    public void getRssi() {
//        for(int i= 0;i<100;i++) {
//            lstRssi.add(100*Math.random());
//        }
    }
}
