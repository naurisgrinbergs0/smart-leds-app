package com.example.bt.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.example.bt.R;

@SuppressLint("AppCompatCustomView")
public class AdvancedSeekBar extends SeekBar {

    private Paint stepPaint;
    private int stepRadius = 5;
    private int steps;
    private int min;

    public AdvancedSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AdvancedSeekBar,
                0, 0);

        try {
            steps = a.getInteger(R.styleable.AdvancedSeekBar_steps, 10);
            steps = (0 <= steps && steps <= 50) ? steps : 50;
            min = a.getInteger(R.styleable.AdvancedSeekBar_min, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        stepPaint = new Paint();
        stepPaint.setColor(Color.WHITE);
        float valStep = (float) (getMax() - min) / steps;
        int nearestStep = (int) (((int)(getProgress() / valStep)) * valStep);
        setProgress(nearestStep);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircles(canvas);
    }

    private void drawCircles(Canvas canvas) {
        float deltaX = (float)(getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) / steps;
        float x = getPaddingLeft();
        float y = (float) getMeasuredHeight() / 2;
        for(int step = 0; step <= steps; step++){
            canvas.drawCircle(x, y, stepRadius, stepPaint);
            x += deltaX;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float progress = (event.getX() - getPaddingLeft())
                / (getMeasuredWidth() - getPaddingLeft() - getPaddingRight());
        float actualVal = (getMax() - min) * progress;
        float valStep = (float) (getMax() - min) / steps;
        int nearestStep = (int) (((int)(actualVal / valStep)) * valStep);
        boolean res = super.onTouchEvent(event);
        setProgress( nearestStep);
        return res;
    }
}
