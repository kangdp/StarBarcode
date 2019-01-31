package com.kdp.starbarcode.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.kdp.starbarcode.camera.open.CameraFacing;
import com.kdp.starbarcode.camera.open.OpenCamera;

/***
 * @author kdp
 * @date 2019/1/7 19:09
 * @description 相机配置
 */
class CameraConfigManager {
    private Context mContext;
    private int cwNeededRotation;
    private Point screenResolution;
    private Point cameraResolution;
    CameraConfigManager(Context mContext) {
        this.mContext = mContext;
    }

    void initCameraParameters(OpenCamera camera){
        Camera.Parameters parameters = camera.getCamera().getParameters();
        WindowManager manager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int displayRotation = display.getRotation();
        int cwRotationFromNaturalToDisplay;

        switch (displayRotation) {
            case Surface.ROTATION_0:
                cwRotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                cwRotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                cwRotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                cwRotationFromNaturalToDisplay = 270;
                break;
            default:
                if (displayRotation % 90 == 0) {
                    cwRotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: " + displayRotation);
                }
        }
        int cwRotationFromNaturalToCamera = camera.getOrientation();
        if (camera.getFacing() == CameraFacing.FRONT) {
            cwRotationFromNaturalToCamera = (360 - cwRotationFromNaturalToCamera) % 360;
        }

        int cwRotationFromDisplayToCamera = (360 + cwRotationFromNaturalToCamera - cwRotationFromNaturalToDisplay) % 360;

        if (camera.getFacing() == CameraFacing.FRONT) {
            cwNeededRotation = (360 - cwRotationFromDisplayToCamera) % 360;
        } else {
            cwNeededRotation = cwRotationFromDisplayToCamera;
        }

        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        screenResolution = theScreenResolution;
        cameraResolution = CameraConfigUtils.findBestPreviewSizeValue(parameters, screenResolution);
    }

    /**
     * 设置预期的相机参数
     * @param camera
     */
    void setDesiredCameraParameters(OpenCamera camera) {
        Camera theCamera = camera.getCamera();
        Camera.Parameters parameters = theCamera.getParameters();
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
        theCamera.setParameters(parameters);
        theCamera.setDisplayOrientation(cwNeededRotation);
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    boolean isPortrait(){
        return screenResolution.x < screenResolution.y;
    }



}
