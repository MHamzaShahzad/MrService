package com.example.mrservice.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BackgroundTasksService extends Service {
    public BackgroundTasksService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
