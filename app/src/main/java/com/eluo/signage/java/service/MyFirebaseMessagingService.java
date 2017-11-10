package com.eluo.signage.java.service;

/**
 * Created by gogumario on 2017-09-01.
 */


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.eluo.signage.MainActivity;
import com.eluo.signage.R;
import com.eluo.signage.java.intro.IntroActivity;
import com.eluo.signage.kotlin.utils.ThreadPolicy;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{
    private static final String TAG = "MyFirebaseMsgService";
    private String sUrl = null;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /*화면 깨움*/
        /*참고URL: http://milkye.tistory.com/8*/
        //화면 깨움
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG" );
        wakeLock.acquire(3000);

        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            //alerDialog();
            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
                Map<String,String> bundle = remoteMessage.getData();
                String sTitle = bundle.get("title");
                String sMessage = bundle.get("message");
                String sType = bundle.get("type");
                sendNotification(sMessage,"",sTitle);
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }
        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
//            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Map<String,String> bundle = remoteMessage.getData();
            //String sTitle = bundle.get("title");
            //String sType = bundle.get("type");
            String sMessage = remoteMessage.getNotification().getBody();
            String sTitle = remoteMessage.getNotification().getTitle();
            Map<String, String> map = new HashMap<String, String>();
            map = remoteMessage.getData();
            alerDialog(sMessage,sTitle,map);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]
    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }
    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody, String sType, String title) {
        Intent intent = null;
        intent = new Intent(this, IntroActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.eluo_icon)
                .setContentTitle(title)
                .setContentText("알림탭을 아래로 천천히 드래그 하세요.")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                //BigTextStyle
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(title)
                        .bigText(messageBody))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
    private void alerDialog(final String sMessage, final String sTitle, final Map<String, String> sData){
        //Toast.makeText(getApplicationContext(),R.string.T_end_meg, Toast.LENGTH_LONG).show();
//        Handler handler = new Handler((Handler.Callback) alert.show());

        Handler mHandler = new Handler(Looper.getMainLooper());
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String ssTitle = sTitle;
                String url = sMessage;
                String sType = "";
                if(url.length()  > 0){
                    if(url.length() >=14){
                        sType = url.substring(url.length()-1, url.length());
                    }else if(url.length() == 3){
                        sType = url;
                    }else if(url.length() == 2){
                        sType = url;
                    }else{
                    }
                }
                //TAC 클린 메인 이동 시간 초기화
                //FAC 클린 층수 초기화
                //AAC 전체 클린

                if(url != null && !url.equals("")) {
                    if(url.length() >=14) {
                        sUrl = url.substring(0, url.length() - 2);
                    }
                    if (sType.equals("A")) {//외부 브라우저
                        //URL 정보로 외부 웹뷰 띄움
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sUrl));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (sType.equals("B")) {   //엑티비티 바로 이동
                        Intent intent = new Intent(MainActivity.instance, com.eluo.signage.EventActivity.class);//엑티비티 생성 작성 화면
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("sEventUrl", sUrl); //조회 키 값을 넘겨준다
                        startActivity(intent); // Sub_Activity 호출
                    } else if (sType.equals("C")) {    //메시지 창에 확인 후 엑티비티 이동
                        // 내용
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance);
                        alert.setPositiveButton("이동", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ThreadPolicy();
                                Intent intent = new Intent(MainActivity.instance, com.eluo.signage.EventActivity.class);//엑티비티 생성 작성 화면
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("sEventUrl", sUrl); //조회 키 값을 넘겨준다
                                startActivity(intent); // Sub_Activity 호출
                                dialog.dismiss();     //닫기
                            }
                        });
                        alert.setMessage(ssTitle);
                        alert.show();
                    }else  if (sType.equals("U")) {//외부 브라우저
                        // 내용
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance);
                        alert.setPositiveButton("이동", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sUrl));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                        alert.setMessage(R.string.D_New_Update);
                        alert.show();
                    }else if(sType.equals("FAC")){
                        String sFloor = "";
                        String sMe = "C";
                        if(sData.size() > 0){
                            for(int i = 0; i<sData.size(); i++){
                                sFloor = sData.get("plo");
                            }
                            sMe = "V";
                        }
                        SharedPreferences prefs = getSharedPreferences("Floor", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("floorCode", sFloor);
                        editor.commit();

                        // 내용
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance);
                        alert.setPositiveButton("앱 종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.instance, MainActivity.class);//엑티비티 생성 작성 화면
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("sRestart", "Y"); //조회 키 값을 넘겨준다
                                startActivity(intent); // Sub_Activity 호출
                                dialog.dismiss();     //닫기
                            }
                        });
                        if(sMe.equals("V")){
                            alert.setMessage(R.string.D_facc);
                        }else{
                            alert.setMessage(R.string.D_fac);
                        }
                        alert.show();
                    }else if(sType.equals("TAC")){
                        String sTime = "";
                        String sMe = "C";
                        if(sData.size() > 0){
                            for(int i = 0; i<sData.size(); i++){
                                sTime = sData.get("tim");
                            }
                            sMe = "V";
                        }
                        SharedPreferences prefs = getSharedPreferences("Timemm", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("Time", sTime);
                        editor.commit();

                        // 내용
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance);
                        alert.setPositiveButton("앱 종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.instance, MainActivity.class);//엑티비티 생성 작성 화면
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("sRestart", "Y"); //조회 키 값을 넘겨준다
                                startActivity(intent); // Sub_Activity 호출
                                dialog.dismiss();     //닫기
                            }
                        });
                        if(sMe.equals("V")){
                            alert.setMessage(R.string.D_tacc);
                        }else{
                            alert.setMessage(R.string.D_tac);
                        }
                        alert.show();
                    }else if(sType.equals("AAC")){
                        String sTime = "";
                        String sFloor = "";
                        String sMe = "C";
                        if(sData.size() > 0){
                            for(int i = 0; i<sData.size(); i++){
                                sTime = sData.get("tim");
                                sFloor = sData.get("plo");
                            }
                            SharedPreferences prefs = getSharedPreferences("Timemm", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("Time", sTime);
                            editor.commit();

                            SharedPreferences pre = getSharedPreferences("Floor", MODE_PRIVATE);
                            SharedPreferences.Editor edi = pre.edit();
                            edi.putString("floorCode", sFloor);
                            edi.commit();
                            sMe = "V";
                        }else {
                            SharedPreferences prefs = getSharedPreferences("Reset", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("allReset", "A");
                            editor.commit();
                        }
                        // 내용
                        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.instance);
                        alert.setPositiveButton("앱 종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.instance, MainActivity.class);//엑티비티 생성 작성 화면
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("sRestart", "Y"); //조회 키 값을 넘겨준다
                                startActivity(intent); // Sub_Activity 호출
                                dialog.dismiss();     //닫기
                            }
                        });
                        if(sMe.equals("V")){
                            alert.setMessage(R.string.D_aaacc);
                        }else{
                            alert.setMessage(R.string.D_aaac);
                        }
                        alert.show();
                    }else if(sType.length() == 2){
                        char cType;
                        for(int i =0; i< sType.length(); i++){
                            cType = sType.charAt(i);
                            if(cType > 47 || cType < 58){
                                SharedPreferences prefs = getSharedPreferences("Event", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("eventTime", sType);
                                editor.commit();
                            }
                        }
                    }else{ }
                }else{
                }
            }
        }, 0);
    }
}