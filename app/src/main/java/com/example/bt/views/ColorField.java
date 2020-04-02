package com.example.bt.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.bt.R;

public class ColorField extends View {

    private Paint paint;
    private Paint strokePaint;
    private RectF boundsRect;

    private int colorValue;
    private int strokeColor;
    private int strokeWidth;


    public ColorField(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ColorField,0, 0);

        try {
            colorValue = a.getInteger(R.styleable.ColorField_color_value, -1);
            strokeColor = a.getInteger(R.styleable.ColorField_stroke_color, Color.parseColor("#000000"));
            strokeWidth = a.getInteger(R.styleable.ColorField_stroke_width, 4);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        boundsRect = new RectF(0, 0, 0, 0);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colorValue);

        strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(strokeWidth);
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setColor(strokeColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawArc(canvas);
    }

    private void drawArc(Canvas canvas){
        boundsRect.left = strokePaint.getStrokeWidth() / 2;
        boundsRect.top = strokePaint.getStrokeWidth() / 2;
        boundsRect.right = getWidth() - strokePaint.getStrokeWidth() / 2;
        boundsRect.bottom = getHeight() - strokePaint.getStrokeWidth() / 2;

        if(colorValue == -1) {
            canvas.drawArc(boundsRect, 45, 160, false, strokePaint);
            canvas.drawArc(boundsRect, -135, 160, false, strokePaint);

            canvas.rotate(45, getWidth()/2, getHeight()/2);
            canvas.drawLine(boundsRect.left + (strokePaint.getStrokeWidth() / 2),
                    getHeight()/2,
                    boundsRect.right,
                    getHeight()/2,
                    strokePaint);
            canvas.save();
            canvas.restore();
        }
        else {
            canvas.drawArc(boundsRect, 0, 360, false, paint);
            canvas.drawArc(boundsRect, 0, 360, false, strokePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            Log.d("ss", "ss");
        }
        return true;
    }
}
