package com.example.bt.activities;

import android.graphics.Color;
import android.os.Bundle;
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
    private CheckBox pulseCheckBox;
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
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });


        colorHiveView.setOnColorChangeEventListener(new ColorHive.OnColorChangeEventListener() {
            @Override
            public void onEvent(int color) {
                if(smoothTransitionCheckBox.isChecked())
                    DataTransfer.SetSmoothColor(color);
                else if(pulseCheckBox.isChecked())
                    DataTransfer.PlayPulse(color);
                else
                    DataTransfer.SetPlainColor(color);
            }
        });


        colorWheelView.setOnColorChangeEventListener(new ColorWheel.OnColorChangeEventListener() {
            @Override
            public void onEvent(int color) {
                if(smoothTransitionCheckBox.isChecked())
                    DataTransfer.SetSmoothColor(color);
                else if(pulseCheckBox.isChecked())
                    DataTransfer.PlayPulse(color);
                else
                    DataTransfer.SetPlainColor(color);
            }
        });


        prevPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerIndex = (colorPickerIndex == 0) ? 1 : (colorPickerIndex -= 1);
                MemoryConnector.setInt(ColorPickActivity.this, getString(R.string.var_picker_index), colorPickerIndex);
                changePicker();
            }
        });
        nextPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerIndex = (colorPickerIndex == 1) ? 0 : (colorPickerIndex += 1);
                MemoryConnector.setInt(ColorPickActivity.this, getString(R.string.var_picker_index), colorPickerIndex);
                changePicker();
            }
        });


        pulseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(pulseCheckBox.isChecked())
                    DataTransfer.PlayPulse(colorHiveView.GetColor(true));
                else
                    DataTransfer.StopPulse();
            }
        });

        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DataTransfer.SetDuration((progress < 10000 ? progress : 9999));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void InitializeFields(){
        colorHiveView = findViewById(R.id.colorHiveView);
        colorWheelView = findViewById(R.id.colorWheelView);
        homeConstraintLayout = findViewById(R.id.homeConstraintLayout);
        smoothTransitionCheckBox = findViewById(R.id.smoothTransitionCheckBox);
        pulseCheckBox = findViewById(R.id.pulseCheckBox);
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
        changePicker();
    }

    private void changePicker(){
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
