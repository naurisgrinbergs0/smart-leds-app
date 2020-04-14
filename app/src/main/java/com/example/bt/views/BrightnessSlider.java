package com.example.bt.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.bt.R;

public class BrightnessSlider extends View {
    private int strokeWidth = 5;
    private float currentValue;
    private Paint barPaint;
    private Paint barStrokePaint;
    private Shader shader;
    private int padding;
    private int height;
    private int touchThreshold = 5;
    private Paint sliderPaint;
    private int sliderRadius = 20;
    private View colorPicker;
    private int colorLeft;
    private int colorRight;

    private OnProgressChangeEventListener onProgressChangeEventListener;


    public void setOnProgressChangeEventListener(OnProgressChangeEventListener onProgressChangeEventListener) {
        this.onProgressChangeEventListener = onProgressChangeEventListener;
    }


    public BrightnessSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.BrightnessSlider,
                0, 0);

        try {
            padding = sliderRadius + strokeWidth + a.getInteger(R.styleable.BrightnessSlider_bar_padding, 0);
            height = a.getInteger(R.styleable.BrightnessSlider_bar_height, 30);
            currentValue = a.getFloat(R.styleable.BrightnessSlider_initial_value, 0f);
            colorLeft = a.getInt(R.styleable.BrightnessSlider_color_left, Color.BLACK);
            colorRight = a.getInt(R.styleable.BrightnessSlider_color_right, Color.WHITE);
        } finally {
            a.recycle();
        }
        init();
    }


    public void BindColorPicker(View colorPicker){
        this.colorPicker = colorPicker;
    }

    public float GetBrightness(){
        return currentValue;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        shader = new LinearGradient(0, getMeasuredHeight()/2, getMeasuredWidth(),
                getMeasuredHeight()/2, colorLeft, colorRight, Shader.TileMode.CLAMP);
        barPaint.setShader(shader);
    }

    private void init(){
        barPaint = new Paint();
        sliderPaint = new Paint();
        sliderPaint.setStrokeWidth(strokeWidth);
        sliderPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        barStrokePaint = new Paint();
        barStrokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        barStrokePaint.setStyle(Paint.Style.STROKE);
        barStrokePaint.setColor(Color.WHITE);
        barStrokePaint.setStrokeWidth(strokeWidth);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBar(canvas);
        drawSlider(canvas);
    }

    private void drawBar(Canvas canvas){
        int top = (int) ((float) padding + (((float)getMeasuredHeight() - ((float) padding*2f)) / 2f) - ((float) height / 2f));
        int boottom = (int) ((float) padding + (((float)getMeasuredHeight() - ((float) padding*2f)) / 2f) + ((float) height / 2f));
        canvas.drawRect(new RectF(padding, top,getMeasuredWidth() - padding,boottom), barPaint);
        //canvas.drawRect(new RectF(padding, top,getMeasuredWidth() - padding,boottom), barStrokePaint);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void drawSlider(Canvas canvas){
        int x = (int) ((((float)getMeasuredWidth() - (float)(padding*2)) * (float)currentValue) + (float)padding);
        int y = (int) ((float)getMeasuredHeight() / 2f);

        // calculate colors
        int red = (int) ((float)Color.red(colorLeft) + ((Color.red(colorRight) - Color.red(colorLeft)) * (float) currentValue));
        int green = (int) ((float)Color.green(colorLeft) + ((Color.green(colorRight) - Color.green(colorLeft)) * (float) currentValue));
        int blue = (int) ((float)Color.blue(colorLeft) + ((Color.blue(colorRight) - Color.blue(colorLeft)) * (float) currentValue));
        //Log.d("COLOR", "" + red + " " + green + " " + blue);

        // draw fill and then stroke
        sliderPaint.setStyle(Paint.Style.FILL);
        sliderPaint.setColor(Color.rgb(red, green, blue));
        canvas.drawCircle(x, y, sliderRadius, sliderPaint);
        sliderPaint.setStyle(Paint.Style.STROKE);
        sliderPaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, sliderRadius, sliderPaint);
        canvas.drawCircle(x, y, sliderRadius, barStrokePaint);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int left = padding - touchThreshold;
        int top = padding - touchThreshold;
        int right = getMeasuredWidth() - padding + touchThreshold;
        int bottom = getMeasuredHeight() - padding + touchThreshold;
        int x = (int) event.getX();
        int y = (int) event.getY();

        // if action is ON DOWN
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            // check if touch in bounds
            if((left <= x && x <= right) && (top <= y && y <= bottom))
                currentValue = (float)((float)x - (float)padding) / ((float)getMeasuredWidth() - (padding*2));
        }else {
            currentValue = (float) ((float) x - padding) / ((float) getMeasuredWidth() - (padding*2));
            if(currentValue < 0)
                currentValue = 0;
            else if(currentValue > 1)
                currentValue = 1;
        }


        if(onProgressChangeEventListener != null)
            onProgressChangeEventListener.onEvent(currentValue);

        // if color picker binded
        if(colorPicker != null){
            if(colorPicker.getClass().getName().equals(ColorHive.class.getName()))
                ((ColorHive)colorPicker).RecalculateColors();
            else if(colorPicker.getClass().getName().equals(ColorWheel.class.getName()))
                ((ColorWheel)colorPicker).RecalculateColors();
        }

        invalidate();
        return true;
    }

    public void RecalculateColors(){
        if(colorPicker.getClass().getName().equals(ColorHive.class.getName()))
            colorRight = ((ColorHive)colorPicker).GetColor(false);
        else if(colorPicker.getClass().getName().equals(ColorWheel.class.getName()))
            colorRight = ((ColorWheel)colorPicker).GetColor(false);

        shader = new LinearGradient(0, getMeasuredHeight()/2, getMeasuredWidth(),
                getMeasuredHeight()/2, colorLeft, colorRight, Shader.TileMode.CLAMP);
        barPaint.setShader(shader);
        invalidate();
    }

    // pass value [0;1]
    public void SetProgress(float progress){
        currentValue = progress;
    }


    public interface OnProgressChangeEventListener {
        void onEvent(float progress);
    }
}
