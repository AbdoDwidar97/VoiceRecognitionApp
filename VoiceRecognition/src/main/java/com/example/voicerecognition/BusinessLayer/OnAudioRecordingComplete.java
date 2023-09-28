package com.example.voicerecognition.BusinessLayer;

public interface OnAudioRecordingComplete
{
    void onSuccess(byte[] audioData);
    void onFail(String error);
}
