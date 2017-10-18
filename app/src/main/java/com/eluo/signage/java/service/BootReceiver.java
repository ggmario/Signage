package com.eluo.signage.java.service;

/**
 * Created by gogumario on 2017-09-28.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.eluo.signage.java.intro.IntroActivity;

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

