package com.example.voicerecognitionapp.libr;

import android.app.Service;

import androidx.annotation.NonNull;

public class RecognitionManager
{
    private AudioRecordingManager recordingManager;
    private final RecognitionRepository recognitionRepository;
    private final Service myService;
    private final OnActionResult actionResult;

    public RecognitionManager(Service myService, @NonNull OnActionResult onActionResult)
    {
        recognitionRepository = new RecognitionRepository(myService);
        this.myService = myService;
        actionResult = onActionResult;
    }

    public void startRecording()
    {
        recordingManager = new AudioRecordingManager(myService, new OnAudioRecordingComplete() {
            @Override
            public void onSuccess(byte[] audioData) {
                recognizeRecord(audioData);
            }

            @Override
            public void onFail(String error) {
                actionResult.onFail(error);
            }

        });

        recordingManager.start();
    }

    void recognizeRecord(byte[] audioData)
    {
        recognitionRepository.transcribeRecording(audioData, new OnActionResult() {
            @Override
            public void onSuccess(String result) {
                actionResult.onSuccess(result);
            }

            @Override
            public void onFail(String error) {
                actionResult.onFail(error);
            }
        });
    }

    public void stopRecording()
    {
        if (recordingManager != null) {
            recordingManager.stop();
            recordingManager = null;
        }
    }

}
