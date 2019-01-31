package com.starbarcode.sample;

import android.app.Application;

/***
 * @author kdp
 * @date 2019/1/28 11:51
 * @description
 */
public class StarBarcodeApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
