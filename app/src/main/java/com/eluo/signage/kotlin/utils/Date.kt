package com.eluo.signage.kotlin.utils

import java.util.Date

/**
 * Created by gogumario on 2017-09-27.
 */
object Date{
    fun sDate(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdfnow = java.text.SimpleDateFormat("MMddHHmm")
        return sdfnow.format(date)
    }
}