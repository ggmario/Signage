package com.eluo.signage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eluo.signage.java.network.WebViewInterface;
import com.eluo.signage.kotlin.utils.BackPressCloseHandler;
import com.eluo.signage.kotlin.utils.ThreadPolicy;
import com.eluo.signage.kotlin.utils.WebViewSettingKt;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 이벤트용 웹뷰
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) EventActivity.java
 * @since 2017-09-29
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-29][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class EventActivity  extends AppCompatActivity {
    private WebView mWebView = null;
    public static EventActivity instance = null;

    private int iTouchTime = 0;
    private String sEventUrl = null;
    private String sFloor, sTouchEventTime = null;
    private String sEvent = "N";
    private String sToken, sUrl, toUrl= null;
    private WebViewSettingKt WebViewSettingkt;
    private WebViewInterface mWebViewInterface;
    private SwipeRefreshLayout mSwipeRefresh; //당겨서 새로고침
    private Context mContext;
    private BackPressCloseHandler backPressCloseHandler;
    private GestureDetector mGestureDetector;

    @JavascriptInterface
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backPressCloseHandler = new BackPressCloseHandler(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_event);
        instance = this;
        sEventUrl = getIntent().getStringExtra("sEventUrl");

        setFullScreen();    //소프트 키 숨김

        new ThreadPolicy();
        loadFloor();
        loadScore();
        idByAndroidId();
        loadEventTime();

        iTouchTime = Integer.parseInt(sTouchEventTime);

        mWebView = (WebView) findViewById(R.id.webViewEvent);       //activity_event.xml에서 id를  가지고 사용
        mWebViewInterface = new WebViewInterface(EventActivity.this, mWebView); //JavascriptInterface 객체화
        mWebView.addJavascriptInterface(mWebViewInterface, "EluoApp"); //웹뷰에 JavascriptInterface를 연결
        mWebView.setWebViewClient(new WishWebViewClient());     //웹뷰 Alert창 출력
        mWebView.setWebChromeClient(new WebChromeClient());     //크롭 Alert창 출력
        mWebView.getSettings().setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
        if(18 < Build.VERSION.SDK_INT ){//캐시 사용안함
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {   // 웹뷰에서 시스템 텍스트 크기를 무시하도록 설정
            mWebView.getSettings().setTextZoom(100);
        }
        if(sEventUrl !=  null && !sEventUrl.equals("") ){
            mWebView.loadUrl(sEventUrl);
        }else{
            mWebView.loadUrl(WebViewSettingKt.formatUrl()+sFloor);            // 웹뷰에서 불러올 URL 입력
            sUrl = WebViewSettingKt.formatUrl() + sFloor;             //인트로 이후 처음 열린 페이지
        }
        mWebView.setWebViewClient(new WishWebViewClient());

        //당겨서 새로 고침
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.activity_event);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });

        mWebView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                switch (arg1.getAction()){
                    case MotionEvent.ACTION_DOWN :iTouchTime = Integer.parseInt(sTouchEventTime);
                        //Log.e("shiki", "온클릭");
                        break;
                }
                return false;
            }
        });

        //일정 시간 간격 실행
        TimerTask adTast = new TimerTask() {
            public void run() {
                System.out.println("EventTime:::"+iTouchTime);
                if(!sEvent.equals("Y")){
                    --iTouchTime;
                    if(iTouchTime == 0){
                        finish();
                        sEvent = "Y";
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(adTast, 0, 60000); // 0초후 첫실행, 60초 마다 계속실행
    }
    private class WishWebViewClient extends WebViewClient {
        //url 주소에 해당하는 웹페이지 로딩
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            toUrl = mWebView.getUrl();
            return true;
        }

        //페이지 로딩시 호출된다.
        @Override
        public void onPageFinished(WebView view, String url)
        {
            mSwipeRefresh.setRefreshing(false);
            super.onPageFinished(view, url);
            mWebView.loadUrl("javascript:deviceUuid('"+sToken+"')");
            mWebView.loadUrl("javascript:deviceScd('"+sFloor+"')");
        }
    }


    //토큰 정보 가져오기
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("PrefName", Activity.MODE_PRIVATE);
        sToken = pref.getString("token", "");
    }

    //안드로이드 고유 ID
    private void idByAndroidId() {
        String idByANDROID_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        System.out.println("==================::::" + idByANDROID_ID);
    }

    /* 프리퍼런스 가져오기 기기 층 정보 */
    private void loadFloor() {
        SharedPreferences pref = getSharedPreferences("Floor", Activity.MODE_PRIVATE);
        sFloor = pref.getString("floorCode", "");

    }
    /* 프리퍼런스 가져오기 기기 메인 이동 시간*/
    private void loadEventTime() {
        SharedPreferences pref = getSharedPreferences("Event", Activity.MODE_PRIVATE);
        sTouchEventTime = pref.getString("eventTime", "");
        System.out.println(";;;;;;;"+sTouchEventTime);
    }

    //소프트 키 숨김
    private void setFullScreen() {
        View view;
        view = findViewById(R.id.activity_event);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

        /*  웹뷰 페이지 히스토리 백 후 처음 페이지로 왔을때 앱 종료 */
//웹뷰에서 뒤로가기 터치시 최초 호출 웹페이지에서 앱 종료 처리
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//웹뷰 히스토리 백 마지막 페이지 경우 실행
    @Override
    public void onBackPressed() {
     backPressCloseHandler.onBackPressed();
      }
}
