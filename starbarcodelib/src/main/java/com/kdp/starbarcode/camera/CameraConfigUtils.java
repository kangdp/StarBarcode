
package com.kdp.starbarcode.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import com.kdp.starbarcode.core.BarCodeScanConfig;

import java.util.Collection;
import java.util.List;

public final class CameraConfigUtils {

    private static final String TAG = "CameraConfiguration";
    private static final int MIN_PREVIEW_PIXELS = 480 * 320; // normal screen
    private static final double MAX_ASPECT_DISTORTION = 0.15; //允许比例误差小于0.15

    /**
     * 找到合适的分辨率
     *
     * @param parameters
     * @param screenResolution
     * @return
     */
    static Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution) {
        List<Camera.Size> rawSupportedSizes = parameters.getSupportedPreviewSizes();
        Log.i(TAG, "findBestPreviewSizeValue: screenWidth: " + screenResolution.x + " screenHeight: " + screenResolution.y);
//        Log.i(TAG, "findBestPreviewSizeValue: defaultWidth: " + parameters.getPreviewSize().width);
//        Log.i(TAG, "findBestPreviewSizeValue: defaultHeight: " + parameters.getPreviewSize().height);
        if (rawSupportedSizes == null) {
            Camera.Size defaultSize = parameters.getPreviewSize();
            if (defaultSize == null) {
                throw new IllegalStateException("Parameters contained no preview size!");
            }
            return new Point(defaultSize.width, defaultSize.height);
        }

        double screenAspectRatio = screenResolution.x / (double) screenResolution.y;
        if (screenResolution.x > screenResolution.y){
            screenAspectRatio = screenResolution.y / (double)screenResolution.x;
        }

        // 找到一个合适的尺寸
        int maxResolution = 0;
        Camera.Size maxResPreviewSize = null;
        for (Camera.Size size : rawSupportedSizes) {
            Log.i(TAG, "findBestPreviewSizeValue: realWidth = " + size.width + " realHeight = "+ size.height);
            int realWidth = size.width;
            int realHeight = size.height;
            int resolution = realWidth * realHeight;
            if (resolution < MIN_PREVIEW_PIXELS) {
                continue;
            }
            double aspectRatio = realHeight / (double) realWidth;
            if (aspectRatio > 1){
                aspectRatio = realWidth / (double) realHeight;
            }
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                continue;
            }

            if (Math.min(realWidth,realHeight) == Math.min(screenResolution.x,screenResolution.y) && Math.max(realWidth,realHeight) == Math.max(screenResolution.x,screenResolution.y) ) {
                Point exactPoint = new Point(realWidth, realHeight);
                return exactPoint;
            }

            // 如果分辨率符合条件，则记录最高的分辨率
            if (resolution > maxResolution) {
                maxResolution = resolution;
                maxResPreviewSize = size;
            }
        }

        //如果找不到精确匹配的分辨率，则使用最大预览尺寸
        if (maxResPreviewSize != null) {
            Point largestSize = new Point(maxResPreviewSize.width, maxResPreviewSize.height);
            return largestSize;
        }

        // 如果找不到合适的，则返回当前的预览尺寸
        Camera.Size defaultPreview = parameters.getPreviewSize();
        if (defaultPreview == null) {
            throw new IllegalStateException("Parameters contained no preview size!");
        }
        Point defaultSize = new Point(defaultPreview.width, defaultPreview.height);
        return defaultSize;
    }

    /**
     * 设置对焦模式
     *
     * @param camera
     * @param config
     */
    public static void setFocus(Camera camera, BarCodeScanConfig config) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        String focusMode = null;
        if (config.isAutofocus()) {
            if (config.isDisableContinuous()) {
                focusMode = findSettableValue(supportedFocusModes, Camera.Parameters.FOCUS_MODE_AUTO);
            } else {
                focusMode = findSettableValue(supportedFocusModes,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE,
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO,
                        Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
        if (config.isAutofocus() && focusMode == null) {
            focusMode = findSettableValue(supportedFocusModes,
                    Camera.Parameters.FOCUS_MODE_MACRO,
                    Camera.Parameters.FOCUS_MODE_INFINITY);
        }
        if (focusMode != null && !focusMode.equals(parameters.getFocusMode())) {
            parameters.setFocusMode(focusMode);
            camera.setParameters(parameters);
        }
    }

    /**
     * 设置闪光灯
     * @param camera
     * @param on
     */
    static void setTorch(Camera camera, boolean on) {
        Camera.Parameters parameters = camera.getParameters();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        String flashMode;
        if (on) {
            flashMode = findSettableValue(supportedFlashModes,
                    Camera.Parameters.FLASH_MODE_TORCH,
                    Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(supportedFlashModes,
                    Camera.Parameters.FLASH_MODE_OFF);
        }
        if (flashMode != null && !flashMode.equals(parameters.getFocusMode())) {
            parameters.setFlashMode(flashMode);
            camera.setParameters(parameters);
        }
    }

    private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    return desiredValue;
                }
            }
        }
        return null;
    }

    /**
     * 判断是否已开启闪光灯
     * @param camera
     * @return
     */
    static boolean isTurnOnFlashLight(Camera camera){
        Camera.Parameters parameters = camera.getParameters();
        String flashMode = parameters.getFlashMode();
        return (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode) || Camera.Parameters.FLASH_MODE_ON.equals(flashMode));
    }

    /**
     * 调节焦距
     * @param camera
     */
    static void adjustFocalDistance(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported()) {
            int zoom = parameters.getZoom();
            int maxZoom = parameters.getMaxZoom();
            if (zoom < maxZoom){
                zoom = zoom+10;
            }else {
                zoom = maxZoom;
            }
            parameters.setZoom(zoom);
            camera.setParameters(parameters);
        }
    }

    /**
     * 设置焦距
     */
    static void setZoom(int zoomValue,Camera camera){
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.isZoomSupported() && zoomValue > 0) {
            int maxZoom = parameters.getMaxZoom();
            if (zoomValue > maxZoom){
                parameters.setZoom(maxZoom);
            }else {
                parameters.setZoom(zoomValue);
            }
            camera.setParameters(parameters);
        }
    }

}
