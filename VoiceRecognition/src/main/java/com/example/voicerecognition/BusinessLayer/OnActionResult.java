package com.example.voicerecognition.BusinessLayer;

public interface OnActionResult
{
    void onSuccess(String result);
    void onFail(String error);
}
