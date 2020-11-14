package com.kdp.starbarcode.inter;

import android.view.SurfaceHolder;

/***
 * @author kdp
 * @date 2019/1/24 15:11
 * @description
 */
public interface ICamera {
    void openCamera(SurfaceHolder holder);
    void closeCamera();
    void startPreview();
    void stopPreview();
    void turnOnFlashLight();
    void turnOffFlashLight();
    boolean isTurnOnFlashLight();
    void setZoom(int zoom);
}
