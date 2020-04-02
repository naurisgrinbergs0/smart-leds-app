package com.example.bt.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.bt.R;
import com.example.bt.SharedServices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MusicActivity extends AppCompatActivity {

    private Button syncButton;
    private SeekBar sensitivitySeekBar;
    private SeekBar smoothnessSeekBar;
    private SeekBar cutoffThresholdSeekBar;

    private View homeConstraintLayout3;

    RecordTask recordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        RequestPermissions();

        InitializeFields();

        // event listeners
        homeConstraintLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
            }
        });

        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(recordTask.isRecording == false) {
                    // start recording
                    recordTask.execute();
                    syncButton.setText("Stop");
                }
                else {
                    // stop recording
                    recordTask.isRecording = false;
                    syncButton.setText("Sync");
                }
            }
        });

        smoothnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SharedServices.DataTransfer.SetDuration(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void RequestPermissions() {
        if(ContextCompat.checkSelfPermission(this.getApplicationContext()
                , Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            // not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 200);
        }
    }

    private void InitializeFields() {
        syncButton = findViewById(R.id.syncButton);
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        smoothnessSeekBar = findViewById(R.id.smoothnessSeekBar);
        cutoffThresholdSeekBar = findViewById(R.id.cutoffThresholdSeekBar);
        recordTask = new RecordTask();
        homeConstraintLayout3 = findViewById(R.id.homeConstraintLayout3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200:{
                if(grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    RequestPermissions();
                return;
            }
        }
    }


    private class RecordTask extends AsyncTask {

        private int SAMPLE_RATE = 44100;
        public boolean isRecording = false;

        @Override
        protected Object doInBackground(Object[] objects) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            isRecording = true;

            // buffer size in bytes
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                bufferSize = SAMPLE_RATE * 2;
            }

            short[] audioBuffer = new short[bufferSize / 2];

            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (record.getState() != AudioRecord.STATE_INITIALIZED) {
                Log.e("AUDIO RECORD", "Audio Record can't initialize!");
                return null;
            }
            record.startRecording();

            Log.v("AUDIO RECORD", "Create recording");

            long shortsRead = 0;
            while (isRecording) {
                int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                shortsRead += numberOfShort;

                // Do something with the audioBuffer
                publishProgress(audioBuffer, numberOfShort);
            }

            record.stop();
            record.release();

            Log.v("AUDIO RECORD", String.format("Recording stopped. Samples read: %d", shortsRead));
            return null;
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onProgressUpdate(Object[] values) {
            short[] buffer = (short[])values[0];
            int size = (int) values[1];
            float sum = 0;
            int min = 0, max = 10000;
            for(int i = 0; i < size; i++){
                sum += Math.abs(buffer[i]); // divide so there are not so big values
            }
            float average = sum / size;
            float averageMapped = (average - (float) min) * (255f - 0f) / (max - min) + 0;

            float coefficient = (float) (255f / Math.pow(255f, sensitivitySeekBar.getProgress() / 10f));
            float value = (float) Math.abs(coefficient * Math.pow(averageMapped, sensitivitySeekBar.getProgress() / 10f));

            if(value < cutoffThresholdSeekBar.getProgress())
                value = 0;

            SharedServices.DataTransfer.SetSmoothColor(Color.rgb(value, 0, 0));
        }
    }

    public static int GetFrequency(int sampleRate, short [] audioData){

        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = numCrossing/2;
        float frequency = numCycles/numSecondsRecorded;

        return (int)frequency;
    }
}
