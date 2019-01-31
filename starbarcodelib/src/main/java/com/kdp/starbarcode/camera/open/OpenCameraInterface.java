
package com.kdp.starbarcode.camera.open;
import android.hardware.Camera;


public final class OpenCameraInterface {

  public static final int NO_REQUESTED_CAMERA = -1;
  private OpenCameraInterface() {
  }
  /**
   * 打开相机
   * @param cameraId
   * @return
   */
  public static OpenCamera open(int cameraId) {

    int numCameras = Camera.getNumberOfCameras();
    if (numCameras == 0) {
      return null;
    }
    if (cameraId >= numCameras) {
      return null;
    }

    if (cameraId <= NO_REQUESTED_CAMERA) {
      cameraId = 0;
      while (cameraId < numCameras) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        if (CameraFacing.values()[cameraInfo.facing] == CameraFacing.BACK) {
          break;
        }
        cameraId++;
      }
      if (cameraId == numCameras) {
        cameraId = 0;
      }
    }
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    Camera.getCameraInfo(cameraId, cameraInfo);
    Camera camera = Camera.open(cameraId);
    if (camera == null) {
      return null;
    }
    return new OpenCamera(cameraId,
                          camera,
                          CameraFacing.values()[cameraInfo.facing],
                          cameraInfo.orientation);
  }

}
