package com.kdp.starbarcode.camera.focus;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Calendar;

/**
 * 加速度控制器  用来控制移动对焦
 */
public class SensorController implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private static SensorController mInstance;
    private CameraFocusListener mCameraFocusListener;

    private static final int STATUS_NONE = 0;
    private static final int STATUS_STATIC = 1;
    private static final int STATUS_MOVE = 2;
    private int mX, mY, mZ;
    private int STATUE = STATUS_NONE;
    private boolean canFocus = false;
    private boolean canFocusIn = false;
    private boolean isFocusing = false;
    private final double moveIs = 1.4;
    private long lastStaticStamp = 0;
    private static final int DELAY_DURATION = 500;

    private SensorController(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
        if (mSensorManager!=null){
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        start();
    }

    static SensorController getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SensorController(context);
        }
        return mInstance;
    }

    void setCameraFocusListener(CameraFocusListener mCameraFocusListener) {
        this.mCameraFocusListener = mCameraFocusListener;
    }

    void start() {
        restParams();
        canFocus = true;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    void stop() {
        mSensorManager.unregisterListener(this, mSensor);
        canFocus = false;
        unlockFocus();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (isFocusing) {
            restParams();
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            Calendar mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis();
//            int second = mCalendar.get(Calendar.SECOND);
            if (STATUE != STATUS_NONE) {
                int px = Math.abs(mX - x);
                int py = Math.abs(mY - y);
                int pz = Math.abs(mZ - z);
                double value = Math.sqrt(px * px + py * py + pz * pz);

                if (value > moveIs) {
                    STATUE = STATUS_MOVE;
                } else {
                    if (STATUE == STATUS_MOVE) {
                        lastStaticStamp = stamp;
                        canFocusIn = true;
                    }

                    if (canFocusIn) {
                        if (stamp - lastStaticStamp > DELAY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!isFocusing) {
                                canFocusIn = false;
                                if (mCameraFocusListener != null) {
                                    mCameraFocusListener.onFocus();
                                }
                            }
                        }
                    }

                    STATUE = STATUS_STATIC;
                }
            } else {
                lastStaticStamp = stamp;
                STATUE = STATUS_STATIC;
            }

            mX = x;
            mY = y;
            mZ = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void restParams() {
        STATUE = STATUS_NONE;
        canFocusIn = false;
        mX = 0;
        mY = 0;
        mZ = 0;
    }

    /**
     * 对焦是否被锁定
     * @return
     */
    boolean isFocusLocked() {
        return canFocus && isFocusing;
    }

    /**
     * 锁定对焦
     */
    void lockFocus() {
        isFocusing = true;
    }

    /**
     * 解锁对焦
     */
    void unlockFocus() {
        isFocusing = false;
    }

    public interface CameraFocusListener {
        /**
         * 相机对焦中
         */
        void onFocus();
    }
}
