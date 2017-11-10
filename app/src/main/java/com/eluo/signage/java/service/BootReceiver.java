package com.eluo.signage.java.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eluo.signage.java.intro.IntroActivity;
/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 브로드캐스트리시버(자동 앱실행)
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) BootReceiver.java
 * @since 2017-09-28
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-28][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class BootReceiver extends BroadcastReceiver {
    // BroadcastReceiver를 상속하여 처리 해줍니다.
    @Override
    public void onReceive(Context context, Intent intent) {

        // TODO Auto-generated method stub
        // 전달 받은 Broadcast의 값을 가져오기
        // androidmanifest.xml에 정의한 인텐트 필터를 받아 올 수 있습니다.
        String action = intent.getAction();
        // 전달된 값이 '부팅완료' 인 경우에만 동작 하도록 조건문을 설정 해줍니다.
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            Intent ii = new Intent(context, IntroActivity.class);
            ii.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(ii);
            // 부팅 이후 처리해야 코드 작성
            // Ex.서비스 호출, 특정 액티비티 호출등등

        }
    }
}

