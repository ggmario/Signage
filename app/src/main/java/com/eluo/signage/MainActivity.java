package com.eluo.signage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.eluo.signage.java.network.WebViewInterface;
import com.eluo.signage.java.service.NetworkReceiver;
import com.eluo.signage.kotlin.network.NetworkUtil;
import com.eluo.signage.kotlin.utils.BackPressCloseHandler;
import com.eluo.signage.kotlin.utils.Date;
import com.eluo.signage.kotlin.utils.ThreadPolicy;
import com.eluo.signage.kotlin.utils.WebViewSettingKt;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView = null;
    public static MainActivity instance = null;
    public static String network = "";
    private ProgressDialog mProgressDialog;
    private int iNowTime = 0;
    private int iTouchTime = 0;
    private String sEventUrl = null;
    private String sFloor, sTouchTime = null;
    private String sToken, sUrl, toUrl = null;
    private String sRestart = "N";
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
        setContentView(R.layout.activity_main);
        instance = this;
        sRestart = getIntent().getStringExtra("sRestart");

        setFullScreen();    //소프트 키 숨김

        new ThreadPolicy();
        loadFloor();
        loadScore();
        loadTime();

        if (sTouchTime.equals("없음")) {
            iTouchTime = 480;
        } else {
            iTouchTime = Integer.parseInt(sTouchTime);
        }
        if(NetworkUtil.INSTANCE.isNetworkConnected(MainActivity.instance)==true){
            //BroadcastReceiver 등록
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            NetworkReceiver receiver = new NetworkReceiver();
            registerReceiver(receiver,filter);

            mWebView = (WebView) findViewById(R.id.webView);       //activity_main.xml에서 id를  가지고 사용
            mWebViewInterface = new WebViewInterface(MainActivity.this, mWebView); //JavascriptInterface 객체화
            mWebView.addJavascriptInterface(mWebViewInterface, "EluoApp"); //웹뷰에 JavascriptInterface를 연결
            mWebView.setWebViewClient(new WishWebViewClient());     //웹뷰 Alert창 출력
            mWebView.setWebChromeClient(new WebChromeClient());     //크롭 Alert창 출력
            mWebView.getSettings().setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
            mWebView.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);//플러그인 사용할수 있도록 설정
            mWebView.setScrollBarStyle(mWebView.SCROLLBARS_OUTSIDE_OVERLAY); //여백제거

            if (18 < Build.VERSION.SDK_INT) {//캐시 사용안함
                mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {   // 웹뷰에서 시스템 텍스트 크기를 무시하도록 설정
                mWebView.getSettings().setTextZoom(100);
            }
            if (sEventUrl != null && !sEventUrl.equals("")) {
                mWebView.loadUrl(sUrl);
            } else {
                mWebView.loadUrl(WebViewSettingKt.formatUrl() + sFloor);            // 웹뷰에서 불러올 URL 입력
                sUrl = WebViewSettingKt.formatUrl() + sFloor;             //인트로 이후 처음 열린 페이지
            }
            mWebView.setWebViewClient(new WishWebViewClient());
            iNowTime = Integer.parseInt(Date.INSTANCE.sDate());  //처음 메인 페이지 호출 시간

            //푸시로 설정값 초기화 되었을때 앱 종료 처리함(재시작 하여 설정값 선택)
            if (sRestart != null && sRestart.equals("Y")) {
                sRestart = null;
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }

            //당겨서 새로 고침
//        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.activity_main);
//        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                mWebView.reload();
//            }
//        });

            mWebView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    // TODO Auto-generated method stub
                    Log.d("화면:", "터치");
                    switch (arg1.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (sTouchTime.equals("없음")) {
                                iTouchTime = 480;
                            } else {
                                iTouchTime = Integer.parseInt(sTouchTime);
                            }
                            //Log.e("shiki", "온클릭");
                            break;
                    }
                    return false;
                }
            });

            //일정 시간 간격 실행
            TimerTask adTast = new TimerTask() {
                public void run() {
                    iNowTime = Integer.parseInt(Date.INSTANCE.sDate());
                    if (toUrl != null) {
                        if (toUrl != sUrl) {
                            --iTouchTime;
                            System.out.println("MainTime:::" + iTouchTime);
                            if (iTouchTime == 0) {
                                mWebView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //동작
                                        mWebView.loadUrl(WebViewSettingKt.formatUrl() + sFloor);
                                        toUrl = sUrl;
                                    }
                                });
                            }
                        }
                    }
                }
            };
            Timer timer = new Timer();
            timer.schedule(adTast, 0, 60000); // 0초후 첫실행, 60초 마다 계속실행
        }else{
            System.out.println("네트워크 연결 끊어짐!!!!!!!!!!!!!!!!!");
        }
   }

   public static  void restartActivity (Activity act){
       Intent intent=new Intent();
       intent.setClass(act, act.getClass());
       act.finish();
       act.startActivity(intent);
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
        public void onPageFinished(WebView view, String url) {
//            mSwipeRefresh.setRefreshing(false);        //당겨서 새로 고침
            super.onPageFinished(view, url);
            mWebView.loadUrl("javascript:deviceUuid('" + sToken + "')");
            mWebView.loadUrl("javascript:deviceScd('" + sFloor + "')");
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            mSwipeRefresh.setRefreshing(false);        //당겨서 새로 고침
            super.onPageStarted(view, url, favicon);
            mWebView.loadUrl("javascript:deviceUuid('" + sToken + "')");
            mWebView.loadUrl("javascript:deviceScd('" + sFloor + "')");
        }
    }

    //토큰 정보 가져오기
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("PrefName", Activity.MODE_PRIVATE);
        sToken = pref.getString("token", "");
    }

    /* 프리퍼런스 가져오기 기기 층 정보 */
    private void loadFloor() {
        SharedPreferences pref = getSharedPreferences("Floor", Activity.MODE_PRIVATE);
        sFloor = pref.getString("floorCode", "");
    }

    /* 프리퍼런스 가져오기 기기 메인 이동 시간*/
    private void loadTime() {
        SharedPreferences pref = getSharedPreferences("Timemm", Activity.MODE_PRIVATE);
        sTouchTime = pref.getString("Time", "");
    }

    //소프트 키 숨김
    private void setFullScreen() {
        View view;
        view = findViewById(R.id.activity_main);
        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    /*  웹뷰 페이지 히스토리 백 후 처음 페이지로 왔을때 앱 종료 */
//웹뷰에서 뒤로가기 터치시 최초 호출 웹페이지에서 앱 종료 처리
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
//            mWebView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

//웹뷰 히스토리 백 마지막 페이지 경우 실행
//    @Override
//    public void onBackPressed() {
//     backPressCloseHandler.onBackPressed();
//      }

    //뒤로 가기로 앱 종료
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage(R.string.D_Question).setCancelable(false).setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        MainActivity.instance.finish();
                    }
                }).setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'NO' Button
                        dialog.cancel();
                    }
                    public boolean onLongClick(View v) {
                        Toast.makeText(getApplicationContext(),
                                "롱클릭", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle(R.string.D_TitleName);
        // Icon for AlertDialog
        alert.setIcon(R.mipmap.eluo_icon);
        alert.show();
    }

    //웹뷰에서 pdf 파일 열기 (구글)
    public void callWebUrl(String url) {
        new ThreadPolicy();
//        mWebView.loadUrl(WebViewSettingKt.googleDocslUrl()+"http://fs.eluocnc.com:8282/html/download/20160615_Manual_Reflects_Kiosk_1.pdf");
        mWebView.loadUrl(WebViewSettingKt.viewPdfUrl()+url);
    }

    //웹뷰에서 기본 브라우저 호출시 실행
    public void callOutBrowser(String url) {
        new ThreadPolicy();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

}

