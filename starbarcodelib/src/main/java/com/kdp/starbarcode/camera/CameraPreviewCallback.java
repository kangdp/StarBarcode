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
           if (cm.isPortrait()) {
               //竖屏则旋转yuv90度
               data = rotateYUV420Degree90(data,cameraResolution.x,cameraResolution.y);
               barCodeProcessor.pushFrame(data, cameraResolution.y, cameraResolution.x);
           }
           else barCodeProcessor.pushFrame(data, cameraResolution.x, cameraResolution.y);
        }
    }


    /**
     * 将yuv数据旋转90度
     * @param data
     * @param imageWidth
     * @param imageHeight
     * @return
     */
    private byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
    {
        byte [] yuv = new byte[imageWidth*imageHeight*3/2];
        // Rotate the Y luma
        int i = 0;
        for(int x = 0;x < imageWidth;x++)
        {
            for(int y = imageHeight-1;y >= 0;y--)
            {
                yuv[i] = data[y*imageWidth+x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth*imageHeight*3/2-1;
        for(int x = imageWidth-1;x > 0;x=x-2)
        {
            for(int y = 0;y < imageHeight/2;y++)
            {
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                i--;
                yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                i--;
            }
        }
        return yuv;
    }
}
