package com.eluo.signage.java.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by gogumario on 2017-11-07.
 */

public class NetworkReceiver extends BroadcastReceiver {
    private static int iCount =0;
    // BroadCast를 받앗을때 자동으로 호출되는 콜백 메소드
    @Override
    public void onReceive(Context context, Intent intent) {
        // 네트워크 변환 Receiver
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        //TODO 네트워크 변환에 따른 이후 로직을 처리한다.
        //ex. noti,activity 호출,service 호출 등등
        Log.d("network Receiver", "network isConnected :  " + isConnected);
//        intent.getStringExtra("");

        boolean sTmp = true;
        if(false == isConnected){
            if(iCount == 0){
                Intent trIntent = new Intent("android.intent.action.MAIN");
                trIntent.setClass(context, com.eluo.signage.MainActivity.class);
                trIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(trIntent);
                iCount = 1;
            }
            Toast.makeText(context, String.format("네트워크 연결이 끊어 졌습니다"),Toast.LENGTH_SHORT).show();
//            Toast.makeText(context, String.format("%s를 수신했습니다.", isConnected),Toast.LENGTH_SHORT).show();
        }else{
            iCount = 0;
            //Toast.makeText(context, String.format("네트워크 연결 되었습니다."),Toast.LENGTH_SHORT).show();
        }



//        Toast.makeText(context, "네트워크 연결이 변경 되었습니다"     , Toast.LENGTH_LONG);
    }










}