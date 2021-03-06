package com.eluo.signage.java.network;

import android.app.Activity;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.eluo.signage.MainActivity;

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 웹뷰 인터페이스
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) WebViewInterface.java
 * @since 2017-09-01
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-01][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class WebViewInterface {
    private WebView mAppView;
    private Activity mContext;
    private final Handler handler = new Handler();
    /**
     * 생성자.
     * @param activity : context
     * @param view : 적용될 웹뷰
     */
    public WebViewInterface(Activity activity, WebView view) {
        mAppView = view;
        mContext = activity;
    }
    /**
     * 안드로이드 토스트를 출력한다. Time Long.
     * @param message : 메시지
     */
    @JavascriptInterface
    public void toastLong (String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }
    /**
     * 안드로이드 토스트를 출력한다. Time Short.
     * @param message : 메시지
     */
    @JavascriptInterface
    public void toastShort (String message) { // Show toast for a short time
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void webData (String message) { // Show toast for a short time
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void webUrl (final String message) { // Show toast for a short time
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        // 디바이스 기본 브라우져 호출
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.instance != null)
                    MainActivity.instance.callWebUrl(message);
            }
        });
    }

    @JavascriptInterface
    public void callCenterDial(final String callDiaNum) { // must be final
        // 전화
        handler.post(new Runnable() {
            @Override
            public void run() {
               // if(MainActivity.instance != null)
                    //MainActivity.instance.callCenterDial(callDiaNum);
            }
        });
    }
    @JavascriptInterface
    public void callOutBrowser(final String redirectUrl) { // must be final
        // 디바이스 기본 브라우져 호출
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (MainActivity.instance != null)
                    MainActivity.instance.callOutBrowser(redirectUrl);
            }
        });
    }

}
