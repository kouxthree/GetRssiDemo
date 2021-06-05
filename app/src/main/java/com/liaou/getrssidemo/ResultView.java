package com.liaou.getrssidemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ResultView extends View {
    public BlDevice currentDevice;
    public int countDown = 0;
    public Boolean isScanning = false;
    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);
        //paint CountDown
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
        if (countDown > 0) {
            canvas.drawText(Integer.toString(countDown), 30, 80, paint);
            return;
        }
        if(currentDevice == null || currentDevice.lstRssi.size() == 0) return;
        for(double d: currentDevice.lstRssi) {
            canvas.drawText("A", (float) d + 150, (float) d + 200, paint);
        }
    }
}