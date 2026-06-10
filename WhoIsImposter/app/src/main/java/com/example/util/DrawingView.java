package com.example.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {

    public static class Stroke {
        public Path path;
        public int color;
        public float width;

        public Stroke(Path path, int color, float width) {
            this.path = path;
            this.color = color;
            this.width = width;
        }
    }

    private final ArrayList<Stroke> strokes = new ArrayList<>();
    private final ArrayList<Stroke> undoneStrokes = new ArrayList<>();

    private Path currentPath;
    private int currentColor = 0xFF3F51B5; // Default primary violet/indigo
    private float currentStrokeWidth = 12f;

    private float lastTouchX;
    private float lastTouchY;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void strokeColor(int color) {
        this.currentColor = color;
    }

    public void strokeWidth(float width) {
        this.currentStrokeWidth = width;
    }

    public void undo() {
        if (!strokes.isEmpty()) {
            Stroke removed = strokes.remove(strokes.size() - 1);
            undoneStrokes.add(removed);
            invalidate();
        }
    }

    public void clear() {
        strokes.clear();
        undoneStrokes.clear();
        invalidate();
    }

    public ArrayList<Stroke> getStrokes() {
        return new ArrayList<>(strokes);
    }

    public void setStrokes(ArrayList<Stroke> newStrokes) {
        this.strokes.clear();
        if (newStrokes != null) {
            this.strokes.addAll(newStrokes);
        }
        this.undoneStrokes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw historic strokes
        for (Stroke s : strokes) {
            Paint paint = getPaintForStroke(s.color, s.width);
            canvas.drawPath(s.path, paint);
        }
        // Draw currently active stroke
        if (currentPath != null) {
            Paint paint = getPaintForStroke(currentColor, currentStrokeWidth);
            canvas.drawPath(currentPath, paint);
        }
    }

    private Paint getPaintForStroke(int color, float width) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(width);
        paint.setAntiAlias(true);
        return paint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                undoneStrokes.clear();
                currentPath = new Path();
                currentPath.moveTo(x, y);
                lastTouchX = x;
                lastTouchY = y;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    // Quadratic bezier curves for super smooth rendering!
                    currentPath.quadTo(lastTouchX, lastTouchY, (x + lastTouchX) / 2, (y + lastTouchY) / 2);
                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                    strokes.add(new Stroke(currentPath, currentColor, currentStrokeWidth));
                    currentPath = null;
                    invalidate();
                }
                break;
        }
        return true;
    }

    public Bitmap getDrawingBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth() > 0 ? getWidth() : 500,
                getHeight() > 0 ? getHeight() : 500, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(0xFFFFFFFF); // White background canvas for exported drawings
        draw(canvas);
        return bitmap;
    }
}
