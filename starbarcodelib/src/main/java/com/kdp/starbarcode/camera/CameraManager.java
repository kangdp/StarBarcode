package com.kdp.starbarcode.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;

import android.view.SurfaceHolder;
import android.widget.Toast;

import com.kdp.starbarcode.core.BarCodeProcessor;
import com.kdp.starbarcode.camera.focus.FocusManager;
import com.kdp.starbarcode.camera.open.OpenCamera;
import com.kdp.starbarcode.camera.open.OpenCameraInterface;
import com.kdp.starbarcode.core.BarCodeScanConfig;
import com.kdp.starbarcode.inter.ICamera;
import com.kdp.starbarcode.view.BarCodePreview;

import java.io.IOException;


/***
 * @author kdp
 * @date 2018/12/11 15:47
 * @description
 */
public class CameraManager implements ICamera {
    private int requestedCameraId = OpenCameraInterface.NO_REQUESTED_CAMERA;
    private OpenCamera mCamera;
    private CameraConfigManager cm;
    private CameraPreviewCallback previewCallback;
    private FocusManager focusManager;
    private BarCodePreview barCodePreview;
    private boolean mInitialized;
    private boolean previewing;
    private Context mContext;

    public CameraManager(BarCodePreview barCodePreview, BarCodeProcessor barCodeProcessor) {
        this.mContext = barCodePreview.getContext();
        this.barCodePreview = barCodePreview;
        this.cm = new CameraConfigManager(mContext);
        this.previewCallback = new CameraPreviewCallback(cm, barCodeProcessor);
    }

    /**
     * 调整识别区域
     */
    private void setRealROI() {
        BarCodeScanConfig barCodeScanConfig = barCodePreview.getBarCodeScanConfig();
        if (barCodeScanConfig == null) return;
        Rect rect = barCodeScanConfig.getROI();
        if (rect == null) return;
        Point cameraResolution = cm.getCameraResolution();
        Point screenResolution = cm.getScreenResolution();
        if (cameraResolution != null && screenResolution != null) {
            checkROIBounds(rect, screenResolution);
            float ratioX, ratioY;
            if (cm.isPortrait()) {
                ratioX = (float) cameraResolution.y / screenResolution.x;
                ratioY = (float) cameraResolution.x / screenResolution.y;
            } else {
                ratioX = (float) cameraResolution.x / screenResolution.x;
                ratioY = (float) cameraResolution.y / screenResolution.y;
            }
            rect.left = (int) (rect.left * ratioX);
            rect.right = (int) (rect.right * ratioX);
            rect.top = (int) (rect.top * ratioY);
            rect.bottom = (int) (rect.bottom * ratioY);
        }
    }


    /**
     * 检查边界
     *
     * @param rect
     * @param resolution
     */
    private void checkROIBounds(Rect rect, Point resolution) {
        if (rect.left < 0) rect.left = 0;
        else if (rect.left > resolution.x) rect.left = resolution.x;
        if (rect.right < rect.left) rect.right = rect.left;
        else if (rect.right > resolution.x) rect.right = resolution.x;
        if (rect.top < 0) rect.top = 0;
        else if (rect.top > resolution.y) rect.top = resolution.y;
        if (rect.bottom < rect.top) rect.bottom = rect.top;
        else if (rect.bottom > resolution.y) rect.bottom = resolution.y;
        //识别区域不能小于1
        if (rect.width() < 1) rect.set(0, rect.top, 1, rect.bottom);
        if (rect.height() < 1) rect.set(rect.left, 0, rect.right, 1);
    }


    /**
     * 打开摄像头
     */
    @Override
    public void openCamera(SurfaceHolder holder) {
        if (holder == null || isOpenCamera()) return;
        OpenCamera openCamera = mCamera;
        //打开摄像头
        if (openCamera == null) {
            openCamera = OpenCameraInterface.open(requestedCameraId);
            if (openCamera == null) {
                try {
                    throw new IOException("Camera.open() failed to return object from driver");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mCamera = openCamera;
        }
        if (openCamera == null) return;
        setCameraParametersAndROI(openCamera);
        Camera camera = openCamera.getCamera();
        camera.setOneShotPreviewCallback(previewCallback);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        startPreview();
    }

    @Override
    public void closeCamera() {
        stopProview();
        try {
            if (isOpenCamera()) {
                Camera theCamera = mCamera.getCamera();
                theCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 摄像头参数
     *
     * @param openCamera
     */
    private void setCameraParametersAndROI(OpenCamera openCamera) {

        if (!mInitialized) {
            cm.initCameraParameters(openCamera);
            setRealROI();
        }
        cm.setDesiredCameraParameters(openCamera);
    }

    private boolean isOpenCamera() {
        return mCamera != null;
    }

    /**
     * 停止预览
     */
    public void stopProview() {
        if (isOpenCamera() && previewing) {
            try {
                Camera theCamera = mCamera.getCamera();
                if (focusManager != null) {
                    focusManager.stop();
                    focusManager = null;
                }
                theCamera.setOneShotPreviewCallback(null);
                theCamera.stopPreview();
                previewing = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {
        if (isOpenCamera() && !previewing) {
            try {
                Camera theCamera = mCamera.getCamera();
                theCamera.startPreview();
                previewing = true;
                if (focusManager == null) {
                    focusManager = new FocusManager(mContext, theCamera, barCodePreview.getBarCodeScanConfig());
                }
                focusManager.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void requestPreviewFrame() {
        if (isOpenCamera() && previewing) {
            try {
                mCamera.getCamera().setOneShotPreviewCallback(previewCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开闪光灯
     */
    @Override
    public void turnOnFlashLight() {
        if (!isSupportFlight()) {
            Toast.makeText(mContext, "当前设备不支持闪光灯", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isOpenCamera() && previewing) {
            CameraConfigUtils.setTorch(mCamera.getCamera(), true);
        }
    }

    /**
     * 关闭闪光灯
     */
    @Override
    public void turnOffFlashLight() {
        if (isSupportFlight() && isOpenCamera() && previewing) {
            CameraConfigUtils.setTorch(mCamera.getCamera(), false);
        }
    }

    private boolean isSupportFlight() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * 放大焦距
     */
    public void adjustFocalDistance() {
        if (isOpenCamera() && previewing) {
            CameraConfigUtils.adjustFocalDistance(mCamera.getCamera());
        }
    }

    /**
     * 重置焦距
     */
//    public void resetFocalDistance() {
//        if (isOpenCamera() && previewing) {
//            Camera theCamera = mCamera.getCamera();
//            CameraConfigUtils.resetFocalDistance(theCamera);
//        }
//    }
}
