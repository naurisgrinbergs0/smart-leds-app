package com.example.bt.views;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.example.bt.R;

import java.io.InputStream;

public class ColorWheel extends View {
    private float brightness = 1;
    private Paint colorCirclePaint;
    private int currentX;
    private int currentY;
    private int colorPrev;
    private int colorCurrent;
    private Bitmap bitmap;
    private float totalRadius;
    private Paint bitmapPaint;

    private OnColorChangeEventListener onColorChangeEventListener;
    private BrightnessSlider brightnessSlider;
    private ColorMatrix colorMatrix;

    private boolean edgeHit = false;
    private boolean edgeReleased = true;

    public void setOnColorChangeEventListener(OnColorChangeEventListener onColorChangeEventListener) {
        this.onColorChangeEventListener = onColorChangeEventListener;
    }

    public ColorWheel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        colorCirclePaint = new Paint();
        colorCirclePaint.setStyle(Paint.Style.STROKE);
        colorCirclePaint.setColor(Color.BLACK);
        colorCirclePaint.setStrokeWidth(4);
        colorCirclePaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        // set bitmap paint
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        colorMatrix = new ColorMatrix();
        colorMatrix.set(new float[] {
                brightness, 0, 0, 0, 0, 0,
                brightness, 0, 0, 0, 0, 0, brightness, 0,
                0, 0, 0, 0, 1, 0 });
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWheel(canvas);
        drawColorCircle(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int centerX = (int) (w / 2f);
        int centerY = (int) (h / 2f);
        totalRadius = Math.min(centerX - 20f, centerY - 20f);
        // set bitmap
        bitmap = Bitmap.createScaledBitmap(
                getBitmapFromAsset(getContext(),"hsvWheel.png"),
                (int)(totalRadius * 2f), (int)(totalRadius * 2f), false);

        SetColor(colorCurrent, false);
    }

    private void drawWheel(Canvas canvas) {
        canvas.drawBitmap(bitmap, (getMeasuredWidth() / 2f) - totalRadius, (getMeasuredHeight() / 2f) - totalRadius, bitmapPaint);
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (Exception e) {}

        return bitmap;
    }

    private void drawColorCircle(Canvas canvas){
        if(currentX == 0 && currentY == 0){
            currentX = getMeasuredWidth() / 2;
            currentY = getMeasuredHeight() / 2;
        }

        // draw fill and then stroke
        colorCirclePaint.setColor(calculateColor(currentX, currentY, true));
        colorCirclePaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(currentX, currentY, 20, colorCirclePaint);
        colorCirclePaint.setColor((brightness > .5f) ? Color.BLACK : Color.WHITE);
        colorCirclePaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(currentX, currentY, 20, colorCirclePaint);
    }


    private int calculateColor(float x, float y, boolean considerBrightness){
        int xOffset, yOffset, xCenter, yCenter, radius;
        float centreOffset, centerAngle;
        int color;
        xCenter = getMeasuredWidth() / 2;
        yCenter = getMeasuredHeight() / 2;

        xOffset = (int) (x - xCenter);
        yOffset = (int) (y - yCenter);
        radius = Math.min(xCenter, yCenter);
        centreOffset = (float) Math.hypot(xOffset, yOffset);
        if (centreOffset <= radius) {
            centerAngle = (float) ((Math.toDegrees(Math.atan2((yOffset), (xOffset))) + 360f) % 360f);
            color = Color.HSVToColor(new float[]{ centerAngle, centreOffset / radius, (considerBrightness) ? brightness : 1 });
        } else {
            color = Color.TRANSPARENT;
        }
        return color;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentX = (int) event.getX();
        currentY = (int) event.getY();
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;
        int radius = Math.min(centerX - 20, centerY - 20);
        int dist = (int) distanceBetweenPoints(currentX, currentY, centerX, centerY);

        // if dragged outside the circle
        if(dist > radius &&
                (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP)){
            double angleRad = Math.atan((float)(currentY - centerY) / (currentX - centerX));
            double dist1 = distanceBetweenPoints(currentX, currentY,
                    (int) (centerX + (radius * Math.cos(angleRad))),
                    (int) (centerY + (radius * Math.sin(angleRad))));
            double dist2 = distanceBetweenPoints(currentX, currentY,
                    (int) (centerX - (radius * Math.cos(angleRad))),
                    (int) (centerY - (radius * Math.sin(angleRad))));

            radius -= 10;
            if(dist1 < dist2){
                currentX = (int) (centerX + (radius * Math.cos(angleRad)));
                currentY = (int) (centerY + (radius * Math.sin(angleRad)));
            }else{
                currentX = (int) (centerX - (radius * Math.cos(angleRad)));
                currentY = (int) (centerY - (radius * Math.sin(angleRad)));
            }
            edgeHit = true;
        }else{
            edgeHit = false;
            edgeReleased = true;
        }

        if(edgeHit && edgeReleased){
            edgeReleased = false;
            // vibrate
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(
                        getContext().getResources().getInteger(R.integer.color_wheel_edge_hit_vibrate_duration),
                        VibrationEffect.DEFAULT_AMPLITUDE));
        }

        int color = calculateColor(currentX, currentY, true);
        if(color != colorPrev){
            colorPrev = color;
            // update brightness slider
            if(brightnessSlider != null)
                brightnessSlider.RecalculateColors();

            if(onColorChangeEventListener != null && (brightness == 0 || color != Color.TRANSPARENT))
                onColorChangeEventListener.onEvent(color);
        }

        invalidate();
        return true;
    }

    public void SetColor(int color, boolean triggerListener){
        colorCurrent = color;

        // hsv
        float[] hsv = new float[3];
        Color.colorToHSV(colorCurrent, hsv);

        float xCenter = getMeasuredWidth() / 2;
        float yCenter = getMeasuredHeight() / 2;
        float radius = Math.min(xCenter, yCenter);
        currentX = (int) (xCenter + (Math.cos(Math.toRadians(hsv[0])) * (hsv[1] * radius)));
        currentY = (int) (yCenter + (Math.sin(Math.toRadians(hsv[0])) * (hsv[1] * radius)));

        // update brightness slider
        if(brightnessSlider != null)
            brightnessSlider.RecalculateColors();

        if(onColorChangeEventListener != null && (brightness == 0 || color != Color.TRANSPARENT))
            onColorChangeEventListener.onEvent(color);

        RecalculateColors(triggerListener);

        invalidate();
    }

    private double distanceBetweenPoints(int x1, int y1, int x2, int y2){
        int dist = (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
        return dist;
    }

    public void RecalculateColors(boolean triggerListener) {
        brightness = brightnessSlider.GetBrightness();
        colorMatrix.set(new float[] {
                brightness, 0, 0, 0, 0, 0,
                brightness, 0, 0, 0, 0, 0, brightness, 0,
                0, 0, 0, 0, 1, 0 });
        bitmapPaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        if(onColorChangeEventListener != null && triggerListener)
            onColorChangeEventListener.onEvent(GetColor(true));

        invalidate();
    }

    public int GetColor(boolean considerBrightness) {
        return calculateColor(currentX, currentY, considerBrightness);
    }

    public void BindBrightnessSlider(BrightnessSlider brightnessSlider) {
        this.brightnessSlider = brightnessSlider;
    }

    public interface OnColorChangeEventListener {
        void onEvent(int color);
    }
}
