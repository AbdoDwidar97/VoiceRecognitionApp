package com.example.voicerecognitionapp.libr;

import android.app.Activity;

import androidx.annotation.NonNull;

public class RecognitionManager
{
    private AudioRecordingManager recordingManager;
    private final RecognitionRepository recognitionRepository;
    private final Activity myActivity;
    private final OnActionResult actionResult;

    public RecognitionManager(Activity myActivity, @NonNull OnActionResult onActionResult)
    {
        recognitionRepository = new RecognitionRepository(myActivity);
        this.myActivity = myActivity;
        actionResult = onActionResult;
    }

    public void startRecording()
    {
        recordingManager = new AudioRecordingManager(myActivity, new OnAudioRecordingComplete() {
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
