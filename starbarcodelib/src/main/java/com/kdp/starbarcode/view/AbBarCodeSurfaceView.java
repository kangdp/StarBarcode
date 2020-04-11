package com.kdp.starbarcode.view;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.kdp.starbarcode.inter.IBarCodeScan;
import com.kdp.starbarcode.core.BarCodeProcessor;
import com.kdp.starbarcode.State;
import com.kdp.starbarcode.inter.OnBarCodeScanResultListener;
import com.kdp.starbarcode.camera.CameraManager;
import com.google.zxing.Result;

import java.lang.ref.WeakReference;


/***
 * @author kdp
 * @date 2019/1/15 9:25
 * @description
 */
public abstract class AbBarCodeSurfaceView extends SurfaceView implements SurfaceHolder.Callback, IBarCodeScan {
    protected OnBarCodeScanResultListener listener;
    protected CameraManager cameraManager;
    protected BarCodeProcessor barCodeProcessor;
    protected boolean isSurfaceCreate;

    public AbBarCodeSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbBarCodeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Handler barCodeSRH = new Handler(new ResultCallback(this));
        barCodeProcessor = new BarCodeProcessor(barCodeSRH, (BarCodePreview) this);
        cameraManager = new CameraManager((BarCodePreview) this, barCodeProcessor);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isSurfaceCreate) {
            isSurfaceCreate = true;
            //打开摄像头
            openCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreate = false;
        cameraManager.stopPreview();
    }


    static class ResultCallback implements Handler.Callback {
        private WeakReference<AbBarCodeSurfaceView> weakReference;

        ResultCallback(AbBarCodeSurfaceView abBarCodeSurfaceView) {
            this.weakReference = new WeakReference<>(abBarCodeSurfaceView);
        }

        @Override
        public boolean handleMessage(Message msg) {
            final AbBarCodeSurfaceView barCodeSFV = weakReference.get();
            if (barCodeSFV == null) return true;
            switch (State.values()[msg.what]){
                case SUCCESS:
                    if (barCodeSFV.listener != null) {
                        barCodeSFV.listener.onSuccess(((Result) msg.obj).getText());
                    }
                    break;
                case NONE:
                    barCodeSFV.cameraManager.adjustFocalDistance();
                    //延时0.5秒继续识别
                    barCodeSFV.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            barCodeSFV.requestPreviewFrame();
                        }
                    }, 500);
                    break;
                case FAILED:
                    if (barCodeSFV.listener != null)
                        barCodeSFV.listener.onFailure();
                    barCodeSFV.requestPreviewFrame();
                    break;
            }
            return true;
        }
    }

     public void openCamera(SurfaceHolder holder) {
        cameraManager.openCamera(holder);
    }

    void requestPreviewFrame() {
        cameraManager.requestPreviewFrame();
    }
}
