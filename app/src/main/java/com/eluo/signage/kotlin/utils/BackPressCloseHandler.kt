package com.eluo.signage.kotlin.utils

import android.app.Activity
import android.widget.Toast

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 뒤로가기 종료 처리
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) BackPressCloseHandler.kt
 * @since 2017-09-20
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-20][ggmario@eluocnc.com][CREATE: STATEMENT]
 */
class BackPressCloseHandler(private val activity: Activity) {

    private var backKeyPressedTime: Long = 0
    private var toast: Toast? = null

    fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.finish()
            toast!!.cancel()
        }
    }

    fun showGuide() {
        toast = Toast.makeText(activity,
                "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_LONG)
        toast!!.show()
    }
}