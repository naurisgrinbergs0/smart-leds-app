package com.example.bt.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.util.ArrayList;

public class ColorHive extends View {

    private int cellLossNumber;
    private int outerCellNumber;
    private int cellPaddingPercent;
    private int centerRadiusPercent;
    private float totalRadius;

    private BrightnessSlider brightnessSlider;
    private Paint cellPaint;
    private Cell selectedCell;

    private ColorHive.OnColorChangeEventListener onColorChangeEventListener;

    private class Cell{
        public int xRelative;
        public int yRelative;
        public int radius;
        public int color;

        Cell(int xRelative, int yRelative, int fullRadius, int color){
            this.xRelative = xRelative;
            this.yRelative = yRelative;
            this.radius = fullRadius;
            this.color = color;
        }
    }
    private class Row{
        public ArrayList<Cell> cells = new ArrayList<>();
        public int radius;
    }
    private ArrayList<Row> rows = new ArrayList<>();


    public void BindBrightnessSlider(BrightnessSlider brightnessSlider){
        this.brightnessSlider = brightnessSlider;
    }


    public ColorHive(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ColorHive,0, 0);

        try {
            outerCellNumber = a.getInteger(R.styleable.ColorHive_outerCellNumber, 20);
            cellPaddingPercent = a.getInteger(R.styleable.ColorHive_cellPaddingPercent, 5);
            centerRadiusPercent = a.getInteger(R.styleable.ColorHive_centerRadiusPercent, 20);
            cellLossNumber = a.getInteger(R.styleable.ColorHive_cellLossNumber, 0);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init(){
        // initialize paints
        cellPaint = new Paint();
        cellPaint.setColor(Color.BLACK);
        cellPaint.setStyle(Paint.Style.FILL);
        cellPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        cellPaint.setStrokeWidth(4);
    }

    public void setOnColorChangeEventListener(ColorHive.OnColorChangeEventListener onColorChangeEventListener) {
        this.onColorChangeEventListener = onColorChangeEventListener;
    }

    public int GetColor(boolean considerBrightness){
        return calculateColor(selectedCell.xRelative + (getMeasuredWidth() / 2f),
                selectedCell.yRelative + (getMeasuredHeight() / 2f), (considerBrightness) ? -1 : 1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        totalRadius = Math.min(w / 2f, h / 2f);
        float radius = totalRadius;
        float shiftAngle = 0;

        // fill row list with rows
        while (true){
            float deltaAngle = 360f / outerCellNumber;
            float cellRadius = (float) Math.sqrt(
                    Math.pow(Math.cos(Math.toRadians(deltaAngle)) * (float)radius - radius, 2) +
                            Math.pow(Math.sin(Math.toRadians(deltaAngle)) * (float)radius - 0, 2) ) / 2f;
            radius -= cellRadius;
            cellRadius = (float) Math.sqrt(
                    Math.pow(Math.cos(Math.toRadians(deltaAngle)) * (float)radius - radius, 2) +
                            Math.pow(Math.sin(Math.toRadians(deltaAngle)) * (float)radius - 0, 2) ) / 2f;

            // if got to center, break
            if(radius - cellRadius <= ((float) centerRadiusPercent / 100f) * totalRadius)
                break;

            Row row = new Row();
            row.radius = (int) radius;
            // fill cell list with cells
            for (int cellNum = 0; cellNum < outerCellNumber; cellNum++){
                float angle = (shiftAngle + (deltaAngle * cellNum)) % 360;
                Cell cell = new Cell(
                        (int)(Math.cos(Math.toRadians(angle)) * radius),
                        (int)(Math.sin(Math.toRadians(angle)) * radius),
                        (int)cellRadius,
                        calculateColor((float) ((w / 2f) + (Math.cos(Math.toRadians(angle)) * radius)),
                                (float)((h / 2f) + (Math.sin(Math.toRadians(angle)) * radius)), -1));
                row.cells.add(cell);
            }
            rows.add(row);
            radius -= cellRadius;
            shiftAngle += deltaAngle / 2;
            outerCellNumber -= cellLossNumber;
        };
        // add center cell
        Row row = new Row();
        Cell centerCell = new Cell(0, 0,
                (int) (totalRadius * ((float)centerRadiusPercent / 100f)), calculateColor(w / 2f, h / 2f, -1));
        row.cells.add(centerCell);
        rows.add(row);
        // set center cell as selected cell
        selectedCell = centerCell;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawHives(canvas);
    }

    private void drawHives(Canvas canvas) {
        int xCenter = (int) (getMeasuredWidth() / 2f);
        int yCenter = (int) (getMeasuredHeight() / 2f);
        cellPaint.setStyle(Paint.Style.FILL);
        for (Row row : rows){
            for(Cell cell : row.cells){
                float multiplier = 1;
                if(cell.equals(selectedCell))
                    multiplier = 1.2f;

                cellPaint.setColor(cell.color);
                canvas.drawCircle(xCenter + cell.xRelative,
                        yCenter + cell.yRelative, (cell.radius - (cell.radius * ((float)cellPaddingPercent / 100f))) * multiplier, cellPaint);

                if(cell.equals(selectedCell)){
                    cellPaint.setStyle(Paint.Style.STROKE);
                    cellPaint.setColor(Color.BLACK);
                    canvas.drawCircle(xCenter + cell.xRelative,
                            yCenter + cell.yRelative, (cell.radius - (cell.radius * ((float)cellPaddingPercent / 100f))) * multiplier, cellPaint);
                    cellPaint.setStyle(Paint.Style.FILL);
                }
            }
        }
    }

    public void RecalculateColors() {
        for (int i = 0; i < rows.size(); i++){
            for (int n = 0; n < rows.get(i).cells.size(); n++) {
                rows.get(i).cells.get(n).color =
                        calculateColor(
                                rows.get(i).cells.get(n).xRelative + (getMeasuredWidth() / 2f),
                                rows.get(i).cells.get(n).yRelative + (getMeasuredHeight() / 2f), -1);
            }
        }
        if(onColorChangeEventListener != null)
            onColorChangeEventListener.onEvent(GetColor(true));
        invalidate();
    }

    private int calculateColor(float x, float y, float brightness){
        int xOffset, yOffset, xCenter, yCenter;
        float centreOffset, centerAngle;
        int color;
        xCenter = (int) (getMeasuredWidth() / 2f);
        yCenter = (int) (getMeasuredHeight() / 2f);

        xOffset = (int) (x - xCenter);
        yOffset = (int) (y - yCenter);

        centreOffset = (float) Math.hypot(xOffset, yOffset);
        if (centreOffset <= totalRadius) {
            centerAngle = (float) ((Math.toDegrees(Math.atan2((yOffset), (xOffset))) + 360f) % 360f);
            color = Color.HSVToColor(new float[]{ centerAngle, centreOffset / totalRadius,
                    (brightness != -1) ? brightness : ((brightnessSlider == null) ? 1 : brightnessSlider.GetBrightness()) });
        } else {
            color = Color.TRANSPARENT;
        }
        return color;
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - (getMeasuredWidth() / 2f);
        float y = event.getY() - (getMeasuredHeight() / 2f);

        Cell closestCell = null;

        float distToCenter = (float) Math.sqrt((x * x) + (y * y));

        // select one of rows
        float closestRowDist = Math.abs(rows.get(0).radius - distToCenter);
        // get closest row
        for(int i = 0; i < rows.size(); i++){
            float currDist = Math.abs(rows.get(i).radius - distToCenter);
            if(currDist <= closestRowDist){
                closestRowDist = currDist;
                if(i == rows.size() - 1 || Math.abs(rows.get(i+1).radius - distToCenter) > closestRowDist){
                    // now we have the closest row, find closest cell
                    float closestCellDist = (float) Math.sqrt(
                        Math.pow(rows.get(i).cells.get(0).xRelative - x, 2) +
                        Math.pow(rows.get(i).cells.get(0).yRelative - y, 2));
                    for (int n = 0; n < rows.get(i).cells.size(); n++){
                        currDist = (float) Math.sqrt(
                                Math.pow(rows.get(i).cells.get(n).xRelative - x, 2) +
                                Math.pow(rows.get(i).cells.get(n).yRelative - y, 2));

                        if(currDist <= closestCellDist){
                            closestCellDist = currDist;
                            if(n == rows.get(i).cells.size() - 1 || Math.sqrt(
                                    Math.pow(rows.get(i).cells.get(n+1).xRelative - x, 2) +
                                            Math.pow(rows.get(i).cells.get(n+1).yRelative - y, 2)) > closestCellDist){
                                // select this cell
                                closestCell = rows.get(i).cells.get(n);
                            }
                        }
                    }
                }
            }
        }

        if(!closestCell.equals(selectedCell)) {
            selectedCell = closestCell;
            invalidate();

            // update brightness slider
            if(brightnessSlider != null)
                brightnessSlider.RecalculateColors();

            if(onColorChangeEventListener != null)
                onColorChangeEventListener.onEvent(GetColor(true));

            // vibrate
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(
                        getContext().getResources().getInteger(R.integer.color_hive_vibrate_duration),
                        VibrationEffect.DEFAULT_AMPLITUDE));
        }

        return true;
    }


    public interface OnColorChangeEventListener {
        void onEvent(int color);
    }
}
