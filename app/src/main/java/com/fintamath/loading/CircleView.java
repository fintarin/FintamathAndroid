package com.fintamath.loading;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CircleView extends View {

    int circleRadius = 20;
    int strokeWidth = 0;

    int circleColor = 0;
    boolean drawOnlyStroke = false;

    boolean isAntiAlias = true;

    private float xyCordinates = 0.0f;
    private Paint paint = new Paint();

    public CircleView(Context context, int circleRadius, int circleColor, boolean isAntiAlias) {
        super(context);
        this.circleRadius = circleRadius;
        this.circleColor = circleColor;
        this.isAntiAlias = isAntiAlias;
        initValues();
    }

    public CircleView(Context context) {
        super(context);
        initValues();
    }

    private void initValues() {
        paint.setAntiAlias(isAntiAlias);

        if (drawOnlyStroke) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth((float) strokeWidth);
        } else {
            paint.setStyle(Paint.Style.FILL);
        }
        paint.setColor(circleColor);

        //adding half of strokeWidth because
        //the stroke will be half inside the drawing circle and half outside
        xyCordinates = (circleRadius + ((float) (strokeWidth / 2)));
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthHeight = (2 * (circleRadius)) + strokeWidth;
        setMeasuredDimension(widthHeight, widthHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(xyCordinates, xyCordinates, (float) circleRadius, paint);
    }
}
