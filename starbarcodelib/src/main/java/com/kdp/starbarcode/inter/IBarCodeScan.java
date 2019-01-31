package com.kdp.starbarcode.inter;

/***
 * @author kdp
 * @date 2019/1/15 20:21
 * @description
 */
public interface IBarCodeScan extends ICamera{
    void openCamera();
    void startRecognize();
    void stopRecognize();
}
