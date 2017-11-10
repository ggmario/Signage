package com.eluo.signage.kotlin.network

import android.content.Context
import android.net.ConnectivityManager

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 네트워크 연결 확인 유틸
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) NetworkReceiver.java
 * @since 2017-09-01
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-01][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

object NetworkUtil {

    val TYPE_WIFI = 1
    val TYPE_MOBILE = 2
    val TYPE_NOT_CONNECTED = 0

    /** 네트워크 타입확인.  */
    fun getConnectivityStatus(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                return TYPE_WIFI
            }
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                return TYPE_MOBILE
            }
        }
        return TYPE_NOT_CONNECTED
    }

    /** 연결 내용 출력  */
    fun getConnectivityStatusString(context: Context): String? {
        val conn = NetworkUtil.getConnectivityStatus(context)
        var status: String? = null

        if (conn == NetworkUtil.TYPE_WIFI) {
            status = "Wifi network enabled"
        } else if (conn == NetworkUtil.TYPE_MOBILE) {
            status = "3G/4G network enabled"
        } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet"
        }
        return status
    }

    /** 네트워크 연결 확인.  */
    fun isNetworkConnected(context: Context): Boolean {
        var isConnected = false
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //시스템으로 부터 서비스를 가져온다.
        val mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)//
        val wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)// WIFI 관련 정보를 가져온다.
        val wimax = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)

        if (wimax != null) {
            isConnected = wimax.isConnected
        }
        if (mobile != null) {
            if (mobile.isConnected || wifi.isConnected || isConnected) {
                isConnected = true
            }
        } else {
            if (wifi.isConnected || isConnected) {
                isConnected = true
            } else {
                isConnected = false
            }
        }
        return isConnected
    }
}