package com.kdp.starbarcode.camera.focus;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;

import com.kdp.starbarcode.core.BarCodeScanConfig;
import com.kdp.starbarcode.camera.CameraConfigUtils;


/***
 * @author kdp
 * @date 2019/1/23 14:26
 * @description 对焦处理
 */
public class FocusManager implements SensorController.CameraFocusListener {
    private Handler mHandler;
    private BarCodeScanConfig mConfig;
    private Camera mCamera;
    private SensorController sensorController;
    private boolean focusing;
    public FocusManager(Context context, Camera camera, BarCodeScanConfig config) {
        this.mCamera = camera;
        this.mConfig = config;
        CameraConfigUtils.setFocus(mCamera,mConfig);
        if (isEnableAutoFocus()){
            if (mHandler == null) mHandler = new Handler();
            sensorController = SensorController.getInstance(context);
            sensorController.setCameraFocusListener(this);
        }
    }

     public void start(){
     if (isEnableAutoFocus())
          sensorController.start();
    }


    public void stop(){
        if (isEnableAutoFocus()){
            mCamera.cancelAutoFocus();
            sensorController.stop();
        }
    }


    @Override
    public void onFocus() {
        if (!sensorController.isFocusLocked()){
            doFocus();
            sensorController.lockFocus();
        }
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                     focusing = false;
                     sensorController.unlockFocus();
                }
            },100);//延时100毫秒后再次对焦
        }
    };

    private void doFocus(){
        if (focusing) return;
        focusing = true;
        mCamera.cancelAutoFocus();
        mCamera.autoFocus(autoFocusCallback);
    }

     private boolean isEnableAutoFocus(){
        return mConfig.isAutofocus() && mCamera.getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO);
    }

}
