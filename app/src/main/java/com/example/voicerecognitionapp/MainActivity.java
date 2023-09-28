package com.example.voicerecognitionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.example.voicerecognitionapp.databinding.ActivityMainBinding;
import com.example.voicerecognitionapp.libr.AudioRecordingManager;
import com.example.voicerecognitionapp.libr.OnActionResult;
import com.example.voicerecognitionapp.libr.RecognitionManager;

/// https://stackoverflow.com/questions/22249789/saving-sharedpreferences-in-android-service

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private RecognitionManager recManager;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, permissions, AudioRecordingManager.REQUEST_RECORD_AUDIO_PERMISSION);

        recManager = new RecognitionManager(this, new OnActionResult() {
            @Override
            public void onSuccess(String result) {
                updateResult(result);
            }

            @Override
            public void onFail(String error) {
                showToast(error);
            }
        });

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AudioRecordingManager.REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

    }

    private void btnStartClick()
    {
        recManager.startRecording();
        showToast("Starting ...");
    }

    private void btnStopClick()
    {
        recManager.stopRecording();
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}