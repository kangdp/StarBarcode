package com.starbarcode.sample.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.starbarcode.sample.R;


/***
 * @author kdp
 * @date 2018/10/27 12:46
 */
public class ScanView extends View{
    private Paint mPaint;
    private int bgColor = 0x80000000;//背景色
    private int borderColor = 0xFF2F9DE3;//扫描边框颜色
    private int borderWidth =  30;//边框宽度
    private int borderHeight = 5;//边框高度
    private int scanLineOffsetY;//扫描线垂直偏移量
    private Rect borderRect = new Rect();
    private Bitmap lineBitmap;

    private ValueAnimator valueAnimator;
    public ScanView(Context context) {
        this(context,null);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
        setLayerType(LAYER_TYPE_NONE,null);
    }

    public ScanView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lineBitmap = ((BitmapDrawable)ContextCompat.getDrawable(getContext(), R.mipmap.ic_scan_line)).getBitmap();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }


    public void setBorder(int[] border){
        borderRect.set(border[0],border[1],border[2],border[3]);
        setScanAnimation();
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {

        drawScanBg(canvas);
        drawScanBorder(canvas);
        drawScanLine(canvas);
    }

    private void drawScanLine(Canvas canvas) {
        //画扫描线
        mPaint.setColor(Color.GREEN);
        canvas.drawBitmap(lineBitmap,
                new Rect(0,0,lineBitmap.getWidth(),lineBitmap.getHeight()),
                new Rect(borderRect.left,borderRect.top+scanLineOffsetY,borderRect.right, (int) (borderRect.top+5+scanLineOffsetY)),
                null);
    }

    private void drawScanBorder(Canvas canvas) {
        //画边框
        mPaint.setColor(borderColor);
        //left-top
        canvas.drawRect(borderRect.left - borderHeight,borderRect.top-borderHeight,borderRect.left+borderWidth,borderRect.top,mPaint);
        canvas.drawRect(borderRect.left - borderHeight,borderRect.top,borderRect.left,borderRect.top+borderWidth,mPaint);
        //left-bottom
        canvas.drawRect(borderRect.left-borderHeight,borderRect.bottom,borderRect.left+borderWidth,borderRect.bottom+borderHeight,mPaint);
        canvas.drawRect(borderRect.left-borderHeight,borderRect.bottom-borderWidth,borderRect.left,borderRect.bottom,mPaint);
        //right-top
        canvas.drawRect(borderRect.right-borderWidth,borderRect.top-borderHeight,borderRect.right+borderHeight,borderRect.top,mPaint);
        canvas.drawRect(borderRect.right,borderRect.top,borderRect.right+borderHeight,borderRect.top+borderWidth,mPaint);
        //right-bottom
        canvas.drawRect(borderRect.right-borderWidth,borderRect.bottom,borderRect.right+borderHeight,borderRect.bottom+borderHeight,mPaint);
        canvas.drawRect(borderRect.right,borderRect.bottom-borderWidth,borderRect.right+borderHeight,borderRect.bottom,mPaint);
    }

    private void drawScanBg(Canvas canvas) {
        //画背景
        mPaint.setColor(bgColor);
        mPaint.setStyle(Paint.Style.FILL);
        //left
        canvas.drawRect(0,borderRect.top,borderRect.left,borderRect.bottom,mPaint);
        //right
        canvas.drawRect(borderRect.right,borderRect.top,getWidth(),borderRect.bottom,mPaint);
        //top
        canvas.drawRect(0,0,getMeasuredWidth(),borderRect.top,mPaint);
        //bottom
        canvas.drawRect(0,borderRect.bottom,getMeasuredWidth(),getMeasuredHeight(),mPaint);
    }

    private void setScanAnimation(){
        valueAnimator = ValueAnimator.ofFloat(0,borderRect.bottom - borderRect.top-5);
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setDuration(2000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                scanLineOffsetY = (int) value;
                //局部刷新
                postInvalidate(borderRect.left,borderRect.top,borderRect.right,borderRect.bottom);
            }
        });
    }

    public void stopScan(){
        if (valueAnimator != null && valueAnimator.isRunning()){
            valueAnimator.cancel();
        }
    }

    public void startScan(){
        if (valueAnimator != null && !valueAnimator.isRunning()){
            valueAnimator.start();
        }
    }

}
