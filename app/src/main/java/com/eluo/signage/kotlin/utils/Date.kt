package com.eluo.signage.kotlin.utils

import java.util.Date


/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 날짜
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) Date.kt
 * @since 2017-09-27
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-27][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

object Date{
    fun sDate(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdfnow = java.text.SimpleDateFormat("MMddHHmm")
        return sdfnow.format(date)
    }
}