package com.example.mrservice.controllers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import com.example.mrservice.services.BackgroundTasksJobService;
import com.example.mrservice.services.BackgroundTasksService;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class MyServicesControllerClass {

    private static JobScheduler jobScheduler;
    private static final int JOB_ID = 1;

    public static void startCustomBackgroundService(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {

            if (jobScheduler == null)
                jobScheduler = (JobScheduler) context
                        .getSystemService(JOB_SCHEDULER_SERVICE);

            ComponentName componentName = new ComponentName(context,
                    BackgroundTasksJobService.class);

            JobInfo jobInfoObj = new JobInfo.Builder(JOB_ID, componentName)
                    .setOverrideDeadline(0)
                    .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_LINEAR)
                    .setPersisted(true)
                    .build();

            jobScheduler.schedule(jobInfoObj);

        } else {
            context.startService(new Intent(context, BackgroundTasksService.class));
        }
    }

    public static void stopCustomBackgroundService(Context context) {

        if (Build.VERSION.SDK_INT >= 21) {

            if (jobScheduler == null)
                jobScheduler = (JobScheduler) context
                        .getSystemService(JOB_SCHEDULER_SERVICE);

            assert jobScheduler != null;
            jobScheduler.cancel(JOB_ID);

        } else {
            context.stopService(new Intent(context, BackgroundTasksService.class));
        }

    }

}
