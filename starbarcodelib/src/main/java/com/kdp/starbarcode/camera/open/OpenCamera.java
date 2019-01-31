
package com.kdp.starbarcode.camera.open;


import android.hardware.Camera;

public final class OpenCamera {
  
  private final int index;
  private final Camera camera;
  private final CameraFacing facing;
  private final int orientation;
  
  OpenCamera(int index, Camera camera, CameraFacing facing, int orientation) {
    this.index = index;
    this.camera = camera;
    this.facing = facing;
    this.orientation = orientation;
  }

  public Camera getCamera() {
    return camera;
  }

  public CameraFacing getFacing() {
    return facing;
  }

  public int getOrientation() {
    return orientation;
  }

  @Override
  public String toString() {
    return "Camera #" + index + " : " + facing + ',' + orientation;
  }

}
