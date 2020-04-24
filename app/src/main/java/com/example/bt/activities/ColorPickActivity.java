package com.example.bt.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bt.Animator;
import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.services.ForegroundService;
import com.example.bt.views.BrightnessSlider;
import com.example.bt.views.ColorHive;
import com.example.bt.views.ColorWheel;

import java.text.DecimalFormat;

import static com.example.bt.Animator.*;
import static com.example.bt.SharedServices.*;

public class ColorPickActivity extends ActivityHelper {

    private ColorHive colorHiveView;
    private ColorWheel colorWheelView;
    private View homeConstraintLayout;
    private CheckBox smoothTransitionCheckBox;
    private int colorPickerIndex = 0;
    private Button prevPickerButton;
    private Button nextPickerButton;
    private SeekBar durationSeekBar;
    private TextView durationTextView;
    private BrightnessSlider brightnessSlider;

    @Override
    protected void onStart() {
        super.onStart();
        ForegroundService service = ((ForegroundService)Service.Get(Service.FOREGROUND));
        if(service.bt.GetConnectedDevice() == null)
            goBack();
        InitializeUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Activity.Add(Activity.COLOR_PICK, this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);

        InitializeFields();

        // event listeners      v   v   v
        homeConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });


        colorHiveView.setOnColorChangeEventListener(new ColorHive.OnColorChangeEventListener() {
            @Override
            public void onEvent(int color) {
                if(smoothTransitionCheckBox.isChecked())
                    DataTransfer.SetSmoothColor(color);
                else
                    DataTransfer.SetPlainColor(color);
            }
        });


        colorWheelView.setOnColorChangeEventListener(new ColorWheel.OnColorChangeEventListener() {
            @Override
            public void onEvent(int color) {
                if(smoothTransitionCheckBox.isChecked())
                    DataTransfer.SetSmoothColor(color);
                else
                    DataTransfer.SetPlainColor(color);
            }
        });


        prevPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerIndex = (colorPickerIndex == 0) ? 1 : (colorPickerIndex -= 1);
                MemoryConnector.setInt(ColorPickActivity.this, getString(R.string.var_picker_index), colorPickerIndex);
                changePicker(true);
            }
        });
        nextPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerIndex = (colorPickerIndex == 1) ? 0 : (colorPickerIndex += 1);
                MemoryConnector.setInt(ColorPickActivity.this, getString(R.string.var_picker_index), colorPickerIndex);
                changePicker(true);
            }
        });


        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DataTransfer.SetDuration(progress);
                DecimalFormat format = new DecimalFormat("0.00");
                durationTextView.setText(format.format(progress / 1000f));
                MemoryConnector.setInt(ColorPickActivity.this, getString(R.string.var_smooth_transition_duration), progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        smoothTransitionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // vibrate
                vibrate(getApplicationContext(), getResources().getInteger(R.integer.smooth_transation_check_box_vibrate_duration));
                DataTransfer.SetDuration(durationSeekBar.getProgress());
                MemoryConnector.setBool(ColorPickActivity.this, getString(R.string.var_smooth_transition_enabled), isChecked);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Activity.Remove(Activity.COLOR_PICK);
    }

    private void goBack() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void InitializeFields(){
        colorHiveView = findViewById(R.id.colorHiveView);
        colorWheelView = findViewById(R.id.colorWheelView);
        homeConstraintLayout = findViewById(R.id.homeConstraintLayout);
        smoothTransitionCheckBox = findViewById(R.id.smoothTransitionCheckBox);
        prevPickerButton = findViewById(R.id.prevPickerButton);
        nextPickerButton = findViewById(R.id.nextPickerButton);
        durationSeekBar = findViewById(R.id.durationSeekBar);
        durationTextView = findViewById(R.id.durationTextView);
        brightnessSlider = findViewById(R.id.brightnessSlider);

        colorHiveView.BindBrightnessSlider(brightnessSlider);
        colorWheelView.BindBrightnessSlider(brightnessSlider);
        brightnessSlider.BindColorPicker(colorHiveView);
    }

    private void InitializeUI(){
        colorPickerIndex = MemoryConnector.getInt(ColorPickActivity.this, getString(R.string.var_picker_index));
        smoothTransitionCheckBox.setChecked(
                MemoryConnector.getBool(ColorPickActivity.this, getString(R.string.var_smooth_transition_enabled)));
        durationSeekBar.setProgress(
                MemoryConnector.getInt(ColorPickActivity.this, getString(R.string.var_smooth_transition_duration)));
        DataTransfer.SetDuration(durationSeekBar.getProgress());
        changePicker(false);
    }

    private void changePicker(boolean vibrate){
        // vibrate
        vibrate(getApplicationContext(), getResources().getInteger(R.integer.switch_color_picker_button_vibrate_duration));

        if(colorPickerIndex == 0){
            appear(colorHiveView);
            vanish(colorWheelView);
            brightnessSlider.BindColorPicker(colorHiveView);
        }else if(colorPickerIndex == 1){
            appear(colorWheelView);
            vanish(colorHiveView);
            brightnessSlider.BindColorPicker(colorWheelView);
        }
    }

    @Override
    public void ActionCallback(Intent intent) {
        if(intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
            BluetoothDevice connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            ForegroundService service = ((ForegroundService)Service.Get(Service.FOREGROUND));
            if(connectedDevice.equals(service.bt.GetConnectedDevice()))
                goBack();
        }
    }
}
