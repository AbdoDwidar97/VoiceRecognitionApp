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
import com.example.voicerecognitionapp.libr.OnAudioRecordingComplete;
import com.example.voicerecognitionapp.libr.RecognitionRepository;

/// https://stackoverflow.com/questions/22249789/saving-sharedpreferences-in-android-service

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AudioRecordingManager recordingManager;
    private boolean permissionToRecordAccepted = false;
    private final String[] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActivityCompat.requestPermissions(this, permissions, AudioRecordingManager.REQUEST_RECORD_AUDIO_PERMISSION);

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
        recordingManager = new AudioRecordingManager(this, new OnAudioRecordingComplete() {
            @Override
            public void onSuccess(byte[] audioData) {
                recognizeRecord(audioData);
            }

            @Override
            public void onFail(String error) {
                showToast(error);
            }

        });

        recordingManager.start();
        showToast("Starting ...");
    }

    void recognizeRecord(byte[] audioData)
    {
        RecognitionRepository repository = new RecognitionRepository(this);
        repository.transcribeRecording(audioData, new OnActionResult() {
            @Override
            public void onSuccess(String result) {
                updateResult(result);
            }

            @Override
            public void onFail(String error) {
                showToast(error);
            }
        });
    }

    private void btnStopClick()
    {
        if (recordingManager != null) {
            recordingManager.stop();
            recordingManager = null;
        }
    }

    private void updateResult(String value)
    {
        binding.txtResult.setText(value);
    }

    private void showToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}