package com.eluo.signage.kotlin.utils

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 URL
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) WebViewSetting.kt
 * @since 2017-09-01
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-01][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

//url 정보
fun formatUrl(): String = "http://www.eluocnc.com/GW_V3/signage/default.asp?scd="   //기본 페이지 URL
fun verIntroUrl():String ="http://www.eluocnc.com/GW_V3/app/signageList.asp?"       //인트로 체크사용 URL
fun viewPdfUrl(): String = "https://docs.google.com/viewer?url="                    //구글 pdf 뷰어 URL
fun googleDocslUrl(): String ="https://docs.google.com/viewer?url="                 //구글 독스 URL
fun verUrl(): String ="http://fs.eluocnc.com:8282/SignageDownload.jsp"                //버전 다운로드 URL
//fun formatUrl(): String = "file:///android_asset/web.html?"                        //내부 html URL
//fun formatUrl(): String = "https://m.naver.com/?"                                   //테스트 URL

