package com.eluo.signage.kotlin.utils

import android.annotation.TargetApi
import android.os.Build
import android.os.StrictMode



/**
 * Created by gogumario on 2017-09-01.
 * 스레드: 백그라운드로 돌려놓고 다른 여러가지 일 처리 목적
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
class ThreadPolicy {
    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
}
