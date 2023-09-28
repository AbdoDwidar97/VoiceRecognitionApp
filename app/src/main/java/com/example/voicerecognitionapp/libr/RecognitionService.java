package com.example.voicerecognitionapp.libr;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import javax.annotation.Nullable;

public class RecognitionService extends Service
{
    public RecognitionService()
    {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        /// here we should start recording.
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}