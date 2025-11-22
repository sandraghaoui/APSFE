package com.example.aps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BoundingBoxOverlay extends View {

    private Rect box;
    private final Paint paint;

    public BoundingBoxOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setColor(0xFFFF0000);   // Red
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6f);
    }

    public void setBox(Rect rect) {
        this.box = rect;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (box != null) {
            canvas.drawRect(box, paint);
        }
    }
}
