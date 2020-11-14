package com.kdp.starbarcode.camera;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import com.kdp.starbarcode.core.BarCodeProcessor;

/***
 * @author kdp
 * @date 2019/1/15 10:49
 * @description
 */
public class CameraPreviewCallback implements Camera.PreviewCallback {
    private CameraConfigManager cm;
    private BarCodeProcessor barCodeProcessor;
    CameraPreviewCallback(CameraConfigManager cm, BarCodeProcessor barCodeProcessor) {
        this.cm = cm;
        this.barCodeProcessor = barCodeProcessor;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Point cameraResolution = cm.getCameraResolution();
        Log.d(CameraPreviewCallback.class.getSimpleName(), "onPreviewFrame: preview: x = " + cameraResolution.x + " y = "+cameraResolution.y);
        if (cameraResolution != null){
           if (cm.isPortrait()) barCodeProcessor.pushFrame(data, cameraResolution.y, cameraResolution.x);
           else barCodeProcessor.pushFrame(data, cameraResolution.x, cameraResolution.y);
        }
    }
}
