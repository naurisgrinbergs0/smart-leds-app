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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MusicActivity extends AppCompatActivity {

    private Button syncButton;
    private SeekBar sensitivitySeekBar;
    private SeekBar smoothnessSeekBar;

    private View homeConstraintLayout3;

    RecordTask recordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

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

        sensitivitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(recordTask != null){
                    recordTask.sensitivity = (sensitivitySeekBar.getMax() - progress) / 10f;
                    recordTask.coefficient = (float) (255f / Math.pow(255f, recordTask.sensitivity));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void InitializeFields() {
        syncButton = findViewById(R.id.syncButton);
        sensitivitySeekBar = findViewById(R.id.sensitivitySeekBar);
        smoothnessSeekBar = findViewById(R.id.smoothnessSeekBar);
        homeConstraintLayout3 = findViewById(R.id.homeConstraintLayout3);

        recordTask = new RecordTask();
        recordTask.sensitivity = (sensitivitySeekBar.getMax() - sensitivitySeekBar.getProgress()) / 10f;
        recordTask.coefficient = (float) (255f / Math.pow(255f, recordTask.sensitivity));
    }


    private class RecordTask extends AsyncTask {

        private int SAMPLE_RATE = 44100;
        public boolean isRecording = false;
        public float sensitivity;
        public float coefficient;

        @Override
        protected Object doInBackground(Object[] objects) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

            isRecording = true;

            // buffer size in bytes
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE)
                bufferSize = SAMPLE_RATE * 2;

            short[] audioBuffer = new short[bufferSize / 2];

            AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize);

            if (record.getState() != AudioRecord.STATE_INITIALIZED)
                return null;

            record.startRecording();

            while (isRecording) {
                int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
                publishProgress(audioBuffer, numberOfShort);
            }

            record.stop();
            record.release();

            return null;
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onProgressUpdate(Object[] values) {
            short[] buffer = (short[])values[0];
            int size = (int) values[1];

            float sum = 0;
            int min = 0, max = 10000;
            for(int i = 0; i < size; i++)
                sum += Math.abs(buffer[i]);

            float average = sum / size;
            float averageMapped = (average - (float) min) * (255f - 0f) / (max - min) + 0;
            float value = (int) Math.abs(coefficient * Math.pow(averageMapped, sensitivity));

            SharedServices.DataTransfer.SetSmoothColor(Color.rgb(value, 0, 0));
        }

        private int GetFrequency(int sampleRate, short [] audioData){

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
}
