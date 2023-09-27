package com.example.voicerecognitionapp.libr;

public interface OnAudioRecordingComplete
{
    void onSuccess(byte[] audioData);
    void onFail(String error);
}
