package com.example.bt.activities;

import android.content.Context;
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

import androidx.appcompat.app.AppCompatActivity;

import com.example.bt.Animator;
import com.example.bt.MemoryConnector;
import com.example.bt.R;
import com.example.bt.views.BrightnessSlider;
import com.example.bt.views.ColorHive;
import com.example.bt.views.ColorWheel;

import static com.example.bt.Animator.*;
import static com.example.bt.SharedServices.*;

public class ColorPickActivity extends AppCompatActivity {

    private ColorHive colorHiveView;
    private ColorWheel colorWheelView;
    private View homeConstraintLayout;
    private CheckBox smoothTransitionCheckBox;
    private int colorPickerIndex = 0;
    private Button prevPickerButton;
    private Button nextPickerButton;
    private SeekBar durationSeekBar;
    private BrightnessSlider brightnessSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_pick);

        InitializeFields();
        InitializeUI();

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
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
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
        brightnessSlider = findViewById(R.id.brightnessSlider);

        colorHiveView.BindBrightnessSlider(brightnessSlider);
        colorWheelView.BindBrightnessSlider(brightnessSlider);
        brightnessSlider.BindColorPicker(colorHiveView);
    }

    private void InitializeUI(){
        colorPickerIndex = MemoryConnector.getInt(ColorPickActivity.this, getString(R.string.var_picker_index));
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
}
