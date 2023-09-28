package com.example.voicerecognitionapp.libr;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.voicerecognitionapp.R;

import javax.annotation.Nullable;

public class RecognitionService extends Service
{
    private RecognitionManager recManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        initService();
    }

    void initService()
    {
        recManager = new RecognitionManager(this, new OnActionResult() {
            @Override
            public void onSuccess(String result) {
                sendMessageToActivity(result, true);
            }

            @Override
            public void onFail(String error) {
                sendMessageToActivity(error, false);
            }
        });
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        final String CHANNELID = "Foreground Service ID";
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification.Builder notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Recording audio running...")
                    .setContentTitle("Recording Audio")
                    .setSmallIcon(R.drawable.ic_launcher_background);
        }

        assert notification != null;
        startForeground(1001, notification.build());

        /// here we should start recording.
        recManager.startRecording();
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessageToActivity(String msg, boolean successStatus) {
        Intent intent = new Intent(RecognitionServiceTitles.RECOGNITION_UPDATE.name());
        // You can also include some extra data.
        intent.putExtra(RecognitionServiceTitles.RESULT.name(), msg);
        intent.putExtra(RecognitionServiceTitles.SUCCESS_STATUS.name(), successStatus);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy()
    {
        recManager.stopRecording();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}