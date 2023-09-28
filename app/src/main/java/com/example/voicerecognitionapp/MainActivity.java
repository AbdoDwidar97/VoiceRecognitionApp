package com.example.voicerecognitionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.voicerecognitionapp.databinding.ActivityMainBinding;
import com.example.voicerecognitionapp.libr.AudioRecordingManager;
import com.example.voicerecognitionapp.libr.RecognitionService;
import com.example.voicerecognitionapp.libr.RecognitionServiceTitles;

/// https://stackoverflow.com/questions/22249789/saving-sharedpreferences-in-android-service

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    // private RecognitionManager recManager;
    private Intent recService;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            boolean isSuccess = intent.getBooleanExtra(RecognitionServiceTitles.SUCCESS_STATUS.name(), false);
            String msg = intent.getStringExtra(RecognitionServiceTitles.RESULT.name());

            if (isSuccess)
                updateResult(msg);
            else
                showToast(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, permissions, AudioRecordingManager.REQUEST_RECORD_AUDIO_PERMISSION);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter(RecognitionServiceTitles.RECOGNITION_UPDATE.name()));

        /*recManager = new RecognitionManager(this, new OnActionResult() {
            @Override
            public void onSuccess(String result) {
                updateResult(result);
            }

            @Override
            public void onFail(String error) {
                showToast(error);
            }
        });*/

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStartClick();
            }
        });

        binding.btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnStopClick();
            }
        });
    }

    public boolean isForegroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if(RecognitionService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AudioRecordingManager.REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

    }

    private void btnStartClick()
    {
        // recManager.startRecording();

        if (!isForegroundServiceRunning())
        {
            recService = new Intent(this, RecognitionService.class);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(recService);
            }
        } else showToast("Service is already running");

        showToast("Starting ...");
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    private void btnStopClick()
    {
        stopService(recService);
        // recManager.stopRecording();
    }

    private void updateResult(String value)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.txtResult.setText(value);
            }
        });
    }

    private void showToast(String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

    }
}