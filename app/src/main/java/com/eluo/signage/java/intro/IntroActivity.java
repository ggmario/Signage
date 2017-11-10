package com.eluo.signage.java.intro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.eluo.signage.MainActivity;
import com.eluo.signage.R;
import com.eluo.signage.kotlin.network.NetworkUtil;
import com.eluo.signage.kotlin.utils.ThreadPolicy;
import com.eluo.signage.kotlin.utils.WebViewSettingKt;
import com.google.firebase.crash.FirebaseCrash;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 인트로
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) IntroActivity.java
 * @since 2017-09-01
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-01][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class IntroActivity extends Activity {
    private ProgressDialog mProgressDialog;
    private Handler mHandler;
    private String sFloor,sUuid,sFloorCd, sFloorNm, sAllReset = null;
    private String sVersion = "1.0";
    private String sTouchTime = null;
    Handler h;//핸들러 선언
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 화면위 타이틀 없애기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 전체화면 만들기

        //앱 설치시 home 화면에 바로가기 앱 아이콘 생성 기능
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        pref.getString("check", "");
        if(pref.getString("check", "").isEmpty()){
            Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent.setClassName(this, getClass().getName());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Intent intent = new Intent();

            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.mipmap.eluo_icon));
            intent.putExtra("duplicate", false);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

            sendBroadcast(intent);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("check", "exist");
        editor.commit();

        try {
            sVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //네트워크 체크 수행
        if (NetworkUtil.INSTANCE.isNetworkConnected(this)) {
            //프리퍼런스 가져오기
            loadScore();
            loadFloor();
            loadTime();

            loadReset();
            if(sAllReset.equals("A")){
                new ThreadPolicy();
                jsonFloorList();
                String[][] parsedData = jsonFloorList();
                if (parsedData != null && parsedData.length > 0) {
                    String[] arrayName = new String[parsedData.length];
                    String[] arrayCode = new String[parsedData.length];

                    for (int i = 0; i < parsedData.length; i++) {
                        arrayCode[i] = parsedData[i][0];
                        arrayName[i] = parsedData[i][1];
                    }
                    DialogSelectOption(arrayCode, arrayName);
                }
            }else {
                if (sFloor != null && !sFloor.equals("")) {
                    if (sTouchTime != null && !sTouchTime.equals("")) {
                        onVer(sVersion);
                    } else {
                        if (sTouchTime == null || sTouchTime.equals("")) {
                            DialogSelectOptionTime();
                            Toast.makeText(getApplicationContext(), R.string.T_time_meg, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    new ThreadPolicy();
                    jsonFloorList();
                    String[][] parsedData = jsonFloorList();
                    if (parsedData != null && parsedData.length > 0) {
                        String[] arrayName = new String[parsedData.length];
                        String[] arrayCode = new String[parsedData.length];

                        for (int i = 0; i < parsedData.length; i++) {
                            arrayCode[i] = parsedData[i][0];
                            arrayName[i] = parsedData[i][1];
                        }
                        DialogSelectOption(arrayCode, arrayName);
                    }
                }
            }
        }else{
            final AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
            //경고 메시지 후 프로그스다이얼로그 표시 할 경우 주석 해제
            alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog = ProgressDialog.show(IntroActivity.this, "네트워크 연결 확인 중...","잠시만 기다려 주세요.", true);
                    new Handler().postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          if (mProgressDialog != null && mProgressDialog.isShowing()) {
                              mProgressDialog.dismiss();
                          }
                          if (NetworkUtil.INSTANCE.isNetworkConnected(IntroActivity.this)) {
                              onVer(sVersion);
                          }else{
                              mProgressDialog.dismiss();
                              alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                  @Override
                                  public void onClick(DialogInterface dialog, int which) {
                                      dialog.dismiss();     //닫기
                                      finish(); //종료
                                  }
                              });
                              alert.setMessage(R.string.network_error_msge);
                              alert.show();
                          }
                      }
                  },15000);
                    //finish(); //종료
                }
            });
            alert.setMessage(R.string.network_error_chkd);
            alert.show();
        }
    }

    Runnable mrun = new Runnable(){
        @Override
        public void run(){
            Intent i = new Intent(IntroActivity.this, MainActivity.class); //인텐트 생성(현 액티비티, 새로 실행할 액티비티)
            startActivity(i);
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            //overridePendingTransition 이란 함수를 이용하여 fade in,out 효과를줌. 순서가 중요
        }
    };
    //인트로 중에 뒤로가기를 누를 경우 핸들러를 끊어버려 아무일 없게 만드는 부분
    //미 설정시 인트로 중 뒤로가기를 누르면 인트로 후에 홈화면이 나옴.
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        h.removeCallbacks(mrun);
    }

    //외부 브라우저 호출 (인트로 화면에서만 사용함)
    public void callBrowser(String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //버전 체크
    public void onVer(String ver){
        String sVersionName = ver;
        try {
            sVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            new ThreadPolicy();
            String sVer = "";
            String[][] parsedData = jsonVersion();

            if (parsedData != null && parsedData.length > 0) {
                sVer = parsedData[0][0];
            } else {
                sVer = sVersionName;
                Log.i("버전 체크:", "앱 버전 체크 실패 하였습니다");
                FirebaseCrash.report(new Exception("앱 버전 체크 실패 하였습니다"));
            }
            if (!sVersionName.equals(sVer)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                // 여기서 부터는 알림창의 속성 설정
                builder.setTitle(R.string.D_TitleName)
                        .setMessage(R.string.D_Update)
                        .setCancelable(false)
                        .setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                                callBrowser(WebViewSettingKt.verUrl());
                            }
                        })
                        .setNegativeButton(R.string.D_Canceled, new DialogInterface.OnClickListener() {
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton) {
                                SharedPreferences prefs = getSharedPreferences("Floor", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("floorCode", "");
                                editor.commit();
                                finish();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                //requestWindowFeature(Window.FEATURE_NO_TITLE); //인트로화면이므로 타이틀바를 없앤다
                setContentView(R.layout.activity_intro);
                h = new Handler(); //딜래이를 주기 위해 핸들러 생성
                h.postDelayed(mrun, 2000); // 딜레이 ( 런어블 객체는 mrun, 시간 2초)
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            AlertDialog.Builder alert = new AlertDialog.Builder(IntroActivity.this);
            alert.setPositiveButton(R.string.D_Approval, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.setMessage(R.string.network_error_msg);
            alert.show();
        }
    }
    /* 프리퍼런스 가져오기 UUID(토큰)*/
    private void loadScore() {
        SharedPreferences pref = getSharedPreferences("PrefName", Activity.MODE_PRIVATE);
        sUuid = pref.getString("token", "");
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

    /* 프리퍼런스 가져오기 기기 초기 설정값 초기화*/
    private void loadReset() {
        SharedPreferences pref = getSharedPreferences("Reset", Activity.MODE_PRIVATE);
        sAllReset = pref.getString("allReset", "");
    }
    //사용할 위치 선택
    private void DialogSelectOption(String[] arrayCode,String[] arrayName) {
        final String items[] = arrayName;
        final String itemsCd[] = arrayCode;
//        final String items[] = { "item1", "item2", "item3" };
        AlertDialog.Builder ab = new AlertDialog.Builder(IntroActivity.this);
        ab.setTitle("사용 할 위치를 선택 하십시오");
        ab.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sFloorCd = itemsCd[whichButton];
                        sFloorNm = items[whichButton];
                        SharedPreferences prefs = getSharedPreferences("Floor", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("floorCode", sFloorCd);
                        editor.commit();

                        SharedPreferences prefsnew = getSharedPreferences("Event", MODE_PRIVATE);
                        SharedPreferences.Editor editornew = prefsnew.edit();
                        editornew.putString("eventTime", "5");
                        editornew.commit();
                        // 각 리스트를 선택했을때
                    }
                }).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                        if(sAllReset.equals("A")){
                            SharedPreferences prefs = getSharedPreferences("Reset", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("allReset", "");
                            editor.commit();
                            DialogSelectOptionTime();
                        }else {
                            if (sFloorCd != null && !sFloorCd.equals("")) {
                                jsonSignageInfo();
                                if (sTouchTime != null && !sTouchTime.equals("")) {
                                    onVer(sVersion);
                                } else {
                                    DialogSelectOptionTime();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),R.string.T_rdo_date, Toast.LENGTH_LONG).show();
                                //인덴드를 새로 시작
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            }
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences prefs = getSharedPreferences("Floor", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("floorCode", "");
                        editor.commit();
                        Toast.makeText(getApplicationContext(),R.string.T_end_meg, Toast.LENGTH_LONG).show();
                        finish();
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }

    //메인 이동 시간 선택
    private  void  DialogSelectOptionTime(){
        final String itemsTime[] = { "5", "10", "15","20","25","30","없음" };
        AlertDialog.Builder ab = new AlertDialog.Builder(IntroActivity.this);
        ab.setTitle(R.string.D_main);
        ab.setSingleChoiceItems(itemsTime, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sTouchTime = itemsTime[whichButton];
                        SharedPreferences prefs = getSharedPreferences("Timemm", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("Time", sTouchTime);
                        editor.commit();

                        // 각 리스트를 선택했을때
                    }
                }).setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {                        // OK 버튼 클릭시 , 여기서 선택한 값을 메인 Activity 로 넘기면 된다.
                        if(sTouchTime != null && !sTouchTime.equals("")){
                            onVer(sVersion);
                        }else{
                            Toast.makeText(getApplicationContext(),R.string.T_time_meg, Toast.LENGTH_LONG).show();
                            //인덴드를 새로 시작
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    }
                }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences prefs = getSharedPreferences("Timemm", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("Time", "");
                        editor.commit();
                        Toast.makeText(getApplicationContext(),R.string.T_end_meg, Toast.LENGTH_LONG).show();
                        finish();
                        // Cancel 버튼 클릭시
                    }
                });
        ab.show();
    }

    //버전 체크
    private String[][] jsonVersion() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            url= new URL(WebViewSettingKt.verIntroUrl()+"scd="+sFloor+"&uuid="+sUuid);
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(500);
            urlc.connect();
        }catch (Exception e){
            return null;
        }
        try {
            //URL 접속
            urlConnection = (HttpURLConnection) url.openConnection();
            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page+=line;
            }
            //읽어들인 JSON포맷의 데이터를 JSON객체로 변환
            JSONObject json = new JSONObject(page);

            //ksk_list 에 해당하는 배열을 할당
            JSONArray jArr = json.getJSONArray("signage");
            String[][] parseredData = new String[jArr.length()][jArr.length()];

//            for (int i=0; i<jArr.length(); i++){
                json = jArr.getJSONObject(0);
                parseredData[0][0] = json.getString("APPVER");
//            }
            return parseredData;
        } catch (Exception e) {
            Log.e("err_v","앱버전 정보 가져오기 실패"+e);
            FirebaseCrash.report(new Exception("앱 버전 정보 가져오기 실패"+e));
            return null;
        }finally{
            urlConnection.disconnect();      //URL 연결 해제
        }
    }

    //층정보 가져오기
    private String[][] jsonFloorList() {
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //웹서버 URL 지정
            url= new URL(WebViewSettingKt.verIntroUrl()+"scd=&uuid=");
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(500);
            urlc.connect();
        }catch (Exception e){
            System.out.println("error::::::"+e);
            FirebaseCrash.report(new Exception("층별 정보가져오기 실패"+e));
            return null;
        }
        try {
            //URL 접속
            urlConnection = (HttpURLConnection) url.openConnection();
            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page+=line;
            }
            //읽어들인 JSON포맷의 데이터를 JSON객체로 변환
            JSONObject json = new JSONObject(page);

            //ksk_list 에 해당하는 배열을 할당
            JSONArray jArr = json.getJSONArray("signage");
            String[][] parseredData = new String[jArr.length()][5];

            for (int i=0; i<jArr.length(); i++){
                json = jArr.getJSONObject(i);
                parseredData[i][0] = json.getString("SCD");
                parseredData[i][1] = json.getString("SNAME");
                parseredData[i][2] = json.getString("UUID");
                parseredData[i][3] = json.getString("DFTCD");
                parseredData[i][4] = json.getString("APPVER");
            }
            return parseredData;
        } catch (Exception e) {
            Log.e("err_v","앱버전 정보 가져오기 실패");
            FirebaseCrash.report(new Exception("앱 버전 정보 가져오기 실패"));
            return null;
        }finally{
            urlConnection.disconnect();      //URL 연결 해제
        }
    }

    //서버에 정보 저장
    private String[][] jsonSignageInfo(){
        URL url = null;
        HttpURLConnection urlConnection = null;
        try{
            //프리퍼런스 가져오기
            loadScore();
            loadFloor();

            //웹서버 URL 지정
            url= new URL(WebViewSettingKt.verIntroUrl()+"scd="+sFloorCd+"&uuid="+sUuid);
            HttpURLConnection urlc =(HttpURLConnection)url.openConnection();
            urlc.setConnectTimeout(500);
            urlc.connect();
        }catch (Exception e){
            return null;
        }
        try {
            //URL 접속
            urlConnection = (HttpURLConnection) url.openConnection();
            //[웹문서 소스를 버퍼에 저장]
            //데이터를 버퍼에 기록
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            Log.d("line:",bufreader.toString());
            String line = null;
            String page = "";

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page+=line;
            }
            //읽어들인 JSON포맷의 데이터를 JSON객체로 변환
            JSONObject json = new JSONObject(page);

            //ksk_list 에 해당하는 배열을 할당
            JSONArray jArr = json.getJSONArray("signage");
            String[][] parseredData = new String[jArr.length()][5];
            //for (int i=0; i<jArr.length(); i++){
            json = jArr.getJSONObject(0);
            parseredData[0][0] = json.getString("SCD");
            parseredData[0][1] = json.getString("SNAME");
            parseredData[0][2] = json.getString("UUID");
            parseredData[0][3] = json.getString("DFTCD");
            parseredData[0][4] = json.getString("APPVER");
            //  }
            return parseredData;
        } catch (Exception e) {
            Log.e("err_v","층별 리스트 조회 실패:"+e);
            FirebaseCrash.report(new Exception("층별 정보 조회 실패"+e));
            return null;
        }finally{
            urlConnection.disconnect();      //URL 연결 해제
        }
    }
}
