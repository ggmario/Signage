package com.eluo.signage.java.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.eluo.signage.MainActivity;
import com.eluo.signage.R;
import com.eluo.signage.kotlin.network.NetworkUtil;
import com.eluo.signage.kotlin.utils.BackPressCloseHandler;
import com.eluo.signage.kotlin.utils.ThreadPolicy;
import com.eluo.signage.kotlin.utils.WebViewSettingKt;

/**
 * Created by gogumario on 2017-11-10.
 */

public class NetworkActivity extends AppCompatActivity {
    private WebView mWebView = null;
    public static NetworkActivity instance = null;
    private ProgressDialog mProgressDialog;
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

        if(NetworkUtil.INSTANCE.isNetworkConnected(MainActivity.instance)==true) {
            finish();
        }else {
            Context ctx = this;
            final AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
            mProgressDialog = ProgressDialog.show(NetworkActivity.instance, "네트워크 연결 확인 중...", "잠시만 기다려 주세요.", true);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (NetworkUtil.INSTANCE.isNetworkConnected(NetworkActivity.instance) == true) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                            NetworkActivity.instance.finish();
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    } else {
                        alert.setTitle("알림");
                        alert.setMessage("네트워크 연결 시도에 실패 하였습니다.\n네트워크 연결을 확인이 필요합니다.");
                        alert.setPositiveButton("재시도", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        });
                        alert.setCancelable(false); //바탕화면 터치시 종료 방지
                        alert.create();
                        alert.show();
                    }
                }
            }, 10000);
        }
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
