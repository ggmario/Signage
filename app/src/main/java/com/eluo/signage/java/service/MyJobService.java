package com.eluo.signage.java.service;

import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

/**
 * Project	  : Eluo Signage
 * Program    : Signage
 * Description	: 엘루오 씨엔시 사이니지 잡 스케줄러
 * Environment	: Android Studio 3.0
 * Notes	    : Developed by
 *
 * @(#) MyJobService.java
 * @since 2017-09-11
 * History	    : [DATE][Programmer][Description]
 * 		        : [2017-09-11][ggmario@eluocnc.com][CREATE: STATEMENT]
 */

public class MyJobService extends JobService {

    private static final String TAG = "MyJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Performing long running task in scheduled job");
        // TODO(developer): add long running task here.
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}