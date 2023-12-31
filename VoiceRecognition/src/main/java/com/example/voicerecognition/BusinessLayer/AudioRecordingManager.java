package com.example.voicerecognition.BusinessLayer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;

public class AudioRecordingManager {
    public static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int[] SAMPLE_RATE_CANDIDATES = new int[]{16000, 11025, 22050, 44100};
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AMPLITUDE_THRESHOLD = 1500;
    private static final int SPEECH_TIMEOUT_MILLIS = 2000;
    private static final int MAX_SPEECH_LENGTH_MILLIS = 30 * 1000;
    private final Context myContext;
    private final OnAudioRecordingComplete onComplete;
    private AudioRecord mAudioRecord;
    private Thread mThread;
    private byte[] mBuffer;
    private byte[] audioData;
    private final Object mLock = new Object();

    /** The timestamp of the last time that voice is heard. */
    private long mLastVoiceHeardMillis = Long.MAX_VALUE;

    /** The timestamp when the current voice is started. */
    private long mVoiceStartedMillis;

    public AudioRecordingManager(Context context, @NonNull OnAudioRecordingComplete onComplete) {

        myContext = context;
        this.onComplete = onComplete;
    }

    /**
     * Starts recording audio.
     *
     * <p>The caller is responsible for calling {@link #stop()} later.</p>
     */
    public void start() {
        // Stop recording if it is currently ongoing.
        stop();
        // Try to create a new recording session.
        mAudioRecord = createAudioRecord();
        if (mAudioRecord == null) {
            onComplete.onFail("Cannot instantiate VoiceRecorder");
        }

        // Start recording.
        mAudioRecord.startRecording();
        // Start processing the captured audio.
        mThread = new Thread(new ProcessVoice());
        mThread.start();
    }

    /**
     * Stops recording audio.
     */
    public void stop() {
        synchronized (mLock) {
            dismiss();
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }
            if (mAudioRecord != null) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
            mBuffer = null;
        }
    }

    /**
     * Dismisses the currently ongoing utterance.
     */
    public void dismiss() {
        if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            onComplete.onSuccess(audioData);
        }
    }


    /**
     * Creates a new {@link AudioRecord}.
     *
     * @return A newly created {@link AudioRecord}, or null if it cannot be created (missing
     * permissions?).
     */
    private AudioRecord createAudioRecord() {
        for (int sampleRate : SAMPLE_RATE_CANDIDATES) {
            final int sizeInBytes = AudioRecord.getMinBufferSize(sampleRate, CHANNEL, ENCODING);
            if (sizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue;
            }

            if (ActivityCompat.checkSelfPermission(myContext, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
            {
                // ActivityCompat.requestPermissions((Activity) myContext, new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);

                final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        sampleRate, CHANNEL, ENCODING, sizeInBytes);
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    mBuffer = new byte[sizeInBytes];
                    return audioRecord;
                } else {
                    audioRecord.release();
                }
            }
        }
        return null;
    }

    /**
     * Continuously processes the captured audio and notifies {@link #} of corresponding
     * events.
     */
    private class ProcessVoice implements Runnable {

        @Override
        public void run() {
            while (true) {
                synchronized (mLock) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    final int size = mAudioRecord.read(mBuffer, 0, mBuffer.length);
                    final long now = System.currentTimeMillis();
                    if (isHearingVoice(mBuffer, size)) {
                        if (mLastVoiceHeardMillis == Long.MAX_VALUE) {
                            mVoiceStartedMillis = now;
                        }
                        audioData = appendByteArrays(audioData, mBuffer);

                        mLastVoiceHeardMillis = now;
                        if (now - mVoiceStartedMillis > MAX_SPEECH_LENGTH_MILLIS) {
                            end();
                        }
                    } else if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
                        audioData = appendByteArrays(audioData, mBuffer);

                        if (now - mLastVoiceHeardMillis > SPEECH_TIMEOUT_MILLIS) {
                            end();
                        }
                    }
                }
            }
        }

        private byte[] appendByteArrays(byte[] array1, byte[] array2) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                outputStream.write(array1);
                outputStream.write(array2);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return outputStream.toByteArray();
        }

        private void end() {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            onComplete.onSuccess(audioData);
        }

        private boolean isHearingVoice(byte[] buffer, int size) {
            for (int i = 0; i < size - 1; i += 2) {
                // The buffer has LINEAR16 in little endian.
                int s = buffer[i + 1];
                if (s < 0) s *= -1;
                s <<= 8;
                s += Math.abs(buffer[i]);
                if (s > AMPLITUDE_THRESHOLD) {
                    return true;
                }
            }
            return false;
        }

    }

}
