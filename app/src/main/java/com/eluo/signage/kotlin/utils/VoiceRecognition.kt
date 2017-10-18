package com.eluo.signage.kotlin.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent


/**
 * Created by gogumario on 2017-09-11.
 * 음성 인식
 * @since  2017-09-11
 */

class VoiceRecognition(ctx: Context) {
    private val pm: PackageManager
    val VOICE_RECOGNITION_REQUEST_CODE = 1234

    init {
        this.pm = ctx.getPackageManager()
    }

    // 음성 인식을 지원하는지 확인
    fun recognitionAvailable(): Boolean {
        val activities = pm.queryIntentActivities(Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0)

        return if (activities.size != 0) {
            true
            // 지원할 경우 true 반환
        } else {
            false
            // 지원하지 않을 경우 false 반환
        }
    }

    // 구글 음성 인식 intent 생성
    fun getVoiceRecognitionIntent(message: String): Intent {
        val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, message)

        return intent
    }
}

