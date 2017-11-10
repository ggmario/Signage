package com.eluo.signage.kotlin.utils

import android.annotation.TargetApi
import android.os.Build
import android.os.StrictMode

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 스레드(백그라운드로 돌려 놓고 다른여러가지 일 처리 목적)
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) ThreadPolicy.kt
 * @since 2017-09-01
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-01][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
class ThreadPolicy {
    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }
}
