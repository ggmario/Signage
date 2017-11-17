package com.eluo.signage.java.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니즈 브로드캐스트리시버(Wifi)
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) WifiReceiver.java
 * @since 2017-11-17
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-11-17][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class WifiReceiver extends BroadcastReceiver {
    private int beforeWifiLevel = 0;               // 이전 wifi 수신 레벨
    private static int iCount =0;
    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiMan.startScan();
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiMan.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

        if(iCount == 0) {
            if (beforeWifiLevel == 2) {
                FirebaseCrash.report(new Exception("Wifi신호 약함:"+beforeWifiLevel));
                Toast.makeText(context, String.format("Wifi 연결 신호가 약합니다"), Toast.LENGTH_SHORT).show();
                iCount = 1;
            }
            if (beforeWifiLevel <= 1) {
                FirebaseCrash.report(new Exception("Wifi신호 매우 약함:"+beforeWifiLevel));
                Toast.makeText(context, String.format("Wifi 연결 신호가 매우 약합니다"), Toast.LENGTH_SHORT).show();
                iCount = 1;
            }
        }
        if(iCount ==1 ){
            if (beforeWifiLevel > 3) {
                Toast.makeText(context, String.format("Wifi 연결 신호가 매우 좋습니다."), Toast.LENGTH_SHORT).show();
                iCount = 0;
            }
            if (beforeWifiLevel == 3) {
                Toast.makeText(context, String.format("Wifi 연결 신호가 좋습니다"), Toast.LENGTH_SHORT).show();
                iCount = 0;
            }
        }
        beforeWifiLevel = level;
    };
}


