package com.example.voicerecognitionapp.libr;

import android.app.Activity;
import android.util.Log;
import com.example.voicerecognitionapp.R;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognizeRequest;
import com.google.cloud.speech.v1.RecognizeResponse;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechRecognitionResult;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.protobuf.ByteString;
import java.io.IOException;

public class RecognitionRepository
{
    private SpeechClient speechClient;
    private final Activity myContext;
    public RecognitionRepository(Activity myContext)
    {
        this.myContext = myContext;
        initializeSpeechClient();
    }

    private void initializeSpeechClient()
    {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(myContext.getResources().openRawResource(R.raw.vivoka_service_cred));
            FixedCredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
            speechClient = SpeechClient.create(SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build());
        } catch (IOException e) {

            Log.e("kya", "InitException" + e.getMessage());
        }
    }

    public void transcribeRecording(byte[] data, OnActionResult onActionResult) {
        try {

            Log.e("API_CALL", "API CALL STARTED...");

            Thread recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        RecognizeResponse response = speechClient.recognize(createRecognizeRequestFromVoice(data));

                        if (response.getResultsList().isEmpty())
                            onActionResult.onFail("Fail to recognize");

                        else
                        {
                            for (SpeechRecognitionResult result : response.getResultsList())
                            {
                                String transcript = result.getAlternativesList().get(0).getTranscript();
                                Log.e("RESULT", ":::: " + transcript);
                                onActionResult.onSuccess(transcript);
                            }
                        }

                    } catch (Exception e) {
                        Log.e("SEECOLE", "" + e.getMessage());
                        onActionResult.onFail(e.getMessage());
                    }

                }
            });
            recordingThread.start();

        } catch (Exception e) {
            Log.e("SEECOLE", "" + e.getMessage());
            onActionResult.onFail(e.getMessage());
        }
    }

    private RecognizeRequest createRecognizeRequestFromVoice(byte[] audioData) {
        RecognitionAudio audioBytes = RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(audioData)).build();

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                .setSampleRateHertz(16000)
                .setLanguageCode("en-US")
                .build();

        return RecognizeRequest.newBuilder()
                .setConfig(config)
                .setAudio(audioBytes)
                .build();
    }

}
